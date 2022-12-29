package org.barrelorgandiscovery.gui.aprintng;

import java.io.File;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.search.BookIndexing;
import org.barrelorgandiscovery.tools.StringTools;

/**
 * Common services associated to the application
 * 
 * @author use
 * 
 */
public interface APrintNGGeneralServices {

	/**
	 * Create a new Window for looking at the virtualbook and working with it
	 * 
	 * @param virtualBook
	 * @param instrument
	 * @throws Exception
	 */
	APrintNGVirtualBookFrame newVirtualBook(org.barrelorgandiscovery.virtualbook.VirtualBook virtualBook,
			Instrument instrument) throws Exception;

	/**
	 * Create a new Window for looking at the virtualbook and working with it
	 * 
	 * @param virtualBook
	 * @param instrument
	 * @param collection
	 * @throws Exception
	 */
	APrintNGVirtualBookFrame newVirtualBook(org.barrelorgandiscovery.virtualbook.VirtualBook virtualBook,
			Instrument instrument, IssueCollection collection) throws Exception;

	/**
	 * create a new empty VirtualBook for a specified instrument
	 * 
	 * @since 2017.06
	 * @param instrument
	 * @return
	 * @throws Exception
	 */
	APrintNGVirtualBookFrame newVirtualBook(Instrument instrument) throws Exception;

	/**
	 * open file
	 * 
	 * @param fileObject
	 * @throws Exception
	 */
	void openFile(AbstractFileObject fileObject) throws Exception;

	/**
	 * Get the instrument repository
	 * 
	 * @return
	 */
	Repository2 getRepository();

	/**
	 * Get the main windows reference for dialog boxes
	 * 
	 * @return
	 */
	Object getOwnerForDialog();

	/**
	 * Get the version of aprint
	 * 
	 * @return
	 */
	String getVersion();

	/**
	 * Add a new Inner Internal Frame
	 * 
	 * @param internalFrame
	 */
	void addNewInternalFrame(APrintNGInternalFrame internalFrame);

	/**
	 * Get the book indexing service associated to the software
	 * 
	 * @since 2011.6.prerelease.150
	 * @return
	 */
	BookIndexing getBookIndexing();

	/**
	 * Return the async job manager this permit to send processing without blocking
	 * the GUI
	 * 
	 * @return objet on which you can list, submit async jobs
	 */
	AsyncJobsManager getAsyncJobs();

	/**
	 * Get all references of sub windows
	 * 
	 * 
	 * @return list of instanciated sub windows
	 * @since 2011.6.prerelease.145
	 */
	APrintNGInternalFrame[] listInternalFrames();

	/**
	 * Create or get a prefs storage by its name
	 * 
	 * @param name
	 * @return an object for storing preferences
	 * @since 2012.6.prerelease.239
	 */
	IPrefsStorage getPrefsStorage(String name);
}
