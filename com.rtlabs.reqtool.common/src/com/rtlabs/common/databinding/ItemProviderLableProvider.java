package com.rtlabs.common.databinding;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EValidator.SubstitutionLabelProvider;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertySource;

/**
 * A validation label provider which looks up object and feature display text with an adapter factory.
 * 
 * This can be set in a validation context map and used to format messages nicely. The {@link Diagnostician} for
 * example uses classes like this.
 */
public class ItemProviderLableProvider implements SubstitutionLabelProvider {
	
	private final AdapterFactory adaperFactory;

	public ItemProviderLableProvider(AdapterFactory adaperFactory) {
		this.adaperFactory = adaperFactory;
	}

	@Override
	public String getValueLabel(EDataType dataType, Object value) {
		return EcoreUtil.convertToString(dataType, value);
	}

	@Override
	public String getObjectLabel(EObject labeledObj) {
		if (labeledObj == null) return "Unknown Object";
		
		IItemLabelProvider labelProvider = (IItemLabelProvider) adaperFactory.adapt(labeledObj, IItemLabelProvider.class);
		if (labelProvider == null) return EcoreUtil.getIdentification(labeledObj);
		String name = labelProvider.getText(labeledObj);
		return name;
	}

	@Override
	public String getFeatureLabel(EStructuralFeature feature) {
		if (feature == null) return "Unknown Feature";
		
		EClass eContainingClass = feature.getEContainingClass();
		
		if (eContainingClass == null) return feature.getName();

		EObject dummyObj = eContainingClass.getEPackage().getEFactoryInstance().create(eContainingClass);
		
		IItemPropertySource propertySource = (IItemPropertySource) adaperFactory.adapt(
			dummyObj, IItemPropertySource.class);
		
		if (propertySource == null) return feature.getName();
		
		return propertySource.getPropertyDescriptor(dummyObj, feature).getDisplayName(null);
	}
}
