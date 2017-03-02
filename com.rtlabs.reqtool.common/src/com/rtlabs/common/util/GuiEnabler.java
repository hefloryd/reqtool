package com.rtlabs.common.util;

import java.util.Set;

import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.google.common.collect.ImmutableSet;
import com.rtlabs.common.Activator;

/**
 * Contains methods for disabling and enabling parts of the GUI.
 */
public class GuiEnabler {
	/**
	 * Used to set the enable state of a tree of controls.
	 */
	public enum EnableState {
		/**
		 * The control is disabled, for when there is no information to show in
		 * it. All controls, including labels, are disabled.
		 */
		DISABLED,
		/**
		 * For when there is information to show in the control, but it should
		 * be read-only. All controls are disabled, except Text which is
		 * non-editable, and Labels which are enabled.
		 */
		READ_ONLY,
		/**
		 * All controls are enabled and editable.
		 */
		EDITABLE
	}
	
	private static final String DISABLED_KEY = Activator.PLUGIN_ID + ".disabled_key";
	private static final String READ_ONLY_KEY = Activator.PLUGIN_ID + ".read_only_key";

	
	public static void addEnablementListener(final Composite container, boolean editable, IObservableValue<Boolean> mainValue, Control... excluded) {
		@SuppressWarnings("unchecked")
		IObservableValue<Boolean> isReadOnlyValue = 
			(IObservableValue<Boolean>) (Object) Observables.<Object>constantObservableValue(!editable, Boolean.class);
		addEnablementListener(container, mainValue, isReadOnlyValue, excluded);
	}

	public static void addEnablementListener(final Composite container, boolean editableOverride, IObservableValue<Boolean> mainValue, IObservableValue<Boolean> readOnly, Control... excluded) {
		@SuppressWarnings("unchecked")
		IObservableValue<Boolean> isReadOnlyValue = editableOverride ? readOnly 
			: (IObservableValue<Boolean>) (Object) Observables.<Object>constantObservableValue(true, Boolean.class);
		addEnablementListener(container, mainValue, isReadOnlyValue, excluded);
	}
	
	
	/**
	 * Adds a IValueChangeListener that disables or makes read-only the children
	 * of container.
	 * 
	 * @param container
	 *            Updates enabled state on this and its children.
	 * @param editable
	 *            If true {@code readOnly} in used to decide editable state. If
	 *            false {@code readOnly} in overridden and control is never
	 *            editable.
	 * @param mainValue
	 *            Uses this to decide if controls should be totally disabled and
	 *            grayed out. Only this object is listened to for updates.
	 * @param readOnly
	 *            Determines if controls should be read-only (when mainValue
	 *            changes)
	 * @param excluded
	 *            Elements of this list and their children do not have their
	 *            enabled state changed. NOTE: Nested controls with their own
	 *            enabled state handling must be added here!
	 */
	public static void addEnablementListener(final Composite container, IObservableValue<Boolean> mainValue, 
			final IObservableValue<Boolean> readOnly, Control... excluded) {
		
		final ImmutableSet<Control> excludedSet = ImmutableSet.copyOf(excluded);
		
		// Set initial state
		setEnabledRecursive(container, calcEnabledState(mainValue.getValue(), readOnly.getValue()), excludedSet);
		
		mainValue.addValueChangeListener(event -> setEnabledRecursive(container, 
			calcEnabledState(readOnly.getValue(), event.diff.getNewValue()), excludedSet));
	}

	private static EnableState calcEnabledState(boolean isReadOnly, boolean isEnabled) {
		if (!isEnabled) return EnableState.DISABLED;
		if (isReadOnly) return EnableState.READ_ONLY;
		return EnableState.EDITABLE;
	}
	
	/**
	 * Disables or makes read-only {@code control} and all its child controls (recursively). 
	 * Also restores the state of controls previously disabled by this method. The action
	 * performed on the controls is determined by {@link EnableState enableState}. 
	 * 
	 * @param excluded These controls (and their children) are not modified by
	 * the method.
	 */
	private static void setEnabledRecursive(Control control, EnableState enableState, Set<Control> excluded) {
		if (excluded.contains(control)) {
			; // Skip excluded
		} else if (control instanceof ExpandableComposite) {
			setEnabledRecursive(((ExpandableComposite) control).getClient() , enableState, excluded);
		} else if (control instanceof Composite && !(control instanceof Combo) && !(control instanceof Canvas)) {
			for (Control child : ((Composite) control).getChildren()) {
				setEnabledRecursive(child, enableState, excluded);
			}
		} else {
			updateControl(control, enableState);
		}
	}
	
	private static void updateControl(Control control, EnableState newState) {
		if (newState == EnableState.DISABLED) {
			makeDisabled(control);
		} else if (newState == EnableState.READ_ONLY) {
			if (control instanceof Text) {
				makeEnabled(control);
				makeNonEditable(control);
			} else if (control instanceof Label || control instanceof Hyperlink) {
				makeEnabled(control);
			} else {
				makeDisabled(control);
			}
		} else if (newState == EnableState.EDITABLE) {
			if (control instanceof Text) {
				makeEnabled(control);
				makeEditable(control);
			} else {
				makeEnabled(control);
			}
		}
	}


	private static void makeEditable(Control control) {
		if (control.getData(READ_ONLY_KEY) != null) {
			control.setData(READ_ONLY_KEY, null);
			((Text) control).setEditable(true);
		}
	}

	private static void makeNonEditable(Control control) {
		if (((Text) control).getEditable()) {
			control.setData(READ_ONLY_KEY, "marked");
			((Text) control).setEditable(false);
		}
	}


	private static void makeEnabled(Control control) {
		if (control.getData(DISABLED_KEY) != null) {
			control.setData(DISABLED_KEY, null);
			control.setEnabled(true);
		}
	}

	private static void makeDisabled(Control control) {
		if (control.getEnabled()) {
			control.setData(DISABLED_KEY, "marked");
			control.setEnabled(false);
		}
	}

}
