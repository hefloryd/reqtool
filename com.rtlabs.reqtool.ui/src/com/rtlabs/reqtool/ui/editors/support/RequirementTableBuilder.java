package com.rtlabs.reqtool.ui.editors.support;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.config.DialogErrorHandling;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.MultiLineTextCellEditor;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
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
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultColumnHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.GradientBackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.LineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.action.ViewportSelectRowAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

import com.rtlabs.reqtool.model.requirements.Priority;
import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.RequirementType;
import com.rtlabs.reqtool.model.requirements.Specification;
import com.rtlabs.reqtool.model.requirements.State;
import com.rtlabs.reqtool.ui.Activator;

/**
 * Creates and configures a {@link NatTable} that will show a list of {@link Requirement} objects.
 */
public class RequirementTableBuilder {
	
	private static final String LABEL_BODY = "_BODY"; // BODY seems to affect entire bodyDataLayer
	private static final String LABEL_TYPE = "TYPE";
	private static final String LABEL_PRIORITY = "PRIORITY";
	private static final String LABEL_STATE = "STATE";

	// Input fields
	private AdapterFactory adapterFactory;
	private Specification specification;
	private Composite parent;
	private IServiceLocator serviceLocator;
	
	// Result fields
	private NatTable natTable;
	private RowSelectionProvider<Requirement> rowSelectionProvider;
	
	public RequirementTableBuilder(AdapterFactory adapterFactory, Specification specification, Composite parent, IServiceLocator serviceLocator) {
		this.adapterFactory = adapterFactory;
		this.specification = specification;
		this.parent = parent;
		this.serviceLocator = serviceLocator;
	}

	public void build() {
		// Property names of the Requirement class
		String[] propertyNames = { "body", "type", "priority", "state", "parents", "children", "created" };

		// Mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("body", "Body");
		propertyToLabelMap.put("type", "Type");
		propertyToLabelMap.put("priority", "Priority");
		propertyToLabelMap.put("state", "State");
		propertyToLabelMap.put("parents", "Parents");
		propertyToLabelMap.put("children", "Children");
		propertyToLabelMap.put("created", "Created");

		IColumnAccessor<Requirement> columnPropertyAccessor = new RequirementTableColumnPropertyAccessor<>(adapterFactory, propertyNames);
		
		IRowDataProvider<Requirement> bodyDataProvider = new ListDataProvider<>(
			specification.getRequirements(), columnPropertyAccessor);
		
		final DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
		DefaultBodyLayerStack bodyLayerStack = new DefaultBodyLayerStack(bodyDataLayer);
		
		// set row selection model with single selection enabled
		RowSelectionModel<Requirement> selectionModel = new RowSelectionModel<>(bodyLayerStack.getSelectionLayer(), 
				bodyDataProvider, Object::toString, false);
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
		
		natTable = new NatTable(parent, gridLayer, false);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration() {
			{
				hAlign = HorizontalAlignmentEnum.LEFT;
				cellPainter = new LineBorderDecorator(new TextPainter(false, true, 5, true));
			}
		});

		natTable.addConfiguration(headerStyle());
		natTable.addConfiguration(selectionStyle());

		natTable.addConfiguration(rowHeaderConfiguration(natTable, serviceLocator));
		natTable.addConfiguration(bodyEditorConfiguration(bodyDataProvider));
		natTable.addConfiguration(typeEditorConfiguration());
		natTable.addConfiguration(priorityEditorConfiguration());
		natTable.addConfiguration(stateEditorConfiguration());
		
		natTable.configure();

		// Fill remainder space with gridlines
		natTable.setLayerPainter(new NatGridLayerPainter(natTable));

		rowSelectionProvider = new RowSelectionProvider<>(bodyLayerStack.getSelectionLayer(), bodyDataProvider, false);

