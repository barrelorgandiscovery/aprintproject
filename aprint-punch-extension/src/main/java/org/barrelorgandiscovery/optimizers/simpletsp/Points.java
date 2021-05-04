package org.barrelorgandiscovery.optimizers.simpletsp;

import java.util.Random;

public class Points
// contains a vector of points with x- and y-coordinate
{
	int N;

	public double X[], Y[];

	public Points(int n) {
		X = new double[n];
		Y = new double[n];
		N = n;
	}

	public void random(Random r)
	// generate random points
	{
		int i;
		for (i = 0; i < N; i++) {
			X[i] = r.nextDouble();
			Y[i] = r.nextDouble();
		}
	}

	public void square(int n, int m)
	// generate points in a nxm grid
	{
		int i, j, k = 0;
		for (i = 0; i < n; i++)
			for (j = 0; j < m; j++) {
				X[k] = i / (double) (n - 1);
				Y[k] = j / (double) (m - 1);
				k++;
			}
	}

	public final int size() {
		return N;
	}

	public void extend(double xn, double yn)
	// add onother point to the vector
	{
		double x[] = new double[N + 1];
		double y[] = new double[N + 1];
		int i;
		for (i = 0; i < N; i++) {
			x[i] = X[i];
			y[i] = Y[i];
		}
		x[N] = xn;
		y[N] = yn;
		X = x;
		Y = y;
		N++;
	}
}
