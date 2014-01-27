package aggressive_learning;

import java.util.List;

import main.SemanticParser;
import edu.illinois.cs.cogcomp.indsup.inference.IInstance;
import edu.illinois.cs.cogcomp.indsup.inference.IStructure;
import edu.illinois.cs.cogcomp.indsup.learning.JLISParameters;
import edu.illinois.cs.cogcomp.indsup.learning.StructuredProblem;
import edu.illinois.cs.cogcomp.indsup.learning.WeightVector;
import edu.illinois.cs.cogcomp.indsup.learning.L2Loss.L2LossJLISLearner;

public class AggressiveLearner {
	private StructuredProblem structuredProblem;
	private JLISParameters params;
	private L2LossJLISLearner learner;
	private InferenceSolver inferenceSolver;

	public AggressiveLearner(List<IInstance> inputList,
			List<IStructure> outputList) {
		structuredProblem = new StructuredProblem();
		structuredProblem.input_list = inputList;
		structuredProblem.output_list = outputList;

		// init param
		params = new JLISParameters();
		params.c_struct = 1.0;
		params.check_inference_opt = false;
		params.total_number_features = SemanticParser.NUMBER_OF_FEATURES;

		learner = new L2LossJLISLearner();

		inferenceSolver = new InferenceSolver();
	}

	public WeightVector train() {
		try {
			return learner.trainStructuredSVM(inferenceSolver,
					structuredProblem, params);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return new WeightVector(new double[] { 1, 1, 1 }, 1);
	}
}
