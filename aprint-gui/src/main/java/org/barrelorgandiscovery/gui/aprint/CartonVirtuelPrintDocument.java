package org.barrelorgandiscovery.gui.aprint;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.SimpleFormatter;

import javax.help.SecondaryWindow;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.ascale.ScaleComponent;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.RegisterCommandStartDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.StreamsTools;
import org.barrelorgandiscovery.tools.TimeUtils;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.SigsEvaluator;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.io.MidiIO;
import org.barrelorgandiscovery.virtualbook.sigs.ComputedSig;

public class CartonVirtuelPrintDocument implements Printable {

	private static final Logger logger = Logger
			.getLogger(CartonVirtuelPrintDocument.class);

	private VirtualBook carton = null;

	// private double vitesse = 60; // en mm par seconde

	// 1 inch = 2,54 cm = 25,4 mm
	// 1/72 inch = 25,4 mm / 72

	private final double mmto72einch = 72.0 / 25.4;

	private int dpi;

	/**
	 * Classe d'impression du carton ...
	 */
	public CartonVirtuelPrintDocument(VirtualBook carton, int dpi) {
		this.carton = carton;
		this.dpi = dpi;
	}

	private double mmToinches(double mm) {
		return mm * mmto72einch * (1.0 * dpi / 72);
	}

	private double inchesTomm(double inches) {
		return inches / (mmto72einch * (1.0 * dpi / 72));
	}

	/**
	 * Converti des secondes en mm par rapport à l'avancée du carton
	 * 
	 * @param secondes
	 * @return
	 */
	private double toMm(double secondes) {
		return secondes * carton.getScale().getSpeed();
	}

	/**
	 * converti des mm en secondes par rapport à l'avancée du carton
	 * 
	 * @param mm
	 * @return
	 */
	private double toSeconds(double mm) {
		return mm / carton.getScale().getSpeed();
	}

	/**
	 * converti
	 * 
	 * @param mm
	 * @return
	 */
	private double toScale(double mm) {
		return mm * mmto72einch;
	}

	private double toPrintAreaY(double y) {
		return toScale(y);
	}

