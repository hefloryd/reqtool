package com.rtlabs.common.model_gui_builder;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditValueProperty;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
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
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.rtlabs.common.databinding.ValidationUtil;
import com.rtlabs.common.databinding.ValidationUtil.ObjectToStringConverter;
import com.rtlabs.common.edit_support.CommandOperation;
import com.rtlabs.common.edit_support.EditContext;

/**
 * Helps with the creation and data binding of GUI controls from information in the model.
 * 
 * The most important method is {@link ModelGuiBuilder#createFeatureControl}. It automatically creates a suitable control
 * depending on the type of a feature.
 * 
 * This class uses a {@link IItemPropertySource} to lookup names of features from generated property files in the 
 * EMF Edit projects.
 */
public class ModelGuiBuilder<T extends EObject> {

	
	private final EditContext editContext;
	private final IObservableValue<T> entity;
	private final FormToolkit toolkit;
	private final IItemPropertySource itemPropertySource;
	
	private boolean isEditable = true;
	private int modelUpdateDelayMs = 500;
	private int widthHint = 150;
	private String selectButtonLabel = "Select...";

	@SuppressWarnings("unchecked")
	public ModelGuiBuilder(FormToolkit toolkit, EditContext editContext, EObject entity) {
		this(toolkit, editContext, 
			(IObservableValue<T>) Observables.<Object>constantObservableValue(entity, entity.eClass()));
	}
	
	public ModelGuiBuilder(FormToolkit toolkit, EditContext editContext, IObservableValue<T> entity) {
		this.editContext = editContext;
		this.entity = entity;
		this.toolkit = toolkit;
		
		EClass entityClass = (EClass) ModelGuiBuilderSupport.getType(entity);
		
		itemPropertySource = Objects.requireNonNull(editContext.getItemPropertySource(
				entityClass.getEPackage().getEFactoryInstance().create(entityClass)),
			"An IItemPropertySource could not be created for " + entityClass);
	}
	
	/**
	 * Creates and data binds controls for all features, with container as parent.
	 */
	public void createFeatureControls(Composite container, EStructuralFeature... features) {
		createFeatureControls(container, Arrays.asList(features));
	}
	
	/**
	 * Creates and data binds controls for all features, with container as parent.
	 */
	public void createFeatureControls(Composite container, List<? extends EStructuralFeature> features) {
		for (EStructuralFeature feature : features) {
			if (// TODO: feature.getEAnnotation(IdGeneratorListener.ANNOTATION_ID) != null || 
				feature.getEAnnotation("com.kvaser.kingdomfounder.hide_in_gui") != null
				|| feature.isDerived()
				) {
				continue;
			}
			
			// Only include data types, no object types
			if (feature.getEType() instanceof EDataType) {
				createFeatureControl(container, feature);
			}
		}
	}

	/**
	 * Creates a label and a control. The specific type of control depends on the type of the feature. The feature
	 * must belong to the entity which was set earlier.
	 */
	public Control createFeatureControl(Composite container, EStructuralFeature feature) {
		Label label = createLabel(container, feature);

		if (feature.getEType().equals(EcorePackage.Literals.ESTRING) || ModelGuiBuilderSupport.isIntegral(feature.getEType().getInstanceClass())) {
			Text text = createAndBindText(container, feature);
			// This is for multi-line texts, it works fine for normal texts also but not all controls 
			label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			return text;
		} else if (feature.getEType() instanceof EEnum) {
			return createAndBindCombo(container, feature).getCombo();
		} else if (feature.getEType().equals(EcorePackage.Literals.EBOOLEAN)) {
			return createAndBindCheckbox(container, feature);
		} else {
			throw new IllegalStateException("Unsupported feature: " + feature);
		}
	}
	
	private Control createAndBindCheckbox(Composite container, EStructuralFeature feature) {
		Button button = createCheckbox(container, feature);
		bindCheckbox(button, feature);
		return button;
	}

	public Button createCheckbox(Composite container, EStructuralFeature feature) {
		Button button = toolkit.createButton(container, "", SWT.CHECK);
		button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		return button;
	}

	public void bindCheckbox(Button button, EStructuralFeature feature) {
		verifyFeature(feature);
		IObservableValue<?> viewObj = WidgetProperties.selection().observeDelayed(modelUpdateDelayMs, button);
		IEMFEditValueProperty p = EMFEditProperties.value(editContext.getEditingDomain(), feature);
		@SuppressWarnings("unchecked")
		IObservableValue<?> modelObj = p.observeDetail(entity);
		
		editContext.getDataBindingContext().bindValue(viewObj, modelObj, 
			ValidationUtil.modelVerifyingStrategy(entity, feature), 
			ValidationUtil.modelVerifyingStrategy(entity, feature));
	}

