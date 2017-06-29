package com.rtlabs.reqtool.ui.adapters;

import org.eclipse.capra.core.adapters.TracePersistenceAdapter;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.rtlabs.reqtool.model.requirements.Specification;

public class TracePersistenceAdapterImpl implements TracePersistenceAdapter {

	@Override
	public EObject getTraceModel(EObject object) {
		Resource resource = object.eResource();
		EObject rootObject = resource.getContents().get(0);
		if (rootObject instanceof Specification) {
			return (Specification) rootObject;
		}		
		return null;
	}

	@Override
	public EObject getArtifactWrappers(EObject object) {
		Resource resource = object.eResource();
		EObject rootObject = resource.getContents().get(0);
		if (rootObject instanceof Specification) {
			Specification specification = (Specification) rootObject;
			return specification.getArtifactContainer();			
		}		
		return null;
	}

	@Override
	public EObject getTraceModel(ResourceSet resourceSet) {
		return null;
	}

	@Override
	public EObject getArtifactWrappers(ResourceSet resourceSet) {
		return null;
	}

	@Override
	public void saveTracesAndArtifacts(EObject traceModel, EObject artifactModel) {
		// Do nothing, model is saved by editor
	}
}
