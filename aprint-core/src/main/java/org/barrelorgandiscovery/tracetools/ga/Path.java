package org.barrelorgandiscovery.tracetools.ga;

import java.util.Random;

public class Path  {

	private Path() {
		optDone = false;
	}

	private Path(Graph m, boolean randomize) {
		optDone = false;
		map = m;
		path = new int[m.getPointCount()];
		for (int i = 0; i < m.getPointCount(); i++)
			path[i] = i;

		if (randomize) {
			for (int i = 0; i < map.getPointCount() * 2; i++) {
				int rand1 = rand.nextInt(map.getPointCount());
				int rand2 = rand.nextInt(map.getPointCount());
				int buf = path[rand1];
				path[rand1] = path[rand2];
				path[rand2] = buf;
			}

		}
	}

	public static Path getRegularTour(Graph m) {
		return new Path(m, false);
	}

	public static Path getRandomTour(Graph m) {
		return new Path(m, true);
	}

	public int getAt(int n) {
		return path[n];
	}

	public void setAt(int n, int val) {
		path[n] = val;
	}

	public int getCount() {
		return path.length;
	}

	public int[] getPath() {
		return path;
	}

	public double getLength() {
		double dist = 0.0D;
		for (int i = 0; i < path.length - 1; i++)
			dist += map.getDistance(path[i], path[i + 1]);

		dist += map.getDistance(path[0], path[path.length - 1]);
		return dist;
	}

	public void reverse(int startIndex, int stopIndex) {
		if (startIndex >= stopIndex || startIndex >= path.length
				|| stopIndex < 0)
			return;
		for (; startIndex < stopIndex; stopIndex--) {
			int tmp = path[startIndex];
			path[startIndex] = path[stopIndex];
			path[stopIndex] = tmp;
			startIndex++;
		}

	}

	public boolean isOptDone() {
		return optDone;
	}

	public void do2Opt() {
		if (optDone)
			return;
		boolean done = false;
		int count = path.length;
		for (int k = 0; k < count && !done; k++) {
			done = true;
			for (int i = 0; i < count; i++) {
				for (int j = i + 2; j < count; j++)
					if (map.getDistance(path[i], path[(i + 1) % count])
							+ map.getDistance(path[j], path[(j + 1) % count]) > map
							.getDistance(path[i], path[j])
							+ map.getDistance(path[(i + 1) % count],
									path[(j + 1) % count])) {
						int tmp = path[(i + 1) % count];
						path[(i + 1) % count] = path[j];
						path[j] = tmp;
						reverse(i + 2, j - 1);
						done = false;
					}

			}

		}

		optDone = true;
	}

	/*
	 
	public int compareTo(Object o) {
		
		int v = (int) (getLength() - ((Path) o).getLength());
		
		if (v == 0)
		{
			if (!this.equals(o))
				v = (o.hashCode() < hashCode() ? -1 : 1);
		}
		
		
		return v;
	}
*/
	public Object clone() {
		Path t = new Path();
		t.map = map;
		t.path = (int[]) path.clone();
		return t;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < path.length; i++) {
			buf.append(path[i]);
			if (i != path.length - 1)
				buf.append(", ");
		}

		buf.append("]");
		return buf.toString();
	}

	static Random rand = new Random();

	Graph map;

	int path[];

	boolean optDone;

}
