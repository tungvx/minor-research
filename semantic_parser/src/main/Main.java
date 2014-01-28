package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpl.Term;
import utils.Utils;
import aggressive_learning.AggressiveLearner;
import aggressive_learning.OutputStructure;
import aggressive_learning.Sentence;
import database_query.QueryObject;
import edu.illinois.cs.cogcomp.indsup.inference.IInstance;
import edu.illinois.cs.cogcomp.indsup.inference.IStructure;
import edu.illinois.cs.cogcomp.indsup.learning.FeatureVector;
import functions.Function;

public class Main {

	public static final String TRAINING_FILE = "training";
	public static final String TESTING_FILE = "testing";

	public static void main(String[] args) {
		// readData();
		int numberth = 250;
		// List<OutputStructure> trainingData = readWorkingData(TRAINING_FILE)
		// .subList(numberth - 1, numberth);
		List<OutputStructure> trainingData = readWorkingData(TRAINING_FILE)
				.subList(0, numberth);
		double[] w = { 1, 1 };
		Function.setNotTrainging();
		for (int i = 0; i < 10; i++) {
			executeParsing(trainingData, w);
		}

		// Direct learning
		// w = directLearning(trainingData, w);

		// Aggressive learning.
		// w = agressiveLearning(trainingData, w);
		// System.out.println("After training: " + w[0] + ", " + w[1]);
		// executeParsing(trainingData, w);
		List<OutputStructure> testingData = readWorkingData(TESTING_FILE);
		// List<OutputStructure> testingData = readWorkingData(TESTING_FILE)
		// .subList(numberth - 1, numberth);
		// executeParsing(testingData, w);
		// Function.setNotTrainging();
		executeParsing(testingData, w);
	}

