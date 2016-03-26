package com.rtlabs.reqtool.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.app4mc.capra.generic.adapters.TraceMetamodelAdapter;
import org.eclipse.app4mc.capra.generic.adapters.TracePersistenceAdapter;
import org.eclipse.app4mc.capra.generic.artifacts.ArtifactWrapperContainer;
import org.eclipse.app4mc.capra.generic.handlers.ArtifactHandler;
import org.eclipse.app4mc.capra.generic.handlers.PriorityHandler;
import org.eclipse.app4mc.capra.generic.helpers.ExtensionPointHelper;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.StructuredSelection;

import com.rtlabs.reqtool.model.requirements.Requirement;

public class TraceManager {

	public void createTrace(Requirement requirement, Object data) {
		
		TraceMetamodelAdapter traceAdapter = ExtensionPointHelper.getTraceMetamodelAdapter().get();
		TracePersistenceAdapter persistenceAdapter = ExtensionPointHelper.getTracePersistenceAdapter().get();

		Optional<EObject> traceModel = persistenceAdapter.getTraceModel(requirement);
		Optional<ArtifactWrapperContainer> existingArtifactWrappers = persistenceAdapter.getArtifactWrappers(requirement);

		Collection<ArtifactHandler> artifactHandlers = ExtensionPointHelper.getArtifactHandlers();

		if (data instanceof StructuredSelection) {
			StructuredSelection selection = (StructuredSelection) data;
			List<?> selectionList = selection.toList();
			ArrayList<Object> list = new ArrayList<Object>(selectionList);
			list.add(0, requirement);

			List<EObject> selectionAsEObjects = mapSelectionToEObjects(existingArtifactWrappers, artifactHandlers, list);

			Collection<EClass> traceTypes = traceAdapter.getAvailableTraceTypes(selectionAsEObjects);
			Optional<EClass> chosenType = traceTypes.stream().findFirst();

			if (chosenType.isPresent()) {
				EObject root = traceAdapter.createTrace(chosenType.get(), traceModel, selectionAsEObjects);
				persistenceAdapter.saveTracesAndArtifactWrappers(root, selectionAsEObjects, existingArtifactWrappers);
				
				requirement.setChildren(requirement.getChildren() + selectionList.size());
				for (Object o : selectionList) {
					if (o instanceof Requirement) {
						Requirement r = (Requirement) o;
						r.setParents(r.getParents() + 1);						
					}
					
				}
			}
	
		}
	}
	
	private List<EObject> mapSelectionToEObjects(
			Optional<ArtifactWrapperContainer> existingArtifactWrappers, 
			Collection<ArtifactHandler> artifactHandlers,
			List<Object> selection) {
		return selection.stream().map(sel -> convertToEObject(sel, artifactHandlers, existingArtifactWrappers))
				.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
	}

	private Optional<EObject> convertToEObject(Object sel,
			Collection<ArtifactHandler> artifactHandlers, Optional<ArtifactWrapperContainer> existingArtifactWrappers) {
		List<ArtifactHandler> availableHandlers = artifactHandlers.stream()
				.filter(handler -> handler.canHandleSelection(sel)).collect(Collectors.toList());
		Optional<PriorityHandler> priorityHandler = ExtensionPointHelper.getPriorityHandler();

		if (availableHandlers.size() == 1) {
			return Optional.of(availableHandlers.get(0).getEObjectForSelection(sel, existingArtifactWrappers));
		} else if (availableHandlers.size() > 1 && priorityHandler.isPresent()) {
			ArtifactHandler selectedHandler = priorityHandler.get().getSelectedHandler(availableHandlers, sel);
			return Optional.of(selectedHandler.getEObjectForSelection(sel, existingArtifactWrappers));
		} else {
			System.out.println("There is no handler for " + sel + " so it will be ignored.");
			return Optional.empty();
		}

	}

}
