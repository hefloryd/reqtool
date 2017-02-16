package com.rtlabs.reqtool.ui.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.rtlabs.reqtool.ui.highlighter.GherkinHighlighter;
import com.rtlabs.reqtool.ui.highlighter.UserStoryHighlighter;
import com.rtlabs.reqtool.util.Result;

public class HighlighterTest {
	@Test
	public void gherkinSenario() {
		Result<String> result = GherkinHighlighter.highlight(
			"Feature: Refund item\n"
			+ "\n"
			+ "  Scenario: Jeff returns a faulty microwave\n"
			+ "    Given Jeff has bought a microwave for $100\n"
			+ "    And he has a receipt\n"
			+ "    When he returns the microwave\n"
			+ "    Then Jeff should be refunded $100\n");

		assertEquals("<strong>Feature</strong>: Refund item<br/>"
			+ "<br/>"
			+ "  <strong>Scenario</strong>: Jeff returns a faulty microwave<br/>"
			+ "    <strong>Given </strong>Jeff has bought a microwave for $100<br/>"
			+ "    <strong>And </strong>he has a receipt<br/>"
			+ "    <strong>When </strong>he returns the microwave<br/>"
			+ "    <strong>Then </strong>Jeff should be refunded $100",
			result.getResult());
	}
	
	@Test
	public void gherkinSenarioOutline() {
		Result<String> result = GherkinHighlighter.highlight(
			"Feature: Refund item\n"
			+ "\n"
			+ "Scenario Outline: eating\n"
			+ "  Given there are <start> cucumbers\n"
			+ "  When I eat <eat> cucumbers\n"
			+ "  Then I should have <left> cucumbers\n"
			+ "\n"
			+ "  Examples:\n"
			+ "    Some example.");
		
		assertEquals("<strong>Feature</strong>: Refund item<br/>"
			+ "<br/>"
			+ "<strong>Scenario Outline</strong>: eating<br/>"
			+ "  <strong>Given </strong>there are <start> cucumbers<br/>"
			+ "  <strong>When </strong>I eat <eat> cucumbers<br/>"
			+ "  <strong>Then </strong>I should have <left> cucumbers<br/>"
			+ "<br/>"
			+ "  <strong>Examples</strong>:<br/>"
			+ "    Some example.",
			result.getResult());
	}
	
	@Test
	public void gherkinError() {
		Result<String> result = GherkinHighlighter.highlight(
			"Feature: Refund item\n"
			+ "\n"
			+ "Scenario Outline: eating\n"
			+ "  Given there are <start> cucumbers\n"
			+ "  When I eat <eat> cucumbers\n"
			+ "  Then I should have <left> cucumbers\n"
			+ "\n"
			+ "  UNEXPECTED_TOKEN");
		
		assertEquals("<strong>Feature</strong>: Refund item<br/>"
			+ "<br/>"
			+ "<strong>Scenario Outline</strong>: eating<br/>"
			+ "  <strong>Given </strong>there are <start> cucumbers<br/>"
			+ "  <strong>When </strong>I eat <eat> cucumbers<br/>"
			+ "  <strong>Then </strong>I should have <left> cucumbers<br/>"
			+ "<br/>"
			+ "  <span style=\"background-color:rgb(255, 128, 128)\"><strong>UNEXPECTED_TOKEN</strong></span>",
			result.getResult());
		
		assertEquals(1, result.getStatuses().size());
		assertTrue(result.getStatuses().get(0).getMessage().contains("UNEXPECTED_TOKEN"));
	}
	
	@Test
	public void userStory() {
		Result<String> result = UserStoryHighlighter.highlight(
			"As a user I want to fnork so that the biggins stay happy.");
		
		assertEquals("<strong>As a</strong> user <strong>I want to</strong> fnork "
			+ "<strong>so that</strong> the biggins stay happy.",
			result.getResult());
	}
	
	@Test
	public void userStoryNoAsA() {
		String text = "User want to fnork so that the biggins stay happy.";
		Result<String> result = UserStoryHighlighter.highlight(text);
		
		assertEquals(text, result.getResult());
		assertEquals(1, result.getStatuses().size());
	}

	@Test
	public void userStoryNoIwant() {
		Result<String> result = UserStoryHighlighter.highlight("As a user I fnork so that the biggins stay happy.");
		
		assertEquals("<strong>As a</strong> user I fnork so that the biggins stay happy.", result.getResult());
		assertEquals(1, result.getStatuses().size());
	}
}
