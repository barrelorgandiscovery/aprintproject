package org.barrelorgandiscovery.optimizers.simpletsp;

public class Graph
// contains a Matrix of distances for a graph.
{
	protected int N;

	double M[][];

	final static double INFTY = 1e50;

	public Graph(int n) {
		N = n;
		M = new double[n][n];
		int i, j;
		// initially disconnect all points
		for (i = 0; i < n; i++)
			for (j = 0; j < n; j++) {
				if (i != j)
					connect(i, j, INFTY);
				else
					connect(i, j, 0);
			}
	}

	void connect(int i, int j, double x) {
		M[i][j] = x;
	}

	final double distance(int i, int j) {
		return M[i][j];
	}

	final int size() {
		return N;
	}
}
