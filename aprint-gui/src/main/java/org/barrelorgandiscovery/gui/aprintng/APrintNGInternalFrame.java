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
	private Dimension preferredSize = defaultSize;

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

		Dimension windowSize = prefixedNamePrefsStorage.getDimension("windowsize"); //$NON-NLS-1$
		if (windowSize != null) {
			setSize(windowSize);
		} else {
			setSize(defaultSize);
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
				try {
					// Save position immediately on move
					if (savePreferencesTimer != null) {
						savePreferencesTimer.restart();
					}
					
					// FlatLaf-specific fix: When moved to a different screen, force repaint
					// FlatLaf can have issues when windows are moved between monitors with different DPI
					java.awt.GraphicsConfiguration newGC = getGraphicsConfiguration();
					if (newGC != null) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								// Force a complete repaint when moving between screens
								// This ensures FlatLaf renders correctly on the new screen
								invalidate();
								validate();
								repaint();
								
								// Also repaint the content pane
								Container contentPane = getContentPane();
								if (contentPane != null) {
									contentPane.invalidate();
									contentPane.validate();
									contentPane.repaint();
								}
							}
						});
					}
				} catch (Exception ex) {
					logger.error("error in componentMoved :" + ex.getMessage(), ex); //$NON-NLS-1$
				}
			}

			public void componentResized(ComponentEvent e) {
				try {
					// Debounce the save - only save after resize completes
					// This prevents interference with the resize drag operation
					if (savePreferencesTimer != null) {
						savePreferencesTimer.restart();
					}
					
					// FlatLaf-specific fix: When resized, ensure proper repainting
					// This helps with multi-monitor setups where DPI/scaling may differ
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							revalidate();
							repaint();
						}
					});
				} catch (Exception ex) {
					logger.error("error in componentResized :" + ex.getMessage(), ex); //$NON-NLS-1$
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
		try {
			Dimension currentSize = getSize();
			logger.debug("saveDimensionPreferences - currentSize: " + currentSize); //$NON-NLS-1$
			if (currentSize != null && currentSize.width > defaultSize.width
					&& currentSize.height > defaultSize.height) {
				logger.debug("saveDimensionPreferences - currentSize is valid: " + currentSize); //$NON-NLS-1$
				prefixedNamePrefsStorage.setDimension("windowsize", currentSize);
			}
			prefixedNamePrefsStorage.setPoint("windowposition", getLocation()); //$NON-NLS-1$
			prefixedNamePrefsStorage.save();
		} catch (Exception ex) {
			logger.error("error in saving dimension preferences :" + ex.getMessage(), ex); //$NON-NLS-1$
		}
	}

	/**
	 * Validates and corrects the window size and position after components are initialized.
	 * This method should be called by child classes after all components are set up and
	 * minimum size constraints are applied.
	 * 
	 * This handles:
	 * - Invalid saved sizes (below minimum)
	 * - Sizes too close to minimum that prevent resizing
	 * - Multi-monitor setups (validates position/size against current screen)
	 * 
	 * @param minSize The minimum size that should be enforced (can be null to use frame's minimum)
	 */
	protected void validateAndFixWindowSize(Dimension minSize) {
		// Use provided minimum size or get from frame
		Dimension actualMinSize = minSize != null ? minSize : getMinimumSize();
		if (actualMinSize == null) {
			actualMinSize = defaultSize;
		}

		Dimension currentSize = getSize();
		Dimension savedSize = prefixedNamePrefsStorage.getDimension("windowsize"); //$NON-NLS-1$

		// Get the screen bounds for the screen where this window is located
		// This is critical for multi-monitor setups
		java.awt.GraphicsConfiguration gc = getGraphicsConfiguration();
		java.awt.Rectangle screenBounds;
		if (gc != null) {
			screenBounds = gc.getBounds();
		} else {
			java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
			screenBounds = new java.awt.Rectangle(0, 0, screenSize.width, screenSize.height);
		}

		if (savedSize != null && currentSize != null) {
			// Check if the current size (loaded from preferences) is valid
			// If it's below minimum or matches the problematic saved size, fix it
			if (currentSize.width < actualMinSize.width || currentSize.height < actualMinSize.height) {
				logger.warn("Saved window size (" + currentSize.width + "x" + currentSize.height + 
					") is below minimum (" + actualMinSize.width + "x" + actualMinSize.height + 
					"). Setting to default size.");
				// Set a reasonable default size that fits on the current screen
				Dimension defaultSize = new Dimension(
					Math.min(1000, screenBounds.width - 50), 
					Math.min(700, screenBounds.height - 100));
				setSize(defaultSize);
				currentSize = defaultSize; // Update for position validation
			} else if (currentSize.width == savedSize.width && currentSize.height == savedSize.height) {
				// If current size matches saved size exactly, validate it's reasonable
				// Sometimes saved sizes can cause layout issues if they're at exact boundaries
				if (currentSize.width <= actualMinSize.width + 10) {
					// Too close to minimum - expand it to allow proper resizing
					// But ensure it fits on the current screen
					int newWidth = Math.min(Math.max(1000, currentSize.width + 100), screenBounds.width - 50);
					logger.debug("Window size (" + currentSize.width + "x" + currentSize.height + 
						") is too close to minimum. Expanding to " + newWidth + "x" + currentSize.height + 
						" (screen: " + screenBounds.width + "x" + screenBounds.height + ")");
					setSize(newWidth, currentSize.height);
					currentSize = new Dimension(newWidth, currentSize.height); // Update for position validation
				}
			}

			// Validate that the window position and size are within the current screen bounds
			// This is important for multi-monitor setups where the window might be on a secondary screen
			Point currentLocation = getLocation();
			if (currentLocation != null && currentSize != null) {
				// Check if window is partially or completely outside the current screen
				boolean needsReposition = false;
				if (currentLocation.x + currentSize.width > screenBounds.x + screenBounds.width) {
					needsReposition = true;
				}
				if (currentLocation.y + currentSize.height > screenBounds.y + screenBounds.height) {
					needsReposition = true;
				}
				if (currentLocation.x < screenBounds.x) {
					needsReposition = true;
				}
				if (currentLocation.y < screenBounds.y) {
					needsReposition = true;
				}

				if (needsReposition) {
					logger.debug("Window position (" + currentLocation.x + "," + currentLocation.y + 
						") is outside screen bounds. Adjusting to fit on screen.");
					// Reposition to fit on the current screen
					int newX = Math.max(screenBounds.x, 
						Math.min(currentLocation.x, screenBounds.x + screenBounds.width - currentSize.width));
					int newY = Math.max(screenBounds.y, 
						Math.min(currentLocation.y, screenBounds.y + screenBounds.height - currentSize.height));
					setLocation(newX, newY);
				}
			}
		} else if (currentSize == null || currentSize.width < actualMinSize.width || currentSize.height < actualMinSize.height) {
			// No valid size set - use default that fits on current screen
			Dimension defaultSize = new Dimension(
				Math.min(1000, screenBounds.width - 50), 
				Math.min(700, screenBounds.height - 100));
			setSize(defaultSize);
		}

		// Force validation to ensure layout is correct
		// Use SwingUtilities.invokeLater to ensure this happens after the window is fully initialized
		// This is especially important for multi-monitor setups and FlatLaf
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				validate();
				repaint();
				
				// Additional FlatLaf-specific fixes for multi-monitor setups
				// Ensure components are properly repainted when moved between screens
				Container contentPane = getContentPane();
				if (contentPane != null) {
					contentPane.revalidate();
					contentPane.repaint();
				}
			}
		});
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

	/**
	 * This method is called when the window is closing, by default, it is called
	 * when the window is closing
	 * 
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
