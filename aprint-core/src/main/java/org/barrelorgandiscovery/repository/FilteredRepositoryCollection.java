package org.barrelorgandiscovery.repository;


/**
 * Filtered vision of a repository collection, keeping the repository collection
 * identity
 * 
 * @author Freydiere Patrice
 * 
 */
public class FilteredRepositoryCollection extends Repository2Collection
		implements DerivedRepository {

	private RepositoryTreeFilter filter;
	private Repository2Collection collection;

	/**
	 * Create a filtered Repository collection
	 * 
	 * @param coll
	 *            the original unfiltered repository collection
	 * @param filter
	 *            the filter
	 */
	public FilteredRepositoryCollection(Repository2Collection coll,
			RepositoryTreeFilter filter) {
		super(coll.getName());

		this.collection = coll;
		this.filter = filter;

		for (int i = 0; i < coll.getRepositoryCount(); i++) {
			super.addRepository(new FilteredRepository(coll.getRepository(i),
					filter));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.repository.Repository2Collection#addRepository
	 * (org.barrelorgandiscovery.repository.Repository2)
	 */
	@Override
	public void addRepository(Repository2 repository) {
		super.addRepository(new FilteredRepository(repository, filter));
	}

	/**
	 * get the unfiltered repository collection
	 * 
	 * @return
	 */
	public Repository2Collection getRepository() {
		return this.collection;
	}

	public Repository2 getUnderlyingRepository() {
		return this.collection;
	}

}
