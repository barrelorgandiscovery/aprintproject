package org.barrelorgandiscovery.gui.aprintng;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.ui.animation.InfiniteProgressPanel;

public class JPanelWaitableInJFrameWaitable extends JPanel implements IAPrintWait {

	private static Logger logger = Logger.getLogger(JPanelWaitableInJFrameWaitable.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 3737504716262151042L;

	public JPanelWaitableInJFrameWaitable() {
		super();
	}

	public JPanelWaitableInJFrameWaitable(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public JPanelWaitableInJFrameWaitable(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public JPanelWaitableInJFrameWaitable(LayoutManager layout) {
		super(layout);

	}

	/**
	 * sequencer utilisé pour jouer le morceau.
	 */
	private InfiniteProgressPanel infiniteprogresspanel = new InfiniteProgressPanel(null, 20, 0.5f, 0.5f);

	Component oldGlassPane = null;

	private InfiniteProgressPanel getOrSetGlassPane() {
		JRootPane old = SwingUtilities.getRootPane(this);
		if (old == null) {
			return null;
		}
		
		Component rootPaneParent = old.getParent();
		
		this.oldGlassPane = old.getGlassPane();
		old.setGlassPane(infiniteprogresspanel);
		old.invalidate();
		old.revalidate();
		return infiniteprogresspanel;
	}

	private void releaseGlassPane() {
		if (oldGlassPane == null) {
			return;
		}
		JRootPane old = SwingUtilities.getRootPane(this);
		old.setGlassPane(oldGlassPane);
		oldGlassPane = null;
	}

	public void infiniteStartWait(String text, ICancelTracker cancelTracker) {

		// put the glasspane

		assert !infiniteprogresspanel.isStarted();
		final String finalText = text;

		infiniteprogresspanel.setCancelTracker(cancelTracker);

		Runnable r = new Runnable() {
			public void run() {
				getOrSetGlassPane();
				infiniteprogresspanel.start(finalText);
				infiniteprogresspanel.revalidate();
				infiniteprogresspanel.repaint();
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

	public void infiniteStartWait(String text) {
		infiniteStartWait(text, null);
	}

	public void infiniteEndWait() {

		Runnable r = new Runnable() {
			public void run() {
				infiniteprogresspanel.stop();
				releaseGlassPane();
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

}
