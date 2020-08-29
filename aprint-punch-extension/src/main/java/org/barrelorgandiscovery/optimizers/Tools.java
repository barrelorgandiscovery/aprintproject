package org.barrelorgandiscovery.optimizers;

import java.util.ArrayList;
import java.util.List;

import org.barrelorgandiscovery.optimizers.model.Extent;
import org.barrelorgandiscovery.optimizers.model.OptimizedObject;
import org.barrelorgandiscovery.optimizers.model.Punch;
import org.barrelorgandiscovery.virtualbook.Hole;

public class Tools {

	/**
	 * Divide holes by number
	 * 
	 * @param notes
	 * @param number nombre de notes par paquets
	 * @return
	 */
	public static ArrayList<ArrayList<Hole>> divide(List<Hole> holes, int number) {

		ArrayList<Hole> modifiableHoleList = new ArrayList<>(holes);

		ArrayList<ArrayList<Hole>> pages = new ArrayList<ArrayList<Hole>>();

		ArrayList<Hole> pagenote = new ArrayList<Hole>();

		for (int i = 0; i < modifiableHoleList.size(); i++) {
			pagenote.add(modifiableHoleList.get(i));
			if (i % number == 0 && i > 0) {
				pages.add(pagenote);
				pagenote = new ArrayList<Hole>();
			}
		}
		pages.add(pagenote);

		return pages;
	}

	/**
	 * divide the punches into pages
	 * 
	 * @param punches
	 * @param pageSize the page size, from 0.0
	 * @return
	 * @throws Exception
	 */
	public static <T extends OptimizedObject>  ArrayList<ArrayList<T>> divide(List<T> modifiablePunches, double pageSize) throws Exception {
		return divideOptimizedObjects(modifiablePunches, pageSize, 200);
	}

	/**
	 * divide the punches into pages
	 * 
	 * @param punches
	 * @param pageSize the page size, from 0.0
	 * @return
	 * @throws Exception
	 */
	public static <T extends OptimizedObject> ArrayList<ArrayList<T>> divideOptimizedObjects(List<T> objectsList,
			double pageSize, int maxObjects) throws Exception {

		assert objectsList != null;
		if (pageSize <= 0)
			throw new Exception("bad parameters");

		ArrayList<T> objectListCopy = new ArrayList<T>(objectsList);
		ArrayList<ArrayList<T>> pages = new ArrayList<>();
		ArrayList<T> pagenote = new ArrayList<>();

		double distance = 0.0;

		while (objectListCopy.size() > 0) {
			int objectsCountPerPage = 0;
			T p = objectListCopy.get(0);
			assert p != null;

			Extent extent = p.getExtent();

			while (p != null && extent.xmin < distance + pageSize) {

				objectListCopy.remove(0);
				pagenote.add(p);

				if (objectListCopy.size() > 0) {
					p = objectListCopy.get(0);
				} else {
					p = null;
				}
				
				if (objectsCountPerPage++ > maxObjects) {
					pages.add(pagenote);
					pagenote = new ArrayList<>();
					objectsCountPerPage = 0;
				}
			}

			pages.add(pagenote);
			pagenote = new ArrayList<>();
			distance += pageSize;

		}

		pages.add(pagenote);

		return pages;
	}

}