	private static double[] directLearning(List<OutputStructure> trainingData,
			double[] w) {
		DirectLearner directLearner = new DirectLearner();
		List<FeatureVector>[] trainingFeatureVectors = (List<FeatureVector>[]) new List<?>[trainingData
				.size()];
		for (int i = 0; i < trainingData.size(); i++)
			trainingFeatureVectors[i] = new ArrayList<FeatureVector>();

		List<Object[]> directTrainingData = new ArrayList<Object[]>();
		boolean isNew = true;
		while (isNew) {
			isNew = false;
			executeParsingForDirectLearning(trainingData, w);

			for (OutputStructure outputStructure : trainingData) {
				FeatureVector featureVector = outputStructure
						.getFeatureVector();
				// featureVector.normalize(outputStructure.getSentence()
				// .getNumberOfConstituents());
				Utils.normalizeFeatureVector(featureVector);

				if (!checkIfExist(
						trainingFeatureVectors[trainingData
								.indexOf(outputStructure)],
						featureVector)) {
					isNew = true;
					trainingFeatureVectors[trainingData
							.indexOf(outputStructure)].add(featureVector);
					if (outputStructure.isResultCorrect()) {
						directTrainingData.add(new Object[] {
								featureVector.getValue(), "positive" });
					} else {
						directTrainingData.add(new Object[] {
								featureVector.getValue(), "negative" });
					}
				}
			}
			try {
				w = directLearner.learn(directTrainingData,
						SemanticParser.NUMBER_OF_FEATURES);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			Function.setNotTrainging();
		}
		return w;
	}

	private static boolean checkIfExist(List<FeatureVector> list,
			FeatureVector featureVector) {
		for (FeatureVector featureVector2 : list) {
			boolean contained = true;
			for (int i = 0; i < SemanticParser.NUMBER_OF_FEATURES; i++) {
				if (featureVector.getValue()[i] != featureVector2.getValue()[i]) {
					contained = false;
				}
			}
			if (contained)
				return contained;
		}
		return false;
	}

	private static double[] agressiveLearning(
			List<OutputStructure> trainingData, double[] w) {

		while (true) {
			boolean newSample = executeParsingForAggLearning(trainingData, w);

			if (!newSample)
				break;

			List<IInstance> sentences = new ArrayList<IInstance>();
			List<IStructure> outputs = new ArrayList<IStructure>();
			for (OutputStructure outputStructure : trainingData) {
				if (outputStructure.isResultCorrect()) {
					sentences.add((IInstance) outputStructure.getSentence());
					outputs.add((IStructure) outputStructure);
				}
			}
			Function.setNotTrainging();
			AggressiveLearner aggressiveLearner = new AggressiveLearner(
					sentences, outputs);
			w = aggressiveLearner.train().getWeightArray();
		}
		return w;
	}

	private static boolean executeParsingForAggLearning(
			List<OutputStructure> trainingData, double[] w) {
		boolean result = false;
		for (OutputStructure outputStructure : trainingData) {
			System.out.print(trainingData.indexOf(outputStructure) + ": "
					+ outputStructure.getResults() + " - ");
			// OutputStructure outputStructure = trainingData.get(0);
			SemanticParser parser = new SemanticParser(Function.getFunctions(),
					outputStructure.getSentence(),
					outputStructure.getResults(), w);

			parser.addObjectivesAndContraints();
			// System.out.println("Start solving");
			OutputStructure tempOutputStructure = parser.parse();
			tempOutputStructure.setResults(outputStructure.getResults());
			if (tempOutputStructure.isResultCorrect()) {
				if (!result && !outputStructure.isResultCorrect())
					result = true;
				tempOutputStructure.copyResults(outputStructure);
			}

			System.out.println(tempOutputStructure.getOutput());
		}
		return result;
	}

	private static void executeParsingForDirectLearning(
			List<OutputStructure> trainingData, double[] w) {
		for (OutputStructure outputStructure : trainingData) {
			System.out.print(trainingData.indexOf(outputStructure) + ": "
					+ outputStructure.getResults() + " - ");
			// OutputStructure outputStructure = trainingData.get(0);
			SemanticParser parser = new SemanticParser(Function.getFunctions(),
					outputStructure.getSentence(),
					outputStructure.getResults(), w);

			parser.addObjectivesAndContraints();
			// System.out.println("Start solving");
			OutputStructure tempOutputStructure = parser.parse();
			tempOutputStructure.copyResults(outputStructure);

			System.out.println(tempOutputStructure.getOutput());
		}
	}

	private static void executeParsing(List<OutputStructure> trainingData,
			double[] w) {
		int correct = 0;
		for (OutputStructure outputStructure : trainingData) {
			outputStructure.setOutput(null);
			System.out.print(trainingData.indexOf(outputStructure) + ": "
					+ outputStructure.getResults() + " - ");
			// OutputStructure outputStructure = trainingData.get(0);
			try {
				SemanticParser parser = new SemanticParser(
						Function.getFunctions(), outputStructure.getSentence(),
						outputStructure.getResults(), w);

				parser.addObjectivesAndContraints();
				// System.out.println("Start solving");
				OutputStructure tempOutputStructure = parser.parse();
				tempOutputStructure.copyResults(outputStructure);

				if (outputStructure.isResultCorrect()) {
					correct++;
				}
				// else
				// return;
				System.out.println(outputStructure.getOutput());
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
		}

		System.out.println("Correct: " + correct + "/" + trainingData.size()
				+ " = " + (double) correct / trainingData.size());
	}

	private static List<OutputStructure> readWorkingData(String fileName) {
		List<OutputStructure> outputStructures = new ArrayList<OutputStructure>();

		BufferedReader br = null;
		QueryObject.getQueryObject();
		try {
			br = new BufferedReader(new FileReader("./database/" + fileName));
			String line = br.readLine();
			int count = 1;
			while (line != null) {
				System.out.println("Reading line: " + count++ + " ...........");
				Pattern constituents = Pattern.compile("\\[.*\\]");
				Matcher matcher = constituents.matcher(line);
				matcher.find();
				String sentence = matcher.group();
				String[] tokens = sentence.substring(1, sentence.length() - 1)
						.split(",");
				for (int i = 0; i < tokens.length; i++) {
					tokens[i] = tokens[i].trim();
				}
				OutputStructure outputStructure = new OutputStructure(
						new Sentence(Arrays.asList(tokens)));

				Pattern pattern = Pattern.compile("answer.*\\).");
				Matcher matcher2 = pattern.matcher(line);
				matcher2.find();
				String query = matcher2.group();
				outputStructure.setResults(QueryObject.execute(query.substring(
						0, query.length() - 2)));
				outputStructures.add(outputStructure);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("................FINISH READING DATA............");
		return outputStructures;
	}

	private static List<Object> convertToObjectList(Term term) {
		List<Object> result = new ArrayList<Object>();
		if (term.arity() > 1) {
			Term temp = term.arg(1);
			if (temp.isFloat() || temp.isInteger())
				result.add(Utils.getNumberFromTerm(temp));
			else
				result.add(Utils.normalizeName(term.arg(1).toString()));
			result.addAll(convertToObjectList(term.arg(2)));
		}
		return result;
	}

	// function for dividing data: 250 queries for training and 250 queries for
	// testing.
	private static void readData() {
		BufferedReader br = null;
		List<String> lines = new ArrayList<String>();
		try {
			br = new BufferedReader(new FileReader(
					"./database/geoqueries880vnnew"));
			String line = br.readLine();
			while (line != null) {
				lines.add(line);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
		Random rand = new Random();
		PrintWriter trainingWriter = null;
		PrintWriter testingWriter = null;
		try {
			trainingWriter = new PrintWriter("./database/" + TRAINING_FILE,
					"UTF-8");
			testingWriter = new PrintWriter("./database/" + TESTING_FILE,
					"UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		for (int i = 0; i < 250; i++) {
			trainingWriter.println(lines.remove(rand.nextInt(lines.size())));
			testingWriter.println(lines.remove(rand.nextInt(lines.size())));
		}
		trainingWriter.close();
		testingWriter.close();
	}

}