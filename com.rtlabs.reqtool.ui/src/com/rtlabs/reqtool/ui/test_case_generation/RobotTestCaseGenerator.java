package com.rtlabs.reqtool.ui.test_case_generation;

import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.ui.highlighter.GherkinHighlighter;
import com.rtlabs.reqtool.ui.highlighter.GherkinHighlighter.CompileResult;
import com.rtlabs.reqtool.util.Result;

/**
 * Facade for the test case generator.
 */
public class RobotTestCaseGenerator {
	public static Result<String> generate(Requirement req) {
		CompileResult compileResult = GherkinHighlighter.compile(req.getBody());

		if (compileResult.isNoErrors()) {
			String generatedTest = new RobotTestCaseGeneratorTemplate().generate(
				new RobotTestCaseGeneratorHelper(compileResult.getResult()));
			return new Result<>(generatedTest, compileResult.getStatuses());
		} else {
			return new Result<>(null, compileResult.getStatuses());
		}
	}
}