		addDragSource(bodyLayerStack, natTable);
		addDropTarget(specification, bodyDataLayer, gridLayer, natTable);
	}

	public ISelectionProvider getRowSelectionProvider() {
		return rowSelectionProvider;
	}
	
	public NatTable getTable() {
		return natTable;
	}
	
	
	private static HeaderMenuConfiguration rowHeaderConfiguration(final NatTable natTable, IServiceLocator serviceLocator) {
		return new HeaderMenuConfiguration(natTable) {
			@Override
			protected PopupMenuBuilder createRowHeaderMenu(NatTable table) {
				return super.createRowHeaderMenu(table)
					.withContributionItem(new CommandContributionItem(new CommandContributionItemParameter(serviceLocator,
						Activator.PLUGIN_ID + ".table.menu.generateRobotTestCase", 
						"com.rtlabs.reqtool.ui.generateRobotTestCase", 
						CommandContributionItem.STYLE_PUSH)) {
							@Override
							public boolean isDynamic() {
								// Work-around for weird behaviour (possibly bug) in MenuManager, which makes item 
								// disappear every other time the menu is showed
								return true;
							}
					});
			}
			
			@Override
			public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
				// Configure a right-click action which both selects a row and opens a menu. This is because when
				// the Generate Test command is invoked from the menu, the selected row should be the one that
				// the user clicked.
				uiBindingRegistry.registerFirstSingleClickBinding(
					MouseEventMatcher.rowHeaderRightClick(SWT.NONE), 
					new IMouseAction() {
						private final IMouseAction selectAction = new ViewportSelectRowAction(false, false);
						private final IMouseAction menuAction = new PopupMenuAction(rowHeaderMenu);
						@Override
						public void run(NatTable table, MouseEvent event) {
							selectAction.run(table, event);
							menuAction.run(table, event);
						}
					});
				
				// Copied from overridden super class method
				uiBindingRegistry.registerMouseDownBinding(
					new MouseEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, MouseEventMatcher.RIGHT_BUTTON),
					new PopupMenuAction(this.colHeaderMenu));
				// Copied from overridden super class method
				uiBindingRegistry.registerMouseDownBinding(
					new MouseEventMatcher(SWT.NONE, GridRegion.CORNER, MouseEventMatcher.RIGHT_BUTTON),
					new PopupMenuAction(this.cornerMenu));
			}
		};
	}

	@SuppressWarnings("unused")
	private static class DebugMenuConfiguration extends AbstractUiBindingConfiguration {

		private final Menu debugMenu;

		public DebugMenuConfiguration(NatTable natTable) {
			// [2] create the menu using the PopupMenuBuilder
			this.debugMenu = new PopupMenuBuilder(natTable).withInspectLabelsMenuItem().build();
		}

		@Override
		public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
			// [3] bind the PopupMenuAction to a right click
			// using GridRegion.COLUMN_HEADER instead of null would
			// for example open the menu only on performing a right
			// click on the column header instead of any region
			uiBindingRegistry.registerMouseDownBinding(
				new MouseEventMatcher(SWT.NONE, null, MouseEventMatcher.RIGHT_BUTTON),
				new PopupMenuAction(this.debugMenu));
		}

	}
	
	private static DefaultColumnHeaderStyleConfiguration headerStyle() {
		// Gives the column header a gray-to-white gradient.
		DefaultColumnHeaderStyleConfiguration columnHeaderStyle = new DefaultColumnHeaderStyleConfiguration();
		columnHeaderStyle.gradientFgColor =  GUIHelper.COLOR_GRAY;
		columnHeaderStyle.gradientBgColor =  GUIHelper.COLOR_WHITE;
		columnHeaderStyle.renderGridLines = true;
		columnHeaderStyle.cellPainter = new GradientBackgroundPainter(new TextPainter(false, false), true);
		return columnHeaderStyle;
	}

	private static DefaultSelectionStyleConfiguration selectionStyle() {
		// Gives the selection same font as rest of the table, with font colour white and gray background 
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
	
	private static IConfiguration bodyEditorConfiguration(IRowDataProvider<Requirement> dataProvider) {
		return new AbstractRegistryConfiguration() {
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				configRegistry.registerConfigAttribute(
						EditConfigAttributes.CELL_EDITOR,
						new MultiLineTextCellEditor(false),
						DisplayMode.EDIT,
						LABEL_BODY);
	
	
				// Highlighting converter for normal mode
				configRegistry.registerConfigAttribute(
						CellConfigAttributes.DISPLAY_CONVERTER, 
						new HighlighterConverter(dataProvider),
						DisplayMode.NORMAL, 
						LABEL_BODY);			
				
				// Normal converter for edit mode
				configRegistry.registerConfigAttribute(
						CellConfigAttributes.DISPLAY_CONVERTER,
						new DefaultDisplayConverter(),
						DisplayMode.EDIT,
						LABEL_BODY);
	
				
				Style cellStyle = new Style();
				cellStyle.setAttributeValue(
						CellStyleAttributes.HORIZONTAL_ALIGNMENT,
						HorizontalAlignmentEnum.LEFT);
				
				configRegistry.registerConfigAttribute(
						CellConfigAttributes.CELL_PAINTER,
						new BackgroundPainter(new PaddingDecorator(new RichTextMultiLineCellPainter(true), 2, 5, 2, 5)),
						DisplayMode.NORMAL,
						LABEL_BODY);
				
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
	
				// Configure the multi line text editor to always open in a sub-dialog.
				// NOTE: Due to a bug in NatTable it is not possible to use this together with and error handler
				// with allowCommit=true.
				//
				//			configRegistry.registerConfigAttribute(
				//					EditConfigAttributes.OPEN_IN_DIALOG,
				//					true,
				//					DisplayMode.EDIT,
				//					LABEL_BODY);
				// 
				// // Configure pop-up editor dialog settings
				// Display display = Display.getCurrent();
				// Map<String, Object> editDialogSettings = new HashMap<>();
				// editDialogSettings.put(ICellEditDialog.DIALOG_SHELL_TITLE, "Enter requirement description");
				// editDialogSettings.put(ICellEditDialog.DIALOG_SHELL_ICON, display.getSystemImage(SWT.ICON_WARNING));
				// editDialogSettings.put(ICellEditDialog.DIALOG_SHELL_RESIZABLE, Boolean.TRUE);
	            // 
				// Point size = new Point(400, 300);
				// editDialogSettings.put(ICellEditDialog.DIALOG_SHELL_SIZE, size);
	            // 
				// int screenWidth = display.getBounds().width;
				// int screenHeight = display.getBounds().height;
				// Point location = new Point(
				// 		(screenWidth / (2 * display.getMonitors().length)) - (size.x / 2),
				// 		(screenHeight / 2) - (size.y / 2));
				// editDialogSettings.put(ICellEditDialog.DIALOG_SHELL_LOCATION, location);
	            // 
				// // Add custom message
				// editDialogSettings.put(ICellEditDialog.DIALOG_MESSAGE, "Enter some free text in here:");
	            // 
				// configRegistry.registerConfigAttribute(
				// 		EditConfigAttributes.EDIT_DIALOG_SETTINGS,
				// 		editDialogSettings,
				// 		DisplayMode.EDIT,
				// 		LABEL_BODY);
	
				configRegistry.registerConfigAttribute(
						EditConfigAttributes.CELL_EDITABLE_RULE,
						IEditableRule.ALWAYS_EDITABLE,
						DisplayMode.EDIT,
						LABEL_BODY);
				
				configRegistry.registerConfigAttribute(
					EditConfigAttributes.VALIDATION_ERROR_HANDLER,
					new DialogErrorHandling(true), 
					DisplayMode.EDIT, 
					LABEL_BODY);
				
				configRegistry.registerConfigAttribute(
					EditConfigAttributes.DATA_VALIDATOR,
					new RequirementTypeValidator(dataProvider),
					DisplayMode.EDIT);
			}
			
		};
	}
	
	private static IConfiguration typeEditorConfiguration() {
		return new AbstractRegistryConfiguration() {
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
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
		};
	}

	private static IConfiguration priorityEditorConfiguration() {
		return new AbstractRegistryConfiguration() {
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
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
		};
	}

	private static IConfiguration stateEditorConfiguration() {
		return new AbstractRegistryConfiguration() {
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
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
		};
	}
	
	private static void addDropTarget(final Specification spec, final DataLayer bodyDataLayer, final GridLayer gridLayer, final NatTable natTable) {
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
		RequirementTableDropTargetListener dropTargetListener = new RequirementTableDropTargetListener(natTable, gridLayer, spec, bodyDataLayer);
		natTable.addDropSupport(DND.DROP_COPY, dropTransfers, dropTargetListener);
	}

	private static void addDragSource(DefaultBodyLayerStack bodyLayerStack, final NatTable natTable) {
		Transfer[] dragTransfers = { 
				org.eclipse.emf.edit.ui.dnd.LocalTransfer.getInstance() 
		};
		RequirementTableDragSourceListener dragSourceListener = new RequirementTableDragSourceListener(bodyLayerStack.getSelectionLayer(), natTable);
		natTable.addDragSupport(DND.DROP_COPY, dragTransfers, dragSourceListener);
	}


}
