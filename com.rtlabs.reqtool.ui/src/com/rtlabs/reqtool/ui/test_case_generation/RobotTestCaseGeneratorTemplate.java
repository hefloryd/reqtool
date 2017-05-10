package com.rtlabs.reqtool.ui.test_case_generation;

import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;

public class RobotTestCaseGeneratorTemplate
{
  protected static String nl;
  public static synchronized RobotTestCaseGeneratorTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    RobotTestCaseGeneratorTemplate result = new RobotTestCaseGeneratorTemplate();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "*** Settings ***" + NL + "Documentation     ";
  protected final String TEXT_2 = NL + "...";
  protected final String TEXT_3 = NL + "...               ";
  protected final String TEXT_4 = NL + NL + "*** Test Cases ***" + NL;
  protected final String TEXT_5 = NL;
  protected final String TEXT_6 = NL + "  ";
  protected final String TEXT_7 = NL + NL + "*** Keywords ***" + NL;
  protected final String TEXT_8 = NL;
  protected final String TEXT_9 = NL + "    # TODO: Generated keyword, to be implemented" + NL;
  protected final String TEXT_10 = NL;

  public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
     RobotTestCaseGeneratorHelper h = (RobotTestCaseGeneratorHelper) argument; 
    stringBuffer.append(TEXT_1);
    stringBuffer.append( h.testCaseName() );
    stringBuffer.append(TEXT_2);
     for (String descriptionLine : h.testCaseDescription()) { 
    stringBuffer.append(TEXT_3);
    stringBuffer.append( descriptionLine );
     } 
    stringBuffer.append(TEXT_4);
     for (ScenarioDefinition senario : h.scenarios()) { 
    stringBuffer.append(TEXT_5);
    stringBuffer.append( senario.getName() );
     for (Step step : senario.getSteps()) { 
    stringBuffer.append(TEXT_6);
    stringBuffer.append( step.getKeyword() );
    stringBuffer.append( step.getText() );
     } 
     } 
    stringBuffer.append(TEXT_7);
     for (ScenarioDefinition scenario : h.scenarios()) { 
     for (Step step : h.uniqueSteps(scenario)) { 
    stringBuffer.append(TEXT_8);
    stringBuffer.append( h.keywordName(step) );
    stringBuffer.append(TEXT_9);
     } 
     } 
    stringBuffer.append(TEXT_10);
    return stringBuffer.toString();
  }
}
