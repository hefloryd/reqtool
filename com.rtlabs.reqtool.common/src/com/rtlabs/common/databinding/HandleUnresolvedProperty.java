package com.rtlabs.common.databinding;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.DecoratingObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.databinding.property.value.ValueProperty;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * A property which returns a message instead of a value when the target is an {@link EObject#eIsProxy()}.
 */
public class HandleUnresolvedProperty<S extends EObject, T> extends ValueProperty<S, T> {

	public enum UnresolvedAction {
		EMPTY, URL, URL_MESSAGE, MESSAGE
	}
	
	/**
	 * Returns a string property. If any of the objects along valuePath is an
	 * unresolved proxy, the property has the value of the URI of the target
	 * resource. If there are no unresolved proxies along the path it has the
	 * real value.
	 * 
	 * @param unresolvedAction
	 *            Determines which kind of value that is used in place of an
	 *            unresolved object.
	 */
	public static <S extends EObject, T> IValueProperty<S, T> value(UnresolvedAction unresolvedAction, FeaturePath featurePath) {
		IValueProperty<?, ?> resultProperty = null;
		
		for (int i = 0; i < featurePath.getFeaturePath().length; i++) {

			// All but the last feature will be to an EObject. They support only UnresolvedAction.EMPTY.
			// The last property will be to a string and supports other UnresolvedActions.
			boolean isLast = i == featurePath.getFeaturePath().length - 1;
			UnresolvedAction action = isLast ? unresolvedAction : UnresolvedAction.EMPTY;

			@SuppressWarnings("unchecked")
			IValueProperty<?, ?> singleProperty = new HandleUnresolvedProperty<>(action, 
				EMFProperties.value(featurePath.getFeaturePath()[i]));
			
			resultProperty = resultProperty == null ? singleProperty : valueFixArgs(resultProperty, singleProperty);
		}

		@SuppressWarnings("unchecked")
		IValueProperty<S, T> result = (IValueProperty<S, T>) resultProperty;
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <M, S, T> IValueProperty<M, T> valueFixArgs(IValueProperty<M, S> m, IValueProperty<?, T> p) {
		return (IValueProperty<M, T>) m.value((IValueProperty<? super S, M>) p);
	}
	
	public static <S extends EObject, T> IValueProperty<S, T> wrap(UnresolvedAction unresolvedAction, IValueProperty<S, T> toWrap) {
		return new HandleUnresolvedProperty<>(unresolvedAction, toWrap);
	}
	
	public static <S extends EObject, T> IValueProperty<S, T> value(UnresolvedAction unresolvedAction, EStructuralFeature feature) {
		return value(unresolvedAction, FeaturePath.fromList(feature));
	}

	
	private final IValueProperty<S, T> delegate;
	private final UnresolvedAction unresolvedAction;

	private HandleUnresolvedProperty(UnresolvedAction unresolvedAction, IValueProperty<S, T> delegateProperty) {
		this.unresolvedAction = unresolvedAction;
		this.delegate = delegateProperty;
	}
	
	@Override
	public IObservableValue<T> observe(Realm realm, final S source) {
		return new DecoratingObservableValue<T>(delegate.observe(realm, source), true) {
			@Override
			public T getValue() {
				if (source == null) {
					return null;
				} else if (source.eIsProxy()) {
					return getResponceValue(source);
				} else {
					return super.getValue();
				}
			}

			@Override
			public void setValue(T value) {
				if (!source.eIsProxy()) {
					super.setValue(value);
				}
			}
		};
	}
	
	
	@Override
	protected T doGetValue(S source) {
		if (source.eIsProxy()) {
			return getResponceValue(source);
		} else {
			return delegate.getValue(source);
		}
	}

	@Override
	protected void doSetValue(S source, T value) {
		if (!source.eIsProxy()) {
			delegate.setValue(source, value);
		}
	}

	@SuppressWarnings("unchecked")
	private T getResponceValue(final S source) {
		switch (unresolvedAction) {
			case EMPTY: return null;
			
			// These cases only makes sence when the feature type is String.
			// This is checked in the value(...) method.
			case URL: return (T) EcoreUtil.getURI(source).toString();
			case URL_MESSAGE: return (T) ("File not found: " + EcoreUtil.getURI(source));
			case MESSAGE: return (T) "File not found";
		}

		throw new IllegalArgumentException("Unexpected enum constant: " + unresolvedAction);
	}

	
	@Override
	public Object getValueType() {
		return delegate.getValueType();
	}

	@Override
	public String toString() {
		return HandleUnresolvedProperty.class.getSimpleName() + "-" + delegate.toString();
	}
}