	private GridData createGridData() {
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1); 
		if (widthHint != -1) {
			data.widthHint = widthHint;
		}
		return data;
	}
	
	/**
	 * Creates and returns a control.
	 */
	public Text createText(Composite container, EStructuralFeature feature) {
		verifyFeature(feature);
		
		boolean isMultiLine = itemPropertySource.getPropertyDescriptor(null, feature).isMultiLine(null);

		Text text = toolkit.createText(container, "", isMultiLine ? SWT.MULTI | SWT.WRAP | SWT.V_SCROLL : SWT.NONE);
		text.setLayoutData(createGridData());
		
		if (isMultiLine) {
			((GridData) text.getLayoutData()).heightHint = text.getLineHeight() * 3;
			// Ensure tab cursor move works in multi-line texts. User has to paste to get tab characters.
			text.addTraverseListener(ModelGuiBuilderSupport.TRAVERSE_LISTENER);
		}
		
		if (ModelGuiBuilderSupport.isIntegral(feature.getEType().getInstanceClass())) {
			text.addVerifyListener(ModelGuiBuilderSupport.getIntegralProtectionVerifyListener(feature.getEType().getInstanceClass()));
		}

		return text;
	}


	/**
	 * Binds a control to a feature, starting at {@link ModelGuiBuilder#entity} and following featurePath.
	 */
	public void bindText(Text text, EStructuralFeature feature) {
		verifyFeature(feature);
		@SuppressWarnings("unchecked")
		IObservableValue<String> viewObj = WidgetProperties.text(SWT.Modify).observeDelayed(modelUpdateDelayMs, text);
		@SuppressWarnings("unchecked")
		IObservableValue<String> modelObj = EMFEditProperties.value(editContext.getEditingDomain(), feature).observeDetail(entity);
		
		UpdateValueStrategy toModel = ValidationUtil.modelVerifyingStrategy(entity, feature);
		UpdateValueStrategy toTarget = ValidationUtil.modelVerifyingStrategy(entity, feature);
		
		if (ModelGuiBuilderSupport.isIntegral(feature.getEType().getInstanceClass())) {
			toTarget.setConverter(new ObjectToStringConverter());
			toModel.setConverter(ValidationUtil.getConverterForIntegralType(feature.getEType().getInstanceClass()));
		}
		
		editContext.getDataBindingContext().bindValue(viewObj, modelObj, toModel, toTarget);
	}
	
	/**
	 * Creates a control, then binds it.
	 */
	public Text createAndBindText(Composite container, EStructuralFeature feature) {
		Text text = createText(container, feature);
		bindText(text, feature);
		return text;
	}
	
	/**
	 * Binds a control to a feature, starting at {@link ModelGuiBuilder#entity} and following featurePath.
	 */
	public void bindCombo(ComboViewer comboViewer, EStructuralFeature feature) {
		verifyFeature(feature);
		IObservableValue<?> viewObj = ViewersObservables.observeSingleSelection(comboViewer);
		@SuppressWarnings("unchecked")
		IObservableValue<?> modelObj = EMFEditProperties.value(editContext.getEditingDomain(), feature).observeDetail(entity);
		
		editContext.getDataBindingContext().bindValue(viewObj, modelObj, 
				ValidationUtil.modelVerifyingStrategy(entity, feature), 
				ValidationUtil.modelVerifyingStrategy(entity, feature));
	}
	
	public ComboViewer createAndBindCombo(Composite container, EStructuralFeature feature) {
		ComboViewer combo = createCombo(container, feature);
		bindCombo(combo, feature);
		return combo;
	}

