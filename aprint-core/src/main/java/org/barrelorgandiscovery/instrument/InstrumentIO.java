package org.barrelorgandiscovery.instrument;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.apache.log4j.lf5.util.StreamUtils;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.ScaleManager;
import org.barrelorgandiscovery.scale.VersionParser;
import org.barrelorgandiscovery.tools.IniFileParser;
import org.barrelorgandiscovery.tools.streamstorage.IStreamRef;
import org.barrelorgandiscovery.tools.streamstorage.StreamRef;
import org.barrelorgandiscovery.tools.streamstorage.StreamStorage;


/**
 * Classe for I/O Instruments
 * 
 * @author Freydiere Patrice
 * 
 */
public class InstrumentIO {

	private static final Logger logger = Logger.getLogger(InstrumentIO.class);

	/**
	 * Read an instrument in a stream storage
	 * 
	 * @param gm
	 * @param fis
	 * @param streamName
	 * @return
	 * @throws Exception
	 */
	public static Instrument readInstrument(ScaleManager gm, StreamStorage fis,
			String streamName) throws Exception {

		// lecture de la version ....

		VersionParser vp = new VersionParser();
		new IniFileParser(new InputStreamReader(fis.openStream(streamName)), vp)
				.parse();

		String version = vp.getVersion();

		InstrumentParser ip;

		if ("1".equals(version)) {
			ip = new InstrumentParser();
		} else {
			ip = new InstrumentParserV1();
		}

		IniFileParser fp = new IniFileParser(new InputStreamReader(fis
				.openStream(streamName)), ip);
		fp.parse();

		Scale g = gm.getScale(ip.getGammeName());
		if (g == null)
			throw new Exception("Scale " + ip.getGammeName() //$NON-NLS-1$
					+ " not defined ... "); //$NON-NLS-1$

		// Lecture de l'image associée à l'instrument ...
		String imagestream = ip.getPicture();
		Image image = null;

		if (imagestream != null && !"".equals(imagestream)) { //$NON-NLS-1$
			logger.debug("reading the instrument picture"); //$NON-NLS-1$

			InputStream is = fis.openStream(imagestream);
			if (is != null) {
				try {
					// read the stream in a byte array
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buffer = new byte[4096];
					int cpt = is.read(buffer, 0, buffer.length);
					while (cpt != -1) {
						baos.write(buffer, 0, cpt);
						cpt = is.read(buffer, 0, buffer.length);
					}

					byte[] imagecontent = baos.toByteArray();

					image = Toolkit.getDefaultToolkit().createImage(
							imagecontent);

					logger.debug("image loaded"); //$NON-NLS-1$

				} catch (Exception ex) {
					logger.error("loadimage", ex); //$NON-NLS-1$
				} finally {
					is.close();
				}

			} else {
				logger.error("picture " + imagestream + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
			}

		} else {
			logger.debug("no pictures"); //$NON-NLS-1$
		}

		// Lecture de la soundbank

		IStreamRef ref = new StreamRef(fis, ip.getPatchstream());

		// Création de l'instrument ...
		Instrument ins = new Instrument(ip.getName(), g, ref, image, ip
				.getInstrumentDescriptionUrl());

		if ("1".equals(version)) {
			InstrumentParserV1 ipv1 = (InstrumentParserV1) ip;

			// ajout du support des sons de registres ...
			logger.debug("adding the bank associated to registers ...");

			ArrayList<RegisterLinkDef> registerLinks = ipv1.getRegisterLinks();
			for (Iterator iterator = registerLinks.iterator(); iterator
					.hasNext();) {
				RegisterLinkDef registerLinkDef = (RegisterLinkDef) iterator
						.next();

				ins.getRegisterSoundLink().defineLink(
						registerLinkDef.pipeStopSet, registerLinkDef.pipeStop,
						registerLinkDef.instrumentNumber);

			}

		}

		return ins;

	}

	/**
	 * Save an instrument in the current version
	 * 
	 * @param fis
	 * @param ins
	 * @param streamName
	 * @throws Exception
	 */
	public static void writeInstrument(StreamStorage fis, Instrument ins,
			String streamName) throws Exception {

		Properties p = new Properties();

		logger.debug("save the instrument stream ...");

		// save the soundbank stream
		InputStream is = ins.getSoundBankStream().open();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StreamUtils.copyThenClose(is, baos);

		String instrumentsoundbankname = "" + ins.getName() + "sb";
		fis.saveStream(instrumentsoundbankname, "sb", new ByteArrayInputStream(
				baos.toByteArray()));

		Scale gamme = ins.getScale();

		logger.debug("save the picture stream ...");

		ByteArrayOutputStream baosimage = new ByteArrayOutputStream();
		String imagename = null;
		Image thumbnail = ins.getThumbnail();
		if (thumbnail != null) {
			imagename = "picture_" + ins.getName();
			OutputStream os = (OutputStream) baosimage;
			ImageIO.write((RenderedImage) thumbnail, "JPG", os);
		}

		p.setProperty("version", "1");

		p.setProperty("name", ins.getName());
		p.setProperty("gamme", gamme.getName());
		p.setProperty("patch", instrumentsoundbankname);

		logger.debug("writing the pipestop definition in the instrument");

		int cpt = 0;
		RegisterSoundLink registerSoundLink = ins.getRegisterSoundLink();
		List<String> groups = registerSoundLink
				.getPipeStopGroupNamesInWhichThereAreMappings();
		for (Iterator iterator = groups.iterator(); iterator.hasNext();) {
			String groupname = (String) iterator.next();
			logger.debug("adding mapping for the group ..." + groupname);

			List<String> pipeStopNamesInWhichThereAreMappings = registerSoundLink
					.getPipeStopNamesInWhichThereAreMappings(groupname);
			for (Iterator iterator2 = pipeStopNamesInWhichThereAreMappings
					.iterator(); iterator2.hasNext();) {
				String pipeStopName = (String) iterator2.next();

				logger.debug("mapping pipestop " + pipeStopName + " in "
						+ groupname);

				p.setProperty("registerpatch." + (cpt++), ""
						+ groupname
						+ ","
						+ pipeStopName
						+ ","
						+ registerSoundLink.getInstrumentNumber(groupname,
								pipeStopName));

			}

		}

		if (thumbnail != null)
			p.setProperty("picture", imagename);

		p.setProperty("url", ins.getDescriptionUrl());

		ByteArrayOutputStream pp = new ByteArrayOutputStream();
		p.save(pp, "instrument file");
		pp.close();

		fis.saveStream(streamName, "instrument", new ByteArrayInputStream(pp
				.toByteArray()));

	}
}
