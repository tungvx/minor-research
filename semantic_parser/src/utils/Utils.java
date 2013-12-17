package utils;

import jpl.Term;

public class Utils {
	public static String normalizeName(String name) {
		if (name.startsWith("'"))
			name = name.substring(1);
		if (name.endsWith("'"))
			name = name.substring(0, name.length() - 1);
		return name.toLowerCase();
	}

	public static String addSurroundingForQueryName(String name) {
		return "'" + name.toLowerCase() + "'";
	}

	public static Number getNumberFromTerm(Term term) {
		// if (term.isFloat())
		return Double.valueOf(term.floatValue());
		// return Integer.valueOf(term.intValue());
	}
}
