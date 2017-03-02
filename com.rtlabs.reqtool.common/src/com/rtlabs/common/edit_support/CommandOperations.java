package com.rtlabs.common.edit_support;

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.rtlabs.common.util.DialogCreator;

public class CommandOperations {

	public static <T extends EObject, R> CommandOperation<T> setDialog(DialogCreator dialogCreator) {
		return setDialog(() -> {
			SelectionDialog dialog = dialogCreator.get();
			return dialog.open() == Window.OK 
				? Optional.of(dialog.getResult()[0])
				: Optional.empty();
		});
	}
	
	public static <T extends EObject, R> CommandOperation<T> setDialog(Supplier<Optional<R>> resultSupplier) {
		return CommandOperation.of("Select...", 
			(EditContext editContext, IObservableValue<T> containingEntity, EStructuralFeature containingFeature) ->
				resultSupplier.get().ifPresent(dialogResult ->
					editContext.getEditingDomain().getCommandStack().execute(
						SetCommand.create(editContext.getEditingDomain(), 
							containingEntity.getValue(), containingFeature, dialogResult))));
	}
}
