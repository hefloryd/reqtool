package com.rtlabs.reqtool.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.ui.editors.SpreadSheetEditorInput;

public class CallEditor extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// get the page
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage page = window.getActivePage();
		
		// get the selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection != null && selection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) selection).getFirstElement();
			// if we had a selection lets open the editor
			if (obj != null) {
				Requirement req = (Requirement) obj;
				SpreadSheetEditorInput input = new SpreadSheetEditorInput();
			}
		}
		return null;
	}
	
}
