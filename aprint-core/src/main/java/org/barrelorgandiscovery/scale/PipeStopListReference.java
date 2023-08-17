package org.barrelorgandiscovery.scale;

import java.util.Vector;

import org.barrelorgandiscovery.messages.Messages;


/**
 * Classe contenant les constantes associées aux gammes
 */
public class PipeStopListReference {

	public static final String REGISTERSET_BASSE = "BASSE"; //$NON-NLS-1$
	public static final String REGISTERSET_ACCOMPAGNEMENT = "ACCOMPAGNEMENT"; //$NON-NLS-1$
	public static final String REGISTERSET_CONTRECHAMP = "CONTRECHAMP"; //$NON-NLS-1$
	public static final String REGISTERSET_CHANT = "CHANT"; //$NON-NLS-1$
	public static final String REGISTERSET_CHANT3 = "CHANT3"; //$NON-NLS-1$
	public static final String REGISTERSET_FIORITURE = "FIORITURE"; //$NON-NLS-1$

	private static final String[] registersetlist = new String[] {
			REGISTERSET_BASSE, REGISTERSET_ACCOMPAGNEMENT,
			REGISTERSET_CONTRECHAMP, REGISTERSET_CHANT, REGISTERSET_CHANT3,
			REGISTERSET_FIORITURE };

	private static final String[] registerlist = new String[] { "BOURDON", //$NON-NLS-1$
			"TROMBONE", "VIOLONS", "CLARINETTE", "PISTON", "FLUTE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"TROMPETTE", "VIOLONCELLE", "VOIXCELESTES", "UDAMORIS", "PICOLLOS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"FLAGEOLETS" , "SAX", "FLUTE8P", "VIBRAPHONE", "ACCORDEON", "XYLOPHONE", "FORTE" , "HARMONICFLUTE", //$NON-NLS-1$
			"TREMOLO", "CELLO", "CELLOPETIT", "CELLOGRAVE", "BIPHONE", "FLUTEJAZZ", "ACCORDEON",
			"VOIXCELESTES","VIBRATON","BAXOPHONE","TREMOLOJAZZ","UNDAMARIS","BARYTON","FLUTE8"}; //$NON-NLS-1$

	public String[] getRegisterSetList() {
		return registersetlist;
	}

	public String[] getRegisterList() {
		return registerlist;
	}

	public ReferencedRegisterSet[] getReferencedRegisterSet() {
		String[] l = getRegisterSetList();

		Vector<ReferencedRegisterSet> v = new Vector<ReferencedRegisterSet>();
		for (int i = 0; i < l.length; i++) {
			v.add(new ReferencedRegisterSet(l[i], Messages
					.getString("RegisterSet." + l[i]))); //$NON-NLS-1$
		}

		ReferencedRegisterSet[] retvalue = new ReferencedRegisterSet[v.size()];
		v.copyInto(retvalue);

		return retvalue;
	}

	public ReferencedRegister[] getReferencedRegister() {
		String[] l = getRegisterList();

		Vector<ReferencedRegister> v = new Vector<ReferencedRegister>();
		for (int i = 0; i < l.length; i++) {
			v.add(new ReferencedRegister(l[i], Messages
					.getString("RegisterSet." + l[i]))); //$NON-NLS-1$
		}

		ReferencedRegister[] retvalue = new ReferencedRegister[v.size()];
		v.copyInto(retvalue);

		return retvalue;
	}

	public static String getLocalizedPipeStopGroup(String pipeStopGroup) {
		return Messages.getString("RegisterSet." + pipeStopGroup);
	}

	public static String getLocalizedPipeStop(String pipeStop) {
		return Messages.getString("Register." + pipeStop);
	}

}
