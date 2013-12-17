package main;

import org.gnu.glpk.GlpkException;

import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryGLPK;
import net.sf.javailp.VarType;

public class IlpSolver {
	private SolverFactory factory;
	private Problem problem;
	private Result result;

	public IlpSolver() {
		factory = new SolverFactoryGLPK();
		factory.setParameter(Solver.VERBOSE, 0);
		// set timeout to 10 seconds
		factory.setParameter(Solver.TIMEOUT, 10);
		problem = new Problem();
	}

	public boolean getBoolean(String variable) {
		if (result == null)
			return false;
		return result.getBoolean(variable);
	}

	public void setObjective(Linear expression, OptType optType) {
		problem.setObjective(expression, optType);
	}

	public void addContraint(Linear lhsExp, Operator operator, Number rhsValue) {
		if (lhsExp.size() > 0)
			problem.add(problem.getConstraintsCount() + "", lhsExp, operator,
					rhsValue);
	}

	public void solve() {
		for (Object variable : problem.getVariables()) {
			problem.setVarType(variable, VarType.BOOL);
		}
		Solver solver = factory.get();
		try {
			result = solver.solve(problem);
		} catch (GlpkException e) {

		}
	}
}
