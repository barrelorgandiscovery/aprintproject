package org.barrelorgandiscovery.optimizers.cad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.cad.CADParameters;
import org.barrelorgandiscovery.extensionsng.perfo.cad.CADVirtualBookExporter;
import org.barrelorgandiscovery.extensionsng.perfo.cad.canvas.LayerFilter;
import org.barrelorgandiscovery.extensionsng.perfo.cad.canvas.PowerCallBack;
import org.barrelorgandiscovery.extensionsng.perfo.cad.canvas.PunchPlanDeviceDrawing;
import org.barrelorgandiscovery.extensionsng.perfo.ng.tools.PunchClassloaderSerializeTools;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.optimizers.Optimizer;
import org.barrelorgandiscovery.optimizers.OptimizerProgress;
import org.barrelorgandiscovery.optimizers.OptimizerResult;
import org.barrelorgandiscovery.optimizers.Tools;
import org.barrelorgandiscovery.optimizers.ga.GeneticSolver;
import org.barrelorgandiscovery.optimizers.ga.Graph;
import org.barrelorgandiscovery.optimizers.ga.Path;
import org.barrelorgandiscovery.optimizers.model.CutLine;
import org.barrelorgandiscovery.optimizers.model.Extent;
import org.barrelorgandiscovery.optimizers.model.GroupedCutLine;
import org.barrelorgandiscovery.optimizers.model.OptimizedObject;

