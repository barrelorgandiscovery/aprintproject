package org.barrelorgandiscovery.tracetools.ga;

import java.util.Random;

import org.barrelorgandiscovery.gui.atrace.Punch;


public class Graph {


	private double distances[][];

	private int count;

	private Random rand;

	private Punch points[];
	
	
	public Graph() {
		count = 0;
		rand = new Random();
	}

	private double sq(double a)
	{
		return a * a;
	}
	
	public Graph(Punch[] punches)
	{
		this();
		this.points = punches;
		this.count = points.length;
		
		this.distances = new double[count][count];
		// calculate distances
		for (int i = 0 ; i < count ; i ++ )
		{
			for (int j = 0 ; j < count ; j ++ )
			{
				distances[i][j] = Math.sqrt( sq(points[i].x - points[j].x) +
											sq(points[i].y - points[j].y));
			}
		}
	}
	

	public int getPointCount() {
		return count;
	}

	public Punch getPoint(int i) {
		return points[i];
	}

	public double getDistance(int i, int j) {
		return distances[i][j];
	}

	public void setDistance(int i, int j, double distance) {
		distances[i][j] = distance;
	}

}
