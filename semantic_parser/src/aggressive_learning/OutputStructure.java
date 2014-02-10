package aggressive_learning;

import java.util.Arrays;
import java.util.HashSet;

import main.SemanticParser;
import jpl.Term;
import jpl.Util;
import edu.illinois.cs.cogcomp.indsup.inference.IStructure;
import edu.illinois.cs.cogcomp.indsup.learning.FeatureVector;
import functions.Function;

public class OutputStructure implements IStructure {

	private Sentence sentence;
	private boolean[][] wordsFunctionsMapping;
	private boolean[][][][] functionsComposition;
	private Term output;
	private Term results;

	public OutputStructure(Sentence sentence) {
		this.sentence = sentence;
		wordsFunctionsMapping = new boolean[sentence.getNumberOfConstituents()][Function
				.getNumberOfFunctions()];
		functionsComposition = new boolean[sentence.getNumberOfConstituents()][Function
				.getNumberOfFunctions()][sentence.getNumberOfConstituents()][Function
				.getNumberOfFunctions()];
	}

	public void setResults(Term results) {
		this.results = results;
	}

	public Term getResults() {
		return results;
	}

	public void setWordFunctionMapping(int wordIndex, int functionIndex) {
		wordsFunctionsMapping[wordIndex][functionIndex] = true;
	}

	public void setFunctionComposition(int firstWordIndex,
			int firstFunctionIndex, int secondWordIndex, int secondFunctionIndex) {
		functionsComposition[firstWordIndex][firstFunctionIndex][secondWordIndex][secondFunctionIndex] = true;
	}

	public boolean getWordFunctionMapping(int wordIndex, int functionIndex) {
		return wordsFunctionsMapping[wordIndex][functionIndex];
	}

	public boolean getFunctionComposition(int firstWordIndex,
			int firstFunctionIndex, int secondWordIndex, int secondFunctionIndex) {
		return functionsComposition[firstWordIndex][firstFunctionIndex][secondWordIndex][secondFunctionIndex];
	}

	@Override
	public FeatureVector getFeatureVector() {
		return computFeatureVector();
	}

	public boolean[][] getWordsFunctionsMapping() {
		return wordsFunctionsMapping;
	}

	public void setWordsFunctionsMapping(boolean[][] wordsFunctionsMapping) {
		this.wordsFunctionsMapping = wordsFunctionsMapping;
	}

	private FeatureVector computFeatureVector() {
		int[] temp = new int[SemanticParser.NUMBER_OF_FEATURES];
		for (int i = 0; i < SemanticParser.NUMBER_OF_FEATURES; i++)
			temp[i] = i + 1;
		FeatureVector featureVector = new FeatureVector(temp,
				new double[SemanticParser.NUMBER_OF_FEATURES]);
		for (int i = 0; i < sentence.getNumberOfConstituents(); i++) {
			for (int j = 0; j < Function.getNumberOfFunctions(); j++) {
				if (wordsFunctionsMapping[i][j])
					featureVector = FeatureVector.plus(
							featureVector,
							new FeatureVector(temp, Function.getFunctions()
									.get(j)
									.getFeatures(sentence.getConstituent(i))));
				for (int k = 0; k < sentence.getNumberOfConstituents(); k++) {
					for (int l = 0; l < Function.getNumberOfFunctions(); l++) {
						if (functionsComposition[i][j][k][l]) {
							featureVector = FeatureVector
									.plus(featureVector,
											new FeatureVector(
													temp,
													Function.getFunctions()
															.get(j)
															.getFeatures(
																	Function.getFunctions()
																			.get(l),
																	l,
																	sentence.getNumberOfConstituents()
																			- Math.abs(i
																					- k),
																	sentence.getConstituent(i),
																	sentence.getConstituent(k))));
						}
					}
				}
			}
		}
		// System.out.println("computed feature vector: "
		// + featureVector.getValue()[0] + " "
		// + featureVector.getValue()[1] + " "
		// + featureVector.getValue()[2]);
		return featureVector;
	}

	public boolean[][][][] getFunctionsComposition() {
		return functionsComposition;
	}

	public void setFunctionsComposition(boolean[][][][] functionsComposition) {
		this.functionsComposition = functionsComposition;
	}

	public void setOutput(Term output) {
		this.output = output;
	}

	public Term getOutput() {
		return output;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public boolean isResultCorrect() {
		return compareOutputs(results, output);
	}

	public boolean compareOutputs(Term term) {
		return compareOutputs(output, term);
	}

	private boolean compareOutputs(Term term1, Term term2) {
		if (term1 == null)
			return term2 == null;
		if (term2 == null)
			return false;
		if (term1.equals(term2))
			return true;
		String[] stringTerm1 = Util.atomListToStringArray(term1);
		String[] stringTerm2 = Util.atomListToStringArray(term2);
		if (stringTerm1 == null || stringTerm2 == null)
			return term1.equals(term2);
		HashSet<String> set1 = new HashSet<String>(Arrays.asList(stringTerm1));
		HashSet<String> set2 = new HashSet<String>(Arrays.asList(stringTerm2));
		return set1.equals(set2);
	}

	public void copyResults(OutputStructure outputStructure) {
		outputStructure.setFunctionsComposition(functionsComposition);
		outputStructure.setWordsFunctionsMapping(wordsFunctionsMapping);
		outputStructure.setOutput(output);
	}
}
