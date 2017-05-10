package com.rtlabs.reqtool.model.requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EValidator.SubstitutionLabelProvider;

import com.rtlabs.common.databinding.ValidationUtil;

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
	
	private List<EStructuralFeature> affectedFeatures = new ArrayList<>();
	
	public DiagnosticReporter(
		DiagnosticChain diagnosticChain,
		Map<?, ?> context, 
		EStructuralFeature feature, 
		EObject object,
		String diagnosticSource,
		int diagnosticCode)
	{
		this.diagnosticChain = diagnosticChain;
		this.context = context;
		this.feature = feature;
		this.object = object;
		this.diagnosticSource = diagnosticSource;
		this.diagnosticCode = diagnosticCode;
	}
	

	public void rawError(String message) {
		if (diagnosticChain == null) return;
		
		diagnosticChain.add(new BasicDiagnostic(Diagnostic.ERROR, diagnosticSource,
			diagnosticCode, message, 
			affectedFeatures.isEmpty() 
				? new Object[] { object, feature }
				: new Object[] { object, feature }));
	}

	public void error(String message) {
		if (diagnosticChain == null) return;
		
		// Should we use a label provider to give more details about the error?
		if (!Objects.equals(context.get(ValidationUtil.SHORT_MESSAGE_KEY), true)) {
			SubstitutionLabelProvider labelProvider = getLabelProvider();
			if (labelProvider != null) { 
				message = "The feature '" + labelProvider.getFeatureLabel(feature) + "' of "
					+ "'" + labelProvider.getObjectLabel(object) + "' has an error: " + message;
			}
		}
		
		rawError(message);
	}

	public SubstitutionLabelProvider getLabelProvider() {
		return (SubstitutionLabelProvider) context.get(SubstitutionLabelProvider.class);
	}
	
	public void addAffectedFeature(EStructuralFeature newFeature) {
		affectedFeatures.add(newFeature);
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
