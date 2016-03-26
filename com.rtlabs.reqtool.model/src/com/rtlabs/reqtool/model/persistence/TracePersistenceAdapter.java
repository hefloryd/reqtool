package com.rtlabs.reqtool.model.persistence;

import java.util.List;
import java.util.Optional;

import org.eclipse.app4mc.capra.generic.artifacts.ArtifactWrapperContainer;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.rtlabs.reqtool.model.requirements.Specification;

public class TracePersistenceAdapter implements org.eclipse.app4mc.capra.generic.adapters.TracePersistenceAdapter {

	@Override
	public Optional<EObject> getTraceModel(EObject object) {
		Resource resource = object.eResource();
		EObject rootObject = resource.getContents().get(0);
		if (rootObject instanceof Specification) {
			Specification specification = (Specification) rootObject;
			return Optional.of(specification.getTraceModel());			
		}		
		return Optional.empty();
	}

	@Override
	public Optional<ArtifactWrapperContainer> getArtifactWrappers(EObject object) {
		Resource resource = object.eResource();
		EObject rootObject = resource.getContents().get(0);
		if (rootObject instanceof Specification) {
			Specification specification = (Specification) rootObject;
			return Optional.of(specification.getArtifactWrapperContainer());			
		}		
		return Optional.empty();
	}

	@Override
	public Optional<EObject> getTraceModel(ResourceSet resourceSet) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<ArtifactWrapperContainer> getArtifactWrappers(ResourceSet resourceSet) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void saveTracesAndArtifactWrappers(EObject traceModel, List<EObject> selectionForTraceCreation,
			Optional<ArtifactWrapperContainer> artifactWrappers) {
		// Do nothing, model is saved by editor
	}

}
