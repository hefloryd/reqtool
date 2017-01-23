package com.rtlabs.reqtool.ui.editors;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.RequirementType;
import com.rtlabs.reqtool.ui.highlighter.GherkinHighlighter;

/**
 * A display converter which performs Gherkin syntax highlighting.
 */
public class HighlighterConverter implements IDisplayConverter {

	private final IRowDataProvider<Requirement> dataProvider;
	
	public HighlighterConverter(IRowDataProvider<Requirement> dataProvider) {
		this.dataProvider = dataProvider;
	}

	@Override
	public Object canonicalToDisplayValue(ILayerCell cell, IConfigRegistry configRegistry, Object text) {
		// Strip leading empty lines since the HTML renderer seems to crash because of them
		String escapedText = StringEscapeUtils.escapeHtml(StringUtils.stripStart((String) text, "\n\r"));
		
		Requirement showedReq = dataProvider.getRowObject(cell.getRowIndex());
		
		HighlightResult highlightResult = null;
		
		if (showedReq.getType() == RequirementType.GHERKIN) {
			highlightResult = GherkinHighlighter.highlight(escapedText);
		}
		
		if (highlightResult == null) {
			return String.join("<br/>", Arrays.asList(escapedText.split("\\r\\n|\\n\\r|\\n|\\r")));
		} else {
			return highlightResult.result;
		}
	}
	
	public static class HighlightResult {
		public final List<String> errors;
		public final String result;
		
		public HighlightResult(String result, List<String> errors) {
			this.result = result;
			this.errors = errors;
		}
	}

	@Override
	public Object canonicalToDisplayValue(Object text) {
		throw new AssertionError();
	}

	@Override
	public Object displayToCanonicalValue(Object displayValue) {
		// Never use this conveter to convert back to normal text
		throw new UnsupportedOperationException();
	}

	@Override
	public Object displayToCanonicalValue(ILayerCell cell, IConfigRegistry configRegistry, Object displayValue) {
		// Never use this conveter to convert back to normal text
		throw new UnsupportedOperationException();
	}
}
