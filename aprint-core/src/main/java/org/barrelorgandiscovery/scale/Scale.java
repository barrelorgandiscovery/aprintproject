package org.barrelorgandiscovery.scale;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.SerializeTools;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.virtualbook.rendering.VirtualBookRendering;

/**
 * Implement a scale definition for an instrument / virtual book
 * 
 * @author Freydiere Patrice
 */
public class Scale implements Serializable {

	private static Logger logger = Logger.getLogger(Scale.class);

	/**
	 * Sérialize id
	 */
	private static final long serialVersionUID = -8783842623884249931L;

	/**
	 * name of the scale, id of the scale
	 */
	private String name;

	/**
	 * free text information about the scale (problems, leak of informations
	 * ...)
	 */
	private String informations;

	/**
	 * Book width in mm
	 */
	private double width;

	/**
	 * index of the first track position from the reference
	 */
	private double premierepiste;

	/**
	 * width of the track (mm)
	 */
	private double largeurpiste;

	/**
	 * inter track width (mm)
	 */
	private double entrepiste;

	/**
	 * track definition
	 */
	private AbstractTrackDef[] notedefs;

	/**
	 * track number in the scale
	 */
	private int nbpistes;

	/**
	 * speed of the rendering, in mm/s
	 */
	private double speed;

	/**
	 * define the registersetlist
	 */
	private PipeStopGroupList registersets;

	/**
	 * constraints associated to the scale
	 */
	private ConstraintList constraintlist;

	/**
	 * scale state "In Progress"
	 */
	public static final String GAMME_STATE_INPROGRESS = "INPROGRESS"; //$NON-NLS-1$

	/**
	 * scale state "completed"
	 */
	public static final String GAMME_STATE_COMPLETED = "COMPLETED"; //$NON-NLS-1$

	/**
	 * state of the scale
	 */
	private String state = null;

	/**
	 * Contact of the scale (email)
	 */
	private String contact = null;

	/**
	 * Rendering of the display
	 */
	private VirtualBookRendering rendering = null;

	/**
	 * Flag specifying that user prefer view the scale inverted Reference of the
	 * scale definition
	 */
	private boolean preferredViewedInversed = false;

	/**
	 * direction of the book motion, if true, the book move right to left
	 */
	private boolean bookMovingRightToLeft = true;

