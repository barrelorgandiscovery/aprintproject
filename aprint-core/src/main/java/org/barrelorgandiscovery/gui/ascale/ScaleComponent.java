package org.barrelorgandiscovery.gui.ascale;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.math.MathVect;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.AbstractScaleConstraint;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.ConstraintList;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.PipeStop;
import org.barrelorgandiscovery.scale.PipeStopGroup;
import org.barrelorgandiscovery.scale.PipeStopGroupList;
import org.barrelorgandiscovery.scale.PipeStopListReference;
import org.barrelorgandiscovery.scale.ReferencedPercussion;
import org.barrelorgandiscovery.scale.ReferencedPercussionList;
import org.barrelorgandiscovery.scale.RegisterCommandStartDef;
import org.barrelorgandiscovery.scale.RegisterSetCommandResetDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.ScaleException;
import org.barrelorgandiscovery.tools.MidiHelper;
import org.barrelorgandiscovery.tools.SerializeTools;
import org.barrelorgandiscovery.virtualbook.rendering.VirtualBookRendering;

public class ScaleComponent extends JComponent {

	/**
	 * serialisation automatique
	 */
	private static final long serialVersionUID = -8615661757777959965L;

	private static final Logger logger = Logger.getLogger(ScaleComponent.class);

	// ///////////////////////////////////////////////////////////////////////
	// Définition de la propriété de la gamme ...

	private double largeur_carton = 200; // par défaut, 20cm, en mm

	private double entrepiste = 3; // en mm

	private double largeurpiste = 3; // en mm

	private int nbpistes = -1; // nombre de pistes dans la gamme

	private double premierepiste = 1.5; // en mm

	private double speed = 60; // en mm par seconde

	private PipeStopGroupList registersets = new PipeStopGroupList();

	private String name = Messages.getString("GammeComponent.0"); //$NON-NLS-1$

	private ArrayList<AbstractTrackDef> notedefs = new ArrayList<AbstractTrackDef>();

	private String infos = null;

	private ConstraintList constraintList = new ConstraintList();

	private Vector<ScaleHighlightListener> hightlightListener = new Vector<ScaleHighlightListener>();

	private boolean speedDraw = false;

	private Map<String, String> properties = new HashMap<String, String>();

	/**
	 * Etat de la gamme
	 */
	private String scalestate = null;

	/**
	 * Contact associé à la gamme
	 */
	private String contact = null;

	// propriétés d'affichage

	// private Color carton_color = new Color(216, 181, 141);

	/**
	 * Largeur de la gamme dessinée dans le composant
	 */
	private double largeur_gamme = 130; // en mm , largeur de la gamme affichée

	/**
	 * Track highlighted
	 */
	private int highligthed_track = -1;

	/**
	 * Track sélectionné
	 */
	private int selected_track = -1;

	private boolean default_selecttrack_if_no_listener = true;

	/**
	 * Listener des actions sur la gamme ...
	 */
	private ScaleComponentListener listener = null;

	/**
	 * rendering options
	 */
	private RenderingHints renderHints = null;

	/**
	 * book moving direction, can be not defined
	 */
	private boolean bookMoveRightToLeft = true;

	/**
	 * Echelle
	 */
	private double scale = 1.0; // par défaut, échelle 1

	/**
	 * cache sur le dpi récupéré
	 */
	private int dpi = -1;

	private Dimension preferredSize = null;
	
	private VirtualBookRendering rendering = null;

	private boolean preferredViewedInverted = false;

	private TexturePaint cartontrame = null;
	
	/**
	 * Constructeur par défaut ...
	 */
	public ScaleComponent() {

		renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		renderHints.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);

		addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {

			}

			public void mouseEntered(MouseEvent e) {

			}

			public void mouseExited(MouseEvent e) {
				highligthed_track = -1;
				repaint();
			}

			public void mousePressed(MouseEvent e) {

			}

