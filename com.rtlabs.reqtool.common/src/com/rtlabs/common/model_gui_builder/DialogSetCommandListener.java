package com.rtlabs.common.model_gui_builder;

//import org.eclipse.core.databinding.observable.value.IObservableValue;
//import org.eclipse.emf.ecore.EObject;
//import org.eclipse.emf.ecore.EStructuralFeature;
//import org.eclipse.emf.edit.command.SetCommand;
//import org.eclipse.jface.window.Window;
//import org.eclipse.ui.dialogs.SelectionDialog;
//
//import com.rtlabs.common.emf_project.edit_support.CommandOperation;
//import com.rtlabs.common.emf_project.edit_support.EditContext;
//import com.rtlabs.common.emf_project.util.DialogCreator;
//
///**
// * A command listener which opens a dialog.
// * 
// * @param <T> The type of the object on which to set the new value.
// */
//class DialogSetCommandListener {
//	private final DialogCreator dialogCreator;
//	private final EStructuralFeature feature;
//
//	public DialogSetCommandListener(DialogCreator dialogCreator, EStructuralFeature feature) {
//		this.dialogCreator = dialogCreator;
//		this.feature = feature;
//	}
//
//	@Override
//	public void run(EditContext editContext, IObservableValue<T> containingEntity, EStructuralFeature containingFeature) {
//		SelectionDialog dialog = dialogCreator.get();
//		int resultCode = dialog.open();
//		
//		if (resultCode != Window.OK) {
//			return;
//		}
//		
//		Object newObject = dialog.getResult()[0];
//		
//		editContext.getEditingDomain().getCommandStack().execute(
//			SetCommand.create(editContext.getEditingDomain(), containingEntity.getValue(), feature, newObject));
//	}
//
//	@Override
//	public String getLabel() {
//		return "Select...";
//	}
//	
//}
