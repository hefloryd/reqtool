package com.rtlabs.reqtool.ui.editors;

import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.SPECIFICATION;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.ui.util.EditUIUtil;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.statushandlers.StatusManager;

import com.rtlabs.common.edit_support.EditContext;
import com.rtlabs.reqtool.model.requirements.Specification;
import com.rtlabs.reqtool.ui.Activator;

/**
 * An editor for the {@link Specification} entity. Contains one page with a list of requirements and 
 * one page with specification details.
 */
public class SpecificationEditor extends FormEditor implements EditContext {
	public static final String EDITOR_ID = "com.rtlabs.reqtool.ui.editor";

	private AdapterFactoryEditingDomain editingDomain;
	private ComposedAdapterFactory adapterFactory;
	private DataBindingContext dataBindingContext = new EMFDataBindingContext();

	private WritableValue<Specification> specification;
	
	public SpecificationEditor() {
		initializeEditingDomain();
	}
	
	protected void initializeEditingDomain() {
		// Create an adapter factory that yields item providers.
		adapterFactory = Activator.createStandardAdaperFactory();

		// Create the command stack that will notify this editor as commands are executed.
		BasicCommandStack commandStack = new BasicCommandStack();

		// Add a listener to set the most recent command's affected objects to be the selection of the viewer with focus.
		commandStack.addCommandStackListener(
			event -> getSite().getShell().getDisplay().asyncExec(
				() -> firePropertyChange(IEditorPart.PROP_DIRTY)));

		// Create the editing domain with a special command stack.
		editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack, new HashMap<>());

		// Makes resource tolerate unknown features in the files. Report these in the GUI instead. 
		editingDomain.getResourceSet().getLoadOptions().put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, true);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		final Map<Object, Object> saveOptions = new HashMap<>();
		saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
		saveOptions.put(Resource.OPTION_LINE_DELIMITER, Resource.OPTION_LINE_DELIMITER_UNSPECIFIED);
		
		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
			// This is the method that gets invoked when the operation runs.
			@Override
			public void execute(IProgressMonitor mon) {
				// Save the resources to the file system.
				//
				for (Resource resource : editingDomain.getResourceSet().getResources()) {
					try {
						resource.save(saveOptions);
					} catch (IOException e) {
						StatusManager.getManager().handle(
							ValidationStatus.error(e.getMessage(), e), StatusManager.BLOCK | StatusManager.LOG);
					}
				}
			}
		};
		
		try {
			operation.run(monitor);
			
			// Refresh the necessary state
			((BasicCommandStack) editingDomain.getCommandStack()).saveIsDone();
			firePropertyChange(IEditorPart.PROP_DIRTY);
			
		} catch (InterruptedException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doSaveAs() {
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		setPartName(input.getName());
		createModel();
	}

	@Override
	public boolean isDirty() {
		return ((BasicCommandStack) editingDomain.getCommandStack()).isSaveNeeded();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	private void createModel() {
		URI resourceURI = EditUIUtil.getURI(getEditorInput(), editingDomain.getResourceSet().getURIConverter());	
		Resource resource = editingDomain.getResourceSet().getResource(resourceURI, true);
		specification = new WritableValue<>((Specification) resource.getContents().get(0), SPECIFICATION);
	}
	
	@Override
	protected void addPages() {
		try {
			// Use this method instead of addPage(IFormPage) to get proper initialisation  
			addPage(new RequirementTablePage(this, getSpecification()), getEditorInput());
			addPage(new SpecificationDetailsPage(this, getSpecification()), getEditorInput());
		} catch (PartInitException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public DataBindingContext getDataBindingContext() {
		return dataBindingContext;
	}

	@Override
	public AdapterFactory getAdapterFactory() {
		return adapterFactory;
	}

	public EditingDomain getEditingDomain() {
		return editingDomain;
	}

	public IObservableValue<Specification> getSpecification() {
		return specification;
	}

	public Specification getSpecificationValue() {
		return getSpecification().getValue();
	}
}
