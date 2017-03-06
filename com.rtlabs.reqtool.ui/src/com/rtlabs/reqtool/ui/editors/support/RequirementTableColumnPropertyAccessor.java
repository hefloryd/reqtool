package com.rtlabs.reqtool.ui.editors.support;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;

/**
 * A class which gets and sets property values on EObjects. Used by NatList. 
 */
public class RequirementTableColumnPropertyAccessor<T extends EObject> implements IColumnPropertyAccessor<T> {
	
	private AdapterFactory adapterFactory;
	private String[] propertyNames;

	public RequirementTableColumnPropertyAccessor(AdapterFactory adapterFactory, String[] propertyNames) {
		this.adapterFactory = adapterFactory;
		this.propertyNames = propertyNames;
	}

	@Override
	public Object getDataValue(T rowObject, int columnIndex) {
		IItemPropertySource adapter = (IItemPropertySource)adapterFactory.adapt(rowObject, IItemPropertySource.class);
		String propertyName = propertyNames[columnIndex];
		IItemPropertyDescriptor descriptor = adapter.getPropertyDescriptor(rowObject, propertyName);
		Object propertyValue = descriptor.getPropertyValue(rowObject);
		if (propertyValue instanceof IItemLabelProvider) {
			IItemLabelProvider labelProvider = (IItemLabelProvider) propertyValue;
			return labelProvider.getText(rowObject);			
		}
		return null;
	}

	@Override
	public void setDataValue(T rowObject, int columnIndex, Object newValue) {
		IItemPropertySource adapter = (IItemPropertySource)adapterFactory.adapt(rowObject, IItemPropertySource.class);
		String propertyName = propertyNames[columnIndex];
		IItemPropertyDescriptor descriptor = adapter.getPropertyDescriptor(rowObject, propertyName);
		descriptor.setPropertyValue(rowObject, newValue);
	}

	@Override
	public int getColumnCount() {
		return propertyNames.length;
	}

	@Override
	public String getColumnProperty(int columnIndex) {
		String propertyName = propertyNames[columnIndex];
		return propertyName;
	}

	@Override
	public int getColumnIndex(String propertyName) {
		for (int i = 0; i < propertyNames.length; i++) {
			if (propertyNames[i].equals(propertyName))
				return i;
		}
		return -1;
	}

}
