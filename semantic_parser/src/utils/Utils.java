package utils;

import main.SemanticParser;
import edu.illinois.cs.cogcomp.indsup.learning.FeatureVector;
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

	public static void normalizeFeatureVector(FeatureVector featureVector) {
		double max = 0;
		double[] values = featureVector.getValue();
		for (int i = 0; i < SemanticParser.NUMBER_OF_FEATURES; i++) {
			if (max < values[i])
				max = values[i];
		}
		featureVector.normalize(max);
	}
}
