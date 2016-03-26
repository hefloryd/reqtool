package com.rtlabs.reqtool.model.fixture;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.app4mc.capra.generic.artifacts.ArtifactWrapper;
import org.eclipse.app4mc.capra.generic.artifacts.ArtifactsFactory;
import org.eclipse.app4mc.capra.simpletrace.tracemetamodel.EObjectToEObject;
import org.eclipse.app4mc.capra.simpletrace.tracemetamodel.TraceElement;
import org.eclipse.app4mc.capra.simpletrace.tracemetamodel.TracemetamodelFactory;
import org.eclipse.emf.ecore.EObject;

import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.RequirementsFactory;
import com.rtlabs.reqtool.model.requirements.Specification;

public class RequirementService {

	private static RequirementService INSTANCE;
	private Specification specification;
	List<Requirement> requirements = new ArrayList<Requirement>();
	List<ArtifactWrapper> wrappers = new ArrayList<ArtifactWrapper>();
	List<TraceElement> traces = new ArrayList<TraceElement>();
	
	public static RequirementService getInstance() {
		if (INSTANCE == null)
			INSTANCE = new RequirementService();

		return INSTANCE;
	}

	private RequirementService() {
	}

	public List<Requirement> createRequirements() {
		Requirement req;
				
		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(1);
		req.setBody ("The motor shall contain an object database");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(2);
		req.setBody ("Objects in the object database shall have the following attributes:\n * name [unique identifier]\n * Datatype\n * value ");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(3);
		req.setBody ("The object database shall support the following datatypes:\n * BOOLEAN\n * FLOAT\n * UINT32\n * STRING\n");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(4);
		req.setBody ("It shall be possible to set object values via a REST API");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(5);
		req.setBody ("It shall be possible to get object values via a REST API");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(6);
		req.setBody ("It shall be possible to set object values via a websocket.");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(7);
		req.setBody ("It shall be possible to get object values via a websocket.");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(8);
		req.setBody ("The REST api shall be accessible at http://<motor>/api/params");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(9);
		req.setBody ("The REST query for setting values shall be ?name=<objectname>&value=<objectvalue> with HTTP method PUT\n");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier (10);
		req.setBody ("The REST query for getting values shall be ?name=<objectname> with HTTP method GET\n");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier (11);
		req.setBody ("The REST query response when getting a value shall be a JSON message of the form {\"name\":\"<objectname>\",\"value\":<objectvalue>}.");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier (12);
		req.setBody ("The websocket request for setting an object value shall be a JSON message of the format {\"op\":\"set\",\"name\":\"<objectname>\",\"value\":<objectvalue>}\n");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier (13);
		req.setBody ("The websocket request for getting an object value shall be a JSON message of the format {\"op\":\"get\",\"name\":\"<objectname>\"}");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier (14);
		req.setBody ("The websocket response for getting an object value shall be a JSON message of the format {\"name\":\"<objectname>\",\"value\":<objectvalue>}");
		requirements.add(req);
		
		return requirements;
	}

	public List<TraceElement> createTraces() {
		EObjectToEObject trace;
		EObject[][] _traces = {
				{ requirements.get(0), requirements.get(1) },
				{ requirements.get(0), requirements.get(2) },
				{ requirements.get(0), requirements.get(3) },
				{ requirements.get(0), requirements.get(4) },				
				{ requirements.get(0), requirements.get(5) },		
				{ requirements.get(0), requirements.get(6) },
				
				{ requirements.get(3), requirements.get(7) },
				{ requirements.get(3), requirements.get(8) },
				
				{ requirements.get(4), requirements.get(7) },
				{ requirements.get(4), requirements.get(9) },
				{ requirements.get(4), requirements.get(10) },
				
				{ requirements.get(5), requirements.get(11) },
				
				{ requirements.get(6), requirements.get(12) },			
				{ requirements.get(6), requirements.get(13) },			

				{ requirements.get(2), wrappers.get(0) },			
				{ requirements.get(0), wrappers.get(1) },			

		};
		
		for (int i = 0; i < _traces.length; i++) {
			trace = TracemetamodelFactory.eINSTANCE.createEObjectToEObject();
			trace.setSource(_traces[i][0]);
			trace.setTarget(_traces[i][1]);
			
			if (trace.getSource() instanceof Requirement) {
				Requirement requirement = (Requirement) trace.getSource();
				requirement.setChildren(requirement.getChildren() + 1);
			}
			if (trace.getTarget() instanceof Requirement) {
				Requirement requirement = (Requirement) trace.getTarget();
				requirement.setParents(requirement.getParents() + 1);
			}
			traces.add(trace);			
		}
		
		return traces;
	}

	public List<ArtifactWrapper> createArtifactWrappers() {
		ArtifactWrapper wrapper;
		
		wrapper = ArtifactsFactory.eINSTANCE.createArtifactWrapper();
		wrapper.setUri("=motor/<src<modules{rest.c");
		wrapper.setName("rest.c");
		wrapper.setArtifactHandler("handlers.CDTHandler");
		wrappers.add(wrapper);
		
		wrapper = ArtifactsFactory.eINSTANCE.createArtifactWrapper();
		wrapper.setUri("http://unjo.rt-labs.intra/collab/ticket/60");
		wrapper.setName("Object dictionary");
		wrapper.setArtifactHandler("handlers.MylynHandler");
		wrappers.add(wrapper);

		return wrappers;
	}

	public Specification getSpecification() {
		if (specification == null) {
			specification = RequirementsFactory.eINSTANCE.createSpecification();
			specification.getRequirements().addAll(createRequirements());
			specification.setNextIdentifier(specification.getRequirements().size() + 1);
			
			specification.setArtifactWrapperContainer(ArtifactsFactory.eINSTANCE.createArtifactWrapperContainer());
			specification.getArtifactWrapperContainer().getArtifacts().addAll(createArtifactWrappers());			
			
			specification.setTraceModel(TracemetamodelFactory.eINSTANCE.createSimpleTraceModel());
			specification.getTraceModel().getTraces().addAll(createTraces());						
		}
		return specification;
	}
	
//	public Requirement getRequirementsByName(String name) {
//		for (Requirement r : requirements) {
//			if (r.getName().equals(name))
//				return r;
//		}
//		return null;
//	}
}
