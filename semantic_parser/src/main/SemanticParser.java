package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.OptType;
import utils.DataConfig;
import utils.Utils;
import aggressive_learning.OutputStructure;
import aggressive_learning.Sentence;
import database_query.QueryObject;
import functions.Function;

public class SemanticParser {
	private List<Function> functions;
	private IlpSolver ilpSolver;
	private Sentence sentence;
	private String[][] wordsFuntionsMapping;
	private boolean[][] wordsFuntionsMappingCheck;
	private String[][][][] functionsCompositions;
	private boolean[][][][] functionsCompositionsCheck;
	// the objective expression:
	private Linear objective;
	private double[] coefficients;
	private boolean isNzColumns = false;

	public static final int NUMBER_OF_FEATURES = 3;

	private static final int MAX_FUNCTIONS_EACH_WORDS = 13;

	public SemanticParser(List<Function> functions, Sentence sentence,
			double[] coefficients) {
		this.functions = functions;
		this.ilpSolver = new IlpSolver();
		this.sentence = sentence;
		this.coefficients = coefficients;
		objective = new Linear();

		wordsFuntionsMapping = new String[sentence.getNumberOfConstituents()][Function
				.getNumberOfFunctions()];

		wordsFuntionsMappingCheck = new boolean[sentence
				.getNumberOfConstituents()][Function.getNumberOfFunctions()];

		functionsCompositions = new String[sentence.getNumberOfConstituents()][Function
				.getNumberOfFunctions()][sentence.getNumberOfConstituents()][Function
				.getNumberOfFunctions()];
		functionsCompositionsCheck = new boolean[sentence
				.getNumberOfConstituents()][Function.getNumberOfFunctions()][sentence
				.getNumberOfConstituents()][Function.getNumberOfFunctions()];

		for (int i = 0; i < sentence.getNumberOfConstituents(); i++) {
			for (int j = 0; j < Function.getNumberOfFunctions(); j++) {
				wordsFuntionsMapping[i][j] = i + "-" + j;
				wordsFuntionsMappingCheck[i][j] = true;

				for (int k = 0; k < sentence.getNumberOfConstituents(); k++) {
					for (int l = 0; l < Function.getNumberOfFunctions(); l++) {
						functionsCompositions[i][j][k][l] = i + "-" + j + "-"
								+ k + "-" + l;
						functionsCompositionsCheck[i][j][k][l] = false;
					}
				}

			}
		}

	}

