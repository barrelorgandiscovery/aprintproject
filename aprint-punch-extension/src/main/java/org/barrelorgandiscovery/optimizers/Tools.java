package org.barrelorgandiscovery.optimizers;

import java.util.ArrayList;
import java.util.List;

import org.barrelorgandiscovery.optimizers.model.Punch;
import org.barrelorgandiscovery.virtualbook.Hole;

public class Tools {

	/**
	 * Divise les notes par paquets
	 * 
	 * @param notes
	 * @param number
	 *            nombre de notes par paquets
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
	 * @param pageSize
	 *            the page size, from 0.0
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<ArrayList<Punch>> divide(List<Punch> modifiablePunches,
			double pageSize) throws Exception {

		assert modifiablePunches != null;
		if (pageSize <= 0)
			throw new Exception("bad parameters");

		ArrayList<Punch> punches = new ArrayList<Punch>(modifiablePunches);
		
		ArrayList<ArrayList<Punch>> pages = new ArrayList<>();

		ArrayList<Punch> pagenote = new ArrayList<>();

		double distance = 0.0;

		while (punches.size() > 0) {

			Punch p = punches.get(0);
			assert p != null;

			while (p != null && p.x < distance + pageSize) {

				punches.remove(0);
				pagenote.add(p);

				if (punches.size() > 0) {
					p = punches.get(0);
				} else {
					p = null;
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
