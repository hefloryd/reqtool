package com.rtlabs.common.edit_support;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;

public class EditSupport {
	public static ComposedAdapterFactory createStandardAdaperFactory(AdapterFactory... entityAdapterFactories) {
		// Create an adapter factory that yields item providers
		ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

		// What is this for? Don't we get all info we need from the entityAdapterFactories?
		adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		
		for (AdapterFactory fact : entityAdapterFactories) {
			adapterFactory.addAdapterFactory(fact);
		}
		
		adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
		
		return adapterFactory;
	}

}
