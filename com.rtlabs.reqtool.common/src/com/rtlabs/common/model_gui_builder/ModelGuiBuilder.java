package com.rtlabs.common.model_gui_builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditValueProperty;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.rtlabs.common.databinding.ValidationUtil;
import com.rtlabs.common.databinding.ValidationUtil.ObjectToStringConverter;
import com.rtlabs.common.edit_support.CommandOperation;
import com.rtlabs.common.edit_support.EditContext;

/**
 * Helps with the creation and data binding of GUI controls from information in the model.
 * 
 * The most important method is {@link ModelGuiBuilderNEW#createFeatureControl}. It automatically creates a suitable control
 * depending on the type of a feature.
 * 
 * This class uses a {@link IItemPropertySource} to lookup names of features from generated property files in the 
 * EMF Edit projects.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ModelGuiBuilder {
	
	/**
	 * This can be used when a dialog must be passed as argument to somewhere. A dialog can only be used once so
	 * it needs to be created for each use.
	 */
	public interface DialogCreator {
		SelectionDialog create();
	}
	
	public interface LayoutDataFactory {
		Object create(Control control, EStructuralFeature feature, boolean isMultiLine);
	}

	private final EditContext editContext;
	private final FormToolkit toolkit;
	private final IItemPropertySource itemPropertySource;
	private final ResourceLocator resourceLocator;
	private LayoutDataFactory layoutDataFactory = new GridDataFactory();

	private final IObservableValue entity;
	private final EClass entityClass;
	private final EObject dummyObject ;

	private final Composite container;

	private boolean isEditable = true;
	private int modelUpdateDelayMs = 500;
	private String selectButtonLabel = "Select...";

	
	private final Map<EStructuralFeature, Binding> bindingMap = new HashMap<>();
	
	public ModelGuiBuilder(FormToolkit toolkit, EditContext editContext, Composite container, EObject entity) {
		this(toolkit, editContext, container, entity.eClass(), 
			Observables.constantObservableValue(entity, entity.eClass()));
	}
	public ModelGuiBuilder(FormToolkit toolkit, EditContext editContext, Composite container, EClass entityClass, IObservableValue entity) {
		this.editContext = editContext;
		this.container = container;
		this.entityClass = entityClass;
		this.entity = entity;
		this.toolkit = toolkit;
		
		dummyObject = entityClass.getEPackage().getEFactoryInstance().create(entityClass);
		
		itemPropertySource = Objects.requireNonNull(
			(IItemPropertySource) editContext.getAdapterFactory().adapt(dummyObject, IItemPropertySource.class),
			"An IItemPropertySource could not be created for " + entityClass);
		
		// This is a bit of a hack, IItemPropertySource is not strictly required to implement ResourceLocator.
		// But all generated code does. So it's pretty safe.
		resourceLocator = (ResourceLocator) itemPropertySource; 
	}
	
	/**
	 * Creates and data binds controls for all features, with container as parent.
	 */
	public void createFeatureControls(EStructuralFeature... features) {
		createFeatureControls(Arrays.asList(features));
	}
	
	/**
	 * Creates and data binds controls for all features, with container as parent.
	 */
	public void createFeatureControls(List<? extends EStructuralFeature> features) {
		for (EStructuralFeature feature : features) {
			if (feature.getEAnnotation("com.kvaser.kingdomfounder.hide_in_gui") != null
				|| feature.equals(entityClass.getEIDAttribute())
				|| feature.isDerived()
				)
			{
				continue;
			}
			
			// Only include data types, no object types
			if (feature.getEType() instanceof EDataType) {
				createFeatureControl(feature);
			}
		}
	}

	/**
	 * Creates a label and a control. The specific type of control depends on the type of the feature. The feature
	 * must belong to the entity which was set earlier.
	 */
	public Control createFeatureControl(EStructuralFeature feature) {
		Label label = createLabel(feature);

		if (feature.getEType().equals(EcorePackage.Literals.ESTRING) || ModelGuiBuilderSupport.isIntegral(feature.getEType().getInstanceClass())) {
			Text text = createAndBindText(feature);
			// This is for multi-line texts, it works fine for normal texts also but not all controls 
			label.setLayoutData(layoutDataFactory.create(label, feature, isMultiLine(feature)));
			return text;
		} else if (feature.getEType() instanceof EEnum) {
			return createAndBindCombo(feature).getCombo();
		} else if (feature.getEType().equals(EcorePackage.Literals.EBOOLEAN)) {
			return createAndBindCheckbox(feature);
		} else {
			throw new IllegalStateException("Unsupported feature: " + feature);
		}
	}
	
	private Control createAndBindCheckbox(EStructuralFeature feature) {
		Button button = createCheckbox(feature);
		bindCheckbox(button, feature);
		return button;
	}

	public Button createCheckbox(EStructuralFeature feature) {
		Button button = toolkit.createButton(container, "", SWT.CHECK);
		button.setLayoutData(layoutDataFactory.create(button, feature, false));
		return button;
	}

	public Binding bindCheckbox(Button button, EStructuralFeature feature) {
		verifyFeature(feature);
		IObservableValue viewObj = WidgetProperties.selection().observeDelayed(modelUpdateDelayMs, button);
		IEMFEditValueProperty p = EMFEditProperties.value(editContext.getEditingDomain(), feature);
		IObservableValue modelObj = p.observeDetail(entity);
		
		Binding binding = editContext.getDataBindingContext().bindValue(viewObj, modelObj, 
			ValidationUtil.modelVerifyingStrategy(entity, feature, editContext.getAdapterFactory()), 
			ValidationUtil.modelVerifyingStrategy(entity, feature, editContext.getAdapterFactory()));
		
		bindingMap.put(feature, binding);
		
		return binding;
	}

	/**
	 * Creates and returns a control.
	 */
	public Text createText(EStructuralFeature feature) {
		return createText(container, feature);
	}
	
	private Text createText(Composite localContainer, EStructuralFeature feature) {
		verifyFeature(feature);
		
		boolean isMultiLine = isMultiLine(feature);

		Text text = toolkit.createText(localContainer, "", isMultiLine ? SWT.MULTI | SWT.WRAP | SWT.V_SCROLL : SWT.NONE);
		text.setLayoutData(layoutDataFactory.create(text, feature, isMultiLine));
		
		if (isMultiLine) {
			// Ensure tab cursor move works in multi-line texts. User has to paste to get tab characters.
			text.addTraverseListener(ModelGuiBuilderSupport.TRAVERSE_LISTENER);
		}
		
		if (ModelGuiBuilderSupport.isIntegral(feature.getEType().getInstanceClass())) {
			text.addVerifyListener(ModelGuiBuilderSupport.getIntegralProtectionVerifyListener(feature.getEType().getInstanceClass()));
		}

		return text;
	}

	private boolean isMultiLine(EStructuralFeature feature) {
		return itemPropertySource.getPropertyDescriptor(null, feature).isMultiLine(null);
	}


	/**
	 * Binds a control to a feature, starting at {@link ModelGuiBuilderNEW#entity} and following featurePath.
	 */
	public Binding bindText(Text text, EStructuralFeature feature) {
		verifyFeature(feature);
		IObservableValue viewObj = WidgetProperties.text(SWT.Modify).observeDelayed(modelUpdateDelayMs, text);
		IObservableValue modelObj = EMFEditProperties.value(editContext.getEditingDomain(), feature).observeDetail(entity);
		
		UpdateValueStrategy toModel = ValidationUtil.modelVerifyingStrategy(entity, feature, editContext.getAdapterFactory());
		UpdateValueStrategy toTarget = ValidationUtil.modelVerifyingStrategy(entity, feature, editContext.getAdapterFactory());
		
		if (ModelGuiBuilderSupport.isIntegral(feature.getEType().getInstanceClass())) {
			toTarget.setConverter(new ObjectToStringConverter());
			toModel.setConverter(ValidationUtil.getConverterForIntegralType(feature.getEType().getInstanceClass()));
		}
		
		Binding binding = editContext.getDataBindingContext().bindValue(viewObj, modelObj, toModel, toTarget);
		
		bindingMap.put(feature, binding);
		
		return binding;
	}
	
	/**
	 * Creates a control, then binds it.
	 */
	public Text createAndBindText(EStructuralFeature feature) {
		Text text = createText(feature);
		bindText(text, feature);
		return text;
	}
	
	/**
	 * Binds a control to a feature, starting at {@link ModelGuiBuilderNEW#entity} and following featurePath.
	 */
	public Binding bindCombo(ComboViewer comboViewer, EStructuralFeature feature) {
		verifyFeature(feature);
		IObservableValue viewObj = ViewersObservables.observeSingleSelection(comboViewer);
		IObservableValue modelObj = EMFEditProperties.value(editContext.getEditingDomain(), feature).observeDetail(entity);
		
		Binding binding = editContext.getDataBindingContext().bindValue(viewObj, modelObj, 
			ValidationUtil.modelVerifyingStrategy(entity, feature, editContext.getAdapterFactory()), 
			ValidationUtil.modelVerifyingStrategy(entity, feature, editContext.getAdapterFactory()));
		
		bindingMap.put(feature, binding);
		
		return binding;
	}
	
	public ComboViewer createAndBindCombo(EStructuralFeature feature) {
		ComboViewer combo = createCombo(feature);
		bindCombo(combo, feature);
		return combo;
	}
	
	/**
	 * Creates a button which opens a dialog for selecting an entity. Creates a text to show the value
	 * of nameFeature in the selected object. (It does not create a label.)
	 * 
	 * @param feature The feature on the entity of this sheet that will be set. Also used as label name.
	 * @param nameProp A path, starting on the selected object, to the feature that will be showed in the GUI. 
	 * @param entityCreator A listener that creates the actual object.
	 */
	public void createAndBindSelectControls(
		EStructuralFeature feature, 
		IValueProperty nameProp, 
		CommandOperation entityCreator)
	{
		Text name = createSelectControls(feature, entityCreator);
		bindSelectNameText(feature, EMFProperties.value(feature).value(nameProp), name);
	}

	public void createAndBindSelectControls(
		EStructuralFeature feature, 
		IValueProperty nameProp, 
		final DialogCreator dialogCreator)
	{
		createAndBindSelectControls(feature, nameProp, new CommandOperation() {
			@Override
			public void run(EditContext cxt, IObservableValue containingEntity, EStructuralFeature containingFeature) {
				SelectionDialog dialog = dialogCreator.create();
				if (dialog.open() == Window.OK) { 
					editContext.getEditingDomain().getCommandStack().execute(
						SetCommand.create(editContext.getEditingDomain(), 
							containingEntity.getValue(), containingFeature, dialog.getResult()[0]));
				}
				
			}
		});
	}

	
	public Text createSelectControls(final EStructuralFeature feature, final CommandOperation selectCommand) {
		Composite valueButtonContainer = toolkit.createComposite(container, SWT.NONE);
		valueButtonContainer.setLayout(new GridLayout(2, false));
		((GridLayout) valueButtonContainer.getLayout()).marginWidth = 1;
		toolkit.paintBordersFor(valueButtonContainer);
		
		valueButtonContainer.setLayoutData(layoutDataFactory.create(valueButtonContainer, feature, false));
		
		Text selectedEntityName = createText(valueButtonContainer, feature);
		selectedEntityName.setEditable(false);
		
		if (isEditable) {
			Button selectButton = toolkit.createButton(valueButtonContainer, selectButtonLabel, SWT.NONE);
			selectButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					selectCommand.run(editContext, entity, feature);
				}
			});
		}
		
		return selectedEntityName;
	}

	/**
	 * Add a binding to the name which generates a warning for unresolved proxy objects.
	 */
	public Binding bindSelectNameText(EStructuralFeature feature, IValueProperty nameProp, Text selectedEntityName) {
		Binding binding =  editContext.getDataBindingContext().bindValue(
			WidgetProperties.text(SWT.Modify).observeDelayed(modelUpdateDelayMs, selectedEntityName),
			nameProp.observeDetail(entity), 
			ValidationUtil.modelVerifyingStrategy(entity, feature, editContext.getAdapterFactory()),
			ValidationUtil.modelVerifyingStrategy(entity, feature, editContext.getAdapterFactory()));
		
		bindingMap.put(feature, binding);
		
		return binding;
	}

	/**
	 * Creates and returns a control.
	 */
	public ComboViewer createCombo(final EStructuralFeature feature) {
		verifyFeature(feature);
		
		if (!(feature.getEType() instanceof EEnum)) {
			throw new IllegalArgumentException("Expexted and enum feature but got: " + feature);
		}
		
		ComboViewer comboViewer = new ComboViewer(container, SWT.READ_ONLY);
		Combo combo = comboViewer.getCombo();
		combo.setLayoutData(layoutDataFactory.create(combo, feature, false));
		toolkit.paintBordersFor(combo);
		
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object en) {
				return ModelGuiBuilderSupport.getEnumLiteralString(resourceLocator, (Enumerator) en);
			}
		});
		comboViewer.setInput(feature.getEType().getInstanceClass().getEnumConstants());
		
		return comboViewer;
	}
	
	private void verifyFeature(EStructuralFeature feature) {
		if (!feature.getContainerClass().isAssignableFrom(entityClass.getInstanceClass())) {
			throw new IllegalStateException("Expexted class " + entity.getValueType() + " but got feature of class " + feature.getContainerClass() + ". Feature: " + feature);
		}
	}
	
	public Label createLabel(EStructuralFeature feature) {
		String displayName = itemPropertySource.getPropertyDescriptor(null, feature).getDisplayName(null);
		Label label = toolkit.createLabel(container, displayName, SWT.NONE);
		label.setToolTipText(itemPropertySource.getPropertyDescriptor(null, feature).getDescription(null));
		return label;
	}

	public Hyperlink createEditorLink(EStructuralFeature nameFeature) {
		return createEditorLink(nameFeature, FeaturePath.fromList(nameFeature));
	}

	public Hyperlink createEditorLink(EStructuralFeature nameFeature, FeaturePath targetPath) {
		String displayName = itemPropertySource.getPropertyDescriptor(null, nameFeature).getDisplayName(null);
		Hyperlink link = toolkit.createHyperlink(container, displayName, SWT.NONE);
		link.setToolTipText(itemPropertySource.getPropertyDescriptor(null, nameFeature).getDescription(null));
		link.setUnderlined(true);
		IObservableValue targetValue = EMFProperties.value(targetPath).observeDetail(entity);
		link.setHref(targetValue);
		link.addHyperlinkListener(ModelGuiBuilderSupport.openEditorListener());
		
		return link;
	}
	
	/**
	 * Like the {@link #createSelectControls} method, but the text field is editable. Hence the feature
	 * must be a string.
	 */
	public void createTextSelectControls(EAttribute feature, CommandOperation selectCommand) {
		createLabel(feature);
		Text name = createSelectControls(feature, selectCommand);
		name.setEditable(isEditable);
		
		IValueProperty valueProp = EMFEditProperties.value(editContext.getEditingDomain(), feature);
		
		bindSelectNameText(feature, valueProp, name);
	}

	/**
	 * Createas update listeners for the bindings of the argument features, which triggers validation
	 * of all of them if any value is changed.
	 */
	public void addCrossValidationUpdates(EStructuralFeature... features) {
		final List<Binding> bindings = new ArrayList<>(features.length);
		for (EStructuralFeature f : features) {
			bindings.add(bindingMap.get(f));
		}
		
		for (final Binding binding : bindings) {
			binding.getModel().addChangeListener(new IChangeListener() {
				@Override
				public void handleChange(ChangeEvent event) {
					for (Binding bind : bindings) {
						if (bind != binding) {
							// This triggers validation, but does not result in any commands 
							// in the command stack (as targetToModel would)
							bind.updateModelToTarget();
						}
					}
				}
			});
		}
	}

	
	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

	public void setModelUpdateDelayMs(int modelUpdateDelayMs) {
		this.modelUpdateDelayMs = modelUpdateDelayMs;
	}
	
	public void setSelectButtonLabel(String selectButtonLabel) {
		this.selectButtonLabel = selectButtonLabel;
	}

	/**
	 * Creatates a builder with the the values as this one. As the entity in the result builder it 
	 * uses the value of the argument feature.
	 */
	public ModelGuiBuilder childBuilder(Composite newContainer, EStructuralFeature feature) {
		return new ModelGuiBuilder(toolkit, editContext, newContainer, (EClass) feature.getEType(), 
			EMFProperties.value(feature).observeDetail(entity));
	}
	
	/**
	 * Creatates a builder with all the same values, except the argument composite control.
	 */
	public ModelGuiBuilder childBuilder(Composite newContainer) {
		return new ModelGuiBuilder(toolkit, editContext, newContainer, entityClass, entity);
	}

	public void setLayoutDataFactory(LayoutDataFactory layoutDataFactory) {
		this.layoutDataFactory = layoutDataFactory;
	}

	private static class GridDataFactory implements LayoutDataFactory {
		@Override
		public Object create(Control control, EStructuralFeature feature, boolean isMultiLine) {
			if (control instanceof Button && (control.getStyle() & SWT.CHECK) != 0) {
				return new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			} 
			
			if (control instanceof Text && isMultiLine) {
				// Multi-line texts should be 3 rows high, and must have a limiting
				// width hint to not swell and become one huge row
				return org.eclipse.jface.layout.GridDataFactory.swtDefaults()
					.align(SWT.FILL, SWT.CENTER)
					.grab(false, false)
					.hint(100, ((Text) control).getLineHeight() * 3).create();
			}
			
			if (control instanceof Label) {
				// Lables for multi-line texts must be align-top, other labels don't need layout data 
				return isMultiLine ? new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1) : null;
			} 
			
			return new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		}
	}
}