//	public void createAndBindSelectEntityControls(final Composite container, final EStructuralFeature feature, FeaturePath namePath, final DialogCreator dialogCreator) {
//		createAndBindSelectControls(container, feature, 
//			HandleUnresolvedProperty.value(UnresolvedAction.URL, namePath) , 
//			CommandOperations.setDialog(dialogCreator));
//	}
//	
//	public void createAndBindDialogSelectControls(final Composite container, final EStructuralFeature feature, IValueProperty<?, String> nameProp, final DialogCreator dialogCreator) {
//		createAndBindSelectControls(container, feature, nameProp, CommandOperations.setDialog(feature, dialogCreator));
//	}
	
	/**
	 * Creates a button which opens a dialog for selecting an entity. Creates a text to show the value
	 * of nameFeature in the selected object. (It does not create a label.)
	 * 
	 * @param feature The feature on the entity of this sheet that will be set. Also used as label name.
	 * @param namePath A path, starting on the selected object, to the feature that will be showed in the GUI. 
	 * @param entityCreator A listener that creates the actual object.
	 */
	public void createAndBindSelectControls(
		Composite container, 
		EStructuralFeature feature, 
		IValueProperty<T, String> nameProp, 
		CommandOperation<T> entityCreator)
	{
		Text name = createSelectControls(container, feature, entityCreator);
		bindSelectNameText(feature, nameProp, name);
	}

	public Text createSelectControls(Composite container, final EStructuralFeature feature, 
			final CommandOperation<T> selectCommand) {
		Composite valueButtonContainer = toolkit.createComposite(container, SWT.NONE);
		valueButtonContainer.setLayout(new GridLayout(2, false));
		((GridLayout) valueButtonContainer.getLayout()).marginWidth = 1;
		toolkit.paintBordersFor(valueButtonContainer);
		
		valueButtonContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
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
	public Binding bindSelectNameText(EStructuralFeature feature, IValueProperty<T, String> nameProp, Text selectedEntityName) {
		return editContext.getDataBindingContext().bindValue(
			WidgetProperties.text(SWT.Modify).observeDelayed(modelUpdateDelayMs, selectedEntityName),
			nameProp.observeDetail(entity), 
			ValidationUtil.modelVerifyingStrategy(entity, feature),
			ValidationUtil.modelVerifyingStrategy(entity, feature));
	}

	/**
	 * Creates and returns a control.
	 */
	public ComboViewer createCombo(Composite container, EStructuralFeature feature) {
		verifyFeature(feature);
		
		if (!(feature.getEType() instanceof EEnum)) {
			throw new IllegalArgumentException("Expexted and enum feature but got: " + feature);
		}
		
		ComboViewer comboViewer = new ComboViewer(container, SWT.READ_ONLY);
		Combo combo = comboViewer.getCombo();
		combo.setLayoutData(createGridData());
		toolkit.paintBordersFor(combo);
		
		comboViewer.setContentProvider(new ArrayContentProvider());
		
		comboViewer.setInput(((EEnum) feature.getEType()).getELiterals().stream()
			.map(EEnumLiteral::getLiteral).toArray());
		
		return comboViewer;
	}
	
	private void verifyFeature(EStructuralFeature feature) {
		if (!feature.getContainerClass().isAssignableFrom(ModelGuiBuilderSupport.getType(entity).getInstanceClass())) {
			throw new IllegalStateException("Expexted class " + entity.getValueType() + " but got feature of class " + feature.getContainerClass() + ". Feature: " + feature);
		}
	}
	
	public Label createLabel(Composite container, EStructuralFeature feature) {
		String displayName = itemPropertySource.getPropertyDescriptor(null, feature).getDisplayName(null);
		Label label = toolkit.createLabel(container, displayName, SWT.NONE);
		label.setToolTipText(itemPropertySource.getPropertyDescriptor(null, feature).getDescription(null));
		return label;
	}

	public Hyperlink createEditorLink(Composite container, EStructuralFeature nameFeature) {
		return createEditorLink(container, nameFeature, FeaturePath.fromList(nameFeature));
	}

	public Hyperlink createEditorLink(Composite container, EStructuralFeature nameFeature, FeaturePath targetPath) {
		String displayName = itemPropertySource.getPropertyDescriptor(null, nameFeature).getDisplayName(null);
		Hyperlink link = toolkit.createHyperlink(container, displayName, SWT.NONE);
		link.setToolTipText(itemPropertySource.getPropertyDescriptor(null, nameFeature).getDescription(null));
		link.setUnderlined(true);
		@SuppressWarnings("unchecked")
		IObservableValue<T> targetValue = EMFProperties.value(targetPath).observeDetail(entity);
		link.setHref(targetValue);
		link.addHyperlinkListener(ModelGuiBuilderSupport.openEditorListener());
		
		return link;
	}
	
	/**
	 * Like the {@link #createSelectControls} method, but the text field is editable. Hence the feature
	 * must be a string.
	 */
	public void createTextSelectControls(Composite container, EAttribute feature, CommandOperation<T> selectCommand) {
		createLabel(container, feature);
		Text name = createSelectControls(container, feature, selectCommand);
		name.setEditable(true);
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		@SuppressWarnings("unchecked")
		IValueProperty<T, String> valueProp = EMFEditProperties.value(editContext.getEditingDomain(), feature);
		
		bindSelectNameText(feature, valueProp, name);
	}

		
	public void setWidthHint(int widthHint) {
		this.widthHint = widthHint;
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

	public <NEW_T extends EObject> ModelGuiBuilder<NEW_T> childBuilder(EStructuralFeature feature) {
		@SuppressWarnings("unchecked")
		IObservableValue<NEW_T> newEntity = EMFProperties.value(feature).observeDetail(entity);
		return new ModelGuiBuilder<NEW_T>(toolkit, editContext, newEntity);
	}
}
	
