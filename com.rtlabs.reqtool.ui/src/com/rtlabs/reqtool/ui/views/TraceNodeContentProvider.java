package com.rtlabs.reqtool.ui.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.app4mc.capra.generic.adapters.Connection;
import org.eclipse.app4mc.capra.generic.adapters.TraceMetamodelAdapter;
import org.eclipse.app4mc.capra.simpletrace.tracemetamodel.EObjectToEObject;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;

import com.rtlabs.reqtool.model.requirements.Requirement;

public class TraceNodeContentProvider implements IStructuredContentProvider, IGraphEntityContentProvider {
	
	private TraceMetamodelAdapter metaModelAdapter;
	private Optional<EObject> traceModel;

	public TraceNodeContentProvider(TraceMetamodelAdapter metaModelAdapter, Optional<EObject> traceModel) {		
		this.metaModelAdapter = metaModelAdapter;	
		this.traceModel = traceModel;
	}
	
	@Override
	public Object[] getConnectedTo(Object entity) {
		if (entity instanceof Requirement) {
			Requirement requirement = (Requirement) entity;			
			List<Connection> connectedElements = metaModelAdapter.getConnectedElements(requirement, traceModel);
			
			List<EObject> list = new ArrayList<EObject>();
			for (Connection c : connectedElements) {
				if (c.getTlink() instanceof EObjectToEObject) {					
					EObjectToEObject trace = (EObjectToEObject) c.getTlink();
					// Only interested in elements this element connects to,
					// not elements that connect to this element
					if (trace.getSource().equals(requirement)) {
						list.addAll(c.getTargets());
					}
				}
			}
			
			return list.toArray();
		}
		return null;
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
			
			List<Connection> traces = metaModelAdapter.getTransitivelyConnectedElements(requirement, traceModel);			
			List<EObject> list = new ArrayList<EObject>();
			for (Connection c : traces) {
				list.addAll(c.getTargets());
			}
			list.add(requirement);
			return list.toArray();
		}
		return null;
	}

}
