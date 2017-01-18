package com.rtlabs.reqtool.ui.views;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.rtlabs.reqtool.ui.highlighter.GherkinHighlighter;

public class HighlighterTest {
	
	private static final String test1 = 
		"Feature: Refund item\n" +
		"\n" +
		"  Scenario: Jeff returns a faulty microwave\n" +
		"    Given Jeff has bought a microwave for $100\n" +
		"    And he has a receipt\n" +
		"    When he returns the microwave\n" +
		"    Then Jeff should be refunded $100\n";

	private static final String test2 = 
		"Feature: Refund item\n" +
	    "\n" +
	    "Scenario Outline: eating\n" +
	    "  Given there are <start> cucumbers\n" +
	    "  When I eat <eat> cucumbers\n" +
	    "  Then I should have <left> cucumbers\n" +
	    "\n" +
	    "  Examples:\n" +
	    "    Some example.";

	
	@Test
	public void basicSenario() {
		String highlighted = GherkinHighlighter.highlight(test1);
		assertEquals(
			"<strong>Feature</strong>: Refund item<br/><br/>"
			+ "  <strong>Scenario</strong>: Jeff returns a faulty microwave<br/>"
			+ "    <strong>Given </strong>Jeff has bought a microwave for $100<br/>"
			+ "    <strong>And </strong>he has a receipt<br/>"
			+ "    <strong>When </strong>he returns the microwave<br/>"
			+ "    <strong>Then </strong>Jeff should be refunded $100",
			highlighted);
	}
	
	@Test
	public void senarioOutline() {
		String highlighted = GherkinHighlighter.highlight(test2);
		assertEquals("<strong>Feature</strong>: Refund item<br/><br/>"
				+ "<strong>Scenario Outline</strong>: eating<br/>"
				+ "  <strong>Given </strong>there are <start> cucumbers<br/>"
				+ "  <strong>When </strong>I eat <eat> cucumbers<br/>"
				+ "  <strong>Then </strong>I should have <left> cucumbers<br/><br/>"
				+ "  <strong>Examples</strong>:<br/>"
				+ "    Some example.",
				highlighted);
	}
}
