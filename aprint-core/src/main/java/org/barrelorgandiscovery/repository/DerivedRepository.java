package org.barrelorgandiscovery.repository;

/**
 * Interface implemented by all the repository wrapper (rights, filtering ...)
 * 
 * @author use
 * 
 */
public interface DerivedRepository {

	/**
	 * Get the underlying repository
	 * 
	 * @return
	 */
	Repository2 getUnderlyingRepository();

}
