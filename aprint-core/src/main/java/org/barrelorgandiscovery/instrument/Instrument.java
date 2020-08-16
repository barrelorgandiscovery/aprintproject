package org.barrelorgandiscovery.instrument;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.ascale.ScaleComponent;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.streamstorage.IStreamRef;

/**
 * Classe décrivant un instrument
 * 
 * @author Freydiere Patrice
 */
public class Instrument implements Disposable {

	private static final Logger logger = Logger.getLogger(Instrument.class);

	private String name;
	private Scale gamme;
	private IStreamRef sb;
	private Image picture;
	private Image miniPicture;
	private String descriptionUrl;
	private RegisterSoundLink registerSoundLink;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            name of the instrument
	 * @param scale
	 *            scale of the instrument
	 * @param sb
	 *            sound bank associated to the instrument
	 * @param picture
	 *            optional picture (may be null)
	 */
	public Instrument(String name, Scale scale, IStreamRef refstreamsoundback,
			Image picture, String descriptionurl) {
		if (name == null || scale == null || refstreamsoundback == null)
			throw new IllegalArgumentException();

		this.name = name;
		this.gamme = scale;
		this.sb = refstreamsoundback;
		this.picture = picture;

		if (picture == null) {
			// create a thumbnail of the scale ...

			BufferedImage scaleImage = ScaleComponent.createScaleImage(scale);
			BufferedImage crop;

			crop = ImageTools.crop(300, 300, scaleImage);

			this.picture = crop;

		}

		this.miniPicture = ImageTools.crop(90, 90, this.picture);

		this.descriptionUrl = descriptionurl;

		this.registerSoundLink = new RegisterSoundLink(scale);

	}

	/**
	 * get the instrument name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * get the scale of the instrument
	 * 
	 * @return
	 */
	public Scale getScale() {
		return gamme;
	}

	/**
	 * open and read the soundbank
	 * 
	 * @return
	 */
	public Soundbank openSoundBank() throws IOException {

		try {
			// ouverture de la soundbank

			logger.debug(sb.getClass().getName());

			return MidiSystem.getSoundbank(sb.open());

		} catch (InvalidMidiDataException ex) {
			logger.info(sb.getClass().getName());
			logger.error("openSoundBank : " + ex.getMessage(), ex); //$NON-NLS-1$
			return null;
		}
	}

	public IStreamRef getSoundBankStream() {
		return sb;
	}

	/**
	 * Get instrument picture
	 * 
	 * @return
	 */
	public Image getThumbnail() {
		return picture;
	}

	/**
	 * Small reduction of the picture
	 * 
	 * @return
	 */
	public Image getMiniPicture() {
		return miniPicture;
	}

	/**
	 * get the description url
	 * 
	 * @return
	 */
	public String getDescriptionUrl() {
		return descriptionUrl;
	}

	/**
	 * Get the register sound link with the sound bank
	 * 
	 * @return
	 */
	public RegisterSoundLink getRegisterSoundLink() {
		return this.registerSoundLink;
	}

	public void dispose() {
		if (sb != null && sb instanceof Disposable) {
			((Disposable) sb).dispose();
		}
	}
	
	@Override
	public String toString() {
		return "Instrument(" + name + ")";
	}

}
