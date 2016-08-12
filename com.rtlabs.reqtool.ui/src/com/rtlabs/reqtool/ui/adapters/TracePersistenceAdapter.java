package com.rtlabs.reqtool.ui.adapters;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.rtlabs.reqtool.model.requirements.Specification;

public class TracePersistenceAdapter implements org.eclipse.app4mc.capra.core.adapters.TracePersistenceAdapter {

	@Override
	public EObject getTraceModel(EObject object) {
		Resource resource = object.eResource();
		EObject rootObject = resource.getContents().get(0);
		if (rootObject instanceof Specification) {
			Specification specification = (Specification) rootObject;
			return specification;			
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
		throw new UnsupportedOperationException();
	}

	@Override
	public EObject getArtifactWrappers(ResourceSet resourceSet) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void saveTracesAndArtifacts(EObject traceModel, EObject artifactModel) {
		// Do nothing, model is saved by editor
	}

}
