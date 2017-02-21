package com.rtlabs.reqtool.ui.test_case_generation;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

class FolderCreator {
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
		if (!folder.exists()) {
			if (folder.getParent() instanceof IFolder) {
				createFolderRecursive((IFolder) folder.getParent());
			} else {
				if (!folder.getParent().exists()) {
					throw new IllegalArgumentException();
				}
			}
			
			folder.create(true, false, null);
		}
	}
}
