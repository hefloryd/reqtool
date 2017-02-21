package com.rtlabs.reqtool.ui.test_case_generation;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.RequirementType;
import com.rtlabs.reqtool.model.requirements.RequirementsFactory;
import com.rtlabs.reqtool.test.util.TestUtil;
import com.rtlabs.reqtool.util.Result;

public class TestCaseGenerationTest {

	@Test
	public void test() {
		Requirement req = RequirementsFactory.eINSTANCE.createRequirement();
		req.setIdentifier(3);
		req.setType(RequirementType.GHERKIN);
		req.setBody (
			"Feature: Refund item\n"
			+ "Description, description, description, description, description.\n"
			+ "\n"
			+ "Scenario Outline: eating\n"
			+ "  Given there are <start> cucumbers\n"
			+ "  When I eat <eat> cucumbers\n"
			+ "  Then I should have <left> cucumbers\n"
			+ "  Examples:\n"
			+ "    | start | eat | left |\n"
			+ "    |  12   |  5  |  7   |\n"
			+ "    |  20   |  5  |  15  |\n"
			+ "\n"
			+ "Scenario: drinking\n"
			+ "  Given there are <start> cucumbers\n"
			+ "  When I drink <eat> cucumbers\n"
			+ "  Then I should have <left> cucumbers\n");

		Result<String> result = RobotTestCaseGenerator.generate(req);
		
		assertTrue(result.isNoWarnings());
		assertTrue(result.getResult().contains("There are ${start} cucumbers"));
		TestUtil.assertContains("Comments exist", result.getResult(), 
			"# TODO: Generated keyword, to be implemented",
			"# TODO: Generated keyword, to be implemented",
			"# TODO: Generated keyword, to be implemented");
		
		TestUtil.assertContains("Drinking scenario exists", result.getResult(), "drinking\\s+Given there are");
		TestUtil.assertContains("Right keywords generated", result.getResult(), 
			"There are", "I eat", "I should have", "I drink");
	}
}
