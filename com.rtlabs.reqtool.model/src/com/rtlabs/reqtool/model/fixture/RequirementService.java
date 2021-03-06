package com.rtlabs.reqtool.model.fixture;

import java.util.ArrayList;
import java.util.List;

import com.rtlabs.reqtool.model.requirements.Artifact;
import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.RequirementType;
import com.rtlabs.reqtool.model.requirements.RequirementsFactory;
import com.rtlabs.reqtool.model.requirements.Specification;
import com.rtlabs.reqtool.model.requirements.Traceable;

/**
 * Singleton which creates and stores {@link Requirement} test objects.
 */
public class RequirementService {

	private static RequirementService INSTANCE;
	private Specification specification;
	List<Requirement> requirements = new ArrayList<Requirement>();
	List<Artifact> wrappers = new ArrayList<Artifact>();
	//List<TraceElement> traces = new ArrayList<TraceElement>();
	
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
		req.setType(RequirementType.USER_STORY);
		req.setBody ("As a motor I want to contain an object database so that my objects are databased.");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(3);
		req.setType(RequirementType.GHERKIN);
		req.setBody (
			"Feature: Refund item\n"
			+ "Description, description, description, description, description.\n"
			+ "Scenario: eating\n"
			+ "  Given there are <start> cucumbers\n"
			+ "  When I eat <eat> cucumbers\n"
			+ "  Then I should have <left> cucumbers\n"
			+ "Scenario: drinking\n"
			+ "  Given there are <start> cucumbers\n"
			+ "  When I drink <eat> cucumbers\n"
			+ "  Then I should have <left> cucumbers\n");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(4);
		req.setBody ("Objects in the object database shall have the following attributes:\n * name [unique identifier]\n * Datatype\n * value ");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(5);
		req.setBody ("The object database shall support the following datatypes:\n * BOOLEAN\n * FLOAT\n * UINT32\n * STRING\n");
		requirements.add(req);


		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(6);
		req.setBody ("It shall be possible to set object values via a REST API");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(7);
		req.setBody ("It shall be possible to get object values via a REST API");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(8);
		req.setBody ("It shall be possible to set object values via a websocket.");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(9);
		req.setBody ("It shall be possible to get object values via a websocket.");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(10);
		req.setBody ("The REST api shall be accessible at http://<motor>/api/params");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(11);
		req.setBody ("The REST query for setting values shall be ?name=<objectname>&value=<objectvalue> with HTTP method PUT\n");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier (12);
		req.setBody ("The REST query for getting values shall be ?name=<objectname> with HTTP method GET\n");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier (13);
		req.setBody ("The REST query response when getting a value shall be a JSON message of the form {\"name\":\"<objectname>\",\"value\":<objectvalue>}.");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier (14);
		req.setBody ("The websocket request for setting an object value shall be a JSON message of the format {\"op\":\"set\",\"name\":\"<objectname>\",\"value\":<objectvalue>}\n");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier (15);
		req.setBody ("The websocket request for getting an object value shall be a JSON message of the format {\"op\":\"get\",\"name\":\"<objectname>\"}");
		requirements.add(req);

		req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier (16);
		req.setBody ("The websocket response for getting an object value shall be a JSON message of the format {\"name\":\"<objectname>\",\"value\":<objectvalue>}");
		requirements.add(req);
		
		return requirements;
	}

	public void createTraces() {
		Traceable[][] traces = {
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

		for (int i = 0; i < traces.length; i++) {
			Traceable parent = traces[i][0];
			Traceable child = traces[i][1];
			parent.getChildren().add(child);
			child.getParents().add(parent);
		}
	}

	public List<Artifact> createArtifactWrappers() {
		Artifact wrapper;
		
		wrapper = RequirementsFactory.eINSTANCE.createArtifact();
		wrapper.setUri("=motor/<src<modules{rest.c");
		wrapper.setName("rest.c");
		wrapper.setArtifactHandler("handlers.CDTHandler");
		wrappers.add(wrapper);
		
		wrapper = RequirementsFactory.eINSTANCE.createArtifact();
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
			specification.setTitle("Some Embedded System Specification");
			specification.setDescription("Test description. At vero eos et accusamus et iusto odio dignissimos ducimus, qui blanditiis praesentium voluptatum deleniti atque corrupti, quos dolores et quas molestias excepturi sint, obcaecati cupiditate non provident, similique sunt in culpa, qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio, cumque nihil impedit, quo minus id, quod maxime placeat, facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet.");
			
			specification.setArtifactContainer(RequirementsFactory.eINSTANCE.createArtifactContainer());
			specification.getArtifactContainer().getArtifacts().addAll(createArtifactWrappers());			

			createTraces();
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
