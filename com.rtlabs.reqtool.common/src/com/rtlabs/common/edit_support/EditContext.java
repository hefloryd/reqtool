package com.rtlabs.common.edit_support;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.IItemPropertySource;

/**
 * Contains components that are needed to display and data bind to model
 * objects.
 * <p/>
 * This is basically a parameter object that is used to avoid having to pass
 * these objects separately to everywhere.
 */
public interface EditContext extends IEditingDomainProvider {
	DataBindingContext getDataBindingContext();
	AdapterFactory getAdapterFactory();

	default IItemPropertySource getItemPropertySource(EObject o) {
		return (IItemPropertySource) getAdapterFactory().adapt(o, IItemPropertySource.class);
	}
	
	@Override
	EditingDomain getEditingDomain();
}