			public void mouseReleased(MouseEvent e) {

				if (listener != null) {

					listener.trackClicked(highligthed_track);

				} else {
					// listener == null
					if (default_selecttrack_if_no_listener) {

						selected_track = highligthed_track;
						fireTrackSelected();

					}

				}
				repaint();

			}
		});
		addMouseMotionListener(new MouseMotionListener() {

			public void mouseDragged(MouseEvent e) {

			}

			public void mouseMoved(MouseEvent e) {

				int x = e.getX();
				int y = e.getY();

				if (x > MmToPixel(largeur_gamme / 2)
						&& x < MmToPixel(largeur_gamme)) {

					double mousey = pixelToMm(y);

					double ref;

					if (preferredViewedInverted) {
						ref = -premierepiste + entrepiste / 2
								+ (largeur_carton - mousey);
					} else {
						ref = entrepiste / 2 - premierepiste + mousey;
					}

					int evaluatedtrack = (int) (ref / entrepiste);

					if (evaluatedtrack < 0 || evaluatedtrack > nbpistes - 1) {
						// en dehors ...

						if (highligthed_track != -1) {
							highligthed_track = -1;
							fireResetHightLight();
							repaint();

						}
						return;
					}

					if (highligthed_track == -1) {
						// pas de sélection ...
						highligthed_track = evaluatedtrack;
						repaint();
					} else {
						if (highligthed_track != evaluatedtrack) {
							highligthed_track = evaluatedtrack;
							fireHighlightTrackDef(notedefs
									.get(highligthed_track));
							repaint();
						}

					}

				} else {
					if (highligthed_track != -1) {
						highligthed_track = -1;
						fireResetHightLight();
						repaint();
					}

				}

			}
		});

		addMouseWheelListener(new MouseWheelListener() {

			public void mouseWheelMoved(MouseWheelEvent e) {
				int rotation = e.getWheelRotation();

				if ((e.getModifiersEx() & MouseWheelEvent.CTRL_DOWN_MASK) != 0) {
					double scale = 2;
					if (rotation != 0) {
						if (rotation < 0) {
							scale = 1 / scale;
						}
						setScale(getScale() * scale);
					}

				}
				repaint();
			}

		});

		newScale();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		// note : surcharge de paint pour pouvoir imprimer ...
		// paintComponent n'est pas appelée sur les demandes d'impression

		// logger
		// .debug(Messages.getString("GammeComponent.1") +
		// System.currentTimeMillis()); //$NON-NLS-1$

		g.setPaintMode();
		Color lastcolor = g.getColor();

		// couleur carton ...

		if (rendering == null) {
			g.setColor(new Color(216, 181, 141));
		} else {
			g.setColor(rendering.getDefaultBookColor());
		}

		Graphics2D g2d = (Graphics2D) g;

		g2d.setRenderingHints(renderHints);

		Paint lastpaint = g2d.getPaint();
		try {

			if (cartontrame != null && !speedDraw) {
				g2d.setPaint(cartontrame);
			}

			g.fillRect(0, 0, MmToPixel(largeur_gamme),
					MmToPixel(largeur_carton));

		} finally {
			g2d.setPaint(lastpaint);
		}

		// Dessin du contour

		g.setColor(Color.BLACK);

		Stroke oldstroke = g2d.getStroke();
		try {
			BasicStroke bs = new BasicStroke(2.0f);

			g2d.setStroke(bs);

			g.drawRect(0, 0, MmToPixel(largeur_gamme),
					MmToPixel(largeur_carton));

			int firsttrack = 0;

			int lasttrack = nbpistes - 1;

			// Dessin des bords du carton ...

			// calcul de l'entre piste en taille

			Font f = g.getFont();
			try {
				// Calcule de la meilleure taille de font pour les pistes ...
				FontMetrics fm = g.getFontMetrics();
				int fontheight = fm.getHeight();

				int entrepistepixel = MmToPixel(entrepiste);

				double correctfontsize = (double) entrepistepixel / fontheight;

				Font newfont = f.deriveFont(
						(float) (f.getSize() * correctfontsize * 0.80))
						.deriveFont(Font.ITALIC + Font.BOLD);

				g.setFont(newfont);

				// Dessin du highlight et de la sélection ...

				if (highligthed_track != -1) {
					// dessin en jaune de la piste highlighté
					g.setColor(Color.YELLOW);
					Rectangle extent = getTrackRectangle(highligthed_track);
					g.fillRect(extent.x, extent.y, extent.width, extent.height);

					g.setColor(Color.BLACK);
				}

				if (selected_track != -1) {
					// dessin en jaune de la piste highlighté
					g.setColor(Color.BLUE);
					Rectangle extent = getTrackRectangle(selected_track);
					g.fillRect(extent.x, extent.y, extent.width, extent.height);

					g.setColor(Color.BLACK);
				}

				// draw reference
				drawReference(g);

				// draw book moving direction
				
				drawDirection(g);

				// Dessin des pistes des gammes
				for (int i = firsttrack; i <= lasttrack; i++) {

					int yscreen;

					if (preferredViewedInverted) {
						yscreen = convertCartonToScreenY(largeur_carton
								- (1.0 * i * entrepiste + premierepiste - entrepiste / 2));
					} else {
						yscreen = convertCartonToScreenY(1.0 * i * entrepiste
								+ premierepiste - entrepiste / 2);
					}

					g.drawLine(MmToPixel(largeur_gamme / 2), yscreen,
							MmToPixel(largeur_gamme), yscreen);

					// affichage de la définition de la piste ...

					// Récupération de la définition de la piste ...
					AbstractTrackDef td = null;
					if (i < notedefs.size())
						td = notedefs.get(i);

					int ybasetexte;

					if (preferredViewedInverted) {

						ybasetexte = convertCartonToScreenY(largeur_carton
								- ((double) i * entrepiste + premierepiste - entrepiste / 2 * 0.75));

					} else {

						ybasetexte = convertCartonToScreenY((double) i
								* entrepiste + premierepiste + entrepiste / 2
								* 0.75);
					}

					String libelle = "" + (i + 1) + " - "; //$NON-NLS-1$ //$NON-NLS-2$

					if (td != null)
						libelle += getTrackLibelle(td);

					g.drawString(libelle, MmToPixel(largeur_gamme / 2),
							ybasetexte);

				}

				// dernière borne
				int yscreen;
				if (preferredViewedInverted) {

					yscreen = convertCartonToScreenY(largeur_carton
							- (1.0 * (lasttrack + 1) * entrepiste
									+ premierepiste - entrepiste / 2));
				} else {
					yscreen = convertCartonToScreenY(1.0 * (lasttrack + 1)
							* entrepiste + premierepiste - entrepiste / 2);
				}

				g.drawLine(MmToPixel(largeur_gamme / 2), yscreen,
						MmToPixel(largeur_gamme), yscreen);

				// dessin du nom de la gamme ...

				// // calcul de la longueur du nom de la gamme
				//
				// Rectangle2D taillenom = fm.getStringBounds(name, g);
				//
				// // positionnement largeur
				// double largeurratio = (double) 1 / 5;
				//
				// // ratio
				// // il faut que la taille fasse 1/3 de la largeur ...
				// double txtratio = MmToPixel(largeur_gamme * largeurratio)
				// / taillenom.getHeight();
				//
				// g.setFont(f.deriveFont(f.getSize2D() * (float) txtratio));
				//
				// AffineTransform oldtransform = g2d.getTransform();
				// try {
				// AffineTransform newt = new AffineTransform(oldtransform);
				// newt.concatenate(AffineTransform
				// .getRotateInstance(3 * Math.PI / 2));
				// g2d.setTransform(newt);
				//
				// String libellegamme = Messages
				// .getString("GammeComponent.4") + name; //$NON-NLS-1$
				//
				// g.drawString(libellegamme, -MmToPixel(largeur_carton),
				// MmToPixel(largeur_gamme * largeurratio));
				//
				// } finally {
				// g2d.setTransform(oldtransform);
				// }
				//

				String infosgamme = createInfoText();

				Point2D.Float pen = new Point2D.Float(
						MmToPixel(largeur_gamme / 25),
						MmToPixel(largeur_gamme / 20));

				String lines[] = infosgamme.split("\n"); //$NON-NLS-1$

				FontRenderContext frc = g2d.getFontRenderContext();

				for (int i = 0; i < lines.length; i++) {

					if (lines[i].length() == 0) {
						pen.y += 20;
						continue;
					}

					AttributedString as = new AttributedString(lines[i]);
					// as.addAttribute(TextAttribute.SIZE, 24f);
					as.addAttribute(TextAttribute.FONT,
							Font.decode("Arial-BOLD-12")); //$NON-NLS-1$

					if (i == 0) {
						as.addAttribute(TextAttribute.FONT,
								Font.decode("Arial-BOLD-24")); //$NON-NLS-1$
						as.addAttribute(TextAttribute.WEIGHT,
								TextAttribute.WEIGHT_ULTRABOLD);

					} else {

						as.addAttribute(TextAttribute.POSTURE,
								TextAttribute.POSTURE_OBLIQUE);
					}

					AttributedCharacterIterator it = as.getIterator();
					LineBreakMeasurer measurer = new LineBreakMeasurer(it,
							BreakIterator.getWordInstance(), frc);
					float wrappingWidth = (float) MmToPixel(largeur_gamme / 2.5);

					while (measurer.getPosition() < lines[i].length()) {

						TextLayout layout = measurer.nextLayout(wrappingWidth);

						pen.y += layout.getAscent();
						float dx = layout.isLeftToRight() ? 0
								: (wrappingWidth - layout.getAdvance());

						layout.draw(g2d, pen.x + dx, pen.y);
						pen.y += layout.getDescent() + layout.getLeading();
					}
				}

			} finally {
				g.setFont(f);
			}

		} finally {
			g2d.setStroke(oldstroke);
		}
		// restauration de l'ancienne couleur
		g.setColor(lastcolor);

	}

	/**
	 * internal method for drawing an arrow
	 * 
	 * @param g
	 *            the graphic context for the draw
	 * @param origine
	 *            the base point of the arrow
	 * @param vector
	 *            the direction of the vector
	 * @param arrowwidth
	 *            the width of the arrow
	 */
	private void drawArrow(Graphics g, Point2D.Double origine, MathVect vector,
			double arrowwidth) {

		assert g != null;
		assert origine != null;
		assert vector != null;
		
		Point2D.Double coordsOrigine = new Point2D.Double(MmToPixel(origine.getX()), MmToPixel(origine.getY()));

		Graphics2D g2d = (Graphics2D) g;
		
		vector = vector.scale( MmToPixel( 1.0/vector.norme()  * arrowwidth) ); // normalise vector

		// end point
		Point2D.Double end = vector.plus(coordsOrigine);

		MathVect v = vector;
		MathVect inverted = v.rotate(Math.PI);

		MathVect o1 = inverted.rotate(Math.PI / 180.0 * 15).scale(0.3);
		MathVect o2 = inverted.rotate(-Math.PI / 180.0 * 15).scale(0.3);

		g2d.draw(new Line2D.Double(coordsOrigine, end));

		g2d.draw(new Line2D.Double(end, o1.plus(end)));
		g2d.draw(new Line2D.Double(end, o2.plus(end)));

	}

	/**
	 * draw direction of the book
	 * 
	 * @param g
	 */
	private void drawDirection(Graphics g) {
		
		double width = 40; // 3cm for the arrow
		
		Point2D.Double d = new Point2D.Double(5.0, largeur_carton - 10.0);
		
		// by default, arrow from left to right
		MathVect v = new MathVect(1, 0);
		
		if (bookMoveRightToLeft)
		{
			// draw arrow in direction of 9h
			v = new MathVect(-1, 0);
			d = new Point2D.Double(d.getX() + width, d.getY());
			
		}
		
		drawArrow(g, d, v, width);
		

	}

	private void drawReference(Graphics g) {
		int y_reference;
		int x_reference;
		int yline1; // for the reference
		int yline2;

		// Dessin de la référence ...
		if (preferredViewedInverted) {
			y_reference = convertCartonToScreenY(largeur_carton);
			x_reference = MmToPixel(largeur_gamme / 2);

			yline1 = convertCartonToScreenY(largeur_carton);
			yline2 = yline1 - yline1 / 30;

		} else {

			int current_fontheight = g.getFontMetrics().getHeight();
			y_reference = convertCartonToScreenY(0.0) + current_fontheight;
			x_reference = MmToPixel(largeur_gamme / 2);

			yline1 = convertCartonToScreenY(0.0);
			yline2 = convertCartonToScreenY(largeur_carton) / 30;

		}

		g.drawString(
				Messages.getString("ScaleComponent.0"), x_reference + 10, y_reference); //$NON-NLS-1$

		g.drawLine(x_reference, yline1, x_reference, yline2);
		g.drawLine(x_reference, yline1, x_reference + 10, yline2);
		g.drawLine(x_reference, yline1, x_reference - 10, yline2);
	}

	@SuppressWarnings(value = "unchecked")
	private String createInfoText() {
		String infosgamme = /* Messages.getString("GammeComponent.4") + */name
				+ "\n\n\n"; //$NON-NLS-1$
		infosgamme += Messages.getString("GammeComponent.9") + largeur_carton + " (mm)" + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		infosgamme += Messages.getString("GammeComponent.12") + nbpistes + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		infosgamme += Messages.getString("GammeComponent.14") + premierepiste //$NON-NLS-1$
				+ " (mm)\n"; //$NON-NLS-1$
		infosgamme += Messages.getString("GammeComponent.16") + entrepiste //$NON-NLS-1$
				+ " (mm)\n"; //$NON-NLS-1$
		infosgamme += Messages.getString("GammeComponent.18") + largeurpiste + " (mm)\n"; //$NON-NLS-1$ //$NON-NLS-2$
		infosgamme += Messages.getString("GammeComponent.20") + speed + " (mm/s)\n"; //$NON-NLS-1$ //$NON-NLS-2$

		// adding the registersets informations (description of the pipestops
		// ... aso)
		if (registersets != null && registersets.size() > 0) {
			infosgamme += "\n\n" + Messages.getString("GammeComponent.48") + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			for (int i = 0; i < registersets.size(); i++) {
				PipeStopGroup rs = registersets.get(i);
				infosgamme += Messages.getString("GammeComponent.23") + (i + 1) + " - " + rs.getName(); //$NON-NLS-1$ //$NON-NLS-2$

				String list = ""; //$NON-NLS-1$
				for (PipeStop r : rs) {
					if (list.length() > 0)
						list += ","; //$NON-NLS-1$

					list += r;
				}

				if (list.length() > 0)
					infosgamme += " (" + list + ")"; //$NON-NLS-1$ //$NON-NLS-2$

				infosgamme += "\n"; //$NON-NLS-1$

			}

		}

		// state of the scale ...
		infosgamme += Messages.getString("ScaleComponent.1") //$NON-NLS-1$
				+ (scalestate == null ? "" : scalestate) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

		// adding the constraints of the instruments ...

		if (this.constraintList != null && this.constraintList.size() > 0) {
			logger.debug("adding constraints list in the scale ... "); //$NON-NLS-1$

			infosgamme += "\n" + Messages.getString("ScaleComponent.100") + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			for (Iterator it = this.constraintList.iterator(); it.hasNext();) {
				AbstractScaleConstraint c = (AbstractScaleConstraint) it.next();
				infosgamme += c.toString() + "\n"; //$NON-NLS-1$
			}
		}

		// contact of the scale , who gives the informations
		infosgamme += Messages.getString("ScaleComponent.4") //$NON-NLS-1$
				+ (contact == null ? Messages.getString("ScaleComponent.5") : contact) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

		// free descriptive information on the scale
		if (infos != null && infos.length() > 0) {
			infosgamme += "\n\n" + Messages.getString("GammeComponent.31") + "\n" + infos + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		return infosgamme;
	};

	// ///////////////////////////////////////////////////////////////////
	// outils de conversion de coordonnées

	private Rectangle getTrackRectangle(int trackno) {

		int yscreen;

		if (preferredViewedInverted) {
			yscreen = convertCartonToScreenY(largeur_carton
					- ((trackno + 1) * entrepiste + premierepiste - entrepiste / 2));
		} else {
			yscreen = convertCartonToScreenY(trackno * entrepiste
					+ premierepiste - entrepiste / 2);
		}

		return new Rectangle(convertCartonToScreenX(largeur_gamme / 2),
				yscreen, convertCartonToScreenX(largeur_gamme / 2),
				MmToPixel(entrepiste));

	}

	/**
	 * Récupération du facteur d'échelle, par exemple 2 pour voir la gamme en 2
	 * fois plus gros
	 * 
	 * @return
	 */
	public double getScale() {
		return 1 / scale;
	}

	/**
	 * Définition du facteur d'échelle, par exemple 2 pour voir la gamme en 2
	 * fois plus gros
	 * 
	 * @param scale
	 */
	public void setScale(double scale) {
		this.scale = 1 / scale;
	}

	/**
	 * Cette méthode converti la coordonnée de l'espace carton vers les
	 * coordonnées écran.
	 */
	private int convertCartonToScreenX(double x) {
		double d = x;
		return MmToPixel(d);
	}

	private int convertCartonToScreenY(double y) {
		double d = +y;
		return MmToPixel(d);
	}

	/**
	 * Récupère le DPI associé à l'écran pour l'affichage à l'échelle 1
	 * 
	 * @return
	 */
	private int getDpi() {
		if (dpi == -1)
			dpi = Toolkit.getDefaultToolkit().getScreenResolution();

		return dpi;
	}

	/**
	 * Converti une distance de mm en pixel
	 * 
	 * @param x
	 * @return
	 */
	private int MmToPixel(double x) {

		double pts_par_mm = (getDpi() * 1.0) / 25.4;
		return (int) (x / scale * pts_par_mm);
	}

	// private double convertScreenXToCarton(int x) {
	// double xmm = pixelToMm(x);
	// return xmm;
	// }
	//
	// private double convertScreenYToCarton(int y) {
	// double ymm = pixelToMm(y);
	// return ymm;
	// }

	/**
	 * Converti une distance de pixel en mm
	 * 
	 * @param x
	 *            le nombre de pixel
	 * @return la distance en mm
	 */
	public double pixelToMm(int x) {

		double pts_par_mm = (getDpi() * 1.0) / 25.4;
		return ((double) x * scale) / pts_par_mm;
	}

	public static String getTrackLibelle(AbstractTrackDef td) {
		return getTrackLibelle(td, true);
	}

	/**
	 * Récupération du libellé de la piste
	 * 
	 * @param td
	 * @return
	 */
	public static String getTrackLibelle(AbstractTrackDef td, boolean showHertz) {
		if (td == null)
			return null;

		if (td instanceof NoteDef) {
			NoteDef nd = (NoteDef) td;
			StringBuffer sb = new StringBuffer();

			if (nd.getRegisterSetName() != null) {
				sb.append(PipeStopListReference.getLocalizedPipeStopGroup(nd
						.getRegisterSetName()));
				sb.append(" - "); //$NON-NLS-1$
			}
			sb.append(MidiHelper.localizedMidiLibelle(nd.getMidiNote()));

			if (showHertz) {
				sb.append(" - ").append( //$NON-NLS-1$
								NumberFormat.getNumberInstance().format(
										MidiHelper.hertz(nd.getMidiNote())))
						.append(" Hz"); //$NON-NLS-1$
			}
			return sb.toString();
		} else if (td instanceof PercussionDef) {
			PercussionDef pd = (PercussionDef) td;

			StringBuilder sb = new StringBuilder();
			sb.append(Messages.getString("GammeComponent.5")); //$NON-NLS-1$ //$NON-NLS-2$

			ReferencedPercussion p = ReferencedPercussionList
					.findReferencedPercussionByMidiCode(pd.getPercussion());
			if (p != null) {
				sb.append(" - "); //$NON-NLS-1$
				sb.append(ReferencedPercussion.getLocalizedDrumLabel(p)); // $NON-NLS-1$
				// //$NON-NLS-1$
				// //$NON-NLS-1$
				// //$NON-NLS-1$
				// //$NON-NLS-1$
			}

			if (!Double.isNaN(pd.getLength())) {
				sb.append(" - "); //$NON-NLS-1$
				sb.append(Messages.getString("GammeComponent.8")).append(pd.getLength()).append("mm"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (!Double.isNaN(pd.getRetard())) {
				sb.append(" - ").append("L ").append(pd.getRetard()).append( //$NON-NLS-1$ //$NON-NLS-2$
						"mm"); //$NON-NLS-1$
			}
			return sb.toString();
		} else if (td instanceof RegisterSetCommandResetDef) {
			RegisterSetCommandResetDef r = (RegisterSetCommandResetDef) td;

			StringBuilder sb = new StringBuilder(
					Messages.getString("GammeComponent.39")); //$NON-NLS-1$

			sb.append(" - "); //$NON-NLS-1$
			if (r.getRegisterSet() == "ALL") { //$NON-NLS-1$
				sb.append("ALL"); //$NON-NLS-1$
			} else {
				sb.append(r.getRegisterSet());
			}

			return sb.toString();
		} else if (td instanceof RegisterCommandStartDef) {
			RegisterCommandStartDef r = (RegisterCommandStartDef) td;

			StringBuilder sb = new StringBuilder(
					Messages.getString("GammeComponent.43")).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(r.getRegisterInRegisterSet());
			sb.append(" ").append(Messages.getString("GammeComponent.46")).append(" ").append(r.getRegisterSetName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			return sb.toString();
		}

		return td.toString();

	}

	private Dimension getComponentInternalSize() {
		return new Dimension(MmToPixel(largeur_gamme),
				MmToPixel(largeur_carton));

	}

	@Override
	public Dimension getPreferredSize() {
		if (this.preferredSize != null)
			return this.preferredSize;
		// sinon
		return getComponentInternalSize();
	}

	@Override
	public void setPreferredSize(Dimension preferredSize) {
		this.preferredSize = preferredSize;
	}

	/**
	 * Définition du listener de composant ...
	 * 
	 * @param listener
	 */
	public void setGammeListener(ScaleComponentListener listener) {
		this.listener = listener;
	}

	public boolean isDefaultSelectTrackIfNoListener() {
		return default_selecttrack_if_no_listener;
	}

	public void setDefaultSelectTrackIfNoListener(
			boolean default_selecttrack_if_no_listener) {
		this.default_selecttrack_if_no_listener = default_selecttrack_if_no_listener;
	}

	// /////////////////////////////////////////////////////////////////////////
	// Fonction de gestion de la gamme ...

	public void newScale() {
		this.largeur_carton = 200; // par défaut, 20cm, en mm

		this.entrepiste = 3; // en mm

		this.largeurpiste = 3; // en mm

		this.nbpistes = 0; // nombre de pistes dans la gamme

		this.premierepiste = 1.5; // en mm

		this.speed = 60; // en mm par seconde

		this.registersets = new PipeStopGroupList();

		this.name = Messages.getString("GammeComponent.0"); //$NON-NLS-1$

		this.notedefs = new ArrayList<AbstractTrackDef>();

		this.infos = null;

		this.constraintList = new ConstraintList();

		this.scalestate = null;

		this.contact = null;

		this.rendering = new VirtualBookRendering();

		try {
			BufferedImage bi = ImageIO.read(rendering.getBackgroundImage());
			this.cartontrame = new TexturePaint(bi, new Rectangle2D.Double(0,
					0, bi.getWidth(), bi.getHeight()));
		} catch (Exception ex) {
			logger.error("setcarton", ex); //$NON-NLS-1$
			this.cartontrame = null;
		}

		this.properties = new HashMap<String, String>();

		invalidate();
		resize();

	}

	/**
	 * Charge la définition d'une gamme dans le composant
	 * 
	 * @param g
	 */
	public void loadScale(Scale g) {

		clearSelectedTrackDef();

		this.largeur_carton = g.getWidth();
		this.entrepiste = g.getIntertrackHeight();
		this.largeurpiste = g.getTrackWidth();
		this.nbpistes = g.getTrackNb();
		this.premierepiste = g.getFirstTrackAxis();
		this.speed = g.getSpeed();
		this.name = g.getName();
		this.registersets = g.getPipeStopGroupList();
		this.infos = g.getInformations();
		this.contact = g.getContact();
		this.preferredViewedInverted = g.isPreferredViewedInversed();
		this.scalestate = g.getState();
		this.properties = g.getAllProperties();

		this.constraintList = g.getConstraints();

		notedefs.clear();
		AbstractTrackDef[] defs = g.getTracksDefinition();
		for (AbstractTrackDef td : defs) {
			notedefs.add(td);
		}

		if (g != null) {
			this.rendering = g.getRendering();
			if (this.rendering == null)
				this.rendering = new VirtualBookRendering();

			try {
				BufferedImage bi = ImageIO.read(rendering.getBackgroundImage());
				cartontrame = new TexturePaint(bi, new Rectangle2D.Double(0, 0,
						bi.getWidth(), bi.getHeight()));
			} catch (Exception ex) {
				logger.error("loadscale", ex); //$NON-NLS-1$
				cartontrame = null;
			}
		} else {
			this.rendering = null;
		}

		this.invalidate();
		resize();
	}

	/**
	 * Vérifie la gamme en cours de construction
	 * 
	 * @return
	 */

	@SuppressWarnings(value = "unused")
	public String checkScale() {
		try {

			Scale g = constructScale();
			return null;

		} catch (ScaleException ex) {
			return ex.getMessage();
		}
	}

	/**
	 * Cree la gamme associée à celle modifiée dans le formulaire
	 * 
	 * @return
	 * @throws ScaleException
	 */
	public Scale constructScale() throws ScaleException {
		// Création d'une copie du tableau de définition des pistes
		AbstractTrackDef[] tds = new AbstractTrackDef[nbpistes];
		for (int i = 0; i < nbpistes; i++) {

			if (notedefs.size() > i) {
				tds[i] = notedefs.get(i);
			}

		}

		return new Scale(name, largeur_carton, entrepiste, largeurpiste,
				premierepiste, nbpistes, tds, registersets, speed,
				constraintList, infos, scalestate, contact, rendering,
				preferredViewedInverted, bookMoveRightToLeft, properties );
	}

	/**
	 * Change de nom de la gamme
	 * 
	 * @param newname
	 */
	public void changeName(String newname) {
		this.name = newname;
		repaint();
	}

	public void changeLargeurCarton(double largeurcarton) {
		this.largeur_carton = largeurcarton;
		resize();
		repaint();
	}

	private void resize() {
		if (getParent() != null) {
			((JComponent) getParent()).revalidate();
			invalidate();
		}
	}

	public void changeEntrePiste(double entrepiste) {
		this.entrepiste = entrepiste;
		repaint();
	}

	public void changeLargeurPiste(double largeurpiste) {
		this.largeurpiste = largeurpiste;
		repaint();
	}

	public void changePremierePiste(double premierepiste) {
		this.premierepiste = premierepiste;
		repaint();
	}

	public void changeVitesse(double vitesse) {
		this.speed = vitesse;
		repaint();
	}

	public void changePreferredViewInverted(boolean pv) {
		this.preferredViewedInverted = pv;
		repaint();
	}

	public void changeInfos(String newinfos) {
		this.infos = newinfos;
		repaint();
	}

	public void changeState(String state) {
		this.scalestate = state;
		repaint();
	}

	public void changeContact(String contact) {
		this.contact = contact;
		repaint();
	}

	public void changeNbPiste(int nbpiste) {
		this.nbpistes = nbpiste;
		for (int i = notedefs.size(); i < nbpistes; i++) {
			notedefs.add(null);
		}

		repaint();
	}

	/**
	 * Change the track def
	 * 
	 * @param track
	 *            the track index
	 * @param td
	 *            the track def
	 * 
	 *            if the track index is not in the range, nothing is done
	 */
	public void changePisteDef(int track, AbstractTrackDef td) {
		if (track >= 0 && track < notedefs.size()) {
			notedefs.set(track, td);
			repaint();
		}
	}

	public void changeBookMovingRightToLeft(boolean bookMoveRightToLeft) {
		this.bookMoveRightToLeft = bookMoveRightToLeft;
		repaint();
	}

	/**
	 * Get the track definition
	 * 
	 * @param track
	 * @return
	 */
	public AbstractTrackDef getTrackDef(int track) {
		if (track >= 0 && track < notedefs.size()) {
			return notedefs.get(track);
		}
		return null;
	}

	public void shiftTracksDown(int track) {
		if (track >= 0 && track < notedefs.size()) {
			notedefs.add(track, null);
			notedefs.remove(notedefs.size() - 1);
			fireHighlightTrackDef(notedefs.get(track));
			repaint();
		}
	}

	public void shiftTracksUp(int track) {
		if (track >= 0 && track < notedefs.size()) {
			notedefs.remove(track);
			notedefs.add(null);
			fireHighlightTrackDef(notedefs.get(track));
			repaint();
		}
	}

	/**
	 * Get the number of tracks
	 * 
	 * @return
	 */
	public int getTrackDefCount() {
		return notedefs.size();
	}

	public void changeConstraintList(ConstraintList newConstraintList) {
		this.constraintList = newConstraintList;
		repaint();
	}

	public void changeRendering(VirtualBookRendering vbrendering) {
		this.rendering = vbrendering;
		repaint();
	}

	/**
	 * get the selected track
	 * 
	 * @return the index, or -1 if none is selected
	 */
	public int getSelectedTrackDef() {
		return selected_track;
	}

	public int getHighLightedTrackDef() {
		return highligthed_track;
	}

	public void setSelectedTrackDef(int newselected) {
		logger.debug("setSelectedTrackDef " + newselected); //$NON-NLS-1$
		selected_track = newselected;
		fireTrackSelected();
		repaint();
	}

	public void clearSelectedTrackDef() {
		setSelectedTrackDef(-1);
	}

	public void changeRegisterSetList(PipeStopGroupList l) {
		registersets = (PipeStopGroupList) SerializeTools.deepClone(l);
		repaint();
	}

	protected void fireTrackSelected() {
		logger.debug("fireTrackSelected for selected " + selected_track); //$NON-NLS-1$
		if (listener != null)
			listener.trackSelected(selected_track);

	}

	public static BufferedImage createScaleImage(Scale scale) {
		ScaleComponent sc = new ScaleComponent();

		sc.loadScale(scale);

		Dimension size = sc.getComponentInternalSize();
		sc.setSize(size);

		// Create a picture of the scale ...
		BufferedImage bi = new BufferedImage(size.width, size.height,
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = bi.createGraphics();
		try {
			sc.paint(g);

			return bi;
		} finally {
			g.dispose();
		}

	}

	public void addListener(ScaleHighlightListener listener) {
		this.hightlightListener.add(listener);
	}

	public void removeListener(ScaleHighlightListener listener) {
		this.hightlightListener.remove(listener);
	}

	@SuppressWarnings(value = "unchecked")
	protected void fireHighlightTrackDef(AbstractTrackDef td) {
		for (Iterator iterator = this.hightlightListener.iterator(); iterator
				.hasNext();) {
			ScaleHighlightListener l = (ScaleHighlightListener) iterator.next();
			l.trackIsHighlighted(td);
		}
	}

	@SuppressWarnings(value = "unchecked")
	protected void fireResetHightLight() {
		for (Iterator iterator = this.hightlightListener.iterator(); iterator
				.hasNext();) {
			ScaleHighlightListener l = (ScaleHighlightListener) iterator.next();
			l.hightlightReseted();
		}
	}

	public void setSpeedDraw(boolean speedDraw) {
		this.speedDraw = speedDraw;
	}

	public boolean isSpeedDraw() {
		return speedDraw;
	}

	public void changeProperties(HashMap<String, String> hash) {
		this.properties = hash;
	}

}
