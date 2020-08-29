package org.barrelorgandiscovery.optimizers.ga;

import java.util.Random;

import org.barrelorgandiscovery.optimizers.model.OptimizedObject;
import org.barrelorgandiscovery.optimizers.model.Punch;


public class Graph<T extends OptimizedObject> {


	private double distances[][];

	private int count;

	private Random rand;

	private T[] points;
	
	
	public Graph() {
		count = 0;
		rand = new Random();
	}

	private double sq(double a)
	{
		return a * a;
	}
	
	public Graph(T[] punches)
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
				distances[i][j] = Math.sqrt( sq(points[i].lastX() - points[j].firstX()) +
											sq(points[i].lastY() - points[j].firstY()));
			}
		}
	}
	

	public int getPointCount() {
		return count;
	}

	public T getPoint(int i) {
		return points[i];
	}

	public double getDistance(int i, int j) {
		return distances[i][j];
	}

	public void setDistance(int i, int j, double distance) {
		distances[i][j] = distance;
	}

}
