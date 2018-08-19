package org.barrelorgandiscovery.extensionsng.perfo.dxf;

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
import org.barrelorgandiscovery.extensionsng.perfo.dxf.canvas.DXFDeviceDrawing;
import org.barrelorgandiscovery.extensionsng.perfo.dxf.canvas.DeviceDrawing;
import org.barrelorgandiscovery.extensionsng.perfo.dxf.drawer.ArrondiHoleDrawer;
import org.barrelorgandiscovery.extensionsng.perfo.dxf.drawer.HoleDrawer;
import org.barrelorgandiscovery.extensionsng.perfo.dxf.drawer.RectangularHoleDrawer;
import org.barrelorgandiscovery.extensionsng.perfo.dxf.drawer.RondHoleDrawer;
import org.barrelorgandiscovery.extensionsng.perfo.dxf.math.Vect;
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

public class CADVirtualBookExporter {

	private static Logger logger = Logger
			.getLogger(CADVirtualBookExporter.class);

	final static String LAYER_BORDS = "BORDS_CARTON";

	final static String LAYER_TROUS = "TROUS";

	final static String LAYER_PLIURES = "PLIURES";

	final static String LAYER_PLIURES_VERSO = "PLIURES_VERSO";

	public static String[] LAYERS = new String[] { LAYER_BORDS, LAYER_TROUS,
			LAYER_PLIURES, LAYER_PLIURES_VERSO };

	public CADVirtualBookExporter() {
	}

