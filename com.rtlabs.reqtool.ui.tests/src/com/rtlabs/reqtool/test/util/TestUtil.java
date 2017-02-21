package com.rtlabs.reqtool.test.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestUtil {
	public static void assertContains(String message, String actualText, String... regexes) {
		int lastMatchIx = 0;
		String lastRegex = null;
		
		for (String regex : regexes) {
			Matcher matcher = Pattern.compile(regex, Pattern.DOTALL).matcher(actualText.substring(lastMatchIx));
			if (!matcher.find()) {
				String m = message + ", regex '" + regex + " starting from index " + lastMatchIx + " ' does not match.\n";
				if (lastRegex != null) m += "Previous match: '" + lastRegex + "'\n"; 
				throw new AssertionError(m + actualText);
			}
			
			lastMatchIx = matcher.end();
			lastRegex = regex;
		}
	}

}
