package com.rtlabs.common.edit_support;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.IItemPropertySource;

import com.rtlabs.common.util.MessageManagerSupport;

/**
 * An {@link EditContext} which uses its own -- local -- data binding context. 
 * 
 * This is used for dialogs. Using the normal data binding context in dialogs causes a crash in
 * {@link MessageManagerSupport}.
 */
public class LocalEditContext implements EditContext {

	// Empty data binding context. This is part of the purpose with this class.
	private final DataBindingContext dataBindingContext = new EMFDataBindingContext();
	private final EditContext editContext;
	
	// Local editing domain. 
	private EditingDomain editingDomain;
	
	public LocalEditContext(EditContext editContext) {
		this.editContext = editContext;
		this.editingDomain = new AdapterFactoryEditingDomain(
				editContext.getAdapterFactory(), new BasicCommandStack());
	}
	
	@Override
	public EditingDomain getEditingDomain() {
		return editingDomain;
	}

	@Override
	public DataBindingContext getDataBindingContext() {
		return dataBindingContext;
	}

	@Override
	public IItemPropertySource getItemPropertySource(EObject o) {
		return editContext.getItemPropertySource(o);
	}

	@Override
	public AdapterFactory getAdapterFactory() {
		return editContext.getAdapterFactory();
	}
}