	private double toPrintAreaX(double x) {
		return toScale(x);
	}

	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {

		logger.debug("printing page :" + pageIndex);

		// on regarde si l'on est à la fin du carton

		if (toSeconds(pageIndex * 1.0
				* inchesTomm(pageFormat.getImageableWidth())) > (1.0 * carton
				.getLength() / 1000000))
			return Printable.NO_SUCH_PAGE;

		Graphics2D g = (Graphics2D) graphics;

		// on centre le carton sur la preview

		double centerOffset = (pageFormat.getImageableHeight() - toPrintAreaY(carton
				.getScale().getWidth())) / 2;

		g.translate(pageFormat.getImageableX(), pageFormat.getImageableY()
				+ centerOffset);

		g.setColor(Color.BLACK);
		g.setPaintMode();

		// Dessin des bords du carton

		Scale scale = carton.getScale();

		int ycarton = (int) toPrintAreaY(scale.getWidth());
		int xcarton = (int) pageFormat.getImageableWidth();

		// draw the guide lines

		Stroke oldS = g.getStroke();
		Color oldc = g.getColor();

		g.setStroke(new BasicStroke(0.1f, BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_ROUND, 1.0f, new float[] { 0.5f, 0.5f }, 0));
		g.setColor(Color.lightGray);
		try {
			for (int i = 0; i < scale.getTrackNb(); i++) {

				int yline = (int) (mmToinches(i
						* carton.getScale().getIntertrackHeight()
						+ carton.getScale().getFirstTrackAxis()));
				g.drawLine(0, yline, xcarton, yline);
			}

		} finally {
			g.setStroke(oldS);
			g.setColor(oldc);
		}

		// dessin du contour du carton
		g.drawRect(0, 0, xcarton, ycarton);

		// affichage de la page

		g.drawString(
				Messages.getString("CartonVirtuelPrintDocument.0") + (pageIndex + 1) + (carton.getName() != null ? " - " + Messages.getString("CartonVirtuelPrintDocument.3") + " :" + carton.getName() : ""), 0, 20); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		g.drawString(
				Messages.getString("CartonVirtuelPrintDocument.1") + (pageIndex + 2), 0, 40); //$NON-NLS-1$

		// dessin de la fleche de la reference

		FontMetrics fontMetrics = g.getFontMetrics();

		String referenceMessage = "Reference";
		g.drawString(referenceMessage,
				xcarton - fontMetrics.stringWidth(referenceMessage), 40);

		int marginForReference = 30;
		int flecheSize = 10;

		g.drawLine(xcarton - marginForReference, 35, xcarton
				- marginForReference, 0);
		g.drawLine(xcarton - marginForReference, 0, xcarton
				- marginForReference - flecheSize, flecheSize);
		g.drawLine(xcarton - marginForReference, 0, xcarton
				- marginForReference + flecheSize, flecheSize);

		double startPageSecond = toSeconds(pageIndex * 1.0
				* inchesTomm(pageFormat.getImageableWidth()));
		double endPageSecond = toSeconds((pageIndex + 1) * 1.0
				* inchesTomm(pageFormat.getImageableWidth()));

		int pas = (int) (mmToinches(carton.getScale().getTrackWidth()));
		int pas2 = (int) ((1.0 * pas) / 2);

		// Font choice for including the text inside the hole

		Font oldFont = g.getFont();
		Font newfont = null;
		try {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			StreamsTools.copyStream(
					getClass().getResourceAsStream("verdana.TTF"), baos);

			ByteArrayInputStream is = new ByteArrayInputStream(
					baos.toByteArray());

			newfont = Font.createFont(Font.TRUETYPE_FONT, is);
			newfont = newfont.deriveFont((float) pas2);
		} catch (Exception ex) {
			logger.error("error loading font :" + ex.getMessage(), ex);
			newfont = oldFont.deriveFont(pas2);
		}

		g.setFont(newfont);
		FontMetrics newFontMetrics = g.getFontMetrics();
		try {

			// Récupération des notes dans le carton
			ArrayList<Hole> notes = carton.findHoles(
					(long) (startPageSecond * 1000000),
					(long) ((endPageSecond - startPageSecond) * 1000000));

			for (int i = 0; i < notes.size(); i++) {
				Hole n = notes.get(i);

				// dessin de la note ...
				int xstart = xcarton
						- (int) (mmToinches(toMm(((double) n.getTimestamp())
								/ 1000000 - startPageSecond)));
				int xend = xcarton
						- (int) (mmToinches(toMm(((double) n.getTimestamp() + n
								.getTimeLength()) / 1000000 - startPageSecond)));

				int y = (int) (mmToinches(n.getTrack()
						* carton.getScale().getIntertrackHeight()
						+ carton.getScale().getFirstTrackAxis()));

				g.setColor(Color.gray);

				// draw the note name ...

				if (carton.getScale().getTracksDefinition()[n.getTrack()] instanceof RegisterCommandStartDef) {
					String libelle = ""

							+ ScaleComponent
									.getTrackLibelle(
											carton.getScale()
													.getTracksDefinition()[n
													.getTrack()], false);

					g.drawString(libelle,
							xstart - newFontMetrics.stringWidth(libelle), y
									+ pas2);
				}

				g.setColor(Color.black);

				g.drawLine(xstart, y - pas2, xend, y + pas2);

				g.drawLine(xstart, y - pas2, xend, y - pas2);
				g.drawLine(xend, y - pas2, xend, y + pas2);
				g.drawLine(xend, y + pas2, xstart, y + pas2);
				g.drawLine(xstart, y + pas2, xstart, y - pas2);

				// ne fonctione pas dans la preview ??
				// g.drawRect(xstart, y - pas2, xend - xstart, pas);
			}

			// print the time and measures

			Stroke oldS2 = g.getStroke();
			Color oldc2 = g.getColor();

			g.setStroke(new BasicStroke(0.1f, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_ROUND, 1.0f, new float[] { 0.5f, 0.5f }, 0));

			try {
				for (int i = (int) startPageSecond; i <= endPageSecond; i++) {
					g.setColor(Color.lightGray);

					int pos = (int) mmToinches(toMm(i - startPageSecond));
					g.drawLine(xcarton - pos, 0, xcarton - pos, ycarton);

					double m = toMm(i) / 1000.0;

					g.setColor(Color.black);
					g.drawString(TimeUtils.toMinSecs(i * 1000000) + " <> "
							+ String.format("%.2f", m) + " m", xcarton - pos,
							ycarton - (int) mmToinches(3.0));

				}

				// Measures

				SigsEvaluator se = new SigsEvaluator();

				List<ComputedSig> results = se.computeSigs(carton
						.getOrderedEventsByRef());
				int current = 0;
				while (current < results.size()
						&& results.get(current).timeStamp < startPageSecond * 1000000) {
					current++;
				}

				current--;
				if (!(current >= results.size() || results.size() == 0)) {

					if (current < 0) {
						current = 0;
					}

					g.setColor(Color.black);
					g.setStroke(new BasicStroke(0.1f, BasicStroke.CAP_SQUARE,
							BasicStroke.JOIN_ROUND, 1.0f, new float[] { 0.75f,
									0.25f }, 0));

					ComputedSig currentSG = results.get(current);
					long timeS = currentSG.timeStamp;
					int currentSig = currentSG.sigNumber;
					while (timeS < endPageSecond * 1000000) {

						double sx = carton.getScale().timeToMM(
								timeS - (long) (startPageSecond * 1000000));

						int posx = xcarton - (int) mmToinches(sx);

						g.drawLine(posx, 0, posx, ycarton);

						g.drawString("[" + currentSig + "]", posx,
								(int) mmToinches(6.0));

						timeS = timeS + currentSG.measureLength;
						currentSig++;
						if (current + 1 < results.size()) {
							ComputedSig nextOne = results.get(current + 1);
							if (nextOne.timeStamp < timeS) {
								current++;
								currentSG = nextOne;
								currentSig = currentSG.sigNumber;
								timeS = currentSG.timeStamp;
							}
						}
					}
				}

			} finally {
				g.setColor(oldc2);
				g.setStroke(oldS2);
			}

		} finally {
			g.setFont(oldFont);
		}

		return Printable.PAGE_EXISTS;
	}

