package com.rtlabs.reqtool.ui.editors;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.validate.DataValidator;
import org.eclipse.nebula.widgets.nattable.data.validate.ValidationFailedException;

import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.RequirementType;
import com.rtlabs.reqtool.ui.editors.HighlighterConverter.HighlightResult;
import com.rtlabs.reqtool.ui.highlighter.GherkinHighlighter;
import com.rtlabs.reqtool.ui.highlighter.UserStoryHighlighter;

/**
 * Validates the body of a requirment according to the {@link RequirementType} of that requirement.
 */
public class RequirementTypeValidator extends DataValidator {

	private IRowDataProvider<Requirement> dataProvider;

	public RequirementTypeValidator(IRowDataProvider<Requirement> dataProvider) {
		this.dataProvider = dataProvider;
	}

	@Override
	public boolean validate(int columnIndex, int rowIndex, Object newValue) {
		
		if (newValue == null) return true;

		Requirement showedReq = dataProvider.getRowObject(rowIndex);
		
		HighlightResult highlightResult = null;
		
		if (showedReq.getType() == RequirementType.GHERKIN) {
			highlightResult = GherkinHighlighter.highlight((String) newValue);
		} else if (showedReq.getType() == RequirementType.USER_STORY) {
			highlightResult = UserStoryHighlighter.highlight((String) newValue);
		}
		
		if (highlightResult == null) {
			return true;
		}

		if (!highlightResult.errors.isEmpty()) {
			throw new ValidationFailedException("There were validation messages:\n"
				+ String.join("\n", highlightResult.errors));
		}
		
		return true;
	}
}

