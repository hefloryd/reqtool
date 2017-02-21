package com.rtlabs.reqtool.ui.highlighter;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.rtlabs.reqtool.util.ReqtoolUtil;
import com.rtlabs.reqtool.util.Result;

import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.ParserException.CompositeParserException;
import gherkin.ParserException.UnexpectedTokenException;
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
 * The parser is retrieved from https://github.com/cucumber/cucumber/tree/master/gherkin .
 */
public class GherkinHighlighter {
	
	// Error markings with dotted underline. Does not seem to be working.
	//	private static final String ERROR_START_TAG = "<span style=\"border-bottom: 1px dotted #ff0000\"><span style=\"border-bottom: 1px dotted #ff0000;\">";
	//	private static final String ERROR_END_TAG = "</span></span>";
	
	private static final String ERROR_START_TAG = "<span style=\"background-color:rgb(255, 128, 128)\"><strong>";
	private static final String ERROR_END_TAG = "</strong></span>";
	
	public static class CompileResult extends Result<GherkinDocument> {
		private List<UnexpectedTokenException> exceptions;

		public CompileResult(GherkinDocument result, List<IStatus> statuses, List<UnexpectedTokenException> exceptions) {
			super(result, statuses);
			this.exceptions = exceptions;
		}

		public List<UnexpectedTokenException> getExceptions() {
			return exceptions;
		}
	}
	
	/**
	 * Compiles test into a Gherkin syntax tree. Returns that together with error information.
	 * 
	 * (This does not really belong in the highlighter class since it is also used elsewhere. But it
	 * can stay here for the moment.)
	 */
	public static CompileResult compile(String text) {
		AstBuilder builder = new AstBuilder();
		List<UnexpectedTokenException> unexpectedTokens = Collections.emptyList();
		List<ParserException> errors = Collections.emptyList();
		
		try {
			new Parser<>(builder).parse(text);
		} catch (CompositeParserException exc) {
			errors = exc.errors;
			unexpectedTokens = exc.errors.stream()
				.filter(err -> err instanceof UnexpectedTokenException)
				.map(err -> (UnexpectedTokenException) err)
				.collect(toList());
		} catch (UnexpectedTokenException e) {
			unexpectedTokens = ImmutableList.of(e);
		} catch (ParserException e) {
			errors = ImmutableList.of(e);
		}
		List<IStatus> errorMessages = errors.stream()
			.map(Exception::getMessage)
			.map(ValidationStatus::error)
			.collect(toList());
		
		return new CompileResult(builder.getResult(), errorMessages, unexpectedTokens);
	}
	
	/**
	 * @return the input as highlighted HTML. 
	 */
	public static Result<String> highlight(String rawText) {

		String[] text = ReqtoolUtil.splitLines(rawText);

		CompileResult compileResult = compile(rawText);
		
		// Using builder.getResult() instead of the result of Paser.parse gives
		// a result even if there was an error
		writeMarkup(text, compileResult.getResult());

		// Highlight errors in output
		for (UnexpectedTokenException err : compileResult.getExceptions()) {
			int line = err.location.getLine() - 1;
			Integer indent = err.receivedToken.line.indent();
			text[line] = insertTag(text[line], 
				err.location.getColumn() - 1,
				err.receivedToken.line.getLineText(indent).length(),
				ERROR_START_TAG, ERROR_END_TAG);
		}
		
		return new Result<>(String.join("<br/>", Arrays.asList(text)), compileResult.getStatuses());
	}

	public static String insertTag(String target, int index, int length, String startTag, String endTag) {
		StringBuilder b = new StringBuilder(target.length() + startTag.length() + endTag.length());
		b.append(target, 0, index);
		b.append(startTag);
		b.append(target, index, index + length);
		b.append(endTag);
		b.append(target, index + length, target.length());
		return b.toString();
	}
	
	
	private static void writeMarkup(String[] input, Node node) {
		String highlighted = getHighlighted(node);

		if (highlighted != null) {
			int line = node.getLocation().getLine() - 1;
			
			input[line] = insertTag(input[line], 
				node.getLocation().getColumn() - 1,
				highlighted.length(),
				"<strong>", "</strong>");
		}
		
		for (Node child : getChildren(node)) {
			writeMarkup(input, child);
		}
	}
	
	private static String getHighlighted(Node node) {
		if (node instanceof Feature)            return ((Feature) node).getKeyword();
		if (node instanceof Step)               return ((Step) node).getKeyword();
		if (node instanceof ScenarioDefinition) return ((ScenarioDefinition) node).getKeyword();
		if (node instanceof Examples)           return ((Examples) node).getKeyword();
		if (node instanceof Tag)                return ((Tag) node).getName();
		if (node instanceof GherkinDocument)    return null;
		
		return null;
	}
	
	private static Collection<? extends Node> getChildren(Node node) {
		if (node instanceof Feature) return ((Feature) node).getChildren();
		if (node instanceof Step) return Optional.fromNullable(((Step) node).getArgument()).asSet();
		if (node instanceof GherkinDocument) return Optional.fromNullable(((GherkinDocument) node).getFeature()).asSet();
		
		if (node instanceof ScenarioDefinition) {
			Builder<Node> b = ImmutableList.<Node>builder().addAll(((ScenarioDefinition) node).getSteps());
			if (node instanceof ScenarioOutline) {
				b.addAll(((ScenarioOutline) node).getTags());
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
