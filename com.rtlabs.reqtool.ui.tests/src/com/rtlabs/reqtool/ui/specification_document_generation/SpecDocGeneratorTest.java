package com.rtlabs.reqtool.ui.specification_document_generation;

import java.time.LocalDate;

import org.junit.Test;

import com.rtlabs.reqtool.model.requirements.Person;
import com.rtlabs.reqtool.model.requirements.Priority;
import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.RequirementType;
import com.rtlabs.reqtool.model.requirements.RequirementsFactory;
import com.rtlabs.reqtool.model.requirements.Specification;
import com.rtlabs.reqtool.test.util.TestUtil;
import com.rtlabs.reqtool.ui.Activator;

public class SpecDocGeneratorTest {

	@Test
	public void test() {
		Specification spec = RequirementsFactory.eINSTANCE.createSpecification();
		spec.setTitle("Test Title");
		spec.setDescription("Test description. At vero eos et accusamus et iusto odio dignissimos ducimus, qui blanditiis praesentium voluptatum deleniti atque corrupti, quos dolores et quas molestias excepturi sint, obcaecati cupiditate non provident, similique sunt in culpa, qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio, cumque nihil impedit, quo minus id, quod maxime placeat, facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet.");
		
		Person person = RequirementsFactory.eINSTANCE.createPerson();
		person.setFirstName("First");
		person.setLastName("Last");
		
		Requirement parent = RequirementsFactory.eINSTANCE.createRequirement();
		parent.setIdentifier(3);
		parent.setType(RequirementType.GHERKIN);
		parent.setBody("Test body.");
		parent.setModified(LocalDate.of(2011, 2, 21));
		parent.setCreated(LocalDate.of(2010, 12, 1));
		parent.setPriority(Priority.HIGH);
		parent.setOwner(person);
		spec.getRequirements().add(parent);

		Requirement child = RequirementsFactory.eINSTANCE.createRequirement();
		child.setIdentifier(4);
		child.setType(RequirementType.FREE_STYLE);
		child.setBody("Another test body that is much longer. At vero eos et accusamus et iusto odio dignissimos ducimus, qui blanditiis praesentium voluptatum deleniti atque corrupti, quos dolores et quas molestias excepturi sint, obcaecati cupiditate non provident, similique sunt in culpa, qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio, cumque nihil impedit, quo minus id, quod maxime placeat, facere possimus, omnis voluptas assumenda est, omnis dolor repellendus.");
		child.setModified(LocalDate.of(2010, 2, 21));
		child.setCreated(LocalDate.of(2009, 12, 1));
		child.setPriority(Priority.LOW);
		child.setOwner(person);
		spec.getRequirements().add(child);

		parent.getChildren().add(child);
		child.getParents().add(parent);

		String output = SpecDocGenerator.run(spec, spec.getRequirements(), Activator.createStandardAdaperFactory());

		// Just sample some of the output without so much though...
		TestUtil.assertContains("Created", output, "\\*\\*Created:\\*\\* 2010-12-01");
		TestUtil.assertContains("Table heading", output, 
			"| *Name *| *Body *| *Priority *| *State *| *Owner *| *Created *|",
			"| *R3 *| Test body\\. *| *HIGH *| *NEW *| *| *2010-12-01 *|");
		TestUtil.assertContains("Children", output, "\\*\\*Children:\\*\\* .* R4");
	}
}
