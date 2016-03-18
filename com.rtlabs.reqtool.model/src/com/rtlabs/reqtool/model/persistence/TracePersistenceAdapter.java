package com.rtlabs.reqtool.model.persistence;

import java.util.List;
import java.util.Optional;

import org.eclipse.app4mc.capra.generic.artifacts.ArtifactWrapperContainer;
import org.eclipse.app4mc.capra.simpletrace.tracemetamodel.SimpleTraceModel;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.rtlabs.reqtool.model.fixture.RequirementService;
import com.rtlabs.reqtool.model.requirements.Specification;

public class TracePersistenceAdapter implements org.eclipse.app4mc.capra.generic.adapters.TracePersistenceAdapter {

	public TracePersistenceAdapter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Optional<EObject> getTraceModel(ResourceSet resourceSet) {
		Specification specification = RequirementService.getInstance().getSpecification();
		SimpleTraceModel traceModel = specification.getTraceModel();
		return Optional.ofNullable(traceModel);
	}

	@Override
	public Optional<ArtifactWrapperContainer> getArtifactWrappers(ResourceSet resourceSet) {
		Specification specification = RequirementService.getInstance().getSpecification();
		ArtifactWrapperContainer artifactWrapperContainer = specification.getArtifactWrapperContainer();
		return Optional.ofNullable(artifactWrapperContainer);
	}

	@Override
	public void saveTracesAndArtifactWrappers(EObject traceModel, List<EObject> selectionForTraceCreation,
			Optional<ArtifactWrapperContainer> artifactWrappers) {
		// TODO Auto-generated method stub

	}

}
