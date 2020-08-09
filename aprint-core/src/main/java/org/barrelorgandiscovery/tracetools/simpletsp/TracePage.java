package org.barrelorgandiscovery.tracetools.simpletsp;

import java.util.ArrayList;
import java.util.List;

import org.barrelorgandiscovery.gui.atrace.Punch;
import org.barrelorgandiscovery.gui.atrace.PunchConverter;
import org.barrelorgandiscovery.gui.atrace.ConverterResult;
import org.barrelorgandiscovery.virtualbook.Hole;


/**
 * Classe définissant une page de gestion du perçage
 * 
 * @author Freydiere Patrice
 */
public class TracePage {

	private List<Hole> refnotes = null;

	/**
	 * Première note définie pour le perçage
	 */
	private Hole firstNote = null;

	/**
	 * Derniere note définie pour le perçage
	 */
	private Hole lastNote = null;

	/**
	 * Gamme associée à la page
	 */
	private PunchConverter converter = null;

	/**
	 * Creation de la page de perçage
	 * 
	 * @param notes
	 *            la liste des notes définissant la page
	 */
	public TracePage(List<Hole> notes, PunchConverter conv) {
		if (notes == null || conv == null)
			throw new IllegalArgumentException();

		this.refnotes = notes;
		this.converter = conv;

		firstNote = notes.get(0);
		lastNote = notes.get(notes.size() - 1);

	}

	/**
	 * Donne le nombre de notes associée à cette page de perçage ..
	 * 
	 * @return
	 */
	public int size() {
		return refnotes.size();
	}

	public Hole getFirstNote() {
		return firstNote;
	}

	public void setFirstNote(Hole firstNote) {
		this.firstNote = firstNote;
	}

	public Hole getLastNote() {
		return lastNote;
	}

	public void setLastNote(Hole lastNote) {
		this.lastNote = lastNote;
	}

	private Punch[] reorder(Punch[] punches) {
		// conversion en points ...

		Points pts = new Points(punches.length);
		for (int i = 0; i < punches.length; i++) {
			pts.X[i] = punches[i].x;
			pts.Y[i] = punches[i].y;
		}

		PlaneGraph pg = new PlaneGraph(pts);

		Path p = Optimizer.optimize(pg, 0, punches.length - 1 ,1000);

		ArrayList<Punch> al = new ArrayList<Punch>();
		for (int i = 0; i < p.From.length; i++) {
			al.add(new Punch(pts.X[p.From[i]], pts.Y[p.From[i]]));
		}

		Punch[] result = new Punch[al.size()];
		al.toArray(result);

		return result;

	}

	public Punch[] optimize() {

		ConverterResult<Punch> res = converter.convert(refnotes);

		return reorder(res.result);

	}

}
