package org.barrelorgandiscovery.tracetools.ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class GeneticSolver {

	/**
	 * Private constructor for setting default parameters
	 */
	private GeneticSolver() {
		population = 0;
		rand = new Random();
		CROSS_FACTOR = 40;
		MUTE_FACTOR = 20;
	}

	/**
	 * Genetic Solver constructor. this constructor use a 40% crossover factor
	 * and 20% mutation factor
	 * 
	 * @param m
	 * @param pop
	 */
	public GeneticSolver(Graph m, int pop) {
		this(m, pop, 40, 20);
	}

	/**
	 * Genetic solver constructor
	 * 
	 * @param m
	 *            the graph
	 * @param pop
	 *            population size
	 * @param crossFactor
	 * @param muteFactor
	 */
	public GeneticSolver(Graph m, int pop, int crossFactor, int muteFactor) {
		population = 0;
		rand = new Random();
		CROSS_FACTOR = 40;
		MUTE_FACTOR = 20;
		map = m;
		population = pop;
		CROSS_FACTOR = crossFactor;
		MUTE_FACTOR = muteFactor;
		genes = new Path[population];
		for (int i = 0; i < population; i++)
			genes[i] = Path.getRandomTour(m);

		sort();
	}

	/**
	 * Get the solution
	 * 
	 * @return
	 */
	public Path getSolution() {
		return genes[0];
	}

	public void doOneGeneration() {
		int toCross = (population * CROSS_FACTOR) / 100;
		int toMute = (population * MUTE_FACTOR) / 100;
		eliminate(toCross, toMute);
		cross(toCross, toMute);
		mute(toCross, toMute);
		sort();
	}

	public void doOneGenerationModified() {
		int toCross = (population * CROSS_FACTOR) / 100;
		int toMute = (population * MUTE_FACTOR) / 100;
		eliminate(toCross, toMute);
		cross(toCross, toMute);
		mute2(toCross, toMute);
		sort();
	}

	private void eliminate(int toCross, int toMute) {
		double l = genes[0].getLength();
		for (int i = 1; i < population - toCross - toMute; i++)
			if (genes[i].getLength() == l)
				genes[i] = Path.getRandomTour(map);
			else
				l = genes[i].getLength();

	}

	private void cross(int toCross, int toMute) {
		int outputStart = population - toCross - toMute;
		for (int i = outputStart; i < population - toMute; i++)
			genes[i] = cross2Genes(genes[rand.nextInt(outputStart)],
					genes[rand.nextInt(outputStart)]);

	}

	public Path cross2Genes(Path g1, Path g2) {
		Path child = Path.getRegularTour(map);
		ArrayList<Integer> tmpArray = new ArrayList<Integer>();
		int town = rand.nextInt(map.getPointCount());
		boolean fg1 = true;
		boolean fg2 = true;
		int townInG1 = 0;
		int townInG2 = 0;
		for (int i = 0; i < map.getPointCount(); i++) {
			if (g1.getAt(i) == town)
				townInG1 = i;
			if (g2.getAt(i) == town)
				townInG2 = i;
		}

		tmpArray.add(new Integer(town));
		do {
			townInG1--;
			townInG2++;
			if (townInG1 < 0)
				fg1 = false;
			if (townInG2 >= map.getPointCount())
				fg2 = false;
			if (fg1) {
				Integer tg1 = new Integer(g1.getAt(townInG1));
				if (tmpArray.contains(tg1))
					fg1 = false;
				else
					tmpArray.add(0, tg1);
			}
			if (fg2) {
				Integer tg2 = new Integer(g2.getAt(townInG2));
				if (tmpArray.contains(tg2))
					fg2 = false;
				else
					tmpArray.add(tg2);
			}
		} while (fg1 || fg2);
		if (tmpArray.size() < map.getPointCount()) {
			Path randTour = Path.getRandomTour(map);
			for (int i = 0; i < map.getPointCount(); i++) {
				Integer rt = new Integer(randTour.getAt(i));
				if (!tmpArray.contains(rt))
					tmpArray.add(rt);
			}

		}
		for (int i = 0; i < map.getPointCount(); i++)
			child.setAt(i, ((Integer) tmpArray.get(i)).intValue());

		return child;
	}

	private void mute(int toCross, int toMute) {
		genes[population - toMute] = (Path) genes[0].clone();
		genes[population - toMute].do2Opt();
		for (int k = 1; k < toMute; k++) {
			Path oldGen = genes[rand.nextInt(population - toMute)];
			if (!oldGen.isOptDone()) {
				Path newGen = (Path) oldGen.clone();
				newGen.do2Opt();
				genes[(k + population) - toMute] = newGen;
			}
		}

	}

	private void mute2(int toCross, int toMute) {
		for (int k = 0; k < toMute; k++) {
			Path oldGen;
			if (k == 0)
				oldGen = genes[0];
			else
				oldGen = genes[rand.nextInt(population - toMute)];
			Path newGen = (Path) oldGen.clone();
			if (!oldGen.isOptDone()) {
				newGen.do2Opt();
			} else {
				int n1 = rand.nextInt(map.getPointCount());
				int n2 = rand.nextInt(map.getPointCount());
				int tmp = newGen.getAt(n1);
				newGen.setAt(n1, newGen.getAt(n2));
				newGen.setAt(n2, tmp);
				newGen.do2Opt();
			}
			genes[(k + population) - toMute] = newGen;
		}

	}

	private void sort() {
		Arrays.sort(genes, new Comparator<Path>() {
			public int compare(Path o1, Path o2) {
				int v = 0;
				if (o1.getLength() > o2.getLength()) {
					v = 1;
				} else if (o1.getLength() < o2.getLength()) {
					v = -1;
				} else {
					// v == 0;
					if (!o1.equals(o2))
						v = (o1.hashCode() < o2.hashCode() ? -1 : 1);

				}

				return v;

			}
		});
	}

	Graph map;

	int population;

	Random rand;

	Path genes[];

	int CROSS_FACTOR;

	int MUTE_FACTOR;
}