	/**
	 * Free properties for this scale or instrument
	 */
	private Map<String, String> properties = null;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            name of the scale
	 * @param width
	 *            the width
	 * @param intertrackdistance
	 *            intertrack distance in mm
	 * @param trackwidth
	 *            with of a track in mm
	 * @param firsttrackaxisdistance
	 *            first track axis distance in mm
	 * @param tracknumber
	 *            track number
	 * @param tracksdefinition
	 *            tracks definition
	 * @param registersets
	 *            registerset definitions
	 * @param speed
	 *            speed
	 * @param constraintlist
	 *            constraint list
	 * @param infos
	 *            scale information (free text)
	 * @param state
	 *            status of this scale
	 * @param contact
	 *            contact for the scale
	 * @param rendering
	 *            display renderer
	 * @param preferredViewedInverted
	 *            is the reference on top
	 * @param bookMovingRightToLeft
	 *            the book is moving on the instrument
	 * @param properties
	 *            additional properties
	 * 
	 */
	public Scale(String name, double width, double intertrackdistance,
			double trackwidth, double firsttrackaxisdistance, int tracknumber,
			AbstractTrackDef[] tracksdefinition,
			PipeStopGroupList registersets, double speed,
			ConstraintList constraintlist, String infos, String state,
			String contact, VirtualBookRendering rendering,
			boolean preferredViewedInverted, boolean bookMovingRightToLeft,
			Map<String, String> properties) throws ScaleException {
		super();

		if ("".equals(name) || name == null) //$NON-NLS-1$
			throw new ScaleException(Messages.getString("Scale.3")); //$NON-NLS-1$

		this.name = name;

		if (width == Double.NaN || width <= 0)
			throw new ScaleException(Messages.getString("Scale.4")); //$NON-NLS-1$

		this.width = width;

		if (intertrackdistance == Double.NaN || intertrackdistance <= 0)
			throw new ScaleException(Messages.getString("Scale.5")); //$NON-NLS-1$

		this.entrepiste = intertrackdistance;

		if (trackwidth == Double.NaN || trackwidth <= 0)
			throw new ScaleException(Messages.getString("Scale.6")); //$NON-NLS-1$

		this.largeurpiste = trackwidth;

		if (firsttrackaxisdistance == Double.NaN || firsttrackaxisdistance <= 0)
			throw new ScaleException(Messages.getString("Scale.7")); //$NON-NLS-1$

		this.premierepiste = firsttrackaxisdistance;

		if (tracknumber < 0)
			throw new ScaleException(Messages.getString("Scale.8")); //$NON-NLS-1$

		this.nbpistes = tracknumber;

		if (tracksdefinition == null || tracksdefinition.length != tracknumber)
			throw new ScaleException(Messages.getString("Scale.9") + name //$NON-NLS-1$
					+ Messages.getString("Scale.10")); //$NON-NLS-1$

		this.notedefs = tracksdefinition;

		this.speed = speed;

		// copie de l'objet
		this.registersets = (PipeStopGroupList) SerializeTools
				.deepClone(registersets);

		if (this.registersets != null && this.registersets.size() == 0) {
			this.registersets = null;
		}

		// Vérification de la cohérence de la gamme ...
		if (this.registersets != null) {
			// vérification ...

			// on vérifie que toutes les notes font partie d'un ensemble de
			// registre
			for (int i = 0; i < notedefs.length; i++) {

				AbstractTrackDef abstractTrackDef = notedefs[i];

				if (abstractTrackDef instanceof NoteDef) {
					NoteDef d = (NoteDef) abstractTrackDef;

					if (d.getRegisterSetName() == null) {
						throw new ScaleException(
								""		+ d.toString() //$NON-NLS-1$
										+ Messages.getString("Scale.12") + i + Messages.getString("Scale.13")); //$NON-NLS-1$ //$NON-NLS-2$
					}

				} else if (abstractTrackDef instanceof RegisterCommandStartDef) {
					RegisterCommandStartDef rcsd = (RegisterCommandStartDef) abstractTrackDef;

					PipeStopGroup associatedRegisterSet = this.registersets
							.get(rcsd.getRegisterSetName());

					if (associatedRegisterSet != null) {
						// on a trouvé
						// on vérifie que le register est existant à l'intérieur
						// ...

						if (!associatedRegisterSet.exist(rcsd
								.getRegisterInRegisterSet())) {
							throw new ScaleException(
									Messages.getString("Scale.14") + i //$NON-NLS-1$
											+ Messages.getString("Scale.15") //$NON-NLS-1$
											+ rcsd.getRegisterInRegisterSet()
											+ Messages.getString("Scale.16") //$NON-NLS-1$
											+ associatedRegisterSet.getName());
						}

					} else {
						throw new ScaleException(
								Messages.getString("Scale.17") + i + Messages.getString("Scale.18") //$NON-NLS-1$ //$NON-NLS-2$
										+ rcsd.getRegisterSetName()
										+ Messages.getString("Scale.19") + rcsd //$NON-NLS-1$
										+ Messages.getString("Scale.20")); //$NON-NLS-1$
					}
				}
			}

			// normalement si on passe tout ça ... c'est bon

		} // (registersets != null)
		else {
			// registersets == null

			// les notes ne doivent pas avoir de registres ...
			// on vérifie que toutes les notes font partie d'un ensemble de
			// registre
			for (int i = 0; i < notedefs.length; i++) {

				AbstractTrackDef abstractTrackDef = notedefs[i];

				if (abstractTrackDef instanceof NoteDef) {
					NoteDef d = (NoteDef) abstractTrackDef;

					if (d.getRegisterSetName() != null) {
						throw new ScaleException(
								Messages.getString("Scale.21") + d.toString() //$NON-NLS-1$
										+ Messages.getString("Scale.22") + d.getRegisterSetName() //$NON-NLS-1$
										+ Messages.getString("Scale.23")); //$NON-NLS-1$
					}

				} else if (abstractTrackDef instanceof RegisterCommandStartDef
						|| abstractTrackDef instanceof RegisterSetCommandResetDef) {
					throw new ScaleException(Messages.getString("Scale.24")); //$NON-NLS-1$
				}
			}

			// c'est OK pour les notes , et les commandes de registre ....

		}

		this.constraintlist = (ConstraintList) SerializeTools
				.deepClone(constraintlist);

		if (this.constraintlist != null && this.constraintlist.size() == 0) {
			this.constraintlist = null;
		}

		if (constraintlist != null) {
			// check contraint list

			// TODO implementation of the check of contraints list
		}

		this.informations = infos;
		this.contact = contact;
		this.state = state;

		VirtualBookRendering d = rendering;

		if (d == null)
			d = new VirtualBookRendering();

		this.rendering = d;

		this.preferredViewedInversed = preferredViewedInverted;

		this.bookMovingRightToLeft = bookMovingRightToLeft;

		if (properties == null) {
			this.properties = new HashMap<String, String>();
		} else {
			this.properties = new HashMap<String, String>(properties);
		}
	}
	
