package com.rtlabs.common.databinding;

import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.databinding.property.value.ValueProperty;

public class IdentityProperty<T> extends ValueProperty<T, T> {
	private final Object valueType;
	
	private IdentityProperty(Object valueType) {
		this.valueType = valueType;
	}
	
	@Override
	public Object getValueType() {
		return valueType;
	}
	
	@Override
	protected T doGetValue(T source) {
		return source;
	}

	@Override
	protected void doSetValue(T source, T value) {
		throw new UnsupportedOperationException();
	}


	@SuppressWarnings("unchecked")
	@Override
	public IObservableValue<T> observe(Realm realm, T source) {
		return Observables.constantObservableValue(source, (T) valueType);
	}

	public static <T> IValueProperty<T, T> value(Object valueType) {
		return new IdentityProperty<>(valueType);
	}
}
