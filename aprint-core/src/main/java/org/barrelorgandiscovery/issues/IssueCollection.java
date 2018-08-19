package org.barrelorgandiscovery.issues;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;


public class IssueCollection implements Iterable<AbstractIssue> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8289281168240707746L;

	private ArrayList<AbstractIssue> internalcollection = new ArrayList<AbstractIssue>();

	private IssueSpatialIndex sindex = new IssueSpatialIndex();

	public IssueCollection() {
		super();
	}

	public void add(AbstractIssue ai) {
		if (ai == null)
			return;

		internalcollection.add(ai);
		// ajout dans les indexes
		if (ai instanceof AbstractSpatialIssue) {
			AbstractSpatialIssue si = (AbstractSpatialIssue) ai;
			sindex.add(si);
		}

	}

	public void addAll(IssueCollection col) {
		for (Iterator<AbstractIssue> iterator = col.iterator(); iterator
				.hasNext();) {
			AbstractIssue ai = iterator.next();
			add(ai);
		}
	}

	public int size() {
		return internalcollection.size();
	}

	public AbstractIssue get(int index) {
		return internalcollection.get(index);
	}

	

	/**
	 * Recherche d'un ensemble de pbs associé à une zone donnée
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public Set<AbstractSpatialIssue> find(long start, long end) {
		return sindex.find(start, end);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<AbstractIssue> iterator() {
		return internalcollection.iterator();
	}

}