	public void addObjectivesAndContraints() {
		for (int i = 0; i < sentence.getNumberOfConstituents(); i++) {
			Linear constraints = new Linear();
			List<Pair> pairs = new ArrayList<Pair>();
			for (int j = 0; j < Function.getNumberOfFunctions(); j++) {
				double[] features1 = functions.get(j).getFeatures(
						sentence.getConstituent(i));
				Pair.insertSort(new Pair(j, product(features1, coefficients)),
						pairs);
			}
			for (int j = 0; j < MAX_FUNCTIONS_EACH_WORDS; j++) {
				Pair pair = pairs.get(j);
				if (pair.getY() > 0) {
					isNzColumns = true;
					// System.out.println(sentence.getConstituent(i) + " - "
					// + functions.get(pair.getX()) + " - " + pair.getY());
					constraints.add(1, wordsFuntionsMapping[i][pair.getX()]);
					objective.add(pair.getY(),
							wordsFuntionsMapping[i][pair.getX()]);
				} else {
					wordsFuntionsMappingCheck[i][pair.getX()] = false;
				}
			}
			for (int j = MAX_FUNCTIONS_EACH_WORDS; j < Function
					.getNumberOfFunctions(); j++) {
				Pair pair = pairs.get(j);
				wordsFuntionsMappingCheck[i][pair.getX()] = false;
			}
			ilpSolver.addContraint(constraints, Operator.LE, 1);
		}

		for (int i = 0; i < sentence.getNumberOfConstituents(); i++) {
			for (int j = 0; j < Function.getNumberOfFunctions(); j++) {
				if (wordsFuntionsMappingCheck[i][j]) {
					Linear compositionContraints = new Linear();
					for (int k = 0; k < sentence.getNumberOfConstituents(); k++) {
						if (i != k) {
							for (int l = 0; l < Function.getNumberOfFunctions(); l++) {
								if (wordsFuntionsMappingCheck[k][l]) {

									double[] features2 = functions
											.get(j)
											.getFeatures(
													functions.get(l),
													l,
													sentence.getNumberOfConstituents()
															- Math.abs(i - k),
													sentence.getConstituent(i),
													sentence.getConstituent(k));

									if (!checkZeroFeatures(features2)) {
										// System.out.println(sentence
										// .getConstituent(i)
										// + " : "
										// + functions.get(j).getName()
										// + " - "
										// + sentence.getConstituent(k)
										// + " : "
										// + functions.get(l).getName()
										// + ": "
										// + product(features2,
										// coefficients));
										functionsCompositionsCheck[i][j][k][l] = true; // check
																						// that
																						// this
																						// composition
																						// is
																						// ok
										compositionContraints
												.add(1,
														functionsCompositions[i][j][k][l]);
										objective
												.add(product(features2,
														coefficients),
														functionsCompositions[i][j][k][l]);

										// second constraint of the paper
										Linear constraints = new Linear();
										constraints
												.add(2,
														functionsCompositions[i][j][k][l]);
										constraints.add(-1,
												wordsFuntionsMapping[i][j]);
										constraints.add(-1,
												wordsFuntionsMapping[k][l]);
										ilpSolver.addContraint(constraints,
												Operator.LE, 0);
									}
								}
							}
						}
					}
					ilpSolver.addContraint(compositionContraints, Operator.LE,
							1);
				}
			}
		}

		Linear innermostContraint = new Linear();
		for (int i = 0; i < sentence.getNumberOfConstituents(); i++) {
			for (int j = 0; j < Function.getNumberOfFunctions(); j++) {
				Linear compositionContraints = new Linear();
				for (int k = 0; k < sentence.getNumberOfConstituents(); k++) {
					for (int l = 0; l < Function.getNumberOfFunctions(); l++) {
						if (functionsCompositionsCheck[i][j][k][l]
								&& functions.get(l).isBaseFunction()) {
							innermostContraint.add(1,
									functionsCompositions[i][j][k][l]);
						}
						if (functionsCompositionsCheck[k][l][i][j]) {
							compositionContraints.add(1,
									functionsCompositions[k][l][i][j]);

							if (!functions.get(j).isBaseFunction()) {
								Linear baseFunctionContraints = new Linear();
								baseFunctionContraints.add(-1,
										functionsCompositions[k][l][i][j]);
								for (int m = 0; m < sentence
										.getNumberOfConstituents(); m++) {
									for (int n = 0; n < Function
											.getNumberOfFunctions(); n++) {
										if (functionsCompositionsCheck[i][j][m][n]) {
											baseFunctionContraints
													.add(1,
															functionsCompositions[i][j][m][n]);
										}
									}
								}
								ilpSolver.addContraint(baseFunctionContraints,
										Operator.GE, 0);
							}
						}

						if (functionsCompositionsCheck[i][j][k][l]
								&& functionsCompositionsCheck[k][l][i][j]) {
							Linear acyclicConstraint = new Linear();
							acyclicConstraint.add(1,
									functionsCompositions[k][l][i][j]);
							acyclicConstraint.add(1,
									functionsCompositions[i][j][k][l]);
							ilpSolver.addContraint(acyclicConstraint,
									Operator.LE, 1);
						}
					}
				}
				ilpSolver.addContraint(compositionContraints, Operator.LE, 1);
			}
		}
		ilpSolver.addContraint(innermostContraint, Operator.EQ, 1);
	}

	public void addDistanceToObjective(OutputStructure goalStructure) {
		boolean[][] goalWordsFunctionsMapping = goalStructure
				.getWordsFunctionsMapping();
		boolean[][][][] goalFunctionsComposition = goalStructure
				.getFunctionsComposition();
		for (int i = 0; i < sentence.getNumberOfConstituents(); i++) {
			for (int j = 0; j < Function.getNumberOfFunctions(); j++) {
				if (wordsFuntionsMappingCheck[i][j]) {
					if (goalWordsFunctionsMapping[i][j])
						objective.add(-1, wordsFuntionsMapping[i][j]);
					else
						objective.add(1, wordsFuntionsMapping[i][j]);
					for (int k = 0; k < sentence.getNumberOfConstituents(); k++) {
						for (int l = 0; l < Function.getNumberOfFunctions(); l++) {
							if (functionsCompositionsCheck[i][j][k][l]) {
								if (goalFunctionsComposition[i][j][k][l])
									objective.add(-1,
											functionsCompositions[i][j][k][l]);
								else
									objective.add(1,
											functionsCompositions[i][j][k][l]);
							}
						}
					}
				}
			}
		}
	}

	public OutputStructure parse() {
		OutputStructure result = new OutputStructure(sentence);
		if (isNzColumns) {
			ilpSolver.setObjective(objective, OptType.MAX);

			ilpSolver.solve();

			getFinalResult(result);
		}
		return result;
	}

