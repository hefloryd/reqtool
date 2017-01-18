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
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
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
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
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

import com.rtlabs.reqtool.model.requirements.Priority;
import com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals;
import com.rtlabs.reqtool.model.requirements.Specification;
import com.rtlabs.reqtool.model.requirements.State;
import com.rtlabs.reqtool.model.requirements.provider.RequirementsItemProviderAdapterFactory;

public class SpreadSheetEditor extends EditorPart implements IEditingDomainProvider {
	public static final Object ID = "com.rtlabs.reqtool.ui.editor";

	private static final String LABEL_BODY = "_BODY"; // BODY seems to affect entire bodyDataLayer
	private static final String LABEL_TYPE = "TYPE";
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
		
		private void registerTypeEditor(IConfigRegistry configRegistry) {
			ComboBoxCellEditor comboBoxCellEditor = new ComboBoxCellEditor(RequirementType.VALUES);
			configRegistry.registerConfigAttribute(
					EditConfigAttributes.CELL_EDITOR,
					comboBoxCellEditor,
					DisplayMode.EDIT,
					LABEL_TYPE);
			configRegistry.registerConfigAttribute(
					EditConfigAttributes.CELL_EDITABLE_RULE,
					IEditableRule.ALWAYS_EDITABLE,
					DisplayMode.EDIT,
					LABEL_TYPE);
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
		commandStack.addCommandStackListener(new CommandStackListener() {
			public void commandStackChanged(final EventObject event) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
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

	    // property names of the Requirement class
		String[] propertyNames = { "body", "priority", "state", "parents", "children", "created" };

	    // mapping from property to label, needed for column header labels
	    Map<String, String> propertyToLabelMap = new HashMap<String, String>();
	    propertyToLabelMap.put("body", "Body");
	    propertyToLabelMap.put("priority", "Priority");
	    propertyToLabelMap.put("state", "State");
	    propertyToLabelMap.put("parents", "Parents");
	    propertyToLabelMap.put("children", "Children");
	    propertyToLabelMap.put("created", "Created");

	    IColumnAccessor<EObject> columnPropertyAccessor = new SpreadSheetColumnPropertyAccessor(adapterFactory, propertyNames);
	    
	    // Create/load the model
	    createModel();

	    @SuppressWarnings("unchecked")
		// property names of the Requirement class
		String[] propertyNames = { "body", "type", "priority", "state", "parents", "children", "created" };

		// mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("body", "Body");
		propertyToLabelMap.put("type", "Type");
		propertyToLabelMap.put("priority", "Priority");
		propertyToLabelMap.put("state", "State");
		propertyToLabelMap.put("parents", "Parents");
		propertyToLabelMap.put("children", "Children");
		propertyToLabelMap.put("created", "Created");
		EList<EObject> requirements = (EList<EObject>) specification.eGet(Literals.SPECIFICATION__REQUIREMENTS);

	    // build the body layer stack
		IRowDataProvider<EObject> bodyDataProvider = new ListDataProvider<EObject>(requirements, columnPropertyAccessor);
	    final DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
	    DefaultBodyLayerStack bodyLayerStack = new DefaultBodyLayerStack(bodyDataLayer);
	    
	    // set row selection model with single selection enabled
	    IRowIdAccessor<EObject> rowIdAccessor = new IRowIdAccessor<EObject>() {			
			@Override
			public Serializable getRowId(EObject object) {
				return object.toString();
			}
		};
	    RowSelectionModel<EObject> selectionModel = new RowSelectionModel<EObject>(bodyLayerStack.getSelectionLayer(), bodyDataProvider, rowIdAccessor, false);
		bodyLayerStack.getSelectionLayer().setSelectionModel(selectionModel);
		
		ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);
		columnLabelAccumulator.registerColumnOverrides(0, LABEL_BODY);
		columnLabelAccumulator.registerColumnOverrides(1, LABEL_TYPE);
		columnLabelAccumulator.registerColumnOverrides(2, LABEL_PRIORITY);
		columnLabelAccumulator.registerColumnOverrides(3, LABEL_STATE);		
		
		// Build the column header layer stack
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap); // TODO: Use EMF ItemProvider
		DataLayer columnHeaderDataLayer = new DataLayer(columnHeaderDataProvider);
		ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayerStack.getViewportLayer(), bodyLayerStack.getSelectionLayer());
		
