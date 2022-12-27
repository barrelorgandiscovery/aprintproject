package org.barrelorgandiscovery.gui.ascale;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.Scale;


/**
 * Classe permettant l'impression de la gamme ...
 * 
 * @author Freydiere Patrice
 * 
 */
public class ScalePrintDocument implements Printable {

	private static final Logger logger = Logger
			.getLogger(ScalePrintDocument.class);

	/**
	 * Gamme à imprimer
	 */
	private Scale gamme;

	/**
	 * Constructeur
	 * 
	 * @param gamme
	 *            la gamme à imprimer
	 */
	public ScalePrintDocument(Scale gamme) {
		super();
		if (gamme == null)
			throw new IllegalArgumentException();

		this.gamme = gamme;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.Printable#print(java.awt.Graphics,
	 *      java.awt.print.PageFormat, int)
	 */
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {

		if (pageIndex != 0)
			return Printable.NO_SUCH_PAGE;

		Graphics2D g = (Graphics2D) graphics;

		g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		
		
		double dpi = Toolkit.getDefaultToolkit().getScreenResolution();

		g.scale(72.0/dpi, 72.0/dpi);
		
		logger.debug("print gamme ..."); //$NON-NLS-1$

		// Création du composant ...
		ScaleComponent c = new ScaleComponent();

		// chargement de la gamme ...
		c.loadScale(gamme);

		c.paint(graphics);

		return Printable.PAGE_EXISTS;
	}

}
