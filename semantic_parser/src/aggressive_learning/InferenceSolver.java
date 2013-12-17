package aggressive_learning;

import main.SemanticParser;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.indsup.inference.AbstractLossSensitiveStructureFinder;
import edu.illinois.cs.cogcomp.indsup.inference.IInstance;
import edu.illinois.cs.cogcomp.indsup.inference.IStructure;
import edu.illinois.cs.cogcomp.indsup.learning.WeightVector;
import functions.Function;

public class InferenceSolver extends AbstractLossSensitiveStructureFinder {

	@Override
	public Pair<IStructure, Double> getLossSensitiveBestStructure(
			WeightVector w, IInstance input, IStructure output)
			throws Exception {
		Sentence sentence = (Sentence) input;

		OutputStructure outputStructure = (OutputStructure) output;

		SemanticParser parser = new SemanticParser(Function.getFunctions(),
				sentence, w.getWeightArray());
		System.out.println(w.getWeightArray()[0] + ", " + w.getWeightArray()[1]
				+ ", " + w.getWeightArray()[2]);
		parser.addObjectivesAndContraints();
		parser.addDistanceToObjective(outputStructure);

		OutputStructure mostViolatedStructure = parser.parse();

		System.out.println(mostViolatedStructure.getOutput());

		double distance = 0;
		for (int i = 0; i < sentence.getNumberOfConstituents(); i++) {
			for (int j = 0; j < Function.getNumberOfFunctions(); j++) {
				if (outputStructure.getWordFunctionMapping(i, j) != mostViolatedStructure
						.getWordFunctionMapping(i, j))
					distance += 1;
				for (int k = 0; k < sentence.getNumberOfConstituents(); k++) {
					for (int l = 0; l < Function.getNumberOfFunctions(); l++) {
						if (outputStructure.getFunctionComposition(i, j, k, l) != mostViolatedStructure
								.getFunctionComposition(i, j, k, l))
							distance += 1;
					}
				}
			}
		}
		System.err.println(distance);
		return new Pair<IStructure, Double>(mostViolatedStructure, distance);
	}

	@Override
	public IStructure getBestStructure(WeightVector w, IInstance input)
			throws Exception {
		Sentence sentence = (Sentence) input;

		SemanticParser parser = new SemanticParser(Function.getFunctions(),
				sentence, w.getWeightArray());

		parser.addObjectivesAndContraints();

		OutputStructure outputStructure = parser.parse();
		return outputStructure;
	}

}
