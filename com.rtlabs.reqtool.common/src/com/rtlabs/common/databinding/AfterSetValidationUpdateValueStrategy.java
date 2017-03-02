package com.rtlabs.common.databinding;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

import com.rtlabs.common.Activator;

/**
 * An {@link UpdateValueStrategy} that can perform validation AFTER a value is set
 * in the model. This is used because undo dosen't work if no model change in made.
 */
public class AfterSetValidationUpdateValueStrategy extends UpdateValueStrategy {

	private IValidator afterSetValidator;
	
	public void setAfterSetValidator(IValidator afterSetValidator) {
		this.afterSetValidator = afterSetValidator;
	}

	@Override
	protected IStatus doSet(@SuppressWarnings("rawtypes") IObservableValue observableValue, Object value) {
		IStatus setStatus = super.doSet(observableValue, value);
		
		if (setStatus.getSeverity() >= IStatus.ERROR || afterSetValidator == null) {
			return setStatus;
		}
		
		IStatus validStatus = afterSetValidator.validate(value); 

		return mergeStatuses(setStatus, validStatus);
	}
	
	/**
	 * Merge the two statuses. Keep errors, throw away OK:s.
	 */
	private static IStatus mergeStatuses(IStatus s1, IStatus s2) {
		if (s1.isOK() && s2.isOK()) {
			return s2;
		} else if (!s1.isOK() && s2.isOK()) {
			return s1;
		} else if (s1.isOK() && !s2.isOK()) {
			return s2;
		//		} else if (s1.isMultiStatus()) {
		//			((MultiStatus) s1).merge(s2);
		//			return s1;
		//		} else if (s2.isMultiStatus()) {
		//			((MultiStatus) s2).merge(s1);
		//			return s2;
		} else {
			return new MultiStatus(Activator.PLUGIN_ID, -1, 
				new IStatus[] { s1, s2 },
				s1.getMessage() + "; " + s2.getMessage(), null);
		}
	}

}
