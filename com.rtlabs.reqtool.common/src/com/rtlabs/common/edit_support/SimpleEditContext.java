package com.rtlabs.common.edit_support;

import java.util.HashMap;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;

/**
 * A simple {@link EditContext} which takes its components as arguments to its constructor.
 */
public class SimpleEditContext implements EditContext {
	private final DataBindingContext dataBindingContext;
	private final AdapterFactory adapterFactory;
	private final EditingDomain editingDomain;
	
	public SimpleEditContext(EditingDomain editingDomain, DataBindingContext dataBindingContext, AdapterFactory adapterFactory) {
		this.editingDomain = editingDomain;
		this.dataBindingContext = dataBindingContext;
		this.adapterFactory = adapterFactory;
	}

	public DataBindingContext getDataBindingContext() {
		return dataBindingContext;
	}

	public AdapterFactory getAdapterFactory() {
		return adapterFactory;
	}

	public EditingDomain getEditingDomain() {
		return editingDomain;
	}

	
	public EditContext copyWithLocalEditingDomain(EditContext editContext) {
		return new SimpleEditContext(new AdapterFactoryEditingDomain(
			editContext.getAdapterFactory(), new BasicCommandStack()),
			editContext.getDataBindingContext(), 
			editContext.getAdapterFactory());
	}

	private static ComposedAdapterFactory createStandardAdaperFactory(AdapterFactory... entityAdapterFactories) {
		// Create an adapter factory that yields item providers
		ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
		adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		
		for (AdapterFactory fact : entityAdapterFactories) {
			adapterFactory.addAdapterFactory(fact);
		}
		
		adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
		
		return adapterFactory;
	}

	public static EditContext createFilled(AdapterFactory... adapterFactores) {
		return create(createStandardAdaperFactory(adapterFactores));
	}
	
	public static EditContext create(AdapterFactory adapterFactory) {
		return new SimpleEditContext(
			new AdapterFactoryEditingDomain(adapterFactory, new BasicCommandStack(), new HashMap<>()), 
			new EMFDataBindingContext(), 
			adapterFactory);
	}
	
}