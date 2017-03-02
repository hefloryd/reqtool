package com.rtlabs.common.util;

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IMessagePrefixProvider;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * An {@link IMessagePrefixProvider} that handles controls consisting of a name field and a "Select" button nested
 * inside a GridLayout.
 */
public class ExtendedMessageManagerPrefixProvider implements IMessagePrefixProvider {

	public String getPrefix(Control c) {
		Composite parent = c.getParent();
		Control[] siblings = parent.getChildren();
		for (int i = 0; i < siblings.length; i++) {
			if (siblings[i] == c) {
				// this is us - go backward until you hit
				// a label-like widget
				for (int j = i - 1; j >= 0; j--) {
					Control label = siblings[j];
					String ltext = null;
					if (label instanceof Label) {
						ltext = ((Label) label).getText();
					} else if (label instanceof Hyperlink) {
						ltext = ((Hyperlink) label).getText();
					} else if (label instanceof CLabel) {
						ltext = ((CLabel) label).getText();
					}
					if (ltext != null) {
						if (!ltext.endsWith(":")) //$NON-NLS-1$
							return ltext + ": "; //$NON-NLS-1$
						return ltext + " "; //$NON-NLS-1$
					}
				}
				break;
			}
		}
		
		// Check for one-row nested GridLayout. Treat that as a single control.
		if (parent.getLayout() instanceof GridLayout) {
			GridLayout layout = (GridLayout) parent.getLayout();
			if (siblings.length <= layout.numColumns) {
				return getPrefix(parent);
			}
		}
		
		return null;
	}
}