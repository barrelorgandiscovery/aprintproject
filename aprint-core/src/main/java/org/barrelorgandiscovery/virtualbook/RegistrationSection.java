package org.barrelorgandiscovery.virtualbook;

import java.io.Serializable;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.PipeStopGroup;
import org.barrelorgandiscovery.scale.PipeStopGroupList;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.SerializeTools;

/**
 * Classe interne définissant une registration sur une période de temps
 * (instrument sur une plage de registres)
 * 
 * pour chaque Jeu de registre, on mémorise les jeux actifs
 * 
 * @author Freydiere Patrice
 * 
 */
class RegistrationSection implements Serializable {

	private static Logger logger = Logger.getLogger(RegistrationSection.class);

	private long start = -1;

	private RegistrationSection previous = null;

	/**
	 * Hash mémorisant les registres activés pour chaque jeu de registre clef :
	 * jeu de registre valeur : liste des registres activés
	 */
	private HashMap<String, TreeSet<String>> activatedRegisters = new HashMap<String, TreeSet<String>>();

	private Scale gamme = null;

	/**
	 * Cree une nouvelle section de registre
	 * 
	 * @param start
	 * @param previous
	 * @param gamme
	 */
	RegistrationSection(long start, RegistrationSection previous, Scale gamme) {

		this.start = start;
		this.gamme = gamme;
		this.previous = previous;
	}

	RegistrationSection() {

	}

	RegistrationSection(RegistrationSection copy) {

		if (copy == null)
			return;
		this.start = copy.start;
		this.gamme = copy.gamme;

		this.activatedRegisters = (HashMap<String, TreeSet<String>>) SerializeTools
				.deepClone(copy.activatedRegisters);

	}

	void setStart(long start) {
		this.start = start;
	}

	void setGamme(Scale gamme) {
		this.gamme = gamme;
	}

	void setPrevious(RegistrationSection previous) {
		this.previous = previous;
	}

	/**
	 * Ajout un registre dans la section
	 * 
	 * @param registerset
	 * @param register
	 */
	void addRegister(String registerset, String register) throws Exception {

		if (logger.isDebugEnabled())
			logger.debug("addRegister " + registerset + " -> " + register);
		assert registerset != null;
		assert register != null;
		assert checkRegisterExist(registerset, register);

		PipeStopGroup safeRegisterSet = getSafeRegisterSet(registerset);
		if (safeRegisterSet == null)
			throw new Exception("registerset " + registerset + " not found");

		if (!safeRegisterSet.exist(register))
			throw new Exception("register " + register + " in " + registerset
					+ " not found");

		TreeSet<String> treeSet = activatedRegisters.get(registerset);
		if (treeSet == null)
			treeSet = new TreeSet<String>();
		treeSet.add(register);

		activatedRegisters.put(registerset, treeSet);
	}

	private boolean checkRegisterExist(String registerset, String register) {
		PipeStopGroup r = getSafeRegisterSet(registerset);
		if (r == null)
			return false;

		return r.exist(register);
	}

	private PipeStopGroup getSafeRegisterSet(String registerset) {
		
		PipeStopGroupList registerSetList = gamme.getPipeStopGroupListRef();
		if (registerSetList == null) {
			// no register set defined
			logger.debug("no registerset list");
			return null;
		}

		assert registerSetList != null;

		PipeStopGroup r = registerSetList.get(registerset);
		if (r == null) {
			logger.debug("no registerset associated to " + registerset);
			return null;
		}
		return r;
	}

	boolean hasRegister(String registerset, String register) {
		PipeStopGroup safeRegisterSet = getSafeRegisterSet(registerset);
		return safeRegisterSet.exist(register);
	}

	/**
	 * Supprime un registre dans la section
	 * 
	 * @param registerset
	 * @param register
	 */
	void removeRegister(String registerset, String register) throws Exception {
		logger.debug("removeRegister " + registerset + " -> " + register);
		assert registerset != null;
		assert register != null;
		assert checkRegisterExist(registerset, register);
		assert !"ALL".equalsIgnoreCase(register);

		PipeStopGroup safeRegisterSet = getSafeRegisterSet(registerset);
		if (safeRegisterSet == null)
			throw new Exception("registerset " + registerset + " not found");

		if (!safeRegisterSet.exist(register))
			throw new Exception("register " + register + " in " + registerset
					+ " not found");

		TreeSet<String> treeSet = activatedRegisters.get(registerset);

		if (treeSet != null)
			treeSet.remove(register);
	}

	void removeRegisters(String registerset) throws Exception {
		logger.debug("removeRegisters in " + registerset);
		assert registerset != null;

		PipeStopGroup safeRegisterSet = getSafeRegisterSet(registerset);
		if (safeRegisterSet == null)
			throw new Exception("registerset " + registerset + " not found");

		TreeSet<String> treeSet = activatedRegisters.get(registerset);
		if (treeSet != null)
			treeSet.clear();

	}

	/**
	 * liste les registres activés pour un jeu de registres
	 * 
	 * @param registerset
	 * @return
	 */
	String[] getRegisters(String registerset) throws Exception {
		logger.debug("getRegisters " + registerset);
		assert registerset != null;

		PipeStopGroup safeRegisterSet = getSafeRegisterSet(registerset);
		if (safeRegisterSet == null)
			throw new Exception("registerset " + registerset + " not found");

		TreeSet<String> treeSet = activatedRegisters.get(registerset);
		if (treeSet == null)
			return new String[0];

		return treeSet.toArray(new String[0]);
	}

	public long getStart() {
		return start;
	}

	public RegistrationSection getPrevious() {
		return previous;
	}
}
