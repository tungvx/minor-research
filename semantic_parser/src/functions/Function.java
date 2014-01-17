package functions;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import main.SemanticParser;
import utils.DataConfig;
import utils.Utils;

import com.google.gson.stream.JsonReader;

import database_query.QueryObject;

public class Function {
	protected List<String> surfaceForms;
	protected List<String> surfaceFormsInEnglish;
	private static int numberOfFunctions;
	private static List<Function> functions;

	private String name;
	private List<String> argTypes;
	private String returnedType;
	private String execution;
	private String getFeatures;
	private boolean isBaseFunction;
	private int numberOfArgs;

	public static final double MAX_SIMILARITY = 20;

	public String getExecution() {
		return execution;
	}

	public String getReturnedType() {
		return returnedType;
	}

	public Function() {
		surfaceForms = new ArrayList<String>();
		surfaceFormsInEnglish = new ArrayList<String>();
	}

	@Override
	public String toString() {
		return name;
	}

	public Function(String name, List<String> argTypes, String returnedType,
			String execution, String getFeatures, List<String> surfaceForms,
			boolean isBaseFunction, int numberOfArgs) {
		this.name = name;
		this.argTypes = argTypes;
		this.returnedType = returnedType;
		this.execution = execution;
		this.getFeatures = getFeatures;
		this.surfaceForms = surfaceForms;
		this.isBaseFunction = isBaseFunction;
		this.numberOfArgs = numberOfArgs;
	}

	public static int getNumberOfFunctions() {
		return numberOfFunctions;
	}

	public boolean isBaseFunction() {
		return isBaseFunction;
	}

	public List<String> getSurfaceForms() {
		return surfaceForms;
	}

	public <T, A> T execute(A argument) {
		return null;

	}

	protected String getCurrentConstituent() {
		return null;

	}

	public void setCurrentConstituent(String constituent) {

	}

	protected double[] getFeatures() {
		return null;

	}

	protected void setFeatures(double[] features) {

	}

	public double[] getFeatures(String constituent) {
		double[] features = new double[3];
		if (getFeatures != null
				&& QueryObject.checkExist(getFeatures.replace(
						DataConfig.CURRENT_CONSTITUENT, Utils
								.addSurroundingForQueryName(Utils
										.normalizeName(constituent))))) {
			for (int i = 0; i < SemanticParser.NUMBER_OF_FEATURES; i++)
				features[i] = MAX_SIMILARITY;
			// System.out.println("check: " + constituent + " - "
			// + getClass().getName() + " - " + features[0] + " - "
			// + features[1] + " - " + features[2] + " ");
			return features;
		}

		if (checkSurfaceForm(constituent.toLowerCase())) {
			for (int i = 0; i < SemanticParser.NUMBER_OF_FEATURES; i++)
				features[i] = MAX_SIMILARITY;
			// System.out.println("check: " + constituent + " - "
			// + getClass().getName() + " - " + features[0] + " - "
			// + features[1] + " - " + features[2] + " ");
			return features;
		} else
			features[1] = matchedWords(constituent) / CountWords(constituent);

		// similarity using wordnet
		// first, we translate the words in to english:
		// String translatedWord;
		// try {
		// translatedWord = Translate.translate(Translate.VIETNAMESE,
		// Translate.ENGLISH, constituent);
		// } catch (IOException e) {
		// return features;
		// }
		// for (String surfaceForm : surfaceFormsInEnglish) {
		// double temp = WordnetSimilarity.getWordnetSimilarity()
		// .getSimilarity(translatedWord, surfaceForm);
		// if (temp > features[0]) {
		// features[0] = temp;
		// }
		// }
		// System.out.println("check: " + constituent + " - "
		// + getClass().getName() + " - " + features[0] + " - "
		// + features[1] + " - " + features[2] + " ");
		return features;
	}

	private int CountWords(String in) {
		String trim = in.trim();
		if (trim.isEmpty())
			return 0;
		return trim.split("\\s+").length; // separate string around spaces
	}

	public double[] getFeatures(Function function, int distance, String word1,
			String word2) {
		double[] result = new double[3];
		if (argTypes.contains(function.getReturnedType())) {
			// double[] feature1 = getFeatures(word1);
			// double[] feature2 = function.getFeatures(word2);
			// for (int i = 0; i < SemanticParser.NUMBER_OF_FEATURES; i++) {
			// result[i] = Math.pow(feature1[i] + feature2[i], 2);
			// }
			for (int i = 0; i < SemanticParser.NUMBER_OF_FEATURES; i++) {
				result[i] = MAX_SIMILARITY;
			}
			// result[1] = 3;
		}

		// System.out.println(word1 + " : " + getClass().getName() + " - " +
		// word2
		// + " : " + function.getClass().getName() + ": " + result[0]
		// + "," + result[1] + "," + result[2]);

		result[2] /= distance;
		return result;
	}

	private double matchedWords(String constituent) {
		double matches = 0;
		String[] constituentTokens = constituent.split(" ");
		for (String surfaceForm : surfaceForms) {
			String[] surfaceFormTokens = surfaceForm.split(" ");
			double temp = 0;
			for (String token : surfaceFormTokens) {
				for (String constituentToken : constituentTokens) {
					if (token.toLowerCase().equals(
							constituentToken.toLowerCase())) {
						temp++;
						break;
					}
				}
			}
			if (matches < temp)
				matches = temp;
		}
		return matches;
	}

	protected boolean checkSurfaceForm(String constituent) {
		return surfaceForms.contains(constituent);
	}

	public static List<Function> getFunctions() {
		if (functions != null)
			return functions;
		functions = new ArrayList<Function>();

		// read the functions definition file:
		try {
			JsonReader jsonReader = new JsonReader(new FileReader(
					"./functions_definition/Functions"));

			jsonReader.beginArray();
			while (jsonReader.hasNext()) {
				functions.add(readFunction(jsonReader));
			}
			jsonReader.endArray();
			jsonReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		numberOfFunctions = functions.size();
		return functions;
	}

	private static Function readFunction(JsonReader jsonReader) {
		String name = null, returnedType = null, execution = null, getFeatures = null;
		List<String> argTypes = new ArrayList<String>();
		List<String> surfaceForms = new ArrayList<String>();
		int numberOfArgs = 1;
		try {
			jsonReader.beginObject();
			while (jsonReader.hasNext()) {
				String nextName = jsonReader.nextName();
				if (nextName.equals("name")) {
					name = jsonReader.nextString();
				} else if (nextName.equals("argTypes")) {
					jsonReader.beginArray();
					while (jsonReader.hasNext()) {
						argTypes.add(jsonReader.nextString());
					}
					jsonReader.endArray();
				} else if (nextName.equals("returnedType")) {
					returnedType = jsonReader.nextString();
				} else if (nextName.equals("execution")) {
					execution = jsonReader.nextString();
				} else if (nextName.equals("getFeatures")) {
					getFeatures = jsonReader.nextString();
				} else if (nextName.equals("surfaceForms")) {
					jsonReader.beginArray();
					while (jsonReader.hasNext()) {
						surfaceForms.add(jsonReader.nextString());
					}
					jsonReader.endArray();
				} else if (nextName.equals("argsNum")) {
					numberOfArgs = jsonReader.nextInt();
				} else {
					jsonReader.skipValue();
				}
			}
			jsonReader.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Function(name, argTypes, returnedType, execution,
				getFeatures, surfaceForms, argTypes.size() == 1
						&& argTypes.get(0).equals("String"), numberOfArgs);
	}

	public String getName() {
		return name;
	}

	public int getNumberOfArgs() {
		return numberOfArgs;
	}
}