	private void getFinalResult(OutputStructure outputStructure) {
		// get final result:
		List<List<int[]>> finalComposition = new ArrayList<List<int[]>>();
		for (int i = 0; i < sentence.getNumberOfConstituents(); i++) {
			for (int j = 0; j < Function.getNumberOfFunctions(); j++) {
				if (wordsFuntionsMappingCheck[i][j]
						&& ilpSolver.getBoolean(wordsFuntionsMapping[i][j])) {
					outputStructure.setWordFunctionMapping(i, j);
				}
				for (int k = 0; k < sentence.getNumberOfConstituents(); k++) {
					for (int l = 0; l < Function.getNumberOfFunctions(); l++) {
						if (functionsCompositionsCheck[i][j][k][l]
								&& ilpSolver
										.getBoolean(functionsCompositions[i][j][k][l])) {
							outputStructure.setFunctionComposition(i, j, k, l);
							appendResult(finalComposition, new int[] { i, j },
									new int[] { k, l });
							System.out.println(sentence.getConstituent(i) + ":"
									+ functions.get(j).getName() + " - "
									+ sentence.getConstituent(k) + ":"
									+ functions.get(l).getName());
							// System.out.println(i + " "
							// + sentence.getConstituent(i) + ":" + j
							// + " "
							// + functions.get(j).getClass().getName()
							// + " - " + k + " "
							// + sentence.getConstituent(k) + ":" + l
							// + " "
							// + functions.get(l).getClass().getName());
						}
					}
				}
			}
		}

		// printFinalComposition(finalComposition);
		flatten(finalComposition);

		// printFinalComposition(finalComposition);

		if (finalComposition.size() > 0) {
			List<int[]> firstFinalComposition = finalComposition.get(0);
			Character var = DataConfig.STARTED_VAR;
			String queryString = "answer(" + var + ",(" + DataConfig.ARG + "))";
			for (int[] temp : firstFinalComposition) {
				String tempQuery = functions.get(temp[1]).getExecution();
				if (tempQuery.contains(DataConfig.RETURNED_VAR)) {
					tempQuery = tempQuery.replace(DataConfig.RETURNED_VAR, ""
							+ var++);
				}

				if (tempQuery.contains(DataConfig.LOCAL_VAR)) {
					tempQuery = tempQuery.replace(DataConfig.LOCAL_VAR, ""
							+ var++);
				}

				tempQuery = tempQuery.replace(DataConfig.CURRENT_CONSTITUENT,
						Utils.addSurroundingForQueryName(sentence
								.getConstituent(temp[0])));
				queryString = queryString.replace(DataConfig.ARG,
						tempQuery.replace(DataConfig.ARG_VAR, "" + var));
				var = (char) (var - functions.get(temp[1]).getNumberOfArgs() + 1);
			}

			System.out.println(queryString);

			outputStructure.setOutput(QueryObject.execute(queryString));
			if (outputStructure.isResultCorrect()) {
				for (int i = 0; i < firstFinalComposition.size() - 1; i++) {
					functions.get(firstFinalComposition.get(i)[1])
							.increaseFrequenciesAt(
									firstFinalComposition.get(i + 1)[1]);
				}
			}
		}
	}

	private void printFinalComposition(List<List<int[]>> finalComposition) {
		System.out.print("[");
		for (List<int[]> test0 : finalComposition) {
			System.out.print("[");
			for (int[] test1 : test0) {
				System.out.print("[");
				System.out.print(test1[0] + ", " + test1[1]);
				System.out.print("], ");
			}
			System.out.print("], ");
		}
		System.out.print("]");
	}

	private void flatten(List<List<int[]>> finalComposition) {
		if (finalComposition == null || finalComposition.size() <= 1)
			return;
		List<int[]> first = finalComposition.get(0);
		boolean flattened = false;
		Iterator<List<int[]>> iterator = finalComposition.iterator();
		iterator.next();
		while (iterator.hasNext()) {
			List<int[]> composition = iterator.next();
			if (Arrays.equals(composition.get(0), first.get(first.size() - 1))) {
				first.remove(first.size() - 1);
				first.addAll(composition);
				iterator.remove();
				flattened = true;
			} else if (Arrays.equals(composition.get(composition.size() - 1),
					first.get(0))) {
				first.remove(0);
				first.addAll(0, composition);
				iterator.remove();
				flattened = true;
			}
		}
		if (flattened) {
			flatten(finalComposition);
		}
	}

	private void appendResult(List<List<int[]>> finalComposition, int[] first,
			int[] second) {
		if (finalComposition.size() == 0) {
			finalComposition.add(new ArrayList<int[]>());
			finalComposition.get(0).addAll(Arrays.asList(first, second));
			return;
		}
		boolean inserted = false;

		for (List<int[]> composition : finalComposition) {
			if (Arrays.equals(second, composition.get(0))) {
				inserted = true;
				composition.add(0, first);
				break;
			} else if (Arrays.equals(first,
					composition.get(composition.size() - 1))) {
				inserted = true;
				composition.add(second);
				break;
			}
		}

		if (!inserted) {
			finalComposition.add(new ArrayList<int[]>());
			finalComposition.get(finalComposition.size() - 1).addAll(
					Arrays.asList(first, second));
		}
	}

	private double product(double[] features, double[] coefficients) {
		double result = 0;
		for (int i = 0; i < NUMBER_OF_FEATURES; i++) {
			result += features[i] * coefficients[i];
		}
		return result;
	}

	private boolean checkZeroFeatures(double[] features) {
		for (int i = 0; i < NUMBER_OF_FEATURES; i++) {
			if (features[i] > 0)
				return false;
		}
		return true;
	}
}