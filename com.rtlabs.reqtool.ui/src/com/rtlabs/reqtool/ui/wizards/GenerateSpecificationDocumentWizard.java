
package com.rtlabs.reqtool.ui.wizards;

import static com.rtlabs.reqtool.ui.model.wizards.WizardsPackage.Literals.EXPORT_DOCUMENT_VIEW_MODEL__EXPORT_ONLY_SELECTED_REQUIREMENTS;
import static com.rtlabs.reqtool.ui.model.wizards.WizardsPackage.Literals.EXPORT_DOCUMENT_VIEW_MODEL__OUTPUT_FILE;
import static com.rtlabs.reqtool.ui.model.wizards.WizardsPackage.Literals.EXPORT_DOCUMENT_VIEW_MODEL__SOURCE_SPECIFICATION_FILE;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.statushandlers.StatusManager;

import com.rtlabs.common.databinding.DiagnosticReporter;
import com.rtlabs.common.dialogs.FilteredElementTreeSelectionDialog;
import com.rtlabs.common.edit_support.CommandOperation;
import com.rtlabs.common.edit_support.EditContext;
import com.rtlabs.common.edit_support.SimpleEditContext;
import com.rtlabs.common.model_gui_builder.ModelGuiBuilder;
import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.Specification;
import com.rtlabs.reqtool.ui.Activator;
import com.rtlabs.reqtool.ui.model.wizards.ExportDocumentViewModel;
import com.rtlabs.reqtool.ui.model.wizards.WizardsFactory;
import com.rtlabs.reqtool.ui.model.wizards.provider.WizardsItemProviderAdapterFactory;
import com.rtlabs.reqtool.ui.specification_document_generation.SpecDocGenerator;
import com.rtlabs.reqtool.util.Result;
/**
 * Wizard for exporting specifications to Markdown documents.
 * <p/> 
 * This wizard has two text fields for input and output files, both with buttons for browsing for files.
 * <p/> 
 * The implementation uses the Model-view-viewmodel pattern (MVVM). It has a "view model" class which contains all
 * data that is showed in the GUI. This makes the data decoupled from the GUI components, and also easily and
 * uniformly accessible.
 * <p/>
 * The view model is create as a local EMF model.
 */
public class GenerateSpecificationDocumentWizard extends Wizard implements IExportWizard {

	/**
	 * The supported extensions for created files.
	 */
	public static final String FILE_EXTENSION = "spec";
	
	/**
	 * This is the file creation page.
	 */
	protected Page page;
	private IWorkbench workbench;
	
	private IStructuredSelection selection;

	private ExportDocumentViewModel viewModel = WizardsFactory.eINSTANCE.createExportDocumentViewModel();
	private EditContext editContext = SimpleEditContext.create(
		Activator.createStandardAdaperFactory(new WizardsItemProviderAdapterFactory()));

	private AggregateValidationStatus aggregateValidationStatus = new AggregateValidationStatus(
		editContext.getDataBindingContext().getBindings(),
		AggregateValidationStatus.MAX_SEVERITY);

	
	@Override
	public void init(IWorkbench wb, IStructuredSelection s) {		
		this.workbench = wb;
		this.selection = s;

		setWindowTitle("Export");
		setHelpAvailable(false);
		
		setInitialValues();
	}

		// Get the the file of the currently edited specification, if any
	private void setInitialValues() {
		Optional<IResource> editorFile = Optional.ofNullable(workbench.getActiveWorkbenchWindow())
			.map(w -> w.getActivePage())
			.map(p -> p.getActiveEditor())
			.map(e -> e.getEditorInput())
			.filter(i -> i instanceof IFileEditorInput)
			.map(i -> (IResource) ((IFileEditorInput) i).getFile())
			.filter(f -> Objects.equals(f.getFileExtension(), FILE_EXTENSION));

		// Choose intput be: selected path if it is present, otherwise editorFile
		IResource inputFile = selectedSpecificationPath().map(Optional::<IResource>of)
			.orElse(editorFile).orElse(null);
		
		if (inputFile == null) return;
		
		viewModel.setSourceSpecificationFile(inputFile.getFullPath().toString());

		// Set output path to the selected container, or 
		IResource outputPath = inputFile.getType() == IResource.FILE
			? inputFile.getParent().getFile(
				new org.eclipse.core.runtime.Path(inputFile.getFullPath().removeFileExtension().addFileExtension("md").lastSegment()))
			: inputFile;

		viewModel.setOutputFile(outputPath.getLocation().toOSString());
	}
	
	private static Optional<IFile> fileObject(String filePath) {
		return Optional.ofNullable(ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(
			new org.eclipse.core.runtime.Path(filePath)));
	}

