package org.barrelorgandiscovery.tracetools.ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.gui.atrace.Optimizer;
import org.barrelorgandiscovery.gui.atrace.OptimizerProgress;
import org.barrelorgandiscovery.gui.atrace.OptimizerResult;
import org.barrelorgandiscovery.gui.atrace.Punch;
import org.barrelorgandiscovery.gui.atrace.PunchConverter;
import org.barrelorgandiscovery.gui.atrace.Tools;
import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class GeneticOptimizer implements Optimizer<Punch> {

	private static final Logger logger = Logger
			.getLogger(GeneticOptimizer.class);

	private double poinconsize = 3.0; // par défaut

	private double recouvrement = 0.5;

	private double notPunchedIfLessThan = 0.0;
	
	private double pageSize = 200.0 ; // 20cm

	public GeneticOptimizer() {

	}

	public GeneticOptimizer(GeneticOptimizerParameters parameters) {
		assert parameters != null;

		this.poinconsize = parameters.getPunchWidth();
		this.recouvrement = parameters.getOverlap();
		this.notPunchedIfLessThan = parameters.getNotPunchedIfLessThan();
		this.pageSize = parameters.getPageSize();
	}

	@Override
	public String getTitle() {
		return Messages.getString("GeneticOptimizer.0"); //$NON-NLS-1$
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	/**
	 * optimize and report progress
	 * 
	 * @param carton
	 * @param progress
	 * @return
	 * @throws Exception
	 */
	public OptimizerResult<Punch> optimize(VirtualBook carton,
			final OptimizerProgress progress, final ICancelTracker ct)
			throws Exception {

		// Découpage en pages du carton ...

		if (progress != null) {
			try {
				progress.report(0, new Punch[0],
						Messages.getString("GeneticOptimizer.1")); //$NON-NLS-1$
			} catch (Exception ex) {
				logger.debug(
						Messages.getString("GeneticOptimizer.2") + ex.getMessage(), ex); //$NON-NLS-1$
			}
		}

		
		if (ct != null && ct.isCanceled())
			return null;
		
		final PunchConverter pc = new PunchConverter(carton.getScale(),
				poinconsize, recouvrement, notPunchedIfLessThan);
		
		OptimizerResult<Punch> converterResult = pc.convert(carton.getOrderedHolesCopy());
		
		Punch[] allPunches = converterResult.result;
		assert allPunches !=null;

		// sort by x
		Arrays.sort(allPunches, new Comparator<Punch>() {
			@Override
			public int compare(Punch o1, Punch o2) {
				int c = Double.compare(o1.x, o2.x);
				if (c != 0)
					return c;
				return Double.compare(o1.y,o2.y);
			}
		});
		
		
		final ArrayList<ArrayList<Punch>> pagePunches = Tools.divide(Arrays.asList(allPunches),pageSize );

		final ArrayList<Punch>[] pagesPunchesResult = (ArrayList<Punch>[]) new ArrayList[pagePunches
				.size()];
		
		
		// final IssueCollection[] errors = new IssueCollection[pages.size()];

		ExecutorService e = Executors.newFixedThreadPool(Runtime.getRuntime()
				.availableProcessors());

		for (int i = 0; i < pagePunches.size(); i++) {

			if (ct != null && ct.isCanceled())
				return null;
			
			
			final int currentPage = i;
			Callable<Void> c = new Callable<Void>() {
				@Override
				public Void call() throws Exception {

					if (ct != null && ct.isCanceled())
						return null;

					ArrayList<Punch> listpunch = new ArrayList<>();

					ArrayList<Punch> originPunchList = pagePunches.get(currentPage);
					
					Punch[] p = originPunchList.toArray(new Punch[originPunchList.size()]);

					if (p != null && p.length > 0) {
						// optimisation ...

						Graph g = new Graph(p);
						g.setDistance(p.length - 1, 0, -10000);

						GeneticSolver gs = new GeneticSolver(g, 100);

						for (int j = 0; j < 50; j++) { // 50 GENS

							if (ct != null && ct.isCanceled())
								return null;

							gs.doOneGenerationModified();
							if (j % 100 == 0) {
								logger.debug("longueur du tracé :" //$NON-NLS-1$
										+ gs.getSolution().getLength());
							}
						}

						Path path = gs.getSolution();

						// on ajoute le résultat
						int cpt = 0;
						while (path.getPath()[cpt % path.getPath().length] != 0)
							cpt++;

						for (int j = 0; j < path.getPath().length; j++) {
							listpunch.add(p[path.getPath()[(cpt + j)
									% path.getPath().length]]);
						}

					}

					pagesPunchesResult[currentPage] = listpunch;

					if (progress != null) {
						try {

							// consolidate the results to show it
							ArrayList<Punch> finalResult = new ArrayList<Punch>();
							for (ArrayList<Punch> punches : pagesPunchesResult) {
								if (punches != null) {
									finalResult.addAll(punches);
								}
							}

							progress.report(currentPage * 1.0 / pagePunches.size(),
									finalResult.toArray(new Punch[finalResult
											.size()]),
									Messages.getString("GeneticOptimizer.4") //$NON-NLS-1$
											+ currentPage + "/" + pagePunches.size()); //$NON-NLS-1$

						} catch (Exception ex) {
							logger.debug("reporter send exception :" //$NON-NLS-1$
									+ ex.getMessage(), ex);
						}
					}
					return null;
				};

			};

			e.submit(c);
		}

		e.shutdown();
		e.awaitTermination(60, TimeUnit.MINUTES);

		if (ct != null && ct.isCanceled())
			return null;
		
		
		// collect results

		OptimizerResult<Punch> result = new OptimizerResult<Punch>();

		IssueCollection errorResult = new IssueCollection();

		ArrayList<Punch> finalResult = new ArrayList<Punch>();
		for (ArrayList<Punch> punches : pagesPunchesResult) {
			if (punches != null) // might be null if not assigned
				finalResult.addAll(punches);
		}

		errorResult.addAll(converterResult.holeerrors);
		
		
		result.result = finalResult.toArray(new Punch[finalResult.size()]);
		result.holeerrors = errorResult;

		return result;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.atrace.Optimizer#optimize(fr.freydierepatrice.
	 * cartonvirtuel.CartonVirtuel)
	 */
	public OptimizerResult optimize(VirtualBook carton) throws Exception {
		return optimize(carton, null, null);
	}

	/**
	 * get the perforator size
	 * 
	 * @return
	 */
	public double getPoinconsize() {
		return poinconsize;
	}

	public double getRecouvrement() {
		return recouvrement;
	}

	public void setRecouvrement(double recouvrement) {
		this.recouvrement = recouvrement;
	}

	public double getNotPunchedIfLessThan() {
		return notPunchedIfLessThan;
	}

	public void setNotPunchedIfLessThan(double notPunchedIfLessThan) {
		this.notPunchedIfLessThan = notPunchedIfLessThan;
	}

	/**
	 * set new perforator size
	 * 
	 * @param poinconsize
	 *            size
	 */
	public void setPoinconsize(double poinconsize) {
		this.poinconsize = poinconsize;
	}

	@Override
	public Object getDefaultParameters() {
		return new GeneticOptimizerParameters();
	}

}
