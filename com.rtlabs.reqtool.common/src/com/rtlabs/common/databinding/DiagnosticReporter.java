package com.rtlabs.common.databinding;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EValidator.SubstitutionLabelProvider;

/**
 * Utility class that helps with reporting issues to a diagnosticChain.
 */
public class DiagnosticReporter {
	private EStructuralFeature feature;
	private final EObject object;
	private final String diagnosticSource;
	private final int diagnosticCode;
	private final DiagnosticChain diagnosticChain;
	private final Map<?, ?> context;
	
	public DiagnosticReporter(DiagnosticChain chain, Map<?, ?> context, EStructuralFeature feature, EObject object, String diagnosticSource,
			int diagnosticCode) {
		super();
		this.diagnosticChain = chain;
		this.context = context;
		this.feature = feature;
		this.object = object;
		this.diagnosticSource = diagnosticSource;
		this.diagnosticCode = diagnosticCode;
	}

	public boolean status(IStatus status) {
		if (!status.isOK()) {
			error(status.getMessage());
		}
		
		return status.isOK();
	}
	

	public void error(String message) {
		issue(message, Diagnostic.ERROR);
	}

	public void issue(String message, int severity) {
		if (diagnosticChain != null) {
			SubstitutionLabelProvider p = (SubstitutionLabelProvider) context.get(SubstitutionLabelProvider.class);
			
			String label = "";
			if (p != null) {
				label = "The feature '" + p.getFeatureLabel(feature) + "' of '" + p.getObjectLabel(object) + "' has an error: ";
			}
			
			diagnosticChain.add(new BasicDiagnostic(severity, diagnosticSource,
				diagnosticCode, label + message, new Object[] { object, feature }));
		}
	}

	public DiagnosticChain getDiagnosticChain() {
		return diagnosticChain;
	}

	public void setFeature(EStructuralFeature feature) {
		this.feature = feature; 
	}
}
