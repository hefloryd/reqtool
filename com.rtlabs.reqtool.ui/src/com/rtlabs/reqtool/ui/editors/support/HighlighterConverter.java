package com.rtlabs.reqtool.ui.editors.support;

import java.util.Arrays;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.RequirementType;
import com.rtlabs.reqtool.ui.highlighter.GherkinHighlighter;
import com.rtlabs.reqtool.ui.highlighter.UserStoryHighlighter;
import com.rtlabs.reqtool.util.ReqtoolUtil;
import com.rtlabs.reqtool.util.Result;

/**
 * A display converter for a NatTable which performs Gherkin syntax highlighting.
 */
public class HighlighterConverter implements IDisplayConverter {

	private final IRowDataProvider<Requirement> dataProvider;
	
	public HighlighterConverter(IRowDataProvider<Requirement> dataProvider) {
		this.dataProvider = dataProvider;
	}

	@Override
	public Object canonicalToDisplayValue(ILayerCell cell, IConfigRegistry configRegistry, Object text) {
		if (text == null) return null;
		
		// Strip leading empty lines since the HTML renderer seems to crash because of them
		String escapedText = StringEscapeUtils.escapeHtml(StringUtils.stripStart((String) text, "\n\r"));
		
		Requirement showedReq = dataProvider.getRowObject(cell.getRowIndex());
		
		Result<String> highlightResult = null;
		
		if (showedReq.getType() == RequirementType.GHERKIN) {
			highlightResult = GherkinHighlighter.highlight(escapedText);
		} else if (showedReq.getType() == RequirementType.USER_STORY) {
			highlightResult = UserStoryHighlighter.highlight(escapedText);
		}
		
		if (highlightResult == null) {
			return String.join("<br/>", Arrays.asList(ReqtoolUtil.splitLines(escapedText)));
		} else {
			return highlightResult.getResult();
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
