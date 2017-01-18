package com.rtlabs.reqtool.ui.editors;

import java.util.Arrays;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;

import com.rtlabs.reqtool.ui.highlighter.GherkinHighlighter;

/**
 * A display converter which performs Gherkin syntax highlighting.
 */
public class GherkinHighlighterConverter extends DisplayConverter {

	@Override
	public Object canonicalToDisplayValue(Object text) {
		// TODO: Somehow get hold of the requirement object and check to type property to choose
		// the right highlighting mode.

		// Strip leading empty lines since HTML renderer seems to crash from them.
		String escapedText = StringEscapeUtils.escapeHtml(StringUtils.stripStart((String) text, "\n\r"));
		
		String highlightedText = GherkinHighlighter.highlight(escapedText);
		
		if (highlightedText == null) {
			// Return origianl text if there was an error
			return String.join("<br/>", Arrays.asList(escapedText.split("\\r\\n|\\n\\r|\\n|\\r")));
		} else {
			return highlightedText;
		}
	}
	

	@Override
	public Object displayToCanonicalValue(Object displayValue) {
		// Never use this conveter to convert back to normal text
		throw new UnsupportedOperationException();
	}
}
