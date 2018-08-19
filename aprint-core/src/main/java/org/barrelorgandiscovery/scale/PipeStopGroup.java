package org.barrelorgandiscovery.scale;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.barrelorgandiscovery.tools.HashCodeUtils;

/**
 * Classe that define a pipestopgroup
 * 
 * @author Freydiere Patrice
 * 
 */
public class PipeStopGroup implements Serializable, Iterable<PipeStop> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3398954422481107322L;

	private String registersetname;

	private Map<String, PipeStop> registers = new HashMap<String, PipeStop>();

	/**
	 * Constructeur
	 * 
	 * @param registersetname
	 *            le nom du jeu de registre
	 * @param registers
	 *            la liste des registres associés, peut être null
	 */
	public PipeStopGroup(String registersetname, PipeStop[] registers) {

		if (registersetname == null)
			throw new IllegalArgumentException();

		this.registersetname = registersetname;

		if (registers != null) {
			for (int i = 0; i < registers.length; i++) {
				this.registers.put(registers[i].getName(), registers[i]);
			}
		}
	}

	public PipeStopGroup(PipeStopGroup copy) {
		this(copy.getName(), copy.getPipeStops());
	}

	/**
	 * Nom du jeu de registre
	 * 
	 * @return
	 */
	public String getName() {
		return this.registersetname;
	}

	/**
	 * Ajoute un registre dans le set
	 * 
	 * @param register
	 */
	public void add(PipeStop register) {
		registers.put(register.getName(), register);
	}

	/**
	 * supprime le register passé en paramètres
	 * 
	 * @param register
	 */
	public void remove(String pipestopname) {
		registers.remove(pipestopname);
	}

	/**
	 * Indique si le registre passé en paramètre existe
	 * 
	 * @param register
	 * @return
	 */
	public boolean exist(String register) {
		return registers.containsKey(register);
	}

	/**
	 * Get a pipestop by its name
	 * 
	 * @param name
	 * @return
	 */
	public PipeStop getPipeStopByName(String name) {
		return registers.get(name);
	}

	/**
	 * Liste des registres associés au jeu de registre
	 * 
	 * @return
	 */
	public PipeStop[] getPipeStops() {

		PipeStop[] retvalue = new PipeStop[registers.size()];
		int cpt = 0;
		for (PipeStop s : registers.values()) {
			retvalue[cpt++] = s;
		}
		return retvalue;
	}

	public PipeStop[] getRegisteredControlledPipeStops() {
		Vector<PipeStop> v = new Vector<PipeStop>();
		for (PipeStop s : registers.values()) {
			if (s.isRegisteredControlled())
				v.add(s);
		}

		PipeStop[] retvalue = new PipeStop[v.size()];
		v.copyInto(retvalue);

		return retvalue;
	}

	public Iterator<PipeStop> iterator() {
		return registers.values().iterator();
	}

	@Override
	public int hashCode() {
		int s = HashCodeUtils.SEED;

		s = HashCodeUtils.hash(s, registersetname);
		for (Entry<String, PipeStop> p : registers.entrySet()) {
			s = HashCodeUtils.hash(s, p.getValue());
		}

		return s;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;

		if (obj.getClass() != getClass())
			return false;

		PipeStopGroup ps = (PipeStopGroup) obj;
		if (ps.registersetname != registersetname)
			return false;

		if (!ps.registers.equals(registers))
			return false;

		return true;
	}

}
