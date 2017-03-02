package com.rtlabs.common.databinding;

import java.util.LinkedHashMap;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EValidator;

/**
 * A validator which calls the EMF validation system and looks for the result
 * for an object and feature.
 * 
 * This is the main validator which is set on most bindings in the application.
 * It bridges data binding validation and EMF validation.
 */
public final class DatabindingToEmfValidator<T extends EObject> implements IValidator {
	private final IObservableValue<T> objectToValidate;
	private final EStructuralFeature featureToValidate;

	public DatabindingToEmfValidator(IObservableValue<T> objectToValidate, EStructuralFeature feature) {
		this.objectToValidate = objectToValidate;
		this.featureToValidate = feature;
	}

	@Override
	public IStatus validate(Object newValue) {
		if (objectToValidate.getValue() == null) {
			// It's the job of the container to validate if null is allowed, so accept it here 
			return Status.OK_STATUS;
		} 

//		Diagnostic chain = Diagnostician.INSTANCE.validate((EObject) objectToValidate.getValue());
		EValidator validator = EValidator.Registry.INSTANCE.getEValidator(featureToValidate.getEContainingClass().getEPackage());
			
		if (validator == null) {
			// No validator registered for this entity, nothing to validate
			return Status.OK_STATUS;
		}
			
		BasicDiagnostic chain = new BasicDiagnostic();
		
		// Call EMF validation
		validator.validate(objectToValidate.getValue(), chain, new LinkedHashMap<>());

		// Check if any error is from the validated feature and return that. If all results are return 
		// messages get duplicated in the MessageManager when there are multiple errors.
		// TODO: What if there are multiple errors for this feature?
		Diagnostic specificResult = ValidationUtil.containsFeature(chain, featureToValidate);
		return specificResult == null ?  Status.OK_STATUS : BasicDiagnostic.toIStatus(specificResult);
	}
}