		// Build the row header layer stack
		IDataProvider rowHeaderDataProvider =  new DefaultRowHeaderDataProvider(bodyDataProvider) {
			@Override
			public Object getDataValue(int columnIndex, int rowIndex) {
				return specification.getRequirements().get(rowIndex).getName();
			}
		};
		DataLayer rowHeaderDataLayer = new DataLayer(rowHeaderDataProvider, 40, 20);
		ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayerStack.getViewportLayer(), bodyLayerStack.getSelectionLayer());
		
		// Build the corner layer stack
		ILayer cornerLayer = new CornerLayer(
				new DataLayer(new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider)), 
				rowHeaderLayer, 
				columnHeaderLayer);
			
		
		// Create the grid layer composed with the prior created layer stacks
		final GridLayer gridLayer = new GridLayer(bodyLayerStack.getViewportLayer(), columnHeaderLayer, rowHeaderLayer, cornerLayer);
		
		final NatTable natTable = new NatTable(parent, gridLayer, false);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration() {
			{
				hAlign = HorizontalAlignmentEnum.LEFT;
				cellPainter = new LineBorderDecorator(new TextPainter(false, true, 5, true));
			}
		});

		natTable.addConfiguration(headerStyle());
		natTable.addConfiguration(selectionStyle());
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable));
		natTable.addConfiguration(new EditorConfiguration());
		natTable.configure();

		// Fill remainder space with gridlines
		natTable.setLayerPainter(new NatGridLayerPainter(natTable));

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

	private DefaultColumnHeaderStyleConfiguration headerStyle() {
		DefaultColumnHeaderStyleConfiguration columnHeaderStyle = new DefaultColumnHeaderStyleConfiguration();
		columnHeaderStyle.gradientFgColor =  GUIHelper.COLOR_GRAY;
		columnHeaderStyle.gradientBgColor =  GUIHelper.COLOR_WHITE;
		columnHeaderStyle.renderGridLines = true;
		columnHeaderStyle.cellPainter = new GradientBackgroundPainter(new TextPainter(false, false), true);
		return columnHeaderStyle;
	}

	private DefaultSelectionStyleConfiguration selectionStyle() {
		DefaultSelectionStyleConfiguration selectionConfig = new DefaultSelectionStyleConfiguration() {

			@Override
			protected void configureSelectionStyle(IConfigRegistry configRegistry) {
				Style cellStyle = new Style();
				cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.selectionBgColor);
				cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.selectionFgColor);
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.SELECT);
			}

			@Override
			protected void configureHeaderHasSelectionStyle(IConfigRegistry configRegistry) {
				super.configureHeaderHasSelectionStyle(configRegistry);
				// Overwrite with and empty style
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, new Style(), DisplayMode.SELECT, GridRegion.COLUMN_HEADER);
			}
		};
		return selectionConfig;
	}

	private void addSelectionProvider(IRowDataProvider<EObject> bodyDataProvider, DefaultBodyLayerStack bodyLayerStack) {
		ISelectionProvider selectionProvider = new RowSelectionProvider<EObject>(
				bodyLayerStack.getSelectionLayer(), 
				bodyDataProvider, 
				false);
		getSite().setSelectionProvider(selectionProvider);
	}

	private void addDropTarget(final Specification spec, final DataLayer bodyDataLayer, final GridLayer gridLayer, final NatTable natTable) {
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
		SpreadSheetDropTargetListener dropTargetListener = new SpreadSheetDropTargetListener(natTable, gridLayer, spec, bodyDataLayer);
		natTable.addDropSupport(DND.DROP_COPY, dropTransfers, dropTargetListener);
	}

	private void addDragSource(DefaultBodyLayerStack bodyLayerStack, final NatTable natTable) {
		Transfer[] dragTransfers = { 
				org.eclipse.emf.edit.ui.dnd.LocalTransfer.getInstance() 
		};
		SpreadSheetDragSourceListener dragSourceListener = new SpreadSheetDragSourceListener(bodyLayerStack.getSelectionLayer(), natTable);
		natTable.addDragSupport(DND.DROP_COPY, dragTransfers, dragSourceListener);
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
