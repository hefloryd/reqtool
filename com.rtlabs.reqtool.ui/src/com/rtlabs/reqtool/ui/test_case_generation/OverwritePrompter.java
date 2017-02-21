package com.rtlabs.reqtool.ui.test_case_generation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Used to prompt users with a dialog for asking about overwriting files.
 */
class OverwritePrompter {
	private final OverwriteDialog overwriteDialog;

	public interface FileAction {
		IFile getTargetFile();
		IStatus run(IFile targetFile) throws CoreException;
	}
	
	public static class GenerateFileAction implements FileAction {
		private final IFile file;
		private final String content;
		
		public GenerateFileAction(IFile file, String content) {
			this.file = file;
			this.content = content;
		}

		@Override
		public IFile getTargetFile() {
			return file;
		}
			
		@Override
		public IStatus run(IFile targetFile) throws CoreException {
			return runString(targetFile);
		}

		public final IStatus runString(IFile targetFile) throws CoreException {
			return runStream(targetFile, new ByteArrayInputStream(content.getBytes()));
		}
	
		public final IStatus runStream(IFile targetFile, InputStream fileContents) throws CoreException {
			if (targetFile.exists()) {
				targetFile.setContents(fileContents, true, true, null);
			} else {
				targetFile.create(fileContents, true, null);
			}
			
			return ValidationStatus.info("Generated file '" + targetFile.getFullPath().toOSString() + "'");
		}
	}

	public enum OverwriteAnswer {
		YES, YES_TO_ALL, NO, NO_TO_ALL, CANCEL;
		
		public boolean isYes() {
			return this == YES || this == YES_TO_ALL;
		}
	}

	/**
	 * Separate dialog class to enable testing.
	 */
	public static class OverwriteDialog {
		public OverwriteAnswer prompt(Shell shell, IFile existingFile, boolean isMany) {
			MessageDialog dialog = new MessageDialog(shell, "Overwrite File?", null, 
				"The file '" + existingFile.getFullPath().toOSString() + "' allready exists. Do you want to overwrite it? ",
				MessageDialog.QUESTION_WITH_CANCEL,
				isMany
					? new String[] { IDialogConstants.YES_LABEL, IDialogConstants.YES_TO_ALL_LABEL, 
						IDialogConstants.NO_LABEL, IDialogConstants.NO_TO_ALL_LABEL, IDialogConstants.CANCEL_LABEL }
					: new String[] { IDialogConstants.YES_LABEL, IDialogConstants.CANCEL_LABEL }, 
				0);
			
			int answer = dialog.open();
			
			switch (answer) {
				case 0: return OverwriteAnswer.YES;
				case 1: return OverwriteAnswer.YES_TO_ALL;
				case 2: return OverwriteAnswer.NO;
				case 3: return OverwriteAnswer.NO_TO_ALL;
				case 4: 
				case -1:
					return OverwriteAnswer.CANCEL;
				default: throw new IllegalArgumentException(Integer.toString(answer));
			}
		}
	}
	
	public OverwritePrompter(OverwriteDialog overwriteDialog) {
		this.overwriteDialog = overwriteDialog;
	}

	public OverwritePrompter() {
		this(new OverwriteDialog());
	}
	
	public List<IStatus> run(Shell shell, List<FileAction> actions) {
		List<IStatus> statuses = new ArrayList<>();
		boolean yesToAll = false;
		boolean noToAll = false;
		
		FILE_ACTION_LOOP:
		for (FileAction fileAction : actions) {
			IFile targetFile = fileAction.getTargetFile();

			try {
				OverwriteAnswer answer = null;
				if (yesToAll) answer = OverwriteAnswer.YES;
				else if (noToAll) answer = OverwriteAnswer.NO;
				
				if (targetFile.exists() && answer == null) {
					answer = overwriteDialog.prompt(shell, targetFile, actions.size() > 1);
					
					if (answer == OverwriteAnswer.CANCEL) {
						break FILE_ACTION_LOOP;
					} else if (answer == OverwriteAnswer.YES_TO_ALL) {
						yesToAll = true;
					} else if (answer == OverwriteAnswer.NO_TO_ALL) {
						noToAll = true;
					}
				}
				
				if (!targetFile.exists() || answer.isYes()) {
					IStatus status = fileAction.run(targetFile);
					if (status != null) statuses.add(status);
				}

			} catch (Exception e) {
				String message = "An error occured while writing to '"+ targetFile.getFullPath().toOSString() + "'. ";
				statuses.add(ValidationStatus.error(message + "See log for details. Error message: " + e, e));
				StatusManager.getManager().handle(ValidationStatus.error(message, e), StatusManager.LOG);
			}
		}
		
		return statuses;
	}
}
