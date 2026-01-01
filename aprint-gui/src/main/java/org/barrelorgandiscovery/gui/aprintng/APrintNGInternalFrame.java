package org.barrelorgandiscovery.gui.aprintng;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.Timer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.prefs.PrefixedNamePrefsStorage;
import org.barrelorgandiscovery.tools.Dirtyable;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.SwingUtils;
import org.barrelorgandiscovery.ui.animation.InfiniteProgressPanel;

/**
 * base class for APrintNG windows all internal aprint frame derives for this
 * base class
 *
 * @author use
 */
public class APrintNGInternalFrame extends JFrame implements IAPrintWait, Dirtyable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1074513965929037352L;

	private static Logger logger = Logger.getLogger(APrintNGInternalFrame.class);

	private Dimension defaultSize = new Dimension(800, 600);

	/**
	 * This function get the internal name of the frame for preferences by default,
	 * take the name of the current class
	 *
	 * @return
	 */
	protected String getInternalFrameNameForPreferences() {
		return getClass().getSimpleName();
	}

	private InfiniteProgressPanel infiniteprogresspanel = new InfiniteProgressPanel(null, 20, 0.5f, 0.5f);

	public APrintNGInternalFrame(IPrefsStorage prefsStorage) throws Exception {
		initPrefsStorage(prefsStorage);
		initializeComponents();
	}

	public APrintNGInternalFrame(IPrefsStorage prefsStorage, String title) throws Exception {
		super(title);
		initPrefsStorage(prefsStorage);
		initializeComponents();
	}

	public APrintNGInternalFrame(IPrefsStorage prefsStorage, String title, boolean resizable) throws Exception {
		super(title);
		initPrefsStorage(prefsStorage);
		setResizable(resizable);
		initializeComponents();
	}

	public APrintNGInternalFrame(IPrefsStorage prefsStorage, String title, boolean resizable, boolean closable)
			throws Exception {
		this(prefsStorage, title, resizable);
	}

	public APrintNGInternalFrame(IPrefsStorage prefsStorage, String title, boolean resizable, boolean closable,
			boolean maximizable) throws Exception {
		this(prefsStorage, title, resizable);
	}

	public APrintNGInternalFrame(IPrefsStorage prefsStorage, String title, boolean resizable, boolean closable,
			boolean maximizable, boolean iconifiable) throws Exception {
		this(prefsStorage, title, resizable);
	}

	/** Save the users preferences */
	protected PrefixedNamePrefsStorage prefixedNamePrefsStorage;
	
	/** Timer to debounce preference saving during resize */
	private Timer savePreferencesTimer;

	protected void initPrefsStorage(IPrefsStorage prefsStorage) {
		prefixedNamePrefsStorage = new PrefixedNamePrefsStorage(getInternalFrameNameForPreferences(), prefsStorage);
		try {
			prefixedNamePrefsStorage.load();
		} catch (Exception ex) {
			logger.error("error in loading prefs :" + ex.getMessage(), ex); //$NON-NLS-1$
		}
	}

	protected void setupIcon() {
		this.setIconImage(APrintNG.getAPrintApplicationIcon());
	}

	protected void initializeComponents() throws Exception {
		setupIcon();
		setGlassPane(infiniteprogresspanel);

		Point windowPosition = prefixedNamePrefsStorage.getPoint("windowposition"); //$NON-NLS-1$
		if (windowPosition != null) {
			setLocation(windowPosition);
		} else {
			SwingUtils.center(this);
		}

		// Ensure frame is resizable
		setResizable(true);
		if (logger.isDebugEnabled()) {
			logger.debug("Frame set to resizable: " + getClass().getSimpleName());
		}
		
		// Ensure no maximum size constraint (allow unlimited resizing)
		setMaximumSize(null);
		if (logger.isDebugEnabled()) {
			logger.debug("Frame maximum size set to null (unlimited): " + getClass().getSimpleName());
		}
		

		Dimension d = prefixedNamePrefsStorage.getDimension("windowsize"); //$NON-NLS-1$
		if (d != null) {
			// Always warn if loading a dimension with width at minimum
			if (d.width == defaultSize.width) {
				logger.warn(String.format(
					"⚠️ Loading saved dimension with width at minimum (%dpx)! %s: %dx%d - This may cause resize issues!",
					defaultSize.width,
					getClass().getSimpleName(), d.width, d.height
				));
				logger.warn("Consider deleting the saved dimension from preferences to reset to default size.");
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug(String.format(
					"Loaded saved dimension from preferences: %dx%d",
					d.width, d.height
				));
			}
			// Validate dimension to ensure it's not too small
			int minWidth = defaultSize.width;
			int minHeight = defaultSize.height;
			if (d.width < minWidth || d.height < minHeight) {
				// Use default size if saved dimension is too small
				logger.warn(String.format(
					"Saved dimension too small (%dx%d), using default %dx%d",
					d.width, d.height, defaultSize.width, defaultSize.height
				));
				setSize(defaultSize);
			} else if (d.width == minWidth) {
				// Warn if width is exactly at minimum - this will prevent horizontal resizing
				logger.warn(String.format(
					"⚠️ Saved dimension width is exactly at minimum (%dpx)! Using default %dx%d instead to allow resizing.",
					minWidth, defaultSize.width, defaultSize.height					
				));
				setSize(defaultSize);
			} else {
				setSize(d);
				if (logger.isDebugEnabled()) {
					logger.debug("Set frame size to saved dimension: " + d);
				}
			}
		} else {
			// default
			if (logger.isDebugEnabled()) {
				logger.warn("No saved dimension found, using default 800x600");
			}
			setSize(defaultSize);
		}

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// add hooks for windows preference saving
		// Use debounced save to avoid interfering with resize operations
		savePreferencesTimer = new Timer(500, e -> {
			saveDimensionPreferences();
		});
		savePreferencesTimer.setRepeats(false); // Only fire once after delay

		addComponentListener(new ComponentListener() {

			public void componentHidden(ComponentEvent e) {
			}

			public void componentMoved(ComponentEvent e) {
				// Save position immediately on move
				if (savePreferencesTimer != null) {
					savePreferencesTimer.restart();
				}
			}

			public void componentResized(ComponentEvent e) {
				
				// Debounce the save - only save after resize completes
				// This prevents interference with the resize drag operation
				if (savePreferencesTimer != null) {
					savePreferencesTimer.restart();
				}
			}

			public void componentShown(ComponentEvent e) {
			}
		});

		wadapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {

				saveDimensionPreferences();

				if (!askForClose()) {
					// keep the window
					// setVisible(true);
						

				} else {
					try {
						dispose();
					} catch (Throwable t) {

					}
				}
			}
		};
		addWindowListener(wadapter);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.gui.aprintng.IAPrintWait#infiniteStartWait(java
	 * .lang.String, org.barrelorgandiscovery.gui.ICancelTracker)
	 */
	public void infiniteStartWait(String text, ICancelTracker cancelTracker) {

		// assert !infiniteprogresspanel.isStarted();
		final String finalText = text;

		infiniteprogresspanel.setCancelTracker(cancelTracker);

		Runnable r = new Runnable() {
			public void run() {
				infiniteprogresspanel.start(finalText);
			}
		};

		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (Exception ex) {
				logger.error("infiniteStartWait :" + ex.getMessage(), ex); //$NON-NLS-1$
			}
		} else {
			r.run();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.gui.aprintng.IAPrintWait#infiniteStartWait(java
	 * .lang.String)
	 */
	public void infiniteStartWait(String text) {
		infiniteStartWait(text, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.gui.aprintng.IAPrintWait#infiniteEndWait()
	 */
	public void infiniteEndWait() {

		Runnable r = new Runnable() {
			public void run() {
				infiniteprogresspanel.stop();
			}
		};
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (Exception ex) {
				logger.error("infiniteEndWait :" + ex.getMessage(), ex); //$NON-NLS-1$
			}
		} else {
			r.run();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.gui.aprintng.IAPrintWait#infiniteChangeText(
	 * java.lang.String)
	 */
	public void infiniteChangeText(final String text) {

		Runnable r = new Runnable() {
			public void run() {
				infiniteprogresspanel.setText(text);
			}
		};

		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (Exception ex) {
				logger.error("infiniteChangeText :" + ex.getMessage(), ex); //$NON-NLS-1$
			}
		} else {
			r.run();
		}
	}

	/** */
	protected void saveDimensionPreferences() {

		Dimension currentSize = getSize();
		if (currentSize != null && currentSize.width > defaultSize.width && currentSize.height > defaultSize.height) {
			prefixedNamePrefsStorage.setDimension("windowsize", currentSize);
		}
		prefixedNamePrefsStorage.setPoint("windowposition", getLocation()); //$NON-NLS-1$
		prefixedNamePrefsStorage.save();
	}

	private boolean isWindowDirty = false;

	public void clearDirty() {
		isWindowDirty = false;
	}

	public boolean isDirty() {
		return isWindowDirty;
	}

	public void toggleDirty() {
		isWindowDirty = true;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension preferredSize = super.getPreferredSize();
		if (preferredSize != null) {
			logger.debug("getPreferredSize - Loading preferred size from super: " + preferredSize);
			return preferredSize;
		}

		// load from preferences
		Dimension savedSize = prefixedNamePrefsStorage.getDimension("windowsize");
		if (savedSize != null) {
			logger.debug("getPreferredSize - Loading saved size from preferences: " + savedSize);
			return savedSize;
		}

		logger.debug("getPreferredSize - No saved size found, using default " + defaultSize);
		return defaultSize;
	}

	/**
	 * This method is called when the window is closing, by default, it is called
	 * when the window is closing
	 * @return false if user ask to Not Close the frame
	 */
	protected boolean askForClose() {
		if (isDirty()) {
			int result = JOptionPane.showConfirmDialog(null, Messages.getString("APrintNGInternalFrame.10")); //$NON-NLS-1$
			if (result == JOptionPane.YES_OPTION) {
				dispose();
			} else {
				return false;
			}

			// do nothing else
		} else {
			dispose();
		}
		return true;
	}

	private boolean frameDisposed = false;
	private WindowAdapter wadapter;

	@Override
	public void dispose() {
		logger.debug("dispose frame");

		// Stop and cleanup the save timer
		if (savePreferencesTimer != null) {
			savePreferencesTimer.stop();
			savePreferencesTimer = null;
		}

		removeWindowListener(wadapter);

		try {

			Container panel = getContentPane();
			Component[] allComponents = panel.getComponents();
			if (allComponents != null) {
				for (Component c : allComponents) {
					if (c instanceof Disposable) {
						try {
							((Disposable) c).dispose();
						} catch (Throwable ex) {
						}
					}
				}
			}

			clearDirty();
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		}
		frameDisposed = true;
		super.dispose();
	}

	public boolean isDisposed() {
		return frameDisposed;
	}

	@Override
	public void setSize(int width, int height) {

		// trigger timer to save the size to the preferences
		if (savePreferencesTimer != null) {
			savePreferencesTimer.restart();
		}
		super.setSize(width, height);
	}

	@Override
	public void setSize(Dimension d) {
		if (d != null) {
			setSize(d.width, d.height);
		} else {
			// save the size to the preferences
			super.setSize(d);
		}
	}


}
