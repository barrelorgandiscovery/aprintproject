package aprintextensions.fr.freydierepatrice.perfo.gerard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.gui.atrace.Punch;
import org.barrelorgandiscovery.gui.atrace.PunchConverter;
import org.barrelorgandiscovery.gui.atrace.ConverterResult;
import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.tools.TimeUtils;
import org.barrelorgandiscovery.tracetools.ga.GeneticSolver;
import org.barrelorgandiscovery.tracetools.ga.Graph;
import org.barrelorgandiscovery.tracetools.ga.Path;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class PerfoPunchConverter {

	private static Logger logger = Logger.getLogger(PerfoPunchConverter.class);

	/**
	 * Listes de pages ...
	 */
	private ArrayList<ArrayList<Punch>> pages = null;

	private VirtualBook vb = null;

	private IssueCollection ic = null;

	private double relativexpunch;
	private double relativeypunch;
	private double pagesize = Double.NaN;

	private boolean haserrors = false;

	public PerfoPunchConverter(VirtualBook vb) {

		VirtualBook flattened = new VirtualBook(vb.getScale());

		ArrayList<Hole> orderedHolesCopy = vb.getOrderedHolesCopy();
		for (Iterator iterator = orderedHolesCopy.iterator(); iterator
				.hasNext();) {
			Hole hole = (Hole) iterator.next();
			flattened.addAndMerge(hole);
		}

		this.vb = flattened;
	}

	/**
	 * Convertis les trous en coups de poinçons
	 * 
	 * @return
	 */
	public boolean convertToPunchPage(double longueurpoinconsenscarton,
			double largeurpoinconsenshauteurpiste, double pagesize,
			double avancement, double lastpoincondistance) {

		relativexpunch = -longueurpoinconsenscarton / 2;
		relativeypunch = 0; // -largeurpoinconsenshauteurpiste / 2;

		this.pagesize = pagesize;

		PunchConverter pc = new PunchConverter(vb.getScale(),
				longueurpoinconsenscarton, longueurpoinconsenscarton
						- avancement, lastpoincondistance);

		ConverterResult<Punch> result = pc.convert(vb.getOrderedHolesCopy());

		this.ic = result.holeerrors;

		// conversion des holes en pages ...

		if (ic.size() > 0) {
			logger
					.warn("le carton a des erreurs, on ne peut pas le convertir en punch");
			this.haserrors = true;
		}

		Punch[] p = result.result;
		for (int i = 0; i < p.length; i++) {
			Punch punch = p[i];
			punch.x += relativexpunch;
			punch.y += relativeypunch;
		}

		// pour le trie en fonction du X, pour les mettre en page ...
		TreeSet<Punch> t = new TreeSet<Punch>(new Comparator<Punch>() {
			public int compare(Punch o1, Punch o2) {

				int res = Double.compare(o1.x, o2.x);
				if (res == 0)
					return Double.compare(o1.y, o2.y);
				return res;
			}
		});

		for (int i = 0; i < p.length; i++) {
			Punch punch = p[i];
			t.add(punch);
		}

		ArrayList<ArrayList<Punch>> pages = new ArrayList<ArrayList<Punch>>();

		for (Iterator<Punch> iterator = t.iterator(); iterator.hasNext();) {
			Punch currentpunch = (Punch) iterator.next();
			int currentpageindex = (int) (currentpunch.x / pagesize);
			while (pages.size() < currentpageindex + 1) {
				pages.add(new ArrayList<Punch>());
			}
			ArrayList<Punch> currentpage = pages.get(currentpageindex);
			currentpage.add(currentpunch);
		}

		this.pages = pages;

		return false;
	}

	public IssueCollection getIssues() {
		return this.ic;
	}

	public void optimize(org.barrelorgandiscovery.gui.aprintng.IAPrintWait ref,
			ICancelTracker cancelTracker) {

		assert pages != null;

		// on les prends par pages ...

		int cptpage = 1;

		long estimatePageTime = -1;

		for (Iterator<ArrayList<Punch>> iterator = pages.iterator(); iterator
				.hasNext();) {
			ArrayList<Punch> currentpage = iterator.next();

			long start = System.currentTimeMillis();

			String message = "Traitement page " + cptpage + " sur "
					+ pages.size();

			if (estimatePageTime != -1) {
				message += " - temps estimé restant : "
						+ TimeUtils.toMinSecs(((long) pages.size() - cptpage)
								* estimatePageTime * 1000);
			}

			ref.infiniteChangeText(message);

			Punch[] p = currentpage.toArray(new Punch[0]);

			if (p != null && p.length > 0) {
				// optimisation ...

				Graph g = new Graph(p);
				g.setDistance(p.length - 1, 0, -10000);

				GeneticSolver gs = new GeneticSolver(g, 100);

				for (int j = 0; j < 50; j++) {

					if (cancelTracker != null && cancelTracker.isCanceled())
						return;

					gs.doOneGenerationModified();

					if (j % 100 == 0) {
						logger.debug("longueur du tracé :"
								+ gs.getSolution().getLength());
					}
				}

				Path path = gs.getSolution();

				// on ajoute le résultat
				int cpt = 0;
				while (path.getPath()[cpt % path.getPath().length] != 0)
					cpt++;

				currentpage.clear();

				for (int j = 0; j < path.getPath().length; j++) {
					currentpage.add(p[path.getPath()[(cpt + j)
							% path.getPath().length]]);
				}

			}
			cptpage++;

			long optimizePageTime = System.currentTimeMillis() - start;

			if (estimatePageTime == -1)
				estimatePageTime = optimizePageTime;
			else
				estimatePageTime = (estimatePageTime + optimizePageTime) / 2;

		}

	}

	public int getPageCount() {
		return pages.size();
	}

	public ArrayList<Punch> getPage(int index) {
		return pages.get(index);
	}

	public Punch[] getPunchesCopy() {
		ArrayList<Punch> p = new ArrayList<Punch>();
		for (Iterator<ArrayList<Punch>> iterator = pages.iterator(); iterator
				.hasNext();) {
			ArrayList<Punch> pagepunches = iterator.next();
			p.addAll(pagepunches);
		}

		return p.toArray(new Punch[0]);
	}

	public boolean hasErrors() {
		return this.haserrors;
	}

	public double getPagesize() {
		return pagesize;
	}

}
