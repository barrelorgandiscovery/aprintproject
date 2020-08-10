package org.barrelorgandiscovery.optimizers.punchconverters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.issues.IssueHole;
import org.barrelorgandiscovery.issues.IssuesConstants;
import org.barrelorgandiscovery.optimizers.OptimizerResult;
import org.barrelorgandiscovery.optimizers.model.Punch;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;

/**
 * This class convert holes in punch
 * 
 * @author use
 * 
 */
public class PunchConverter {
 
	private static final Logger logger = Logger.getLogger(PunchConverter.class);

	/**
	 * the associated scale
	 */
	private Scale gamme;

	/**
	 * Largeur du poincon en mm
	 */
	private double largeurpoincon;

	/**
	 * recouvrement pour les coups de poinçons
	 */
	private double recouvrement;
	
	
	
	public PunchConverter(Scale gamme, double largeurpoincon) {
		this.gamme = gamme;
		this.largeurpoincon = largeurpoincon;
		this.recouvrement = 0;
	}

	public PunchConverter(Scale gamme, double largeurpoincon,
			double recouvrement) {
		this.gamme = gamme;
		this.largeurpoincon = largeurpoincon;
		this.recouvrement = recouvrement;
		assert this.recouvrement < this.largeurpoincon;
	}

	private double lastdistance = Double.NaN;

	public PunchConverter(Scale gamme, double largeurpoincon,
			double recouvrement, double lastdistance) {
		this(gamme, largeurpoincon, recouvrement);
		this.lastdistance = lastdistance;
	}

	/**
	 * Converti un timestamp en mm, en fonction de la vitesse de l'instrument
	 * 
	 * @param millis
	 *            temps en x
	 * @return la longueur en mm
	 */
	private double toX(long millis) {
		return (1.0 * (millis / 1000) / 1000.0) * gamme.getSpeed();
	}

	/**
	 * Converti un ensemble de note en coups de poinçon
	 * 
	 * @param notes
	 *            la liste des notes
	 * @param largeurpoincon
	 *            la largeur du poinçon
	 * @return
	 */
	public OptimizerResult<Punch> convert(List<Hole> notes) {

		OptimizerResult<Punch> result = new OptimizerResult<>();

		ArrayList<Punch> p = new ArrayList<Punch>();

		for (Iterator<Hole> it = notes.iterator(); it.hasNext();) {
			Hole n = it.next();

			if (toX(n.getTimeLength()) < largeurpoincon) {
				result.holeerrors.add(new IssueHole(
						IssuesConstants.HOLE_TOO_SMALL, new Hole[] { n }));
				// logger.error("la note " + n
				// + " est trop petite pour pouvoir être percée");

			} else {

				double y;

				y = n.getTrack() * gamme.getIntertrackHeight()
						+ gamme.getFirstTrackAxis();

				double start = toX(n.getTimestamp()) + largeurpoincon / 2;
				double end = toX(n.getTimestamp() + n.getTimeLength())
						- largeurpoincon / 2;

				ArrayList<Punch> currentholepunches = new ArrayList<Punch>();
				for (double x = start; x < end; x += (largeurpoincon - recouvrement)) {
					currentholepunches.add(new Punch(x, y));
				}

				if (!Double.isNaN(lastdistance)) {
					// traitement du dernier coup de poinçon ...

					if (currentholepunches.size() > 0
							&& (end
									- currentholepunches.get(currentholepunches
											.size() - 1).x < lastdistance)) {
						// nothing to do ...
					} else {
						currentholepunches.add(new Punch(end, y));
					}

				} else {
					currentholepunches.add(new Punch(end, y));
				}

				p.addAll(currentholepunches);

			}

		}

		Punch[] tmp = new Punch[p.size()];
		p.toArray(tmp);
		result.result = tmp;
		return result;
	}
}
