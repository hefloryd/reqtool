package com.rtlabs.common.databinding;

import java.util.Map;
import java.util.function.Function;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;


/**
 * Common code for validation.
 */
public class ValidationUtil {
	
	/**
	 * Check recursively if children of diagnostic contains soughtFeature. Returns the first such child.
	 */
	static Diagnostic containsFeature(Diagnostic diagnostic, EStructuralFeature soughtFeature) {
		if (diagnostic.getData() != null && diagnostic.getData().size() >= 2 && soughtFeature.equals(diagnostic.getData().get(1))) {
			return diagnostic;
		}
		
		for (Diagnostic child : diagnostic.getChildren()) {
			Diagnostic result = containsFeature(child, soughtFeature);
			if (result != null) return result;
		}
		
		return null;
	}

	/**
	 * @return An IConverter from String to the type of targetType. The
	 *         converter treats the empty string as 0.
	 */
	public static IConverter getConverterForIntegralType(Class<?> targetType) {
		return INTEGRAL_CONVERTERS.get(Primitives.wrap(targetType));
	}

	/**
	 * Returns the main KF update strategy. It validates using a EMF validator, AFTER values have been
	 * set in the model.
	 * @param entity The object on which the validator is run. This should contain the validator
	 * 		which is relevant for the validated binding.
	 * @param featureToValidate The feature to validate
	 */
	public static UpdateValueStrategy modelVerifyingStrategy(IObservableValue<? extends EObject> entity, EStructuralFeature featureToValidate) {
		AfterSetValidationUpdateValueStrategy strat = new AfterSetValidationUpdateValueStrategy();
		strat.setAfterSetValidator(new DatabindingToEmfValidator<>(entity, featureToValidate));
		return strat;
	}

	/**
	 * Values that are stored as non-strings and edited as strings need this converter;
	 */
	public static UpdateValueStrategy objectToStringStrategy(IObservableValue<? extends EObject> entity, EStructuralFeature featureToValidate) {
		UpdateValueStrategy s = modelVerifyingStrategy(entity, featureToValidate);
		s.setConverter(new ObjectToStringConverter());
		return s;
	}

	public static UpdateValueStrategy stringStrategy(IObservableValue<? extends EObject> entity, EStructuralFeature featureToValidate) {
		UpdateValueStrategy s = modelVerifyingStrategy(entity, featureToValidate);
		s.setConverter(new IdentityConverter(String.class, String.class));
		return s;
	}

	private static Map<Class<?>, IConverter> INTEGRAL_CONVERTERS = ImmutableMap.<Class<?>, IConverter>of(
		Byte.class,    new IntegralConverter<>(Byte.class,   (byte) 0,  Byte::valueOf),
		Short.class,   new IntegralConverter<>(Short.class,  (short) 0, Short::valueOf),
		Integer.class, new IntegralConverter<>(Integer.class, 0,        Integer::valueOf),
		Long.class,    new IntegralConverter<>(Long.class,    0L,       Long::valueOf));

	/**
	 * Base for converters from string to integral types.
	 */
	private static class IntegralConverter<T> extends Converter {
		private final Object zero;
		private final Function<String, T> func;

		public IntegralConverter(Object toType, Object zero, Function<String, T> func) {
			super(String.class, toType);
			this.zero = zero;
			this.func = func;
		}

		@Override
		public final Object convert(Object fromObject) {
			if (fromObject == null) return null;
			if (((String) fromObject).isEmpty()) return zero;
			
			return func.apply((String) fromObject);
		}
	}
	

	public static class IdentityConverter extends Converter {
		public IdentityConverter(Object fromType, Object toType) {
			super(fromType, toType);
		}
		@Override
		public Object convert(Object fromObject) {
			return fromObject;
		}
	}

	
	public static class ObjectToStringConverter extends Converter {
		public ObjectToStringConverter() {
			super(Object.class, String.class);
		}

		@Override
		public Object convert(Object fromObject) {
			return fromObject == null ? null : fromObject.toString();
		}
	}
}


