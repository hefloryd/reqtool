package com.rtlabs.reqtool.util;

import java.util.regex.Pattern;

public class ReqtoolUtil {

	private static final Pattern LINES_PATTERN = Pattern.compile("\\r\\n|\\n\\r|\\n|\\r");
	
	/**
	 * Splits the input into an array of lines. Splits on all known newline separators.
	 */
	public static String[] splitLines(String text) {
		return LINES_PATTERN.split(text);	
	}
}
