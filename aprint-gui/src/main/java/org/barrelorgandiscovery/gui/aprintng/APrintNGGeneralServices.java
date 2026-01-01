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
	
	/**
	 * Get information about the currently active window (VirtualBookFrame or Script Console).
	 * 
	 * @return ActiveWindowInfo with details about the active window, or null if no window is active
	 */
	ActiveWindowInfo getActiveWindow();
	
	/**
	 * Get information about a Swing component in a window
	 * @param windowId ID of the window (frameId or console resource URI)
	 * @param componentPath Path to the component (e.g., "frame_1/toolbarPanel/button_0")
	 * @return Component information or null if not found
	 */
	SwingComponentInfo getComponentInfo(String windowId, String componentPath);
	
	/**
	 * List all components in a window
	 * @param windowId ID of the window
	 * @param filterType Optional filter by component type (e.g., "JButton")
	 * @param maxDepth Maximum depth to traverse (default: 10)
	 * @return List of component information
	 */
	java.util.List<SwingComponentInfo> listComponents(String windowId, String filterType, int maxDepth);
	
	/**
	 * Find components matching criteria
	 * @param windowId ID of the window
	 * @param criteria Search criteria
	 * @return List of matching components
	 */
	java.util.List<SwingComponentInfo> findComponents(String windowId, ComponentSearchCriteria criteria);
	
	/**
	 * Get the value of a component
	 * @param windowId ID of the window
	 * @param componentPath Path to the component
	 * @return Component value (text, selection, etc.) or null
	 */
	Object getComponentValue(String windowId, String componentPath);
	
	/**
	 * Get a specific property of a component
	 * @param windowId ID of the window
	 * @param componentPath Path to the component
	 * @param propertyName Name of the property
	 * @return Property value or null
	 */
	Object getComponentProperty(String windowId, String componentPath, String propertyName);
	
	/**
	 * List all open windows (VirtualBookFrames, consoles, etc.)
	 * @return List of window information ordered by most recent usage
	 */
	java.util.List<ActiveWindowInfo> listAllWindows();
	
	/**
	 * Activate/focus a window by its ID
	 * @param windowId ID of the window to activate
	 * @return true if window was found and activated, false otherwise
	 */
	boolean activateWindow(String windowId);
	
	/**
	 * Register a window for usage tracking (to determine most recently used window)
	 * @param window The window to register
	 */
	void registerWindowForTracking(java.awt.Window window);
	
	/**
	 * Get the window activation history
	 * @param limit Maximum number of events to return (0 for all)
	 * @return List of activation events (most recent first)
	 */
	java.util.List<WindowActivationHistory.ActivationEvent> getWindowActivationHistory(int limit);
	
	/**
	 * Get activation history for a specific window
	 * @param windowId ID of the window
	 * @return List of activation events for that window
	 */
	java.util.List<WindowActivationHistory.ActivationEvent> getWindowActivationHistoryForWindow(String windowId);
	
	/**
	 * Get the current active window from history
	 * @return Current active window event or null
	 */
	WindowActivationHistory.ActivationEvent getCurrentActiveWindowFromHistory();
}
