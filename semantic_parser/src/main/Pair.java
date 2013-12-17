package main;

import java.util.List;

public class Pair {
	private int x;
	private double y;

	public Pair(int x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getY() {
		return y;
	}

	public int getX() {
		return x;
	}

	public static void insertSort(Pair pair, List<Pair> pairs) {
		for (Pair pair2 : pairs) {
			if (pair.getY() > pair2.getY()) {
				pairs.add(pairs.indexOf(pair2), pair);
				return;
			}
		}
		pairs.add(pair);
	}
}
