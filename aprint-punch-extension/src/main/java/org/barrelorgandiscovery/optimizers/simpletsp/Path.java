package org.barrelorgandiscovery.optimizers.simpletsp;

import java.util.Random;

public class Path
// A path in a graph.
// From[i] is the index of the point leading to i.
// To[i] the index of the point after i.
// The path can optimize itself in a graph.
{
	Graph G;

	int N;

	double L;

	public int From[], To[];

	public Path(Graph g) {
		N = g.size();
		G = g;
		From = new int[N];
		To = new int[N];
	}

	public Object clone()
	// return a clone path
	{
		Path p = new Path(G);
		p.L = L;
		int i;
		for (i = 0; i < N; i++) {
			p.From[i] = From[i];
			p.To[i] = To[i];
		}
		return p;
	}

	public void random(Random r)
	// random path.
	{
		int i, j, i0, j0, k;
		for (i = 0; i < N; i++)
			To[i] = -1;
		for (i0 = i = 0; i < N - 1; i++) {
			j = (int) (r.nextLong() % (N - i));
			To[i0] = 0;
			for (j0 = k = 0; k < j; k++) {
				j0++;
				while (To[j0] != -1)
					j0++;
			}
			while (To[j0] != -1)
				j0++;
			To[i0] = j0;
			From[j0] = i0;
			i0 = j0;
		}
		To[i0] = 0;
		From[0] = i0;
		getlength();
	}

	public double length() {
		return L;
	}

	public boolean improve()
	// try to find another path with shorter length
	// using removals of points j and inserting i,j,i+1
	{
		int i, j, h;
		double d1, d2;
		double H[] = new double[N];
		for (i = 0; i < N; i++)
			H[i] = -G.distance(From[i], i) - G.distance(i, To[i])
					+ G.distance(From[i], To[i]);
		for (i = 0; i < N; i++) {
			d1 = -G.distance(i, To[i]);
			j = To[To[i]];
			while (j != i) {
				d2 = H[j] + G.distance(i, j) + G.distance(j, To[i]) + d1;
				if (d2 < -1e-10) {
					h = From[j];
					To[h] = To[j];
					From[To[j]] = h;
					h = To[i];
					To[i] = j;
					To[j] = h;
					From[h] = j;
					From[j] = i;
					getlength();
					return true;
				}
				j = To[j];
			}
		}
		return false;
	}

	public boolean improvecross()
	// improve the path locally, using replacements
	// of i,i+1 and j,j+1 with i,j and i+1,j+1
	{
		int i, j, h, h1, hj;
		double d1, d2, d;
		for (i = 0; i < N; i++) {
			d1 = -G.distance(i, To[i]);
			j = To[To[i]];
			d2 = 0;
			d = 0;
			while (To[j] != i) {
				d += G.distance(j, From[j]) - G.distance(From[j], j);
				d2 = d1 + G.distance(i, j) + d + G.distance(To[i], To[j])
						- G.distance(j, To[j]);
				if (d2 < -1e-10) {
					h = To[i];
					h1 = To[j];
					To[i] = j;
					To[h] = h1;
					From[h1] = h;
					hj = i;
					while (j != h) {
						h1 = From[j];
						To[j] = h1;
						From[j] = hj;
						hj = j;
						j = h1;
					}
					From[j] = hj;
					getlength();
					return true;
				}
				j = To[j];
			}
		}
		return false;
	}

	void getlength()
	// compute the length of the path
	{
		L = 0;
		int i;
		for (i = 0; i < N; i++) {
			L += G.distance(i, To[i]);
		}
	}

	void localoptimize()
	// find a local optimum starting from this path
	{
		do {
			while (improve())
				;
		} while (improvecross());
	}
}
