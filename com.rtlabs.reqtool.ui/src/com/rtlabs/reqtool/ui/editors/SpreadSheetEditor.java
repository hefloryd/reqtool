package com.rtlabs.reqtool.ui.editors;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.util.EditUIUtil;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.MultiLineTextCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.gui.ICellEditDialog;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.LineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.EditorPart;

import com.rtlabs.reqtool.model.fixture.RequirementService;
import com.rtlabs.reqtool.model.requirements.Priority;
import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.Specification;
import com.rtlabs.reqtool.model.requirements.State;
import com.rtlabs.reqtool.model.requirements.provider.RequirementsItemProviderAdapterFactory;

public class SpreadSheetEditor extends EditorPart {
	public static final Object ID = "com.rtlabs.reqtool.ui.editor";

	private static final String LABEL_BODY = "_BODY"; // BODY seems to affect entire bodyDataLayer
	private static final String LABEL_PRIORITY = "PRIORITY";
	private static final String LABEL_STATE = "STATE";

	private AdapterFactoryEditingDomain editingDomain;
	private ComposedAdapterFactory adapterFactory;

	private Specification specification;
	
	public class EditorConfiguration extends AbstractRegistryConfiguration {

		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {
			//configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.NEVER_EDITABLE);
		
			registerBodyEditor(configRegistry);
			registerPriorityEditor(configRegistry);
			registerStateEditor(configRegistry);
		}

		private void registerBodyEditor(IConfigRegistry configRegistry) {
			configRegistry.registerConfigAttribute(
					EditConfigAttributes.CELL_EDITOR,
					new MultiLineTextCellEditor(false),
					DisplayMode.NORMAL,
					LABEL_BODY);

            // configure the multi line text editor to always open in a
            // subdialog
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.OPEN_IN_DIALOG,
                    Boolean.FALSE,
                    DisplayMode.EDIT,
                    LABEL_BODY);

