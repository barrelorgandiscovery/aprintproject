package org.barrelorgandiscovery.instrument;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.scale.PipeStop;
import org.barrelorgandiscovery.scale.PipeStopGroup;
import org.barrelorgandiscovery.scale.Scale;

/**
 * This class store the link between the register name and the preset number in
 * the soundbank
 * 
 * @author Freydiere Patrice
 * 
 */
public class RegisterSoundLink implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3314055443638238383L;

	private static Logger logger = Logger.getLogger(RegisterSoundLink.class);

	private static class PipestopSoundBankLink implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3021960417898978497L;
		public String pipestopName;
		public int soundbankPreset;
	}

	/**
	 * links
	 */
	private Map<String, Map<String, PipestopSoundBankLink>> links = new HashMap<String, Map<String, PipestopSoundBankLink>>();

	/**
	 * the scale reference
	 */
	private Scale scale;

	/**
	 * Drum soundbank link
	 */
	private int drumSoundBank = -1;
	
	/**
	 * constructor
	 * 
	 * @param scale
	 *            the scale reference
	 * 
	 */
	RegisterSoundLink(Scale scale) {
		this.scale = scale;
	}

	/**
	 * get a list of the pipe stop group names in which there are mapping
	 */
	public List<String> getPipeStopGroupNamesInWhichThereAreMappings() {
		ArrayList<String> a = new ArrayList<String>();
		for (Iterator iterator = links.entrySet().iterator(); iterator
				.hasNext();) {
			Entry<String, Map<String, PipestopSoundBankLink>> e = (Entry<String, Map<String, PipestopSoundBankLink>>) iterator
					.next();

			a.add(e.getKey());
		}
		return a;
	}

	public List<String> getPipeStopNamesInWhichThereAreMappings(
			String pipestopgroupname) {
		ArrayList<String> a = new ArrayList<String>();
		Map<String, PipestopSoundBankLink> sbl = links.get(pipestopgroupname);
		if (sbl != null) {
			for (Iterator iterator = sbl.keySet().iterator(); iterator
					.hasNext();) {
				String name = (String) iterator.next();
				a.add(name);
			}
		}
		return a;
	}

	public int getInstrumentNumber(String pipeStopGroup, String pipeStop)
			throws Exception {
		PipeStopGroup pipeStopGroup2 = scale.getPipeStopGroupList().get(
				pipeStopGroup);

		/**
		 * Vérification des paramètres
		 */

		if (pipeStopGroup2 == null)
			throw new Exception("pipe stop group " + pipeStopGroup
					+ " not found in the scale");

		if (IEditableInstrument.DEFAULT_PIPESTOPGROUPNAME.equals(pipeStop)) {
			// default sounds on the registration element
			logger.debug("default sound on the specific registration");

		} else {

			if (!pipeStopGroup2.exist(pipeStop))
				throw new Exception("pipe stop " + pipeStop + " in "
						+ pipeStopGroup + " does not exist");

			PipeStop pipeStop1 = pipeStopGroup2.getPipeStopByName(pipeStop);
			if (!pipeStop1.isRegisteredControlled())
				throw new Exception("pipe stop " + pipeStop
						+ " is not controlled by register");

		}
		Map<String, PipestopSoundBankLink> psgl = links.get(pipeStopGroup);
		assert psgl != null;

		PipestopSoundBankLink l = psgl.get(pipeStop);
		assert l != null;

		return l.soundbankPreset;

	}

	public void defineLink(String pipestopgroup, String pipestopname, int link)
			throws Exception {

		PipeStopGroup pipeStopGroup2 = scale.getPipeStopGroupList().get(
				pipestopgroup);

		/**
		 * Vérification des paramètres
		 */

		if (pipeStopGroup2 == null)
			throw new Exception("pipe stop group " + pipestopgroup
					+ " not found in the scale");

		if (IEditableInstrument.DEFAULT_PIPESTOPGROUPNAME.equals(pipestopname)) {
			// default sounds on the registration element
			logger.debug("default sound on the specific registration");

		} else {
			// check

			if (!pipeStopGroup2.exist(pipestopname))
				throw new Exception("pipe stop " + pipestopname + " in "
						+ pipestopgroup + " does not exist");

			PipeStop pipeStop = pipeStopGroup2.getPipeStopByName(pipestopname);
			if (!pipeStop.isRegisteredControlled())
				throw new Exception("pipe stop " + pipestopname
						+ " is not controlled by register");

		}

		Map<String, PipestopSoundBankLink> psgl = links.get(pipestopgroup);

		if (psgl == null) {
			psgl = new HashMap<String, PipestopSoundBankLink>();
		}

		PipestopSoundBankLink pipestopSoundBankLink = new PipestopSoundBankLink();
		pipestopSoundBankLink.pipestopName = pipestopname;
		pipestopSoundBankLink.soundbankPreset = link;

		psgl.put(pipestopname, pipestopSoundBankLink);

		links.put(pipestopgroup, psgl);

	}

	public void setDrumSoundBank(int drumSoundBank) {
		this.drumSoundBank = drumSoundBank;
	}
	
	/**
	 * return the drum soundbank link
	 * @return drum sound bank
	 */
	public int getDrumSoundBank() {
		return drumSoundBank;
	}
	
}
