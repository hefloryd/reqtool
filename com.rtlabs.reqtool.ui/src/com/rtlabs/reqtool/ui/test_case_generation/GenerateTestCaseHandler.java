package com.rtlabs.reqtool.ui.test_case_generation;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.ui.dialogs.DiagnosticDialog;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;

import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.ui.Activator;
import com.rtlabs.reqtool.ui.TraceManager;
import com.rtlabs.reqtool.ui.test_case_generation.OverwritePrompter.FileAction;
import com.rtlabs.reqtool.ui.test_case_generation.OverwritePrompter.GenerateFileAction;
import com.rtlabs.reqtool.util.Result;

/**
 * Implements the test case generation command.
 * 
 * - Gets the current selection
 * - If that contains requirements it generates test cases for them
 * - Writes the test cases to disk.
 * - Displays status messages
 *
 */
public class GenerateTestCaseHandler extends AbstractHandler {

	private IoOperations ioOperations;
	private IContainer previousTargetDir;
	private OverwritePrompter overwritePrompter;
	
	public GenerateTestCaseHandler(IoOperations ioOperations, OverwritePrompter overwritePrompter) {
		this.ioOperations = ioOperations;
		this.overwritePrompter = overwritePrompter;
	}

	public GenerateTestCaseHandler() {
		this(new IoOperations(), new OverwritePrompter());
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = ioOperations.getActiveWorkbenchWindow(event);
		Shell shell = window.getShell();
		ISelection selection = window.getActivePage().getSelection();
		
		List<Requirement> reqs = Collections.emptyList();
		
		if (selection instanceof IStructuredSelection) {
			reqs = Arrays.stream(((IStructuredSelection) selection).toArray())
				.filter(e -> e instanceof Requirement)
				.map(e -> (Requirement) e)
				.collect(toList());
		}
		
		if (reqs.isEmpty()) {
			ioOperations.displayInfo(shell, "To generate test cases some requrements must be selected.");
			return null;
		}
		
		IContainer targetFolder = ioOperations.promptForAndCreateTargetFolder(shell, previousTargetDir);
		previousTargetDir = targetFolder;

		List<IStatus> statuses = new ArrayList<>(); 
		List<FileAction> fileGenerationActions = new ArrayList<>();
		
		for (Requirement req : reqs) {
			Result<String> result = RobotTestCaseGenerator.generate(req);
			if (result.isNoErrors()) {
				String testFileName = "test_" + req.getName().replaceAll("\\s", "_") + ".robot";
				IFile testFile = targetFolder.getFile(new Path(testFileName));
				fileGenerationActions.add(new GenerateFileAction(testFile, result.getResult()) {
					@Override
					public IStatus run(IFile targetFile) throws CoreException {
						if (!targetFile.exists()) {
							// Only create trace if target does not exist, to not annoy user by
							// making them repeatedly remove it.
							new TraceManager().createTrace(req, new StructuredSelection(targetFile));
						}
						return super.run(targetFile);
					}
				});
			}
			
			if (!result.isAllOk()) {
				statuses.addAll(result.getStatuses());
			}
		}
		
		statuses.addAll(overwritePrompter.run(shell, fileGenerationActions));
		
		if (!statuses.isEmpty()) {
			ioOperations.reportResult(shell, statuses);
		}
		
		return null;
	}
	
	
	/**
	 * This classes contains code for dialogs and interactions with the Eclipse framework. It enables mocking
	 * of those operations for testing.
	 */
	private static class IoOperations {

		public void displayInfo(Shell shell, String message) {
			MessageDialog.openError(shell, "Info", message);
		}

		public IWorkbenchWindow getActiveWorkbenchWindow(ExecutionEvent event) throws ExecutionException {
			return HandlerUtil.getActiveWorkbenchWindowChecked(event);
		}

		public void reportResult(Shell shell, List<IStatus> statuses) {
			BasicDiagnostic rootStatus = new BasicDiagnostic(Activator.PLUGIN_ID, IStatus.ERROR, 
				"The test case generation procedure has completed.\n\n"
					+ "The following are the status messages from the generation steps.", null);
			
			for (IStatus s : statuses) {
				rootStatus.add(BasicDiagnostic.toDiagnostic(s));
			}
			
			DiagnosticDialog dialog = new DiagnosticDialog(shell, "Test Case Generation Complete", 
					null, rootStatus, Diagnostic.INFO | Diagnostic.WARNING | Diagnostic.ERROR) {
				public void create() {
					super.create();
					getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							buttonPressed(IDialogConstants.DETAILS_ID);
						}
					});
				}
			};
				
			dialog.open();
		}
		
		/**
		 * Opens a dialog prompting the user to select a target folder.
		 */
		private IContainer promptForAndCreateTargetFolder(Shell shell, IContainer previousTargetDir) {
				IContainer targetFolder = promptForTargetFolder(shell, previousTargetDir);
				if (targetFolder == null) return null;
				
				try {
					FolderCreator.createContainerRecursive(targetFolder);
				} catch (CoreException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
						"An error occured when creating the output folder.", e), StatusManager.BLOCK);
					return null;
				}
				
				return targetFolder;
		}

		public IContainer promptForTargetFolder(Shell shell, IContainer previousTargetFolder) {
			ContainerSelectionDialog  dialog = new ContainerSelectionDialog(shell, previousTargetFolder, true, 
					"Choose a folder for generated test case files");
			dialog.showClosedProjects(false);
			dialog.setValidator(FOLDER_VALIDATOR);
			
			dialog.open();
			
			if (dialog.getResult() == null) return null;
			IPath targetPath = (IPath) dialog.getResult()[0];
			
			if (targetPath.segmentCount() == 1) {
				return getWorkspace().getRoot().getProject(targetPath.segment(0));
			}

			return getWorkspace().getRoot().getFolder(targetPath);
		}			
		
		public IWorkspace getWorkspace() {
			return ResourcesPlugin.getWorkspace();
		}
		
		private static final ISelectionValidator FOLDER_VALIDATOR = new ISelectionValidator() {
			@Override
			public String isValid(Object selection) {
				IPath path = (IPath) selection;
				
				if (path == null || path.segmentCount() < 1) {
					return "Invalid path.";
				}
				
				if (!ResourcesPlugin.getWorkspace().getRoot().exists(new Path(path.segment(0)))) {
					return "Project '" + path.segment(0) + "' does not exist.";
				}
				
				return null;
			}
		};
	}
}
