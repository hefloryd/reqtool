package com.rtlabs.reqtool.ui.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.rtlabs.reqtool.ui.editors.HighlighterConverter.HighlightResult;
import com.rtlabs.reqtool.ui.highlighter.GherkinHighlighter;

public class HighlighterTest {
	
	@Test
	public void basicSenario() {
		HighlightResult result = GherkinHighlighter.highlight(
			"Feature: Refund item\n\n" +
			"  Scenario: Jeff returns a faulty microwave\n" +
			"    Given Jeff has bought a microwave for $100\n" +
			"    And he has a receipt\n" +
			"    When he returns the microwave\n" +
			"    Then Jeff should be refunded $100\n");

		assertEquals("<strong>Feature</strong>: Refund item<br/><br/>"
			+ "  <strong>Scenario</strong>: Jeff returns a faulty microwave<br/>"
			+ "    <strong>Given </strong>Jeff has bought a microwave for $100<br/>"
			+ "    <strong>And </strong>he has a receipt<br/>"
			+ "    <strong>When </strong>he returns the microwave<br/>"
			+ "    <strong>Then </strong>Jeff should be refunded $100",
			result.result);
	}
	
	@Test
	public void senarioOutline() {
		HighlightResult result = GherkinHighlighter.highlight(
			"Feature: Refund item\n\n" +
			"Scenario Outline: eating\n" +
			"  Given there are <start> cucumbers\n" +
			"  When I eat <eat> cucumbers\n" +
			"  Then I should have <left> cucumbers\n" +
			"\n" +
			"  Examples:\n" +
			"    Some example.");
		
		assertEquals("<strong>Feature</strong>: Refund item<br/><br/>"
				+ "<strong>Scenario Outline</strong>: eating<br/>"
				+ "  <strong>Given </strong>there are <start> cucumbers<br/>"
				+ "  <strong>When </strong>I eat <eat> cucumbers<br/>"
				+ "  <strong>Then </strong>I should have <left> cucumbers<br/><br/>"
				+ "  <strong>Examples</strong>:<br/>"
				+ "    Some example.",
				result.result);
	}
	
	@Test
	public void error() {
		HighlightResult result = GherkinHighlighter.highlight(
			"Feature: Refund item\n\n" +
			"Scenario Outline: eating\n" +
			"  Given there are <start> cucumbers\n" +
			"  When I eat <eat> cucumbers\n" +
			"  Then I should have <left> cucumbers\n\n" +
			"  UNEXPECTED_TOKEN");
		
		assertEquals("<strong>Feature</strong>: Refund item<br/><br/>"
			+ "<strong>Scenario Outline</strong>: eating<br/>"
			+ "  <strong>Given </strong>there are <start> cucumbers<br/>"
			+ "  <strong>When </strong>I eat <eat> cucumbers<br/>"
			+ "  <strong>Then </strong>I should have <left> cucumbers<br/><br/>"
			+ "  <span style=\"background-color:rgb(255, 128, 128)\"><strong>UNEXPECTED_TOKEN</strong></span>",
			result.result);
		
		assertEquals(1, result.errors.size());
		assertTrue(result.errors.get(0).contains("UNEXPECTED_TOKEN"));
	}

}
