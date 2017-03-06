package com.rtlabs.reqtool.ui.editors;

import org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor;

/**
 * Used to contribute standard EMF versions of edit actions: copy, cut, past, undo, redo.
 * 
 * TODO: This file is not used for the moment, because it messes ut edit action in text field.
 * 
 */
public class SpreadSheetActionBarContributor extends EditingDomainActionBarContributor {
	public SpreadSheetActionBarContributor() {
		super(ADDITIONS_LAST_STYLE);
	}
}
