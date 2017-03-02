package com.rtlabs.common.edit_support;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Used to enable clients to run a command. Passes arguments that are required for most commands. 
 */
public interface CommandOperation<T extends EObject> {

	void run(EditContext editContext, IObservableValue<T> containingEntity, EStructuralFeature containingFeature);
	String getLabel();

	public interface CommandListener<T> {
		void run(EditContext editContext, IObservableValue<T> containingEntity, EStructuralFeature containingFeature);
	}
	
	
//	public class SimpleCommandOperation<T extends EObject> implements CommandOperation<T> {
//		
//		private final String label;
//		private final CommandListener<T> listener;
//		
//		public SimpleCommandOperation(String label, CommandListener<T> listener) {
//			this.label = label;
//			this.listener = listener;
//		}
//		
//		public String getLabel() {
//			return label;
//		}
//
//		@Override
//		public void run(EditContext editContext, IObservableValue<T> containingEntity, EStructuralFeature containingFeature) {
//			listener.run(editContext, containingEntity, containingFeature);
//		}
//	}
//	
//	public static <T extends EObject> CommandOperation<T> of(String label, CommandListener<T> listener) {
//		return new SimpleCommandOperation<>(label, listener);
//	}
	
//	void run(EditContext editContext, IObservableValue<T> containingEntity, EStructuralFeature containingFeature);
//	String getLabel();
//
//	
	public static <T extends EObject> CommandOperation<T> of(String label, CommandListener<T> op) {
		return new CommandOperation<T>() {
			@Override
			public void run(EditContext editContext, IObservableValue<T> containingEntity, EStructuralFeature containingFeature) {
				op.run(editContext, containingEntity, containingFeature);
			}
			
			public String getLabel() {
				return label;
			}
		};
	}
}