			Style cellStyle = new Style();
            cellStyle.setAttributeValue(
                    CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                    HorizontalAlignmentEnum.LEFT);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    cellStyle,
                    DisplayMode.NORMAL,
                    LABEL_BODY);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    cellStyle,
                    DisplayMode.EDIT,
                    LABEL_BODY);

            // configure custom dialog settings
            Display display = Display.getCurrent();
            Map<String, Object> editDialogSettings = new HashMap<String, Object>();
            editDialogSettings.put(ICellEditDialog.DIALOG_SHELL_TITLE, "My custom value");
            editDialogSettings.put(ICellEditDialog.DIALOG_SHELL_ICON, display.getSystemImage(SWT.ICON_WARNING));
            editDialogSettings.put(ICellEditDialog.DIALOG_SHELL_RESIZABLE, Boolean.TRUE);

            Point size = new Point(400, 300);
            editDialogSettings.put(ICellEditDialog.DIALOG_SHELL_SIZE, size);

            int screenWidth = display.getBounds().width;
            int screenHeight = display.getBounds().height;
            Point location = new Point(
                    (screenWidth / (2 * display.getMonitors().length)) - (size.x / 2),
                    (screenHeight / 2) - (size.y / 2));
            editDialogSettings.put(ICellEditDialog.DIALOG_SHELL_LOCATION, location);

            // add custom message
            editDialogSettings.put(ICellEditDialog.DIALOG_MESSAGE, "Enter some free text in here:");

            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.EDIT_DIALOG_SETTINGS,
                    editDialogSettings,
                    DisplayMode.EDIT,
                    LABEL_BODY);

			configRegistry.registerConfigAttribute(
					EditConfigAttributes.CELL_EDITABLE_RULE,
					IEditableRule.ALWAYS_EDITABLE,
					DisplayMode.EDIT,
					LABEL_BODY);
		}
		
		private void registerPriorityEditor(IConfigRegistry configRegistry) {
			ComboBoxCellEditor comboBoxCellEditor = new ComboBoxCellEditor(Priority.VALUES);
			configRegistry.registerConfigAttribute(
					EditConfigAttributes.CELL_EDITOR,
					comboBoxCellEditor,
					DisplayMode.EDIT,
					LABEL_PRIORITY);
			configRegistry.registerConfigAttribute(
					EditConfigAttributes.CELL_EDITABLE_RULE,
					IEditableRule.ALWAYS_EDITABLE,
					DisplayMode.EDIT,
					LABEL_PRIORITY);
		}

		private void registerStateEditor(IConfigRegistry configRegistry) {
			ComboBoxCellEditor comboBoxCellEditor = new ComboBoxCellEditor(State.VALUES);
			configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    comboBoxCellEditor,
                    DisplayMode.EDIT,
                    LABEL_STATE);
			configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE,
                    IEditableRule.ALWAYS_EDITABLE,
                    DisplayMode.EDIT,
                    LABEL_STATE);
		}

	}

	public SpreadSheetEditor() {
		super();	
		initializeEditingDomain();
	}
	
	protected void initializeEditingDomain() {
		// Create an adapter factory that yields item providers.
		//
		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

		adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		adapterFactory.addAdapterFactory(new RequirementsItemProviderAdapterFactory());
		adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());

		// Create the command stack that will notify this editor as commands are executed.
		//
		BasicCommandStack commandStack = new BasicCommandStack();

		// Add a listener to set the most recent command's affected objects to be the selection of the viewer with focus.
		//
		commandStack.addCommandStackListener
			(new CommandStackListener() {
				 public void commandStackChanged(final EventObject event) {
					 PlatformUI.getWorkbench().getDisplay().asyncExec
						 (new Runnable() {
							  public void run() {
								  firePropertyChange(IEditorPart.PROP_DIRTY);
							  }
						  });
				 }
			 });

		// Create the editing domain with a special command stack.
		//
		editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack, new HashMap<Resource, Boolean>());
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		final Map<Object, Object> saveOptions = new HashMap<Object, Object>();
		saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
		saveOptions.put(Resource.OPTION_LINE_DELIMITER, Resource.OPTION_LINE_DELIMITER_UNSPECIFIED);
		
		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
			// This is the method that gets invoked when the operation runs.
			//
			@Override
			public void execute(IProgressMonitor monitor) {
				// Save the resources to the file system.
				//
				for (Resource resource : editingDomain.getResourceSet().getResources()) {
					try {
						resource.save(saveOptions);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		try {
			operation.run(monitor);
			
			// Refresh the necessary state.
			//
			((BasicCommandStack)editingDomain.getCommandStack()).saveIsDone();
			firePropertyChange(IEditorPart.PROP_DIRTY);
			
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
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
		specification.getRequirements().addAll(RequirementService.getInstance().createRequirements());
	}
	
	protected void createInitialModel() {
		specification = RequirementService.getInstance().getSpecification();
//		EClass eClass = (EClass)RequirementsPackage.eINSTANCE.getEClassifier("Specification");
//		specification = (Specification) RequirementsPackage.eINSTANCE.getRequirementsFactory().create(eClass);
//		specification.getRequirements().addAll(RequirementService.getInstance().createRequirements());
//		specification.getArtifactWrapperContainer().getArtifacts().addAll(RequirementService.getInstance().createArtifactWrappers());
//		specification.getTraceModel().getTraces().addAll(RequirementService.getInstance().createTraces());
	}

	@Override
	public void createPartControl(Composite parent) {
	    parent.setLayout(new GridLayout());

	    // property names of the Requirement class
		String[] propertyNames = { "body", "priority", "state", "outgoing", "created" };

	    // mapping from property to label, needed for column header labels
	    Map<String, String> propertyToLabelMap = new HashMap<String, String>();
	    propertyToLabelMap.put("body", "Body");
	    propertyToLabelMap.put("priority", "Priority");
	    propertyToLabelMap.put("state", "State");
	    propertyToLabelMap.put("outgoing", "Outgoing");
	    propertyToLabelMap.put("created", "Created");

	    IColumnPropertyAccessor<Requirement> columnPropertyAccessor = 
	        new ReflectiveColumnPropertyAccessor<Requirement>(propertyNames);

	    // Create/load the model
	    //createModel();
	    createInitialModel();
	    
	    // build the body layer stack
		IRowDataProvider<Requirement> bodyDataProvider = new ListDataProvider<Requirement>(specification.getRequirements(), columnPropertyAccessor);
	    final DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
	    DefaultBodyLayerStack bodyLayerStack = new DefaultBodyLayerStack(bodyDataLayer);
	    
	    // set row selection model with single selection enabled
	    IRowIdAccessor<Requirement> rowIdAccessor = new IRowIdAccessor<Requirement>() {			
			@Override
			public Serializable getRowId(Requirement requirement) {
				return requirement.getName();
			}
		};
	    RowSelectionModel<Requirement> selectionModel = new RowSelectionModel<Requirement>(bodyLayerStack.getSelectionLayer(), bodyDataProvider, rowIdAccessor, false);
		bodyLayerStack.getSelectionLayer().setSelectionModel(selectionModel);

	    final ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);
		columnLabelAccumulator.registerColumnOverrides(0, LABEL_BODY);
		columnLabelAccumulator.registerColumnOverrides(1, LABEL_PRIORITY);
		columnLabelAccumulator.registerColumnOverrides(2, LABEL_STATE);		
		
	    // build the column header layer stack
	    IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
	    DataLayer columnHeaderDataLayer = new DataLayer(columnHeaderDataProvider);
	    ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayerStack.getViewportLayer(), bodyLayerStack.getSelectionLayer());
	    
	    // build the row header layer stack
	    IDataProvider rowHeaderDataProvider =  new DefaultRowHeaderDataProvider(bodyDataProvider) {

			@Override
			public Object getDataValue(int columnIndex, int rowIndex) {
				return specification.getRequirements().get(rowIndex).getName();
			}
	    	
	    };
	    DataLayer rowHeaderDataLayer = new DataLayer(rowHeaderDataProvider, 40, 20);
	    ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayerStack.getViewportLayer(), bodyLayerStack.getSelectionLayer());
	    
	    // build the corner layer stack
	    ILayer cornerLayer = new CornerLayer(
	    		new DataLayer(new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider)), 
	            rowHeaderLayer, 
	            columnHeaderLayer);
	        
	    
	    // create the grid layer composed with the prior created layer stacks
	    final GridLayer gridLayer = new GridLayer(bodyLayerStack.getViewportLayer(), columnHeaderLayer, rowHeaderLayer, cornerLayer);
	    
	    final NatTable natTable = new NatTable(parent, gridLayer, false);
	    natTable.addConfiguration(new DefaultNatTableStyleConfiguration() {
	    	{
	            hAlign = HorizontalAlignmentEnum.LEFT;
	    		cellPainter = new LineBorderDecorator(
	    						new TextPainter(false, true, 5, true));
	    	}
	    });	    
	    natTable.addConfiguration(new HeaderMenuConfiguration(natTable));
	    natTable.addConfiguration(new EditorConfiguration());
	    natTable.configure();

	    // Fill remainder space with gridlines
	    NatGridLayerPainter layerPainter = new NatGridLayerPainter(natTable);
	    natTable.setLayerPainter(layerPainter);

	    // Listen to model changes, refresh UI
	    specification.eAdapters().add(new EContentAdapter() {

			@Override
			public void notifyChanged(Notification notification) {
				super.notifyChanged(notification);
				natTable.refresh();
			}
	    	
	    });

		addDragSource(bodyLayerStack, natTable);
	    addDropTarget(specification, bodyDataLayer, gridLayer, natTable);
		addSelectionProvider(bodyDataProvider, bodyLayerStack);
	    
	    GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
	}

	private void addSelectionProvider(IRowDataProvider<Requirement> bodyDataProvider, DefaultBodyLayerStack bodyLayerStack) {
		ISelectionProvider selectionProvider = new RowSelectionProvider<Requirement>(
				bodyLayerStack.getSelectionLayer(), 
				bodyDataProvider, 
				false);
		getSite().setSelectionProvider(selectionProvider);
	}

	private void addDropTarget(final Specification specification, final DataLayer bodyDataLayer, final GridLayer gridLayer, final NatTable natTable) {
		Transfer[] dropTransfers = new Transfer[] { 
				org.eclipse.ui.part.EditorInputTransfer.getInstance(), 
				org.eclipse.swt.dnd.FileTransfer.getInstance(),
				org.eclipse.swt.dnd.RTFTransfer.getInstance(),
				org.eclipse.swt.dnd.TextTransfer.getInstance(),
				org.eclipse.swt.dnd.URLTransfer.getInstance(),
				org.eclipse.jface.util.LocalSelectionTransfer.getTransfer(),
				org.eclipse.ui.part.ResourceTransfer.getInstance(),
				org.eclipse.emf.edit.ui.dnd.LocalTransfer.getInstance()
		};
		SpreadSheetDropTargetListener dropTargetListener = new SpreadSheetDropTargetListener(natTable, gridLayer, specification, bodyDataLayer);
		natTable.addDropSupport(DND.DROP_COPY, dropTransfers, dropTargetListener);
	}

	private void addDragSource(DefaultBodyLayerStack bodyLayerStack, final NatTable natTable) {
		Transfer[] dragTransfers = new Transfer[] { 
				org.eclipse.emf.edit.ui.dnd.LocalTransfer.getInstance() 
		};
		SpreadSheetDragSourceListener dragSourceListener = new SpreadSheetDragSourceListener(bodyLayerStack.getSelectionLayer(), natTable);
		natTable.addDragSupport(DND.DROP_COPY, dragTransfers, dragSourceListener);
	}

	@Override
	public void setFocus() {
	}
	
	
}
