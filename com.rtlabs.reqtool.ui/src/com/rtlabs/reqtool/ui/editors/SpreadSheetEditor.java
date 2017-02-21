package com.rtlabs.reqtool.ui.editors;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.util.EditUIUtil;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.EditorPart;

import com.rtlabs.reqtool.model.requirements.Specification;
import com.rtlabs.reqtool.model.requirements.provider.RequirementsItemProviderAdapterFactory;

/**
 * An editor which displays a table of requirements.
 */
public class SpreadSheetEditor extends EditorPart implements IEditingDomainProvider {
	public static final Object ID = "com.rtlabs.reqtool.ui.editor";

	private AdapterFactoryEditingDomain editingDomain;
	private ComposedAdapterFactory adapterFactory;

	private Specification specification;
	
	public SpreadSheetEditor() {
		initializeEditingDomain();
	}
	
	protected void initializeEditingDomain() {
		// Create an adapter factory that yields item providers.
		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

		adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		adapterFactory.addAdapterFactory(new RequirementsItemProviderAdapterFactory());
		adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());

		// Create the command stack that will notify this editor as commands are executed.
		BasicCommandStack commandStack = new BasicCommandStack();

		// Add a listener to set the most recent command's affected objects to be the selection of the viewer with focus.
		commandStack.addCommandStackListener(
			event -> PlatformUI.getWorkbench().getDisplay().asyncExec(
				() -> firePropertyChange(IEditorPart.PROP_DIRTY)));

		// Create the editing domain with a special command stack.
		editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack, new HashMap<>());
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
						e.printStackTrace();
					}
				}
			}
		};
		
		try {
			operation.run(monitor);
			
			// Refresh the necessary state.
			((BasicCommandStack)editingDomain.getCommandStack()).saveIsDone();
			firePropertyChange(IEditorPart.PROP_DIRTY);
			
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		setSite(site);
		setInputWithNotify(input);
		setPartName(input.getName());
	}

	@Override
	public boolean isDirty() {
		return ((BasicCommandStack)editingDomain.getCommandStack()).isSaveNeeded();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public void createModel() {
		URI resourceURI = EditUIUtil.getURI(getEditorInput(), editingDomain.getResourceSet().getURIConverter());	
		Resource resource = editingDomain.getResourceSet().getResource(resourceURI, true);
		specification = (Specification) resource.getContents().get(0);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());

		// Create/load the model
		createModel();

		RequirementTableBuilder tableBuilder = new RequirementTableBuilder(adapterFactory, specification, parent, getSite());
		tableBuilder.build();
		NatTable natTable = tableBuilder.getTable();
		
		getSite().setSelectionProvider(tableBuilder.getRowSelectionProvider());

		// Listen to model changes, refresh UI
		specification.eAdapters().add(new EContentAdapter() {
			@Override
			public void notifyChanged(Notification notification) {
				super.notifyChanged(notification);
				natTable.refresh();
			}
		});

		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
	}


	@Override
	public void setFocus() {
	}
	
	public Specification getSpecification() {
		return specification;
	}
	
	public EditingDomain getEditingDomain() {
		return editingDomain;
	}
}