	/**
	 * 
	 * get the distance between each track (in mm)
	 * 
	 * @return the distance
	 */
	public double getIntertrackHeight() {
		return entrepiste;
	}

	/**
	 * get width of a track (in mm)
	 * 
	 * @return the width of tracks
	 */
	public double getTrackWidth() {
		return largeurpiste;
	}

	/**
	 * get track number
	 * 
	 * @return the track number
	 */
	public int getTrackNb() {
		return nbpistes;
	}

	/**
	 * get the definition of the tracks
	 * 
	 * @return an array containing the definition of the track (note, drum,
	 *         register ...)
	 */
	public AbstractTrackDef[] getTracksDefinition() {
		return notedefs;
	}

	/**
	 * get the first track axis distance from the reference
	 * 
	 * @return
	 */
	public double getFirstTrackAxis() {
		return premierepiste;
	}

	/**
	 * get the width of the scale (the virtual book)
	 * 
	 * @return the width in mm
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * get the scale name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * get the speed of the organ
	 * 
	 * @return speed in mm/s
	 */
	public double getSpeed() {
		return speed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj.getClass() != getClass())
			return false;

		Scale g = (Scale) obj;

		if (registersets == null) {
			if (g.registersets != null)
				return false;

		} else {

			if (!registersets.equals(g.registersets))
				return false;

		}

		logger.debug("register sets OK ... ");

		if (constraintlist == null) {
			if (g.constraintlist != null)
				return false;
		} else {
			if (!constraintlist.equals(g.constraintlist))
				return false;
		}

		if (properties == null) {
			if (g.properties != null)
				return false;
		} else {
			if (!properties.equals(g.properties))
				return false;
		}

		logger.debug("constraints OK ... ");

		boolean r = name.equals(g.name) && entrepiste == g.entrepiste
				&& largeurpiste == g.largeurpiste && nbpistes == g.nbpistes
				&& Arrays.equals(notedefs, g.notedefs)
				&& premierepiste == g.premierepiste && width == g.width
				&& speed == g.speed && g.rendering.equals(rendering)
				&& StringTools.equals(contact, g.contact)
				&& StringTools.equals(informations, g.informations)
				&& StringTools.equals(state, g.state);

