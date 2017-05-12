package com.rtlabs.reqtool.ui.adapters;

import static java.util.Collections.emptyList;

import java.util.List;

import org.eclipse.capra.core.adapters.AbstractArtifactMetaModelAdapter;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.EObject;

import com.rtlabs.common.util.RtUtil;
import com.rtlabs.reqtool.model.requirements.Artifact;
import com.rtlabs.reqtool.model.requirements.ArtifactContainer;
import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.RequirementsFactory;

/**
 * AbstractArtifactMetaModelAdapter for the ReqTool project.
 */
public class ArtifactMetaModelAdapterImpl extends AbstractArtifactMetaModelAdapter {

	/**
	 * Don't support creating model this way. 
	 */
	@Override
	public EObject createModel() {
		return null;
	}
	
	@Override
	public IPath getArtifactPath(EObject artifact) {
		if (artifact instanceof Requirement && ((Requirement) artifact).eResource() != null) {
			return RtUtil.toEclipseFile(((Requirement) artifact).eResource().getURI()).getFullPath();
		}
		
		return null;
	}

	// TODO: Don't annotate with @Override to be able to work with both old and new API. 
	// Remove this method the implemented method disappears. 
	// @Override
	public EObject createArtifact(EObject artifactModel, String artifactHandler,
		String artifactId, String artifactName, String path) {
		return createArtifact(artifactModel, artifactHandler, artifactId, artifactId, artifactName, path);
	}

	// TODO: Don't annotate to be able to work with both old and new API. Add @Override annotation
	// when this method has been added to the interface.
	// @Override
	public EObject createArtifact(EObject artifactModel, String artifactHandler, String artifactUri,
		String artifactId, String artifactName, String path)
	{
		EObject existing = getArtifact(artifactModel, artifactHandler, artifactUri);
		if (existing != null) return existing;
		
		if (artifactModel instanceof ArtifactContainer) {
			ArtifactContainer container = (ArtifactContainer) artifactModel;
			Artifact artifact = RequirementsFactory.eINSTANCE.createArtifact();
			artifact.setArtifactHandler(artifactHandler);
			artifact.setName(artifactName);
			artifact.setUri(artifactUri);
			artifact.setIdentifier(artifactId);
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
				if (getArtifactHandler(artifact).equals(artifactHandler)
					&& getArtifactUri(artifact).equals(artifactUri))
				{								
					return artifact;			
				}
			}
			return null;
		}
		return null;
	}

	@Override
	public String getArtifactHandler(EObject artifact) {
		if (artifact instanceof Artifact) {
			return ((Artifact) artifact).getArtifactHandler();
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
			return "platform://resource/" + artifact.eResource().getURI().toPlatformString(true) + "/" 
				+ ((Requirement) artifact).getIdentifier();
		}
		
		return null;
	}
	
	@Override
	public String getArtifactIdentifier(EObject artifact) {
		if (artifact instanceof Artifact) {
			return ((Artifact) artifact).getIdentifier();
		}
		
		if (artifact instanceof Requirement) {
			return Integer.toString(((Requirement) artifact).getIdentifier());
		}
		
		return null;
	}


	@Override
	public List<EObject> getAllArtifacts(EObject artifactModel) {
		// The ReqTool model do not keep track of the artifacts in the model
		// so we don't participate when all of them are needed. For example in the 
		// notification system.
		return emptyList();
	}
}
