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

		Dimension d = prefixedNamePrefsStorage.getDimension("window"); //$NON-NLS-1$
		if (d != null) {
			setSize(d);
		} else {
			// default
			setSize(800, 600);
		}

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// add hooks for windows preference saving

		addComponentListener(new ComponentListener() {

			public void componentHidden(ComponentEvent e) {
			}

			public void componentMoved(ComponentEvent e) {
			}

			public void componentResized(ComponentEvent e) {
				saveDimensionPreferences();
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
		prefixedNamePrefsStorage.setDimension("window", getSize()); //$NON-NLS-1$
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

}
