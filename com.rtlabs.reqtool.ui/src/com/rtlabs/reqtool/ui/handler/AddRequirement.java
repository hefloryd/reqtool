package com.rtlabs.reqtool.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.Specification;
import com.rtlabs.reqtool.ui.editors.SpreadSheetEditor;

public class AddRequirement extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		if (activeEditor instanceof SpreadSheetEditor) {
			SpreadSheetEditor editor = (SpreadSheetEditor) activeEditor;
			Specification specification = editor.getSpecificationValue();
			EditingDomain editingDomain = editor.getEditingDomain();
			Requirement requirement = specification.createNewRequirement();
			AddCommand command = new AddCommand(editingDomain, specification.getRequirements(), requirement);
			editingDomain.getCommandStack().execute(command);
		}
		return null;
	}

}
