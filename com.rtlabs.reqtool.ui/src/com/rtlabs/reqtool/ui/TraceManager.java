package com.rtlabs.reqtool.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.capra.core.adapters.TraceMetaModelAdapter;
import org.eclipse.capra.core.adapters.TracePersistenceAdapter;
import org.eclipse.capra.core.helpers.ArtifactHelper;
import org.eclipse.capra.core.helpers.ExtensionPointHelper;
import org.eclipse.capra.core.helpers.TraceHelper;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.StructuredSelection;

import com.rtlabs.reqtool.model.requirements.Requirement;

public class TraceManager {

	public void createTrace(Requirement requirement, Object data) {
		
		TraceMetaModelAdapter traceAdapter = ExtensionPointHelper.getTraceMetamodelAdapter().get();
		TracePersistenceAdapter persistenceAdapter = ExtensionPointHelper.getTracePersistenceAdapter().get();

		EObject traceModel = persistenceAdapter.getTraceModel(requirement);
		EObject artifactModel = persistenceAdapter.getArtifactWrappers(requirement);
		
		TraceHelper traceHelper = new TraceHelper(traceModel);
		ArtifactHelper artifactHelper = new ArtifactHelper(artifactModel);
	
		if (data instanceof StructuredSelection) {
			StructuredSelection selection = (StructuredSelection) data;
			List<?> selectionList = selection.toList();
			ArrayList<Object> list = new ArrayList<Object>(selectionList);
			list.add(0, requirement);

			List<EObject> wrappers = artifactHelper.createWrappers(list);

			Collection<EClass> traceTypes = traceAdapter.getAvailableTraceTypes(wrappers);

			traceTypes.stream().findFirst().ifPresent((EClass chosenType) -> {
				traceHelper.createTrace(wrappers, chosenType);
				traceHelper.annotateTrace(wrappers);
				persistenceAdapter.saveTracesAndArtifacts(traceModel, artifactModel);				
			});
			
		}
	}
	
}
