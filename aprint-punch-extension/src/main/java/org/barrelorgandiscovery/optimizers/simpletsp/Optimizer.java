package org.barrelorgandiscovery.optimizers.simpletsp;

import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;

public class Optimizer {

	private static Logger logger = Logger.getLogger(Optimizer.class);
	
	/**
	 * Optimize a Path from a Graph
	 * 
	 * @param g
	 *            le graph
	 * @param maxiter
	 *            maximum iteration
	 * @return a Path
	 */
	public static Path optimize(Graph g, int maxiter) {
		if (g == null)
			return null;

		Date d = new Date();
		Random r = new Random(d.getTime());

		Path pa = new Path(g), pmin = null;
		double lmin = 1e50;
		int count = 0;
		do {
			pa.random(r);
			pa.localoptimize();
			if (pa.length() < lmin - 1e-10) {
				pmin = (Path) pa.clone();
				lmin = pa.length();
				count = 0;
				logger.debug("New length " + lmin);
			} else
				count++;
		} while (count < maxiter);

		return pmin;
	}

	/**
	 * Optimize the Path defining the start point and end point
	 * 
	 * The graph is modified
	 * 
	 * @param g
	 *            the graph
	 * @param start
	 *            the start point
	 * @param end
	 *            the end point
	 * @param maxiter
	 *            the maximum iteration
	 * @return
	 */
	public static Path optimize(Graph g, int start, int end, int maxiter) {

		if (g == null)
			return null;

		g.connect(end, start, -10000);

		Path p = optimize(g, maxiter);

		// reorder the path

		int startindex = 0;
		while (p.From[startindex] != start)
			startindex++;

		Path sol = new Path(g);
		// startindex contain the start
		for (int i = 0 ; i < p.N ; i ++)
		{
			sol.From[i] = p.From[(startindex + i) % p.N];
			sol.To[i] = p.To[(startindex + i) % p.N];
		}
		
		return sol;
	}

}
