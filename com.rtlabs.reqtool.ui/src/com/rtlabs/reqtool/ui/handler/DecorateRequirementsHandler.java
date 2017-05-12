package com.rtlabs.reqtool.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.rtlabs.reqtool.ui.editors.SpecificationEditor;

public class DecorateRequirementsHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		
		if (!(editor instanceof SpecificationEditor)) return null;
		
		((SpecificationEditor) editor).decorateWithTestResults();
		
		return null;
	}
}
