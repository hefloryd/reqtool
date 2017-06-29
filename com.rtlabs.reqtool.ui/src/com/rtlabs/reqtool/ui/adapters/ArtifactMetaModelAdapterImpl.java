package com.rtlabs.reqtool.ui.adapters;

import org.eclipse.capra.core.adapters.AbstractArtifactMetaModelAdapter;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.EObject;

import com.rtlabs.common.util.RtUtil;
import com.rtlabs.reqtool.model.requirements.Artifact;
import com.rtlabs.reqtool.model.requirements.ArtifactContainer;
import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.RequirementsFactory;

public class ArtifactMetaModelAdapterImpl extends AbstractArtifactMetaModelAdapter {

	@Override
	public EObject createModel() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IPath getArtifactPath(EObject artifact) {
		if (!(artifact instanceof Requirement)) return null;
		return RtUtil.toEclipseFile(((Requirement) artifact).eResource().getURI()).getFullPath();
	}

	@Override
	public EObject createArtifact(EObject artifactModel, String artifactHandler, String artifactUri,
			String artifactName, String path)
	{
		EObject existing = getArtifact(artifactModel, artifactHandler, artifactUri);
		if (existing != null)
			return existing;
		
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
			return ((Artifact) artifact).getName();
		}
		
		if (artifact instanceof Requirement) {
			return ((Requirement) artifact).getName();
		}

		return null;
	}

	@Override
	public String getArtifactUri(EObject artifact) {
		if (artifact instanceof Artifact) {
			return ((Artifact) artifact).getUri();
		}
		
		if (artifact instanceof Requirement) {
			return artifact.eResource().getURI().toPlatformString(true);
		}
		
		return null;
	}
}