		logger.debug("retvalue for equals :" + r);
		return r;
	}

	private static Scale midiInstance = null;

	/**
	 * utility function that create from scratch a dummy scale of the midi
	 * definition
	 * 
	 * @return the midi scale
	 */
	public static Scale getGammeMidiInstance() {

		if (midiInstance != null)
			return midiInstance;

		synchronized (Scale.class) {
			if (midiInstance != null)
				return midiInstance;

			AbstractTrackDef[] notes = new AbstractTrackDef[256];
			for (int i = 0; i < notes.length; i++) {
				if (i > 127) {
					notes[i] = new PercussionDef(i % 128, Double.NaN,
							Double.NaN);
				} else {
					notes[i] = new NoteDef(i % 128);
				}
			}
			try {
				Scale g = new Scale(
						"Midi", 256, 1, 1, 0.5, 256, notes, null, 60, //$NON-NLS-1$
						null, null, null, null, null, false, false, null);

				midiInstance = g;

				return g;

			} catch (Exception ex) {
				ex.printStackTrace(System.err); // Erreur d'implantation
				return null;
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	// utilisé pour swing ...
	public String toString() {
		return name;
	}

	/**
	 * look for a track associated to a midi note
	 * 
	 * @param midinote
	 *            the midi note
	 * @return track index of this note, -1 returned else if not found
	 */
	public int findNoteDefTrack(int midinote) {
		for (int i = 0; i < notedefs.length; i++) {
			if (notedefs[i] == null)
				continue; // suivant
			if (notedefs[i] instanceof NoteDef) {
				NoteDef nd = (NoteDef) notedefs[i];
				if (nd.getMidiNote() == midinote)
					return i;
			}
		}
		return -1; // non trouvé
	}

	/**
	 * Get all the percussion defs, this function remove the PercussionDef
	 * duplicated
	 * 
	 * @return an array of percussiondef in the scale
	 */
	public PercussionDef[] findUniquePercussionDefs() {
		Set<PercussionDef> plist = new TreeSet<PercussionDef>();
		for (int i = 0; i < notedefs.length; i++) {
			if (notedefs[i] == null)
				continue; // suivant

			if (notedefs[i] instanceof PercussionDef) {
				plist.add((PercussionDef) notedefs[i]);
			}
		}
		return plist.toArray(new PercussionDef[0]);
	}

	/**
	 * get the first track associated with a mininote, for a register
	 * 
	 * @param midinote
	 * @param registersetname
	 * @return the track index, or -1 if not found
	 */
	public int findNoteDefTrack(int midinote, String registersetname) {
		if (registersetname == null)
			return findNoteDefTrack(midinote);

		for (int i = 0; i < notedefs.length; i++) {
			if (notedefs[i] == null)
				continue; // suivant
			if (notedefs[i] instanceof NoteDef) {
				NoteDef nd = (NoteDef) notedefs[i];
				if (nd.getMidiNote() == midinote
						&& registersetname.equals(nd.getRegisterSetName()))
					return i;
			}
		}
		return -1; // non trouvé
	}

	/**
	 * find all tracks associated to a specific registerset name
	 * 
	 * @param registersetname
	 *            name of the registerset
	 * @return a track list of the founded notes associated to the registerset
	 */
	public int[] findNoteDefTrack(String registersetname) {
		ArrayList<Integer> retvalue = new ArrayList<Integer>();

		if (registersetname == null)
			return new int[0];

		for (int i = 0; i < notedefs.length; i++) {
			if (notedefs[i] == null)
				continue; // suivant
			if (notedefs[i] instanceof NoteDef) {
				NoteDef nd = (NoteDef) notedefs[i];
				if (registersetname.equals(nd.getRegisterSetName())) {
					retvalue.add(i);
				}
			}
		}

		int[] r = new int[retvalue.size()];
		for (int i = 0; i < r.length; i++) {
			r[i] = retvalue.get(i);
		}

		return r;
	}

	/**
	 * get the track index for a given percussion
	 * 
	 * @param percussion
	 *            midi code
	 * @return the index, -1 if not found
	 */
	public int findPercussionDef(int percussion) {
		for (int i = 0; i < notedefs.length; i++) {
			if (notedefs[i] == null)
				continue; // suivant
			if (notedefs[i] instanceof PercussionDef) {
				PercussionDef nd = (PercussionDef) notedefs[i];
				if (nd.getPercussion() == percussion)
					return i;
			}
		}
		return -1; // non trouvé
	}

	/**
	 * 
	 * mm to Time
	 * 
	 * @param mm
	 *            the distance
	 * @return the time in microseconds
	 */
	public long mmToTime(double mm) {
		return (long) (mm / getSpeed() * 1000000);
	}

	/**
	 * time to mm
	 * 
	 * @param time
	 *            the time in microseconds
	 * @return the distance in mm
	 */
	public double timeToMM(long time) {
		return ((double) time) * getSpeed() / 1000000;
	}

	/**
	 * get a registerset liste copy
	 * 
	 * @return the copy
	 */
	public PipeStopGroupList getPipeStopGroupList() {
		if (registersets == null)
			return null;

		// Récupère une copie de la définition des listes (pour éviter les
		// problèmes de modifications par référence)

		return new PipeStopGroupList(registersets);

		// Perfs pbs
		// return (PipeStopGroupList) SerializeTools.deepClone(registersets);
	}

	public PipeStopGroupList getPipeStopGroupListRef() {
		return registersets;
	}

	/**
	 * Get a constraint list copy
	 * 
	 * @return the constraintlist
	 */
	public ConstraintList getConstraints() {
		return (ConstraintList) SerializeTools.deepClone(constraintlist);
	}

	/**
	 * get the informations
	 * 
	 * @return
	 */
	public String getInformations() {
		return informations;
	}

	/**
	 * get the scale state
	 * 
	 * @return
	 */
	public String getState() {
		return this.state;
	}

	/**
	 * get the scale contact
	 * 
	 * @return
	 */
	public String getContact() {
		return this.contact;
	}

	/**
	 * get the rendering associated to this scale
	 * 
	 * @return
	 */
	public VirtualBookRendering getRendering() {
		return this.rendering;
	}

	/**
	 * Get a flag indicating that the user prefer view the scale inverted
	 * 
	 * @return
	 */
	public boolean isPreferredViewedInversed() {
		return preferredViewedInversed;
	}

	/**
	 * get the book orientation
	 * 
	 * @return
	 */
	public boolean isBookMovingRightToLeft() {
		return bookMovingRightToLeft;
	}

	/**
	 * define the orientation of the book
	 * 
	 * @param bookMovingRightToLeft
	 */
	public void setBookMovingRightToLeft(boolean bookMovingRightToLeft) {
		this.bookMovingRightToLeft = bookMovingRightToLeft;
	}

	/**
	 * get the free property associated to this scale
	 * 
	 * @param key
	 * @return
	 */
	public String getProperty(String key) {
		return this.properties.get(key);
	}

	/**
	 * get All the properties
	 * 
	 * @return
	 */
	public Map<String, String> getAllProperties() {
		return new HashMap<String, String>(this.properties);
	}

}
