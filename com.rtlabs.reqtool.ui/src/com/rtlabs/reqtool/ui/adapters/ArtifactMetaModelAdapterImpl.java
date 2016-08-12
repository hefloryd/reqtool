package com.rtlabs.reqtool.ui.adapters;

import org.eclipse.app4mc.capra.core.adapters.ArtifactMetaModelAdapter;
import org.eclipse.emf.ecore.EObject;

import com.rtlabs.reqtool.model.requirements.Artifact;
import com.rtlabs.reqtool.model.requirements.ArtifactContainer;
import com.rtlabs.reqtool.model.requirements.RequirementsFactory;

public class ArtifactMetaModelAdapterImpl implements ArtifactMetaModelAdapter {

	@Override
	public EObject createModel() {
		throw new UnsupportedOperationException();
	}

	@Override
	public EObject createArtifact(EObject artifactModel, String artifactHandler, String artifactUri,
			String artifactName) {
		if (artifactModel instanceof ArtifactContainer) {
			ArtifactContainer container = (ArtifactContainer) artifactModel;
			Artifact artifact = RequirementsFactory.eINSTANCE.createArtifact();
			artifact.setArtifactHandler(artifactHandler);
			artifact.setName(artifactName);
			artifact.setUri(artifactUri);
			container.getArtifacts().add(artifact);
			return artifact;
		}
		return null;
	}

	@Override
	public EObject getArtifact(EObject artifactModel, String artifactHandler, String artifactUri) {
		if (artifactModel instanceof ArtifactContainer) {
			ArtifactContainer container = (ArtifactContainer) artifactModel;
			for (Artifact artifact : container.getArtifacts()) {
				if (getArtifactHandler(artifact).equals(artifactHandler) && getArtifactUri(artifact).equals(artifactUri))								
					return artifact;			
			}
			return null;
		}
		return null;
	}

	@Override
	public String getArtifactHandler(EObject artifact) {
		if (artifact instanceof Artifact) {
			Artifact a = (Artifact) artifact;
			return a.getArtifactHandler();
		}
		return null;
	}

	@Override
	public String getArtifactName(EObject artifact) {
		if (artifact instanceof Artifact) {
			Artifact a = (Artifact) artifact;
			return a.getName();
		}
		return null;
	}

	@Override
	public String getArtifactUri(EObject artifact) {
		if (artifact instanceof Artifact) {
			Artifact a = (Artifact) artifact;
			return a.getUri();
		}
		return null;
	}

}
