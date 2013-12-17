package aggressive_learning;

import jpl.Term;
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
		FeatureVector featureVector = new FeatureVector(new int[] { 1, 2, 3 },
				new double[] { 0, 0, 0 });
		for (int i = 0; i < sentence.getNumberOfConstituents(); i++) {
			for (int j = 0; j < Function.getNumberOfFunctions(); j++) {
				if (wordsFunctionsMapping[i][j])
					featureVector = FeatureVector.plus(
							featureVector,
							new FeatureVector(new int[] { 1, 2, 3 }, Function
									.getFunctions().get(j)
									.getFeatures(sentence.getConstituent(i))));
				for (int k = 0; k < sentence.getNumberOfConstituents(); k++) {
					for (int l = 0; l < Function.getNumberOfFunctions(); l++) {
						if (functionsComposition[i][j][k][l]) {
							featureVector = FeatureVector
									.plus(featureVector,
											new FeatureVector(
													new int[] { 1, 2, 3 },
													Function.getFunctions()
															.get(j)
															.getFeatures(
																	Function.getFunctions()
																			.get(l),
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
		if (results == null)
			return output == null;
		return results.equals(output);
	}

	public void copyResults(OutputStructure outputStructure) {
		outputStructure.setFunctionsComposition(functionsComposition);
		outputStructure.setWordsFunctionsMapping(wordsFunctionsMapping);
		outputStructure.setOutput(output);
	}
}