	private Result<Boolean> generateSpecDoc() throws CoreException, IOException {
		Result<Specification> specResult = readSpec();
		if (!specResult.isAllOk()) return specResult.castError();
		Specification spec = specResult.getResult();

		Path specFile = Paths.get(
			ResourcesPlugin.getWorkspace().getRoot().findMember(viewModel.getSourceSpecificationFile()).getLocationURI());
		
		Result<Path> outputResult = checkedToPath(viewModel.getOutputFile());
		if (!outputResult.isAllOk()) return Result.failure("The output path is invalid: " + outputResult.getMessage());
		Path outputFile = outputResult.getResult();

		String docPrefix = "";
		if (!spec.getDocumentPrefixFile().isEmpty()) {
			Result<Path> prefixPath = toExistingFile(spec.getDocumentPrefixFile(), 
				specFile.getParent(), "Document prefix file");
			if (!prefixPath.isAllOk()) return prefixPath.castError();
			docPrefix = readFile(prefixPath.getResult(), StandardCharsets.UTF_8);
		}
		
		String docSuffix = "";
		if (!spec.getDocumentSuffixFile().isEmpty()) {
			Result<Path> postfixPath = toExistingFile(spec.getDocumentSuffixFile(), 
				specFile.getParent(), "Document suffix file");
			if (!postfixPath.isAllOk()) return postfixPath.castError();
			docSuffix = readFile(postfixPath.getResult(), StandardCharsets.UTF_8);
		}
		
		if (Files.exists(outputFile)) {
			if (!promptOverwrite(outputFile)) return Result.success(false);
		}

		outputDocument(spec, outputFile, docPrefix, docSuffix);

		updateWorkbench(outputFile);
		
		return Result.success(true);
	}

	private void outputDocument(Specification spec, Path outputFile, String docPrefix, String docSuffix) throws IOException {
		// Take the selected requirements if that is selected in the GUI, or all reqs in the spec
		List<Requirement> reqs = viewModel.isExportOnlySelectedRequirements()
			? Arrays.stream(selection.toArray())
				.filter(e -> e instanceof Requirement)
				.map(e -> (Requirement)  e).collect(toList())
			: spec.getRequirements();

		String generatedText = SpecDocGenerator.run(spec, reqs, Activator.createStandardAdaperFactory());
		
		String doc = docPrefix + generatedText + docSuffix;
		
		Files.createDirectories(outputFile.getParent());
		Files.write(outputFile, doc.getBytes(StandardCharsets.UTF_8));
	}

	private boolean promptOverwrite(Path outputFile) {
		boolean overwriteOk = MessageDialog.openConfirm(getShell(), "Overwrite file?", 
			"The file output already exists. Should it be overwritten?\n\n"
			+ "File: " + outputFile);
		return overwriteOk;
	}
	
	private Result<Specification> readSpec() {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getLoadOptions().put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, true);
		String outputFileStr = viewModel.getSourceSpecificationFile();
		URI fileURI = URI.createPlatformResourceURI(outputFileStr, true);
		Resource resource = resourceSet.getResource(fileURI, true);
		Specification spec = (Specification) resource.getContents().get(0);

