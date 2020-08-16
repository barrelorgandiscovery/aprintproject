package org.barrelorgandiscovery.virtualbook.transformation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.AbstractEvent;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Cette classe gère une transposition point à point
 * 
 * 
 * @author Freydiere Patrice
 */
public class LinearTransposition extends AbstractTransposeVirtualBook {

	private Scale source;

	private Scale dest;

	private String name;

	private boolean builtin = false;

	private class ArrayInteger extends ArrayList<Integer> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7195557889715754168L;

		/**
		 * permutation circulaire des éléments du tableau
		 */
		public void shift() {
			if (size() > 0) {
				int temp = get(0);
				remove(0);
				add(temp);
			}
		}
	}

	/**
	 * Tableau de correspondance avec rotation
	 */
	private ArrayInteger[] correspondance;

	private boolean applydischarge = true;

	/**
	 * Cree une nouvelle transposition entre deux gammes
	 * 
	 * @param source
	 *            la gamme source de la transposition
	 * @param destination
	 *            la gamme de destination de la transposition
	 */
	public LinearTransposition(Scale source, Scale destination, String name,
			boolean applydischarge, boolean isbuiltin) {

		this.source = source;

		this.dest = destination;

		this.name = name; // nom de la transposition

		correspondance = new ArrayInteger[source.getTrackNb()];

		this.applydischarge = applydischarge;

		this.builtin = isbuiltin;

	}

	/**
	 * Définit une correspondance entre une note source et sa correspondance
	 * dans la destination (piste)
	 * 
	 * @param source
	 *            la note de la gamme source
	 * @param destination
	 *            la note de la gamme destination
	 */
	public void setCorrespondance(int source, int destination)
			throws TranspositionException {

		if (destination >= dest.getTrackNb())
			throw new TranspositionException(Messages
					.getString("LinearTransposition.0")); //$NON-NLS-1$

		if (correspondance[source] == null) {
			correspondance[source] = new ArrayInteger();
		}

		correspondance[source].add(destination);

	}

	/**
	 * clear correspondance
	 * 
	 * @param source
	 * @throws TranspositionException
	 */
	public void clearCorrespondance(int source) throws TranspositionException {
		correspondance[source] = null;
	}

	/**
	 * remove from transposition the destination track
	 * 
	 * @param track
	 * @throws TranspositionException
	 */
	public void clearCorrespondanceTo(int track) throws TranspositionException {
		for (int i = 0; i < correspondance.length; i++) {
			ArrayInteger ai = correspondance[i];
			if (ai != null) {
				ai.remove((Integer) track);
			}
		}
	}

	/**
	 * Récupère la prochaine correspondance à la touche
	 * 
	 * @param source
	 * @return
	 */
	public int getNextCorrespondance(int source) {

		ArrayInteger tmp = correspondance[source];

		if (tmp == null)
			return -1;

		if (tmp.size() == 0)
			return -1;

		int c = tmp.get(0);

		tmp.shift();

		return c;

	}

	/**
	 * Récupère la liste des correspondances associées à un "track"
	 * 
	 * @param source
	 * @return
	 */
	public int[] getAllCorrespondances(int source) {

		int[] retvalue = null;
		if (correspondance[source] != null) {
			ArrayInteger ai = correspondance[source];
			if (ai.size() > 0) {
				int[] tmp = new int[ai.size()];
				for (int i = 0; i < ai.size(); i++) {
					tmp[i] = ai.get(i);
				}
				retvalue = tmp;

			} // sinon retourne null ...
		}

		return retvalue;
	}

	public boolean hasCorrespondance(int source) {

		if (correspondance[source] != null && correspondance[source].size() > 0)
			return true;

		return false;
	}

	public Scale getScaleDestination() {
		return dest;
	}

	public Scale getScaleSource() {
		return source;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Effectue une transposition, par défaut, on effectue une translation de
	 * notes ...
	 * 
	 * @param carton
	 *            le carton à transposer ..
	 * @return le résultat de la transposition ...
	 */
	public TranspositionResult transpose(VirtualBook carton) {

		Scale gammedestination = getScaleDestination();
		Scale gammesource = carton.getScale();

		VirtualBook retvalue = new VirtualBook(gammedestination);
		retvalue.setName(carton.getName());

		ArrayList<Hole> notesnontransposees = new ArrayList<Hole>();

		List<Hole> notes = carton.getOrderedHolesCopy();
		Iterator<Hole> it = notes.iterator();
		while (it.hasNext()) {
			Hole n = it.next();

			// parcours de tous les "trous"

			if (hasCorrespondance(n.getTrack())) {

				int newpiste = getNextCorrespondance(n.getTrack());

				long timestamp = n.getTimestamp();

				long holelength = n.getTimeLength();

				AbstractTrackDef pisteorigine = gammesource
						.getTracksDefinition()[n.getTrack()];
				if (pisteorigine instanceof PercussionDef) {
					
					// on redresse le timestamp en fonction du retard de la
					// gamme d'origine

					PercussionDef d = (PercussionDef) pisteorigine;
					if (!Double.isNaN(d.getRetard())) {
						// on a un retard dans la gamme d'origine ...

						long decalage = (long) (d.getRetard()
								/ gammesource.getSpeed() * 1000000);
						timestamp += decalage; // on corrige le tire
					}

				}

				AbstractTrackDef pistedestination = gammedestination
						.getTracksDefinition()[newpiste];

				if (pistedestination instanceof PercussionDef) {

					if (applydischarge) {

						PercussionDef d = (PercussionDef) pistedestination;
						if (!Double.isNaN(d.getRetard())) {
							long decalage = (long) (d.getRetard()
									/ gammedestination.getSpeed() * 1000000);
							timestamp -= decalage;
						}

						if (!Double.isNaN(d.getLength())) {
							// la longueur de la note est imposé
							long longueur = (long) (d.getLength()
									/ gammedestination.getSpeed() * 1000000);
							holelength = longueur;
						}
					}
				}

				Hole r = new Hole(newpiste, timestamp, holelength);
				retvalue.addHole(r);

			} else {
				// la note n'a pas de correspondance
				// on a enlevé une note ...
				notesnontransposees.add(n);
			}
		}

		// adding events ..
		
		Set<AbstractEvent> events = carton.getOrderedEventsByRef();
		for (Iterator iterator = events.iterator(); iterator.hasNext();) {
			AbstractEvent abstractEvent = (AbstractEvent) iterator.next();
			retvalue.addEvent(abstractEvent);
		}
		
		
		TranspositionResult r = new TranspositionResult();
		r.untransposedholes = notesnontransposees;
		r.virtualbook = retvalue;

		return r;
	}

	/**
	 * Cette fonction cree une transposition fictive de midi à midi
	 * 
	 * @return
	 */
	public static AbstractTransposeVirtualBook getMidiTransposition()
			throws TranspositionException {
		LinearTransposition tr = new LinearTransposition(Scale
				.getGammeMidiInstance(), Scale.getGammeMidiInstance(), "Midi", //$NON-NLS-1$
				true, true);
		for (int i = 0; i < tr.getScaleSource().getTrackNb(); i++) {
			tr.setCorrespondance(i, i);
		}
		return tr;
	}

	/**
	 * Récupère l'ordre d'affichage de la transposition
	 */
	public int getOrder() {
		return 0;
	}

	public boolean isBuiltin() {
		return builtin;
	}

}
