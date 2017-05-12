package com.rtlabs.reqtool.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;

import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.Traceable;

public class TraceNodeContentProvider implements IStructuredContentProvider, IGraphEntityContentProvider {
	
	@Override
	public Object[] getConnectedTo(Object entity) {
		if (entity instanceof Traceable) {
			Traceable traceable = (Traceable) entity;
			return getConnectedTo(traceable);
		}
		return null;
	}

	public Object[] getConnectedTo(Traceable traceable) {
		List<EObject> list = new ArrayList<EObject>();
		//list.addAll(traceable.getParents());		
		list.addAll(traceable.getChildren());		
		// System.out.println("getConnectedTo(" + traceable + "): " + list);
		return list.toArray();
	}

	@Override
	public void dispose() {		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object entity) {
		if (entity instanceof Requirement) {
			Requirement requirement = (Requirement) entity;
			return getElements(requirement);
		}
		return null;
	}

	public Object[] getElements(Requirement requirement) {
		List<EObject> list = new ArrayList<EObject>();
		list.addAll(requirement.getParents());
		list.addAll(requirement.getChildren());		
		list.add(requirement);
		// System.out.println("getElements(" + requirement + "): " + list);
		return list.toArray();
	}

}
