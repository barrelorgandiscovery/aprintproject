package org.barrelorgandiscovery.extensionsng.perfo.cad;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.barrelorgandiscovery.extensionsng.perfo.cad.canvas.DXFDeviceDrawing;
import org.barrelorgandiscovery.extensionsng.perfo.cad.canvas.DeviceDrawing;
import org.barrelorgandiscovery.extensionsng.perfo.cad.canvas.SVGDeviceDrawing;
import org.barrelorgandiscovery.extensionsng.perfo.cad.drawer.ArrondiHoleDrawer;
import org.barrelorgandiscovery.extensionsng.perfo.cad.drawer.HoleDrawer;
import org.barrelorgandiscovery.extensionsng.perfo.cad.drawer.RectangularHoleDrawer;
import org.barrelorgandiscovery.extensionsng.perfo.cad.drawer.RondHoleDrawer;
import org.barrelorgandiscovery.extensionsng.perfo.cad.math.Vect;
import org.barrelorgandiscovery.repository.Repository;
import org.barrelorgandiscovery.repository.RepositoryFactory;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractTransformation;
import org.barrelorgandiscovery.virtualbook.transformation.LinearTransposition;
import org.barrelorgandiscovery.virtualbook.transformation.importer.LinearMidiImporter;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiConversionResult;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFile;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFileIO;
import org.barrelorgandiscovery.virtualbook.transformation.importer.Utils;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * this class export the book drawing to a device, this can be display device,
 * or DXF, SVG, or GCODE
 * 
 * @author pfreydiere
 * 
 */
public class CADVirtualBookExporter {

	private static Logger logger = Logger.getLogger(CADVirtualBookExporter.class);

	public final static String LAYER_BORDS = "BORDS_CARTON";

	public final static String LAYER_TROUS = "TROUS";

	public final static String LAYER_PLIURES = "PLIURES";

	public final static String LAYER_PLIURES_NON_CUT = "PLIURES_NON_CUT";

	public final static String LAYER_PLIURES_VERSO = "PLIURES_VERSO";

	public final static String LAYER_PLIURES_VERSO_NON_CUT = "PLIURES_VERSO_NON_CUT";

	public final static String LAYER_REFERENCE = "REFERENCE";

	public static String[] LAYERS = new String[] { LAYER_BORDS, LAYER_TROUS, LAYER_PLIURES, LAYER_PLIURES_VERSO,
			LAYER_REFERENCE, LAYER_PLIURES_NON_CUT, LAYER_PLIURES_VERSO_NON_CUT };

	public CADVirtualBookExporter() {
	}

