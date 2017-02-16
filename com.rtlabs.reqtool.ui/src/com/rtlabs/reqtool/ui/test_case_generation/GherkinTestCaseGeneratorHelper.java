package com.rtlabs.reqtool.ui.test_case_generation;

import java.util.List;

import org.apache.commons.lang.WordUtils;

import com.rtlabs.reqtool.util.ReqtoolUtil;

import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioDefinition;

/**
 * This class is called from the code generation class. It contains all logic for the code generation.
 */
public class GherkinTestCaseGeneratorHelper {

	private final GherkinDocument document;
	
	public GherkinTestCaseGeneratorHelper(GherkinDocument document) {
		this.document = document;
	}

	public String testCaseName() {
		return document.getFeature().getName();
	}

	public String[] testCaseDescription() {
		return ReqtoolUtil.splitLines(document.getFeature().getDescription());
	}

	public String libraryName() {
		// Convert to camel case
		return WordUtils.capitalizeFully(document.getFeature().getName()).replaceAll("\\s", "") + "Library.robot";
	}
	
	public List<ScenarioDefinition> senarios() {
		return document.getFeature().getChildren();
	}
}



