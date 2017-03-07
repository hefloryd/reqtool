package com.rtlabs.reqtool.model.requirements;

import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.SPECIFICATION__DOCUMENT_PREFIX_FILE;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.SPECIFICATION__DOCUMENT_SUFFIX_FILE;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.google.common.base.Strings;

public class SpecificationValidation {

	public static boolean validate(Specification spec, DiagnosticChain chain, Map<?, ?> context) {
		boolean result = true;
		result &= validateFile(spec, SPECIFICATION__DOCUMENT_PREFIX_FILE, chain, context);
		result &= validateFile(spec, SPECIFICATION__DOCUMENT_SUFFIX_FILE, chain, context);
		return result;
	}
	
	public static boolean validateFile(Specification spec, EStructuralFeature feature, DiagnosticChain chain, Map<?, ?> context) {
		DiagnosticReporter reporter = new DiagnosticReporter(chain, context, feature, spec, 
			SpecificationValidation.class.getName(), -1);
		
		String pathText = (String) spec.eGet(feature);
		
		if (Strings.isNullOrEmpty(pathText)) {
			reporter.error("This field must not be empty.");
			return false;
		}
		
		Path path;
		
		try {
			path = Paths.get(pathText);
		} catch (InvalidPathException e) {
			reporter.error("The file path is invalid.");
			return false;
		}
		
		if (!path.isAbsolute()) {
			Path specPath = toSystemPath(spec.eResource().getURI());
			if (specPath == null) {
				reporter.error("No specification file path exists and the path is relative.");
				return false;
			}

			path = specPath.getParent().resolve(path);
		}
		
		if (!Files.exists(path)) {
			reporter.error("The file does not exist.");
			return false;
		}
		
		if (!Files.isRegularFile(path)) {
			reporter.error("The path is not a file.");
			return false;
		}
		
		return true;
	}
	
	
	public static Path toSystemPath(URI resource) {
		if (resource == null) {
			return null;
		} else if (resource.isPlatform()) {
			String platformString = resource.toPlatformString(true);
			if (platformString == null) return null;
			return Paths.get(ResourcesPlugin.getWorkspace().getRoot().getFile(
				new org.eclipse.core.runtime.Path(platformString)).getRawLocationURI());
		} else if (resource.isFile()) {
			return Paths.get(resource.toFileString());
		} else {
			return null;
		}
	}

}