	/**
	 * Routine de test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			CartonVirtuelPrintDocument d = new CartonVirtuelPrintDocument(
					MidiIO.readCarton(new File(
							"C:\\Documents and Settings\\Freydiere Patrice\\Bureau\\midi\\76tromb_.mid")), 72); //$NON-NLS-1$

			PrinterJob pjob = PrinterJob.getPrinterJob();
			PageFormat pf = pjob.defaultPage();
			pjob.setPrintable(d, pf);

			// Affiche la boite de dialogue standard
			if (pjob.printDialog()) {
				pjob.print();
			}

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}

	/**
	 * Routine de tests
	 * 
	 * @param args
	 */
	public static void main2(String[] args) {

		try {
			CartonVirtuelPrintDocument d = new CartonVirtuelPrintDocument(
					MidiIO.readCarton(new File(
							"C:\\Documents and Settings\\Freydiere Patrice\\Bureau\\midi\\76tromb_.mid")), 72); //$NON-NLS-1$

			/*
			 * Construct the print request specification. The print data is a
			 * Printable object. the request additonally specifies a job name, 2
			 * copies, and landscape orientation of the media.
			 */
			DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
			PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
			// aset.add(OrientationRequested.LANDSCAPE);

			aset.add(new JobName("My job", null)); //$NON-NLS-1$

			/* locate a print service that can handle the request */
			PrintService[] services = PrintServiceLookup.lookupPrintServices(
					flavor, aset);

			if (services.length > 0) {
				logger.debug("selected printer " + services[1].getName()); //$NON-NLS-1$

				/* create a print job for the chosen service */
				DocPrintJob pj = services[1].createPrintJob();

				try {
					/*
					 * Create a Doc object to hold the print data.
					 */
					Doc doc = new SimpleDoc(d, flavor, null);

					/* print the doc as specified */
					pj.print(doc, aset);

					/*
					 * Do not explicitly call System.exit() when print returns.
					 * Printing can be asynchronous so may be executing in a
					 * separate thread. If you want to explicitly exit the VM,
					 * use a print job listener to be notified when it is safe
					 * to do so.
					 */

				} catch (PrintException e) {
					logger.error("carton virtuel print", e); //$NON-NLS-1$
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}

}