	/**
	 * Exporte un carton en fichier DXF
	 * 
	 * @param vb          le carton à exporter
	 * @param mecanique   booleen indiquant si le type est mecanique
	 * @param tailleTrous taille des trous dans le cas du pneumatique
	 * @param ponts       taille des ponts pour le pneumatique
	 * @throws Exception
	 */
	public void export(VirtualBook vb, CADParameters parameters, DeviceDrawing device) throws Exception {

		CADParameters p = CADParameters.class.newInstance();

		// clone the parameters, in serializing them
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(baos);
		parameters.writeExternal(os);
		os.close();

		p.readExternal(new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())));

		ArrayList<Hole> holesCopy = vb.getOrderedHolesCopy();

		logger.debug("hole to translate :" + holesCopy.size());

		Scale scale = vb.getScale();

		double xratio = 1.0 / 1_000_000.0 * scale.getSpeed();

		// adding the vb edges

		long firstTimeStamp = vb.getFirstHoleStart();
		if (firstTimeStamp == Long.MAX_VALUE)
			throw new Exception("no holes in the book, cannot compute");

		// calc the beginning offset of the book
		double startBook = firstTimeStamp * xratio - p.getStartBookAdjustementFromBeginning()
				- p.getNombreDePlisAAjouterAuDebut() * p.getTaillePagePourPliure();

		// startBook can be negative ...

		double vbend = vb.getLength() * xratio;
		// round the end to a integer value of the page size
		vbend = (Math.ceil((vbend - startBook) / p.getTaillePagePourPliure()) + 1) * p.getTaillePagePourPliure()
				+ startBook;

		if (p.isExportDecoupeDesBords()) {

			logger.debug("ecriture des bords");

			device.setCurrentLayer(LAYER_BORDS);

			device.startGroup();
			try {
				// ligne horizontale
				device.drawLine(startBook, 0, vbend, 0);
			} finally {
				device.endGroup();
			}
			// verticales ?
			device.startGroup();
			try {
				device.drawLine(vbend, scale.getWidth(), startBook, scale.getWidth());
			} finally {
				device.endGroup();
			}

		}

		device.setCurrentLayer(LAYER_REFERENCE);

		// draw the reference arrow
		// dest
		double arrowy = 0;

		// origin
		double arrowlength = 30; // length of the reference arrow

		Coordinate origin = new Coordinate(10 /* X */, arrowy + arrowlength);
		device.startGroup();
		try {

			Vect arrowVector = new Vect(0, -arrowlength);

			if (!device.ignoreReference() && !scale.isPreferredViewedInversed()) {
				// reference is at the bottom
				// move origin, and revert the arrow vector
				origin = new Coordinate(10, scale.getWidth() - arrowlength);
				// revert the arrow
				arrowVector = arrowVector.moins();
			}

			device.drawArrow(arrowVector, origin, 10);

		} finally {
			device.endGroup();
		}

		if (p.isExportPliures()) {

			logger.debug("ajout pliures ... ");

			// Ecriture des pliures

			device.setCurrentLayer(LAYER_PLIURES_VERSO);
			double start = startBook;

			while (start <= vbend + 1) {

				if (p.getTypePliure() == TypePliure.POINTILLEE) {
					device.startGroup();
					try {
						device.drawImprovedDottedLines(start, 0, start, scale.getWidth(), 2, 5);
					} finally {
						device.endGroup();
					}
				} else if (p.getTypePliure() == TypePliure.CONTINUE) {

					device.setCurrentLayer(LAYER_PLIURES_VERSO_NON_CUT);
					try {
						device.startGroup();
						try {
							device.drawLine(start, 0, start, scale.getWidth());
						} finally {
							device.endGroup();
						}
					} finally {
						device.setCurrentLayer(LAYER_PLIURES_VERSO);
					}
				} else if (p.getTypePliure() == TypePliure.ALTERNE_CONTINU_POINTILLEE) {

					// pointillés avec non déoupe au bord des deux cotés, sur 5mm (sinon,
					// fragilise carton)
					double startNoDots = 5.0;
					double endDotsWidth = scale.getWidth() - 5.0;
					assert scale.getWidth() > 10.0;
					device.startGroup();
					try {
						device.drawImprovedDottedLines(start, startNoDots, start, endDotsWidth, 2, 5);
					} finally {
						device.endGroup();
					}
				} else {
					throw new Exception("unknown bend : " + p.getTypePliure() + " unknown");
				}
				start += p.getTaillePagePourPliure() * 2;
			}

			// fin d'écriture des pliures dans la layer concernée

			start = startBook + p.getTaillePagePourPliure();
			while (start <= vbend + 1) {
				device.setCurrentLayer(LAYER_PLIURES);
				if (p.getTypePliure() == TypePliure.POINTILLEE) {
					device.startGroup();
					try {
						device.drawImprovedDottedLines(start, 0, start, scale.getWidth(), 2, 5);
					} finally {
						device.endGroup();
					}
				} else if (p.getTypePliure() == TypePliure.CONTINUE) {

					device.setCurrentLayer(LAYER_PLIURES_NON_CUT);
					device.startGroup();
					try {
						device.drawLine(start, 0, start, scale.getWidth());
					} finally {
						device.endGroup();
					}

				} else if (p.getTypePliure() == TypePliure.ALTERNE_CONTINU_POINTILLEE) {

					device.setCurrentLayer(LAYER_PLIURES_NON_CUT);

					device.startGroup();
					try {
						device.drawLine(start, 0, start, scale.getWidth());
					} finally {
						device.endGroup();
					}

				} else {
					throw new Exception("type pliure " + p.getTypePliure() + " unknown");
				}

				start += p.getTaillePagePourPliure() * 2;
			}
		}

		logger.debug("dessin des trous");

		device.setCurrentLayer(LAYER_TROUS);

		if (parameters.isExportTrous()) {

			HoleDrawer drawer = null;

			if (p.getTypeTrous().getType() == TrouType.TROUS_RECTANGULAIRES.getType()) {
				drawer = new RectangularHoleDrawer(device, p.getTailleTrous(), p.getPont(), p.getPasDePontSiIlReste());
			} else if (p.getTypeTrous().getType() == TrouType.TROUS_ARRONDIS.getType()) {
				drawer = new ArrondiHoleDrawer(device, p.getTailleTrous(), p.getPont(), p.getPasDePontSiIlReste());
			} else if (p.getTypeTrous().getType() == TrouType.TROUS_RONDS.getType()) {
				drawer = new RondHoleDrawer(device, p.getTailleTrous(), p.getPont(), p.getPasDePontSiIlReste());
			} else {
				throw new Exception("unsupported type " + p.getTypeTrous().getType());
			}

			// define holes height
			double halfheight = scale.getTrackWidth() / 2.0;
			if (p.isSurchargeLargeurTrous()) {
				logger.debug("take the parameters width");
				halfheight = p.getLargeurTrous() / 2.0;
			}

			for (Iterator<Hole> iterator = holesCopy.iterator(); iterator.hasNext();) {
				Hole hole = iterator.next();

				int piste = hole.getTrack();

				double ypiste = piste * scale.getIntertrackHeight() + scale.getFirstTrackAxis();

				if (!device.ignoreReference() && !scale.isPreferredViewedInversed()) {
					ypiste = scale.getWidth() - ypiste;
				}

				double x = hole.getTimestamp() * xratio;
				double endx = (hole.getTimestamp() + hole.getTimeLength()) * xratio;

				// ecriture des trous ...
				device.startGroup();
				try {
					drawer.drawHole(ypiste, halfheight, x, endx);
				} finally {
					device.endGroup();
				}
				logger.debug("export :" + hole);
			}

		}
	}

	/**
	 * test function
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new ConsoleAppender(new PatternLayout()));
		MidiFile midiFile = MidiFileIO
				.read(new File("C:/Projets/APrintPerfoExtension/doc/Perçage_Lazer/BEER essai.mid"));

		Properties properties = new Properties();
		properties.setProperty("folder", "C:/Projets/APrint/gammes");
		Repository r = RepositoryFactory.create(properties);

		Scale ermanscale = r.getScaleManager().getScale("27 Erman");
		ArrayList<AbstractTransformation> trans = r.getTranspositionManager()
				.findTransposition(Scale.getGammeMidiInstance(), ermanscale);

		LinearMidiImporter lmi = Utils.linearToMidiImporter((LinearTransposition) trans.get(0));

		MidiConversionResult result = lmi.convert(midiFile);

		VirtualBook vb = result.virtualbook;

		CADParameters p = new CADParameters();

		p.setTailleTrous(10.0);
		p.setPont(1.0);

		CADVirtualBookExporter e = new CADVirtualBookExporter();
		// e.export(vb, p, new File("export_test_file.dxf"));

		DXFDeviceDrawing device = new DXFDeviceDrawing();
		e.export(vb, p, device);
		device.write(new File("export_test_file.dxf"), new String[] { LAYER_BORDS, LAYER_TROUS, LAYER_PLIURES });

		SVGDeviceDrawing svgDevice = new SVGDeviceDrawing(2000, 200);
		e.export(vb, p, svgDevice);
		svgDevice.write(new File("export_test_svg.svg"), new String[] {});

	}

}
