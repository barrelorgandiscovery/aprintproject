package org.barrelorgandiscovery.gui.aprintng;

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.ui.animation.InfiniteProgressPanel;

public class JFrameWaitable extends JFrame implements IAPrintWait{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1547398895400684254L;

	private static Logger logger = Logger.getLogger(JFrameWaitable.class);
	
	/**
	 * sequencer utilise pour jouer le morceau
	 */
	private InfiniteProgressPanel infiniteprogresspanel = new InfiniteProgressPanel(
				null, 20, 0.5f, 0.5f);

	public JFrameWaitable() throws HeadlessException {
		super();
		setGlassPane(infiniteprogresspanel);
	}

	public JFrameWaitable(GraphicsConfiguration gc) {
		super(gc);
		setGlassPane(infiniteprogresspanel);
	}

	public JFrameWaitable(String title) throws HeadlessException {
		super(title);
		setGlassPane(infiniteprogresspanel);
	}

	public JFrameWaitable(String title, GraphicsConfiguration gc) {
		super(title, gc);
		setGlassPane(infiniteprogresspanel);
	}

	public void infiniteStartWait(String text, ICancelTracker cancelTracker) {
	
		assert !infiniteprogresspanel.isStarted();
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

	public void infiniteStartWait(String text) {
		infiniteStartWait(text, null);
	}

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