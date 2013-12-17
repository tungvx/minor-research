package aggressive_learning;

import java.util.List;

import edu.illinois.cs.cogcomp.indsup.inference.IInstance;

public class Sentence implements IInstance {
	private List<String> constituents;
	private int numberOfConstituents;

	public Sentence(List<String> constituents) {
		this.constituents = constituents;
		numberOfConstituents = constituents.size();
	}

	public String getConstituent(int index) {
		return constituents.get(index);
	}

	@Override
	public double size() {
		return numberOfConstituents;
	}

	public int getNumberOfConstituents() {
		return numberOfConstituents;
	}

	public List<String> getConstituents() {
		return constituents;
	}

	@Override
	public String toString() {
		return constituents.toString();
	}
}