		return spec == null  
			? Result.failure("The selected specification file could not be read.")
			: Result.success(spec);
	}

	private void updateWorkbench(Path outputFile) throws CoreException {
		IFile outputWorkspaceFile = fileObject(outputFile.toString()).orElse(null);
		
		if (outputWorkspaceFile != null) {
			// Refresh if output file is in the workspace
			outputWorkspaceFile.refreshLocal(IResource.DEPTH_ZERO, null);
		
			// Select the new file resource in the current view
			IWorkbenchPart activePart = workbench.getActiveWorkbenchWindow().getActivePage().getActivePart();
			if (activePart instanceof ISetSelectionTarget) {
				ISelection newSelection = new StructuredSelection(outputWorkspaceFile);			
				getShell().getDisplay().asyncExec(
					() -> ((ISetSelectionTarget) activePart).selectReveal(newSelection));
			}
		}
	}

	@Override
	public boolean performFinish() {
		try {
			Result<Boolean> result = generateSpecDoc();
			
			if (result.isAllOk()) {
				return result.getResult();
			} else {
				StatusManager.getManager().handle(result.toMultiStatus("Problem when generating document."), 
					StatusManager.BLOCK | StatusManager.LOG);
				return false;
			}
		} catch (Exception e) {
			StatusManager.getManager().handle(
				ValidationStatus.error("Unexpected error while generating document.", e), 
				StatusManager.BLOCK | StatusManager.LOG);
			
			return false;
		}
	}
	
	/**
	 * The framework calls this to create the contents of the wizard.
	 */
	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}
		
	public class Page extends WizardPage {
		public Page() {
			super("name");
			setTitle("Export Specification Document");
			setDescription("Export a specification document as a Markdown file.");
		}
		
		@Override
		public void createControl(Composite parent) {
			FormToolkit toolkit = createDialogsFormsToolkit(parent.getShell());
			
			ModelGuiBuilder<ExportDocumentViewModel> guiBuilder = new ModelGuiBuilder<>(toolkit, editContext, viewModel);
			
			Composite container = new Composite(parent, SWT.NONE);
			container.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).spacing(15, 5).create());
			toolkit.paintBordersFor(container);

			guiBuilder.createTextSelectControls(container, EXPORT_DOCUMENT_VIEW_MODEL__SOURCE_SPECIFICATION_FILE, selectSourceDialog(container.getShell()));
			guiBuilder.createTextSelectControls(container, EXPORT_DOCUMENT_VIEW_MODEL__OUTPUT_FILE, selectTargetDialog(container.getShell()));

			guiBuilder.createFeatureControl(container, EXPORT_DOCUMENT_VIEW_MODEL__EXPORT_ONLY_SELECTED_REQUIREMENTS);
			
			aggregateValidationStatus.addValueChangeListener(validationStatusListener());

			editContext.getDataBindingContext().updateTargets();
			
			setErrorMessage(null);
			setMessage(null);
			setPageComplete(aggregateValidationStatus.getValue().getSeverity() <= IStatus.INFO);
			setControl(container);
		}

		// A listener which updates the wizard messages based on the aggregated valiidation status
		private IValueChangeListener<? super IStatus> validationStatusListener() {
			return event -> {
				IStatus s = event.diff.getNewValue();
				setErrorMessage(s.getSeverity() >= IStatus.WARNING ? s.getMessage() : null);
				setMessage(s.getSeverity() == IStatus.INFO ? s.getMessage() : null);
				setPageComplete(s.getSeverity() <= IStatus.INFO);
			};
		}

		private <T extends EObject> CommandOperation<T> selectSourceDialog(Shell shell) {
			return (EditContext editCxt, IObservableValue<T> containingEntity, EStructuralFeature containingFeature) -> {
				FilteredElementTreeSelectionDialog dialog = new FilteredElementTreeSelectionDialog(
					shell, new WorkbenchLabelProvider(), new WorkbenchContentProvider());
				
				dialog.setTitle("Specification File Selection");
				dialog.setMessage("Select the specification file which will be exported as a document.");
				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());

				IResource intputFile = ResourcesPlugin.getWorkspace().getRoot().findMember(viewModel.getSourceSpecificationFile());
				if (intputFile != null) dialog.setInitialSelection(intputFile);
			
				dialog.setQuickSelectionMode(true);
				dialog.addFilter(typedPredicateFilter(IFile.class, true,  
					f -> f.getFullPath().lastSegment().endsWith("." + FILE_EXTENSION)));
				dialog.setAllowMultiple(false);
				
				dialog.setValidator(s -> Arrays.stream(s).allMatch(f -> f instanceof IFile)
					? new Status(IStatus.OK, "*", "") : ValidationStatus.error("A specification file must be selected."));
				
				if (dialog.open() == Window.OK) { 
					editContext.getEditingDomain().getCommandStack().execute(
						SetCommand.create(editContext.getEditingDomain(), 
							containingEntity.getValue(), containingFeature, dialog.getFirstResult()));
				}
			};
		}

		private CommandOperation<ExportDocumentViewModel> selectTargetDialog(Shell shell) {
			return (EditContext editCxt, IObservableValue<ExportDocumentViewModel> containingEntity, EStructuralFeature containingFeature) -> {

				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				dialog.setOverwrite(true);
				// Set output path text as default
				dialog.setFilterPath(viewModel.getOutputFile());

				String result = dialog.open();
				if (result != null) { 
					editContext.getEditingDomain().getCommandStack().execute(
						SetCommand.create(editContext.getEditingDomain(), 
							containingEntity.getValue(), containingFeature, result));
				}
			};
		}
	}
	
	private static <T> ViewerFilter typedPredicateFilter(
		Class<T> filterClass, 
		boolean includeOtherClasses, 
		Predicate<? super T> fileterPredicate)
	{
		return new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return filterClass.isInstance(element) ? fileterPredicate.test(filterClass.cast(element)) : includeOtherClasses;
			}
		};
	}

	public static boolean validateObject(ExportDocumentViewModel model, DiagnosticChain chain, Map<?, ?> context) {
		boolean result = true;
		DiagnosticReporter reporter = new DiagnosticReporter(chain, context, null, model, "*", -1);

		result &= validateSourceFile(model, reporter);
		result &= validateTargerFile(model, reporter);
		
		return result;
	}

	private static boolean validateSourceFile(ExportDocumentViewModel model, DiagnosticReporter reporter) {
		reporter.setFeature(EXPORT_DOCUMENT_VIEW_MODEL__SOURCE_SPECIFICATION_FILE);
		IStatus status = validatePathSyntax(IResource.FILE, model.getSourceSpecificationFile());
		if (!status.isOK()) {
			reporter.error("Source file: " + status.getMessage());
			return false;
		}
		
		IResource file = ResourcesPlugin.getWorkspace().getRoot().findMember(model.getSourceSpecificationFile());
		
		if (file == null) {
			reporter.error("The source file does not exist.");
			return false;
		}
		
		if (file.getType() != IResource.FILE) {
			reporter.error("The source path is not a file.");
			return false;
		}
		
		if (!FILE_EXTENSION.equals(file.getFileExtension())) {
			reporter.error("The source file must have extension " + FILE_EXTENSION + ".");
			return false;
		}
		
		return true;
	}

	private static Result<Path> toExistingFile(String pathStr, Path resolveAgainst, String guiName) {
		try {
			Path path = Paths.get(pathStr);

			if (!path.isAbsolute()) {
				path = resolveAgainst.resolve(path);
			}
			
			if (!Files.exists(path)) return Result.failure(guiName + "'" + path + "'." +  " does not exist.");
			if (!Files.isRegularFile(path)) return Result.failure(guiName + "'" + path + "'." +  " is not a file.");
			
			return Result.success(path);
		} catch (InvalidPathException e) {
			return Result.failure(guiName + ": " + e.getMessage());
		}
	}
	
	private static Result<Path> checkedToPath(String path) {
		try {
			return Result.success(Paths.get(path));
		} catch (InvalidPathException e) {
			return Result.failure(e.getMessage());
		}
	}
	
	private static boolean validateTargerFile(ExportDocumentViewModel model, DiagnosticReporter reporter) {
		reporter.setFeature(EXPORT_DOCUMENT_VIEW_MODEL__OUTPUT_FILE);
		if (model.getOutputFile().isEmpty()) {
			reporter.error("The output file field must not be empty.");
			return false;
		}
		
		Result<Path> outPath = checkedToPath(model.getOutputFile());
		
		if (!outPath.isAllOk() || !outPath.getResult().isAbsolute()) {
			reporter.error("The output file path is invalid: " + outPath.getMessage());
			return false;
		}

		if (Files.isDirectory(outPath.getResult())) {
			reporter.error("The output path is a directory.");
			return false;
		}
		
		if (Files.exists(outPath.getResult())) {
			reporter.issue("The output file  already exists.", IStatus.INFO);
		}
		
		return true;
	}
	
	private static IStatus validatePathSyntax(int type, String path) {
		if (path.isEmpty()) {
			return ValidationStatus.error("The path can not be empty.");
		}
		
		return ResourcesPlugin.getWorkspace().validatePath(path, type);
	}

	private static FormToolkit createDialogsFormsToolkit(Shell shell) {
		FormColors colors = new FormColors(shell.getDisplay());
		colors.setBackground(shell.getBackground());
		colors.setForeground(shell.getForeground());

		FormToolkit toolkit = new FormToolkit(colors) {
			@Override
			public Text createText(Composite parent, String value, int style) {
				Text text = super.createText(parent, value, style);
				text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
				return text;
			}
		};
		return toolkit;
	}

	private Optional<IContainer> selectedContainer() {
		return selectedResource()
		   .map(r -> r instanceof IContainer ? (IContainer) r : r.getParent());
	}

	private Optional<? extends IResource> selectedSpecificationPath() {
		Optional<IResource> specFile = selectedResource()
			   .map(r -> r instanceof IFile ? r : null)
			   .filter(f -> FILE_EXTENSION.equals(f.getFileExtension()));

		if (specFile.isPresent()) return specFile;
		
		return selectedContainer();
	}
	
	private Optional<IResource> selectedResource() {
		return Arrays.stream(selection.toArray())
			   .filter(o -> o instanceof IAdaptable)
			   .map(o -> ((IAdaptable) o).getAdapter(IResource.class))
			   .findAny();
	}
	
	static String readFile(Path path, Charset encoding) throws IOException {
		return new String(Files.readAllBytes(path), encoding);
	}
}