import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class XOptim implements Optimizer<OptimizedObject> {

	private static Logger logger = Logger.getLogger(XOptim.class);

	private XOptimParameters parameters = new XOptimParameters();

	// used for classloading issues
	private PunchClassloaderSerializeTools serializeTools = new PunchClassloaderSerializeTools();

	public XOptim() {

	}

	public XOptim(XOptimParameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public String getTitle() {
		return "Simple Lazer Optimizer";
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public Object getDefaultParameters() {
		return parameters;
	}

	/**
	 * because we use the export function to convert the book to cad this function
	 * creates the associated CADParameters
	 * 
	 * @return
	 */
	CADParameters constructCADParameters() {
		CADParameters parameters = new CADParameters();
		parameters.setExportDecoupeDesBords(false);
		parameters.setExportPliures(this.parameters.isExportPliures());
		parameters.setPasDePontSiIlReste(this.parameters.getPasDePontSiIlReste());
		parameters.setNombreDePlisAAjouterAuDebut(this.parameters.getNombreDePlisAAjouterAuDebut());
		parameters.setPont(this.parameters.getPont());
		parameters.setTaillePagePourPliure(this.parameters.getTaillePagePourPliure());
		parameters.setTailleTrous(this.parameters.getTailleTrous());
		parameters.setTypePliure(this.parameters.getTypePliure());
		parameters.setTypeTrous(this.parameters.getTypeTrous());
		parameters.setTypePonts(this.parameters.getTypePonts());
		parameters.setExportTrous(this.parameters.isExportTrous());
		parameters.setSurchargeLargeurTrous(this.parameters.isSurchargeLargeurTrous());
		parameters.setLargeurTrous(this.parameters.getLargeurTrous());
		return parameters;
	}

	@Override
	public OptimizerResult<OptimizedObject> optimize(VirtualBook carton) throws Exception {
		return optimize(carton, null, null);
	}

	/**
	 * this function change the power and speed parameters, and handle the pass1 and
	 * pass2 elements
	 * 
	 * @param objects
	 * @return
	 */
	private OptimizedObject[] handle2Passes(OptimizedObject[] objects) {
		if (objects == null || objects.length == 0) {
			// nothing to do, and avoid npe on for, below
			return new OptimizedObject[0];
		}

		ArrayList<OptimizedObject> resultlist = new ArrayList<>();

		for (OptimizedObject o : objects) {

			if (o instanceof CutLine) {
				// just add it
				resultlist.add(o);
			} else if (o instanceof GroupedCutLine) {

				GroupedCutLine gcl = (GroupedCutLine) o;
				resultlist.add(gcl);

				// multiple pass for pliures
				if (gcl.userInformation != null && gcl.userInformation.equals(CADVirtualBookExporter.LAYER_PLIURES)
						&& parameters.isPliureMultipass() && parameters.getPliuresMultipassPassNumber() >= 1) {
					// handling pliures
					
					GroupedCutLine pass2group = serializeTools.deepClone(gcl);
					List<CutLine> innerLines = gcl.getLinesByRefs();

					// multiple cut
					for (int i = 0; i < parameters.getPliuresMultipassPassNumber(); i++) {

						// change power for the duplicate, and only for cut layers
						assert innerLines != null;
						for (CutLine c : innerLines) {
								c.powerFraction = parameters.getPliuresMultipassPowerFraction();
								c.speedFraction = parameters.getPliuresMultipassSpeedFraction();
						}
						resultlist.add(pass2group);

					}
				} else {

					if (parameters.isHasMultiplePass() && parameters.getMultiplePass() >= 1) {
						logger.debug("multiple pass handling");
						// as we are not in the same classloader
						// serialization tool must be instanciated in this class loader

						GroupedCutLine pass2group = serializeTools.deepClone(gcl);
						List<CutLine> innerLines = gcl.getLinesByRefs();

						// multiple cut
						for (int i = 0; i < parameters.getMultiplePass(); i++) {

							// change power for the duplicate, and only for cut layers
							assert innerLines != null;
							for (CutLine c : innerLines) {
								if (gcl.userInformation == null) {
									c.powerFraction = parameters.getPowerFractionMultiplePass();
									c.speedFraction = parameters.getSpeedFractionMultiplePass();
								}
							}
							resultlist.add(pass2group);

						}
					}
				}
			}
		}

		return resultlist.toArray(new OptimizedObject[resultlist.size()]);
	}

	@Override
	public OptimizerResult<OptimizedObject> optimize(VirtualBook carton, OptimizerProgress progress, ICancelTracker ct)
			throws Exception {
		CADParameters cadparameters = constructCADParameters();
		CADVirtualBookExporter exporter = new CADVirtualBookExporter();

		// create a punch plan device drawing,
		// this
		PunchPlanDeviceDrawing pp = new PunchPlanDeviceDrawing();

		PowerCallBack powerCallBackForHalfCutAndFirstPass = new PowerCallBack() {
			// this function is called to define the power of the drawn object
			@Override
			public double getPowerForLayer(String layer) {
				if (CADVirtualBookExporter.LAYER_PLIURES_NON_CUT.equals(layer)
						|| CADVirtualBookExporter.LAYER_PLIURES_VERSO_NON_CUT.equals(layer)) {
					return parameters.getHalfCutPower();
				} else if (CADVirtualBookExporter.LAYER_PLIURES.equals(layer)) {
					return parameters.getPowerFractionPliures();
				}
				return parameters.getPowerFractionPass1();
			}

			@Override
			public double getSpeedForLayer(String layer) {
				
				if (CADVirtualBookExporter.LAYER_PLIURES_NON_CUT.equals(layer)
						|| CADVirtualBookExporter.LAYER_PLIURES_VERSO_NON_CUT.equals(layer)) {
					return parameters.getHalfCutPower();
				} else if (CADVirtualBookExporter.LAYER_PLIURES.equals(layer)) {
					return parameters.getSpeedFractionPliures();
				}
				
				return parameters.getSpeedFractionPass1();
			}
		};

		pp.setPowerCallBack(powerCallBackForHalfCutAndFirstPass);

		exporter.export(carton, cadparameters, pp);

		ArrayList<OptimizedObject> optimized = pp.getCurrentDraw();

		OptimizerResult<OptimizedObject> result = new OptimizerResult<>();

		result.result = optimized.toArray(new OptimizedObject[optimized.size()]);

		// sort the elements by x, either groups and cut lines
		// this permit to construct pages, in grouping elements
		Arrays.sort(result.result, new Comparator<OptimizedObject>() {
			@Override
			public int compare(OptimizedObject arg0, OptimizedObject arg1) {
				assert arg0 != null;
				assert arg1 != null;

				Extent e = arg0.getExtent();
				Extent e2 = arg1.getExtent();
				int c = Double.compare(e.xmin, e2.xmin);

				if (c != 0)
					return c;
				return Double.compare(e.ymin, e2.ymin);
			}
		});

		// optimize placement section

		double pageSize = parameters.getOptimPageSize() * 10.0;// in mm
		if (pageSize <= 10.0) {
			logger.warn("pageSize at " + pageSize + " mm is too small, extend it to 10.0 mm");
			pageSize = 10.0;
		}
		int MAX_OBJECTS = 300; // in the limit of the given number of objects (otherwise it's too slow)

		final ArrayList<ArrayList<OptimizedObject>> pageObjects = Tools
				.divideOptimizedObjects(Arrays.asList(result.result), pageSize, MAX_OBJECTS);

		final ArrayList<OptimizedObject>[] pagesObjectsResult = (ArrayList<OptimizedObject>[]) new ArrayList[pageObjects
				.size()];

		ExecutorService e = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (int i = 0; i < pageObjects.size(); i++) {

			if (ct != null && ct.isCanceled())
				return null;

			final int currentPage = i;
			Callable<Void> c = new Callable<Void>() {
				@Override
				public Void call() throws Exception {

					if (ct != null && ct.isCanceled())
						return null;

					ArrayList<OptimizedObject> listobject = new ArrayList<>();

					ArrayList<OptimizedObject> originPunchList = pageObjects.get(currentPage);

					OptimizedObject[] p = originPunchList.toArray(new OptimizedObject[originPunchList.size()]);

					// listobject = originPunchList;

					if (p != null && p.length > 0) {

						// optimisation ...
						Graph<OptimizedObject> g = new Graph<>(p);
						g.setDistance(p.length - 1, 0, -10000);

						GeneticSolver gs = new GeneticSolver(g, 100);

						for (int j = 0; j < 50; j++) { // 50 GENS
							if (ct != null && ct.isCanceled())
								return null;
							gs.doOneGenerationModified();
							if (j % 100 == 0) {
								logger.debug("longueur du trac� :" //$NON-NLS-1$
										+ gs.getSolution().getLength());
							}
						}

						Path path = gs.getSolution();

						// on ajoute le r�sultat
						int cpt = 0;
						while (path.getPath()[cpt % path.getPath().length] != 0)
							cpt++;

						for (int j = 0; j < path.getPath().length; j++) {
							listobject.add(p[path.getPath()[(cpt + j) % path.getPath().length]]);
						}

					}

					pagesObjectsResult[currentPage] = listobject;

					if (progress != null) {
						try {

							// consolidate the results to show it
							ArrayList<OptimizedObject> finalResult = new ArrayList<OptimizedObject>();
							for (ArrayList<OptimizedObject> punches : pagesObjectsResult) {
								if (punches != null) {
									finalResult.addAll(punches);
								}
							}

							// report to gui
							progress.report(currentPage * 1.0 / pageObjects.size(),
									finalResult.toArray(new OptimizedObject[finalResult.size()]),
									Messages.getString("GeneticOptimizer.4") //$NON-NLS-1$
											+ currentPage + "/" + pageObjects.size()); //$NON-NLS-1$

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
		e.awaitTermination(60, TimeUnit.MINUTES); // far away

		if (ct != null && ct.isCanceled())
			return null;

		// construct final ....
		ArrayList<OptimizedObject> finalResult = new ArrayList<>();
		for (ArrayList<OptimizedObject> objects : pagesObjectsResult) {
			if (objects != null) { // might be null if not assigned
				finalResult.addAll(objects);
			}
		}

		// handle pass 1 and pass 2, this add additional pass for cutted objects
		result.result = handle2Passes(finalResult.toArray(new OptimizedObject[finalResult.size()]));

		return result;

	}

}
