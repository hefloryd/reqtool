package com.rtlabs.common.model_gui_builder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IFile;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.statushandlers.StatusManager;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Primitives;
import com.rtlabs.common.util.RtUtil;

/**
 * Support code for {@link ModelGuiBuilder}.
 */
class ModelGuiBuilderSupport {
	/**
	 * Returns a listener which blocks input that is not a digit and is outside the range.  
	 */
	private static VerifyListener integralVerifyingListener(final int nrSafeDigits, final long min, final long max) {
		return new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				for (int i = 0; i < e.text.length(); i++) {
					if (!Character.isDigit(e.text.charAt(i))) {
						e.doit = false;
						return;
					}
				}
				
				String fullText = ((Text) e.widget).getText();
				// Compute the text after this change
				fullText = fullText.substring(0, e.start) + e.text + fullText.substring(e.end);
				
				if (nrSafeDigits == -1) return;
				if (fullText.length() <= nrSafeDigits) return;
				
				Long l = Longs.tryParse(fullText);
				
				if (l == null) {
					e.doit = false;
					return;
				}
				
				if (!(min <= l && l <= max)) {
					e.doit = false;
				}
			}
		};
	}

	/**
	 * @return A listener which opens an editor on the resource of an EObject.
	 *         The href of the link must contain an observable value that
	 *         returns the EObject.
	 */
	public static HyperlinkAdapter openEditorListener() {
		return new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent event) {
				IObservableValue<?> obs = (IObservableValue<?>) event.getHref();
				if (obs.getValue() == null) return;
				
				try {
					IFile entityFile = RtUtil.toEclipseFile(((EObject) obs.getValue()).eResource());
					if (entityFile == null || !entityFile.exists()) {
						return;
					}
					
					IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), entityFile);
				} catch (PartInitException e) {
					StatusManager.getManager().handle(ValidationStatus.error( 
						"An error occured when opening to editor of " + ((EObject) obs.getValue()).eResource(), e), 
						StatusManager.BLOCK);
				}
			}
		};
	}

	private static final Class<?>[] INTEGRAL_CLASSES = {
		Byte.class,
		Short.class,
		Integer.class,
		Long.class,
	};
	
	private static Map<Class<?>, VerifyListener> INTEGRAL_PROTECTION_VERIFY_LISTENERS = ImmutableMap.<Class<?>, VerifyListener>builder()
		.put(Byte.class,       integralVerifyingListener(2,  Byte.MIN_VALUE,    Byte.MAX_VALUE   ))
		.put(Short.class,      integralVerifyingListener(4,  Short.MIN_VALUE,   Short.MAX_VALUE  ))
		.put(Integer.class,    integralVerifyingListener(9,  Integer.MIN_VALUE, Integer.MAX_VALUE))
		.put(Long.class,       integralVerifyingListener(18, Long.MIN_VALUE,    Long.MAX_VALUE   ))
		.put(BigInteger.class, integralVerifyingListener(-1, 0, 0                                ))
		.put(BigDecimal.class, integralVerifyingListener(-1, 0, 0                                ))
		.build();
	
	public static final TraverseListener TRAVERSE_LISTENER = (TraverseEvent e) -> {
			if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
				e.doit = true;
			}
		};
	
	/**
	 * Returns a VerifyListener that block input which can not be parsed to the type of
	 * targetType. It blocks illegal characters and numbers that are to large.
	 */
	public static VerifyListener getIntegralProtectionVerifyListener(Class<?> targetType) {
		return INTEGRAL_PROTECTION_VERIFY_LISTENERS.get(Primitives.wrap(targetType));
	}
	
	/**
	 * Returns whether cls is of an integral type, that is byte, short, int or long.
	 */
	public static boolean isIntegral(Class<?> cls) {
		Class<?> boxed = Primitives.wrap(cls);
		
		for (Class<?> c : INTEGRAL_CLASSES) {
			if (c.equals(boxed)) return true;
		}
		return false;
	}
	
	// This could be used to pass objects back to clients:
	
	public static class ControlResult {
		private Control control;
		private Composite container;
		private Binding binding;
		
//			public SheetControls(Control control, Composite container) {
//				this.control = control;
//				this.container = container;
//			}

		public Control getControl() {
			return control;
		}

		public Composite getContainer() {
			return container;
		}

		public Binding getBinding() {
			return binding;
		}
	}

	@SuppressWarnings("unused")
	private FeaturePath addFeatureFront(EStructuralFeature feature, FeaturePath namePath) {
		List<EStructuralFeature> nameList = new ArrayList<>(namePath.getFeaturePath().length + 1);
		nameList.add(feature);
		nameList.addAll(Arrays.asList(namePath.getFeaturePath()));
		return FeaturePath.fromList(nameList.toArray(new EStructuralFeature[0]));
	}

	
	/**
	 * Returns the most appropriate {@link EClassifier} of an observable value, depending on whether
	 * the valueType contains an EClassifier or an ETypedElement. 
	 */
	public static EClassifier getType(IObservableValue<?> o) {
		Object type = o.getValueType();
		
		if (type == null) {
			throw new IllegalArgumentException("Make sure to set the type of the observable to "
				+ "an EClassifier or an ETypedElemet. Obsesrvable: " + o);
		}
		
		if (type instanceof ETypedElement) {
			return ((ETypedElement) type).getEType();
		} else if (type instanceof EClassifier) {
			return (EClassifier) type;
		} else {
			return null;
		}
	}
}
