package org.barrelorgandiscovery.scale;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.barrelorgandiscovery.tools.HashCodeUtils;

/**
 * Collection de jeu de registres
 * 
 * @author Freydiere Patrice
 */
public class PipeStopGroupList implements Serializable, Iterable<PipeStopGroup> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2295459070303741371L;

	private ArrayList<PipeStopGroup> sets = new ArrayList<PipeStopGroup>();
	private HashMap<String, PipeStopGroup> gbyname = new HashMap<String, PipeStopGroup>();

	public PipeStopGroupList() {

	}

	public PipeStopGroupList(PipeStopGroup[] registersets) {
		if (registersets == null)
			throw new IllegalArgumentException();
		for (PipeStopGroup r : registersets) {
			put(r);
		}
	}
	
	public PipeStopGroupList(PipeStopGroupList copy)
	{
		for (Iterator iterator = copy.sets.iterator(); iterator.hasNext();) {
			PipeStopGroup psg = (PipeStopGroup) iterator.next();
			put(new PipeStopGroup(psg));
		}
	}

	/**
	 * Annule et remplace ou Ajoute un RegisterSet
	 * 
	 * @param r
	 */
	public void put(PipeStopGroup r) {
		if (r == null)
			return;

		PipeStopGroup oldRegisterSet = get(r.getName());
		
		if (oldRegisterSet != null)
			return;
		// sets.remove(oldRegisterSet);
		
		sets.add(r);
		gbyname.put(r.getName(), r);
	}

	/**
	 * Supprime le registerset nommé
	 * 
	 * @param registersetname
	 */
	public void remove(String registersetname) {

		PipeStopGroup todelete = null;
		for (PipeStopGroup s : sets) {
			if (s.getName().equals(registersetname)) {
				todelete = s;
				break;
			}
		}
		if (todelete != null)
		{
			sets.remove(todelete);
			gbyname.remove(todelete.getName());
		}
	}

	/**
	 * Donne le nombre de registerset
	 * 
	 * @return
	 */
	public int size() {
		return sets.size();
	}

	/**
	 * Recupère le registerset i
	 * 
	 * @param i
	 * @return
	 */
	public PipeStopGroup get(int i) {
		return sets.get(i);
	}

	/**
	 * Récupère un registerset par son nom
	 * 
	 * @param name
	 * @return
	 */
	public PipeStopGroup get(String name) {
		
		return gbyname.get(name);
		
//		
//		for (int i = 0; i < size(); i++) {
//			PipeStopGroup registerSet = get(i);
//			if (name.equals(registerSet.getName()))
//				return registerSet;
//		}
//		return null;
//		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<PipeStopGroup> iterator() {
		return sets.iterator();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;

		if (obj.getClass() != getClass())
			return false;

		PipeStopGroupList psgl = (PipeStopGroupList) obj;
		if (psgl.sets.size() != sets.size())
			return false;

		HashMap<String, PipeStopGroup> h = new HashMap<String, PipeStopGroup>();
		for (PipeStopGroup ps : sets) {
			h.put(ps.getName(), ps);
		}

		for (PipeStopGroup g : psgl.sets) {
			if (!h.containsKey(g.getName()))
				return false;

			if (!(h.get(g.getName()).equals(g))) {
				return false;
			}
		}

		return true;

	}

	@Override
	public int hashCode() {
		int seed = HashCodeUtils.SEED;
		for (Iterator<PipeStopGroup> iterator = sets.iterator(); iterator.hasNext();) {
			PipeStopGroup g = iterator.next();
			HashCodeUtils.hash(seed, g.hashCode());
		}

		return seed;
	}

}
