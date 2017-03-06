package com.rtlabs.reqtool.ui.editors.support;

import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.ui.action.RedoAction;
import org.eclipse.emf.edit.ui.action.UndoAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;


/**
 * Contributes undo and redo actions for a model entity editor.
 */
public class SpecificationEditorActionBarContributor extends MultiPageEditorActionBarContributor {

	private UndoAction undoAction;
	private RedoAction redoAction;

	@Override
	public void init(IActionBars actionBars) {
		super.init(actionBars);
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();

		undoAction = new UndoAction();
		undoAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);

		redoAction = new RedoAction();
		redoAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);
	}
	
	@Override
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);
	}

	@Override
	public void setActivePage(IEditorPart part) {
		update(part);
	}

	private void update(IEditorPart part) {
		if (part instanceof IEditingDomainProvider) {
			undoAction.setActiveWorkbenchPart(part);
			redoAction.setActiveWorkbenchPart(part);	
		} else {
			undoAction.setActiveWorkbenchPart(null);
			redoAction.setActiveWorkbenchPart(null);	
		}
	}
}
