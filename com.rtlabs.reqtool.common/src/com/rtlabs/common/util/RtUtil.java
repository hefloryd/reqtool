package com.rtlabs.common.util;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

public class RtUtil {
	/**
	 * @return An Eclipse IFile that is referenced by the given EMF resource. 
	 */
	public static IFile toEclipseFile(Resource resource) {
		if (resource == null) return null;
		return toEclipseFile(resource.getURI());
	}

	/**
	 * @return An Eclipse IFile that is referenced by the given EMF resource. 
	 */
	public static IFile toEclipseFile(URI resource) {
		return toEclipseFile(resource, ResourcesPlugin.getWorkspace());
	}

	public static IFile toEclipseFile(URI resource, IWorkspace workspace) {
		if (resource == null) return null;
		String platformString = resource.toPlatformString(true);
		if (platformString == null) return null;
		return workspace.getRoot().getFile(new Path(platformString));
	}

	
	public static URI toEmfUri(IFile file) {
		return URI.createPlatformResourceURI(file.getFullPath().toString(), true);
	}

	
	/**
	 * Makes sure container and all of its parents exists. If container is a project verify its existence. If it is a
	 * folder then create it.
	 */
	public static void createContainerRecursive(IContainer container) throws CoreException {
		if (container instanceof IFolder) {
			createFolderRecursive((IFolder) container);
		} else {
			if (!container.exists()) {
				throw new IllegalArgumentException();
			}
		}
	}
	
	/**
	 * Creates folder and all parent folders. Throws if one of the parents is a non-folder (ex a project)
	 * that doesn't exist.
	 */
	public static void createFolderRecursive(IFolder folder) throws CoreException {
		if (folder.exists()) return;
		
		if (folder.getParent() instanceof IFolder) {
			createFolderRecursive((IFolder) folder.getParent());
		} else if (!folder.getParent().exists()) {
			// Parent is probably a project which doesn't exist
			throw new IllegalArgumentException();
		}
		
		folder.create(true, false, null);
	}
}
