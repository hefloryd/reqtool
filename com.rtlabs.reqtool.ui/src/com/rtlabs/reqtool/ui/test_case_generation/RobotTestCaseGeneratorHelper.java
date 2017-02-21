package com.rtlabs.reqtool.ui.test_case_generation;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;

import org.apache.commons.lang.WordUtils;

import com.rtlabs.reqtool.util.ReqtoolUtil;

import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;

/**
 * This class is called from the code generation template class. It contains all logic for the code generation.
 */
class RobotTestCaseGeneratorHelper {

	private final GherkinDocument document;
	
	public RobotTestCaseGeneratorHelper(GherkinDocument document) {
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
	
	// Use to avoid generating duplicate keywords 
	private Set<String> allUniqueSteps = new HashSet<>();
	
	public Collection<Step> uniqueSteps(ScenarioDefinition scenario) {
		Map<String, Step> newSteps = scenario.getSteps().stream().collect(toLinkedHashMap(Step::getText));
		newSteps.keySet().removeAll(allUniqueSteps);
		allUniqueSteps.addAll(newSteps.keySet());
		return newSteps.values();
	}
	
	
	// There is not strong reason for making these into separate methods. But the code looks so much nicer like this!
	private static <T, K> Collector<T, ?, LinkedHashMap<K, T>> toLinkedHashMap(Function<T, K> k) {
		return toLinkedHashMap(k, o -> o);
	}
	
	private static <T, K, V> Collector<T, ?, LinkedHashMap<K, V>> toLinkedHashMap(Function<T, K> k, Function<T, V> v) {
		return Collector.of(LinkedHashMap<K, V>::new, 
			(m, e) -> m.put(k.apply(e), v.apply(e)), 
			(m1, m2) -> { m1.putAll(m2); return m1; });
	}
	
	private static final Pattern GHERKIN_VAR = Pattern.compile("<([^>]+)>");

	public String keywordName(Step step) {
		// Substitute all <var_name> for ${var_name}
		Matcher varMatcher = GHERKIN_VAR.matcher(step.getText());
		String text = step.getText();
		while (varMatcher.find()) {
			text = text.substring(0, varMatcher.start())
				+ "${" + varMatcher.group(1) + "}"
				+ text.substring(varMatcher.end());
		}
		
		return text.substring(0, 1).toUpperCase() + text.substring(1);
	}
	
	public List<ScenarioDefinition> scenarios() {
		return document.getFeature().getChildren();
	}
}


