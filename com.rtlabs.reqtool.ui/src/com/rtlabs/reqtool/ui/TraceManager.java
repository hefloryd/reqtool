package com.rtlabs.reqtool.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.capra.core.adapters.TraceMetaModelAdapter;
import org.eclipse.capra.core.adapters.TracePersistenceAdapter;
import org.eclipse.capra.core.handlers.ArtifactHandler;
import org.eclipse.capra.core.handlers.PriorityHandler;
import org.eclipse.capra.core.helpers.ExtensionPointHelper;
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

		Collection<ArtifactHandler> artifactHandlers = ExtensionPointHelper.getArtifactHandlers();

		if (data instanceof StructuredSelection) {
			StructuredSelection selection = (StructuredSelection) data;
			List<?> selectionList = selection.toList();
			ArrayList<Object> list = new ArrayList<Object>(selectionList);
			list.add(0, requirement);

			List<EObject> selectionAsEObjects = mapSelectionToEObjects(artifactModel, artifactHandlers, list);

			Collection<EClass> traceTypes = traceAdapter.getAvailableTraceTypes(selectionAsEObjects);
			
			traceTypes.stream().findFirst().ifPresent((EClass chosenType) -> {
				EObject root = traceAdapter.createTrace(chosenType, traceModel, selectionAsEObjects);
				persistenceAdapter.saveTracesAndArtifacts(root, artifactModel);				
			});
		}
	}
	
	private List<EObject> mapSelectionToEObjects(
			EObject artifactModel, 
			Collection<ArtifactHandler> artifactHandlers,
			List<Object> selection) {
		return selection.stream().map(sel -> convertToEObject(sel, artifactHandlers, artifactModel))
				.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
	}

	private Optional<EObject> convertToEObject(Object sel,
			Collection<ArtifactHandler> artifactHandlers, EObject artifactModel) {
		List<ArtifactHandler> availableHandlers = artifactHandlers.stream()
				.filter(handler -> handler.canHandleSelection(sel)).collect(Collectors.toList());
		Optional<PriorityHandler> priorityHandler = ExtensionPointHelper.getPriorityHandler();

		if (availableHandlers.size() == 1) {
			return Optional.of(availableHandlers.get(0).getEObjectForSelection(sel, artifactModel));
		} else if (availableHandlers.size() > 1 && priorityHandler.isPresent()) {
			ArtifactHandler selectedHandler = priorityHandler.get().getSelectedHandler(availableHandlers, sel);
			return Optional.of(selectedHandler.getEObjectForSelection(sel, artifactModel));
		} else {
			System.out.println("There is no handler for " + sel + " so it will be ignored.");
			return Optional.empty();
		}

	}

}
