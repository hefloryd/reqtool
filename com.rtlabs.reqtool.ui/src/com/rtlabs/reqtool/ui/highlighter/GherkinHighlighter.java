package com.rtlabs.reqtool.ui.highlighter;

import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.ast.Examples;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.Node;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.Step;
import gherkin.ast.Tag;

/**
 * Performs syntax highlighting for the Gherkin language. Uses the Gherkin project parser.
 * 
 * The parser is retreived from https://github.com/cucumber/cucumber/tree/master/gherkin .
 */
public class GherkinHighlighter {
	
//	markupConverter.registerMarkup("Feature", 
//	"<span style=\"background-color:rgb(255, 0, 0)\"><strong><s><u>", "</u></s></strong></span>");

	/**
	 * @return the input as highlighted HTML. 
	 */
	public static String highlight(String text) {
		// 
		AstBuilder builder = new AstBuilder();

		try {
			new Parser<>(builder).parse(text);
		} catch (ParserException e) {
			return null;
		}
		
		// This gives a result even if there was an error
		return highlight(text.split("\\r\\n|\\n\\r|\\n|\\r"), builder.getResult());
	}

	// This class is used because we need to update the location from within a
	// recursive method
	private static class InputLocation {
		int row;
		int column;

		public InputLocation(int row, int column) {
			this.row = row;
			this.column = column;
		}
	}

	private static String highlight(String[] text, Node node) {
		StringBuilder output = new StringBuilder();
		InputLocation loc = new InputLocation(0, 0);

		writeMarkup(text, loc, output, node);
		
		// Copy text after last highlighted word
		output.append(text[loc.row], loc.column, text[loc.row].length());
		loc.row++;
		for (; loc.row < text.length; loc.row++) {
			output.append("<br/>").append(text[loc.row]);
		}
		
		return output.toString();
	}
	
	private static void writeMarkup(String[] text, InputLocation loc, StringBuilder output, Node node) {
	
		String highlighted = getHighlighted(node);
			
		if (highlighted != null) {
			// Copy end of row, and rows without keywords, to output
			for (; loc.row < node.getLocation().getLine() - 1; loc.row++) {
				output.append(text[loc.row], loc.column, text[loc.row].length());
				output.append("<br/>");
				loc.column = 0;
			}
			
			// Copy start of row before keywork to output
			int endColumn = node.getLocation().getColumn() - 1;
			output.append(text[loc.row], loc.column, endColumn);
			loc.column += endColumn - loc.column;
			
			// Highlight and update column
			output.append("<strong>");
			output.append(highlighted);
			output.append("</strong>");
			
			loc.column += highlighted.length();
		}
		
		for (Node child : getChildren(node)) {
			writeMarkup(text, loc, output, child);
		}
	}

	private static String getHighlighted(Node node) {
		if (node instanceof Feature) return ((Feature) node).getKeyword();
		if (node instanceof Step) return ((Step) node).getKeyword();
		if (node instanceof ScenarioDefinition) return ((ScenarioDefinition) node).getKeyword();
		if (node instanceof Examples) {
			return ((Examples) node).getKeyword();
		}
		if (node instanceof Tag) return ((Tag) node).getName();
		if (node instanceof GherkinDocument) return null;
		
		return null;
	}
	
	private static Collection<? extends Node> getChildren(Node node) {
		if (node instanceof Feature) return ((Feature) node).getChildren();
		if (node instanceof Step) return Optional.fromNullable(((Step) node).getArgument()).asSet();
		if (node instanceof GherkinDocument) return ImmutableList.of(((GherkinDocument) node).getFeature());
		
		if (node instanceof ScenarioDefinition) {
			Builder<Node> b = ImmutableList.<Node>builder().addAll(((ScenarioDefinition) node).getSteps());
			if (node instanceof ScenarioOutline) {
				b.addAll(((ScenarioOutline) node).getExamples());
			}
			return b.build();
		}
		
		if (node instanceof Examples) {
			return ImmutableList.<Node>builder()
				.addAll(Optional.fromNullable(((Examples) node).getTableBody()).or(Collections.emptyList()))
				.addAll(((Examples) node).getTags()).build();
		}
		
		return ImmutableList.of();
	}

}
