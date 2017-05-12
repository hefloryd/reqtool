package com.rtlabs.reqtool.ui.editors.support;

import org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor;

/**
 * Used to contribute standard EMF versions of edit actions: copy, cut, past, undo, redo.
 * 
 * TODO: This file is not used for the moment, because it messes up edit action in text field.
 * 
 */
public class EmfSpecificationEditorActionBarContributor extends EditingDomainActionBarContributor {
	public EmfSpecificationEditorActionBarContributor() {
		super(ADDITIONS_LAST_STYLE);
	}
}