	/**
	 * Exporte un carton en fichier DXF
	 * 
	 * @param vb
	 *            le carton à exporter
	 * @param mecanique
	 *            booleen indiquant si le type est mecanique
	 * @param tailleTrous
	 *            taille des trous dans le cas du pneumatique
	 * @param ponts
	 *            taille des ponts pour le pneumatique
	 * @param baseexportFile
	 * @throws Exception
	 */
	public void export(VirtualBook vb, DXFParameters parameters,
			DeviceDrawing device) throws Exception {

		DXFParameters p = DXFParameters.class.newInstance();

		// clone the parameters

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(baos);
		parameters.writeExternal(os);
		os.close();

		p.readExternal(new ObjectInputStream(new ByteArrayInputStream(baos
				.toByteArray())));

		ArrayList<Hole> holesCopy = vb.getOrderedHolesCopy();

		System.out.println("hole to translate :" + holesCopy.size());

		Scale scale = vb.getScale();

		double xratio = 1.0 / 1000000.0 * scale.getSpeed();

		// adding the vb edges

		
		long firstTimeStamp = vb.getFirstHoleStart();
		if (firstTimeStamp == Long.MAX_VALUE)
			throw new Exception("no holes in the book, cannot compute");

		logger.debug("ecriture des bords");

		device.setCurrentLayer(LAYER_BORDS);

		// calc the beginning offset of the book
		double startBook = firstTimeStamp * xratio
				- p.getStartBookAdjustementFromBeginning()
				- p.getNombreDePlisAAjouterAuDebut()
				* p.getTaillePagePourPliure();

		// startBook can be negative ...

		double vbend = vb.getLength() * xratio;
		// round the end to a integer value of the page size
		vbend = (Math.ceil((vbend - startBook ) / p.getTaillePagePourPliure()) + 1)
				* p.getTaillePagePourPliure() + startBook;

		
		
		// ligne horizontale
		device.drawLine(startBook, 0, vbend, 0);

		// device.drawLine(vbend, 0, vbend, scale.getWidth());

		device.drawLine(vbend, scale.getWidth(), startBook, scale.getWidth());

		// device.drawLine(0, scale.getWidth(), 0, 0);

		
		// draw the reference arrow
		//dest
		double arrowy = scale.getWidth();
		if (scale.isPreferredViewedInversed())
			arrowy = scale.getWidth() - arrowy;
		
		// origin
		double arrowy2 = scale.getWidth() - 30; // 3cm
		if (scale.isPreferredViewedInversed())
			arrowy2 = scale.getWidth() - arrowy2;
		
		Coordinate origin = new Coordinate(10,arrowy2);
		device.drawArrow(new Vect(0, arrowy - arrowy2), origin, 10);
		
		
		
		if (p.isExportPliures()) {

			logger.debug("ajout pliures ... ");

			// Ecriture des pliures

			device.setCurrentLayer(LAYER_PLIURES_VERSO);
			double start = startBook;
			while (start <= vbend + 1) {

				if (p.getTypePliure() == TypePliure.POINTILLEE) {
					device.drawImprovedDottedLines(start, 0, start,
							scale.getWidth(), 2, 5);
				} else {
					device.drawLine(start, 0, start, scale.getWidth());
				}
				start += p.getTaillePagePourPliure() * 2;
			}

			// fin d'écriture des pliures

			device.setCurrentLayer(LAYER_PLIURES);
			start = startBook + p.getTaillePagePourPliure();
			while (start <= vbend + 1) {
				if (p.getTypePliure() == TypePliure.POINTILLEE) {
					device.drawImprovedDottedLines(start, 0, start,
							scale.getWidth(), 2, 5);
				} else {
					device.drawLine(start, 0, start, scale.getWidth());
				}
				start += p.getTaillePagePourPliure() * 2;

			}
		}

		logger.debug("dessin des trous");

		device.setCurrentLayer(LAYER_TROUS);

		HoleDrawer drawer = null;

		if (p.getTypeTrous().getType() == TrouType.TROUS_RECTANGULAIRES
				.getType()) {

			drawer = new RectangularHoleDrawer(device, p.getTailleTrous(),
					p.getPont(), p.getPasDePontSiIlReste());
		} else if (p.getTypeTrous().getType() == TrouType.TROUS_ARRONDIS
				.getType()) {
			drawer = new ArrondiHoleDrawer(device, p.getTailleTrous(),
					p.getPont(), p.getPasDePontSiIlReste());
		} else if (p.getTypeTrous().getType() == TrouType.TROUS_RONDS.getType()) {
			drawer = new RondHoleDrawer(device, p.getTailleTrous(),
					p.getPont(), p.getPasDePontSiIlReste());
		} else {
			throw new Exception("unsupported trou type");
		}

		for (Iterator iterator = holesCopy.iterator(); iterator.hasNext();) {
			Hole hole = (Hole) iterator.next();

			int piste = hole.getTrack();

			double ypiste = piste * scale.getIntertrackHeight()
					+ scale.getFirstTrackAxis();

			if (!scale.isPreferredViewedInversed()) {
				ypiste = scale.getWidth() - ypiste;
			}

			double halfheight = scale.getTrackWidth() / 2;

			double x = hole.getTimestamp() * xratio;
			double endx = (hole.getTimestamp() + hole.getTimeLength()) * xratio;

			// ecriture des trous ...
			drawer.drawHole(ypiste, halfheight, x, endx);

			logger.debug("export :" + hole);
		}

		device.flushLine();

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new ConsoleAppender(new PatternLayout()));
		MidiFile midiFile = MidiFileIO
				.read(new File(
						"C:/Projets/APrintPerfoExtension/doc/Perçage_Lazer/BEER essai.mid"));

		Properties properties = new Properties();
		properties.setProperty("folder", "C:/Projets/APrint/gammes");
		Repository r = RepositoryFactory.create(properties);

		Scale ermanscale = r.getScaleManager().getScale("27 Erman");
		ArrayList<AbstractTransformation> trans = r.getTranspositionManager()
				.findTransposition(Scale.getGammeMidiInstance(), ermanscale);

		LinearMidiImporter lmi = Utils
				.linearToMidiImporter((LinearTransposition) trans.get(0));

		MidiConversionResult result = lmi.convert(midiFile);

		VirtualBook vb = result.virtualbook;

		DXFParameters p = new DXFParameters();

		p.setTailleTrous(10.0);
		p.setPont(1.0);

		CADVirtualBookExporter e = new CADVirtualBookExporter();
		// e.export(vb, p, new File("export_test_file.dxf"));

		DXFDeviceDrawing device = new DXFDeviceDrawing();
		e.export(vb, p, device);
		device.write(new File("export_test_file.dxf"), new String[] {
				LAYER_BORDS, LAYER_TROUS, LAYER_PLIURES });

	}

}
