package org.barrelorgandiscovery.tracetools.simpletsp;

public class PlaneGraph extends Graph
// A graph, which can be initialized with points.
{
	final double sqr(double x) {
		return x * x;
	}

	public PlaneGraph(Points p) {
		super(p.size());
		int i, j;
		for (i = 0; i < N; i++)
			for (j = 0; j < N; j++)
				connect(i, j, Math.sqrt(sqr(p.X[i] - p.X[j])
						+ sqr(p.Y[i] - p.Y[j])));
	}
}
