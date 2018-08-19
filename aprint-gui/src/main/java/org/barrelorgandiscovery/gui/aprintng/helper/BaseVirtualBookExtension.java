package org.barrelorgandiscovery.gui.aprintng.helper;

import groovy.lang.Closure;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InformCurrentInstrumentExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InformCurrentVirtualBookExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.LayersExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.ToolbarAddExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.InformVirtualBookFrameExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.VirtualBookFrameExtensionPoints;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Class For Ease VirtualBook Extensions
 * 
 * 
 */
public abstract class BaseVirtualBookExtension extends BaseExtension implements
		VirtualBookFrameExtensionPoints, ToolbarAddExtensionPoint,
		InformCurrentInstrumentExtensionPoint,
		InformCurrentVirtualBookExtensionPoint, LayersExtensionPoint,
		InformVirtualBookFrameExtensionPoint {

	private static Logger logger = Logger
			.getLogger(BaseVirtualBookExtension.class);

	/**
	 * Current Virtual Book Window Instrument
	 */
	protected Instrument currentInstrument;

	/**
	 * Current Virtual Book Reference
	 */
	protected VirtualBook currentVirtualBook;

	/**
	 * Frame of the VirtualBook, for getting frame services
	 */
	protected APrintNGVirtualBookFrame currentFrame;

	
	/**
	 * Constructor
	 * 
	 * @throws Exception
	 */
	public BaseVirtualBookExtension() throws Exception {

	}

	@Override
	protected void setupExtensionPoint(List<ExtensionPoint> initExtensionPoints)
			throws Exception {

		logger.debug("setupExtensionPoint");

		super.setupExtensionPoint(initExtensionPoints);

		initExtensionPoints
				.add(createExtensionPoint(VirtualBookFrameExtensionPoints.class));

		initExtensionPoints
				.add(createExtensionPoint(InformCurrentVirtualBookExtensionPoint.class));

		initExtensionPoints
				.add(createExtensionPoint(InformCurrentInstrumentExtensionPoint.class));

		initExtensionPoints
				.add(createExtensionPoint(LayersExtensionPoint.class));

		initExtensionPoints
				.add(createExtensionPoint(ToolbarAddExtensionPoint.class));

		initExtensionPoints
				.add(createExtensionPoint(InformVirtualBookFrameExtensionPoint.class));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.aprintng.extensionspoints.
	 * VirtualBookFrameExtensionPoints#newExtension()
	 */
	public IExtension newExtension() {
		try {
			logger.debug("newExtension");
			return (IExtension) getClass().newInstance();

		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public void informCurrentVirtualBook(VirtualBook vb) {
		logger.debug("informCurrentVirtualbook");
		this.currentVirtualBook = vb;
	}

	public void informCurrentInstrument(Instrument instrument) {
		logger.debug("informCurrentInstrument");
		currentInstrument = instrument;
	}

	public void informVirtualBookFrame(APrintNGVirtualBookFrame frame) {
		logger.debug("informVirtualBookFrame");
		currentFrame = frame;
	}

	/**
	 * Override this method to add new layer on the book component
	 */
	public abstract void addLayers(JVirtualBookScrollableComponent c);

	/**
	 * Override this method to add a new toolbar to the virtualbook window
	 */
	public abstract JToolBar[] addToolBars();

	/**
	 * Internal method for ease the creation of a groovy script based button
	 * 
	 * @param closure
	 *            closure to be launch with a hash in parameter containing the
	 *            variable accessible (pianoroll, currentinstrument,
	 *            virtualbook)
	 * @param icon
	 *            the icon of the button
	 * @param text
	 *            the text of the command
	 * @return the created button
	 */
	protected JButton createGroovyButton(final Closure closure, Image icon,
			String text) {

		JButton btn = new JButton();
		if (icon != null) {
			ImageIcon imageIcon = new ImageIcon(icon);
			btn.setIcon(imageIcon);
		}

		if (text != null)
			btn.setText(text);

		
		btn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					
					HashMap h = new HashMap();
					h.put("services", application);
					h.put("pianoroll", currentFrame.getPianoRoll());
					h.put("virtualbook", currentFrame.getVirtualBook());
					h.put("currentinstrument", currentFrame.getCurrentInstrument());

					
					logger.debug("launch groovy action");

					closure.call(h);

					logger.debug("done !");

				} catch (Throwable t) {
					logger.error(
							"error in executing the groovy button :"
									+ t.getMessage(), t);
					JMessageBox.showError(currentFrame, t);
				}

			}
		});

		return btn;

	}
}