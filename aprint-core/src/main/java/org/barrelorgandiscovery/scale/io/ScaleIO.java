package org.barrelorgandiscovery.scale.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.AbstractScaleConstraint;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.ConstraintList;
import org.barrelorgandiscovery.scale.ConstraintMinimumHoleLength;
import org.barrelorgandiscovery.scale.ConstraintMinimumInterHoleLength;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.PipeStop;
import org.barrelorgandiscovery.scale.PipeStopGroup;
import org.barrelorgandiscovery.scale.PipeStopGroupList;
import org.barrelorgandiscovery.scale.RegisterCommandStartDef;
import org.barrelorgandiscovery.scale.RegisterSetCommandResetDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.ScaleException;
import org.barrelorgandiscovery.scale.VersionParser;
import org.barrelorgandiscovery.tools.IniFileParser;
import org.barrelorgandiscovery.tools.MidiHelper;
import org.barrelorgandiscovery.virtualbook.rendering.VirtualBookRendering;

/**
 * Objet permettant de lire et écrire un fichier de gamme
 * 
 * @author Freydiere Patrice
 * 
 */
public class ScaleIO {

	private static Logger logger = Logger.getLogger(ScaleIO.class);

	public static final String SCALE_FILE_EXTENSION = "gamme";//$NON-NLS-1$

	/**
	 * Lecture d'un fichier de gamme
	 * 
	 * @param fichiergamme
	 *            le nom du fichier contenant une gamme
	 * @return
	 */
	public static Scale readGamme(File fichiergamme) throws IOException,
			ScaleException {

		if (fichiergamme == null)
			throw new IllegalArgumentException();

		logger.debug("readGamme " + fichiergamme.getAbsolutePath()); //$NON-NLS-1$
		return readGamme(new FileInputStream(fichiergamme));
	}

	/**
	 * Lecture d'un fichier de gamme à partir d'un flux
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 * @throws ScaleException
	 */
	public static Scale readGamme(InputStream is) throws IOException,
			ScaleException {

		// Lecture du flux dans un buffer, pour permettre de rembobiner
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int cpt;
		while ((cpt = is.read(buffer)) != -1) {
			baos.write(buffer, 0, cpt);
		}

		byte[] result = baos.toByteArray();

		// Lecture de la version du fichier de gamme ...

		VersionParser vp = new VersionParser();
		IniFileParser vfp = new IniFileParser(new InputStreamReader(
				new ByteArrayInputStream(result), Charset.forName("UTF-8")), vp);

		try {
			vfp.parse();
		} catch (Exception ex) {
			throw new ScaleException(ex);
		}

		String version = vp.getVersion();

		int versionno = 0;

		if (version != null) {
			try {
				versionno = Integer.parseInt(version);
			} catch (NumberFormatException ex) {
				throw new ScaleException(
						Messages.getString("ScaleIO.1") + version); //$NON-NLS-1$
			}
		}

		switch (versionno) {
		case 0:
			return readGammeV0(result);
		case 1:
			return readGammeV1(result);
		case 2:
			return readGammeV2(result);
		case 3:
			return readGammeV3(result);
		case 4:
		case 5:
			return readGammeV4_V5(result);
		case 6:
			return readGammeV6(result);
		default:
			throw new ScaleException(
					Messages.getString("ScaleIO.2") + versionno //$NON-NLS-1$
							+ Messages.getString("ScaleIO.3")); //$NON-NLS-1$
		}

	}

	/**
	 * Lecture de la gamme pour les version 0
	 * 
	 * @param gammecontent
	 * @return
	 * @throws ScaleException
	 */
	private static Scale readGammeV0(byte[] gammecontent) throws ScaleException {
		ScaleParser gp = new ScaleParser(0);

		IniFileParser fp = new IniFileParser(new InputStreamReader(
				new ByteArrayInputStream(gammecontent)), gp);
		try {
			fp.parse();
		} catch (Exception ex) {
			throw new ScaleException(ex);
		}

		Scale g = new Scale(gp.getName(), gp.getWidth(), gp.getEntrepiste(),
				gp.getLargeurpiste(), gp.getFirsttrack(), gp.getNbpistes(),
				gp.getNotemidi(), null, gp.getVitesse(), null, null,
				gp.getState(), gp.getContact(), null, false, false, null);

		return g;
	}

	/**
	 * Lecture de la gamme pour les version 1
	 * 
	 * @param gammecontent
	 * @return
	 * @throws ScaleException
	 */
	private static Scale readGammeV1(byte[] gammecontent) throws ScaleException {
		ScaleParser gp = new ScaleParser(1);

		IniFileParser fp = new IniFileParser(new InputStreamReader(
				new ByteArrayInputStream(gammecontent)), gp);
		try {
			fp.parse();
		} catch (Exception ex) {
			throw new ScaleException(ex);
		}

		Scale g = new Scale(gp.getName(), gp.getWidth(), gp.getEntrepiste(),
				gp.getLargeurpiste(), gp.getFirsttrack(), gp.getNbpistes(),
				gp.getNotemidi(), null, gp.getVitesse(), null, null,
				gp.getState(), gp.getContact(), null, false, false, null);

		return g;
	}

	/**
	 * Lecture de la gamme pour les version 2
	 * 
	 * @param gammecontent
	 * @return
	 * @throws ScaleException
	 */
	private static Scale readGammeV2(byte[] gammecontent) throws ScaleException {
		ScaleParserV2 gp = new ScaleParserV2();

		IniFileParser fp = new IniFileParser(new InputStreamReader(
				new ByteArrayInputStream(gammecontent)), gp);
		try {
			fp.parse();
		} catch (Exception ex) {
			throw new ScaleException(ex);
		}

		Scale g = new Scale(gp.getName(), gp.getWidth(), gp.getEntrepiste(),
				gp.getLargeurpiste(), gp.getFirsttrack(), gp.getNbpistes(),
				gp.getNotemidi(), (gp.getRegisterSets() == null ? null
						: new PipeStopGroupList(gp.getRegisterSets())),
				gp.getVitesse(), gp.getConstraints(), gp.getInfos(),
				gp.getState(), gp.getContact(), null, false, false, null);

		return g;
	}

	private static Scale readGammeV3(byte[] gammecontent) throws ScaleException {

		logger.debug("read V3 version"); //$NON-NLS-1$
		ScaleParserV3 gp = new ScaleParserV3();

		IniFileParser fp = new IniFileParser(new InputStreamReader(
				new ByteArrayInputStream(gammecontent)), gp);
		try {
			fp.parse();
		} catch (Exception ex) {
			throw new ScaleException(ex);
		}

		Scale g = new Scale(gp.getName(), gp.getWidth(), gp.getEntrepiste(),
				gp.getLargeurpiste(), gp.getFirsttrack(), gp.getNbpistes(),
				gp.getNotemidi(), (gp.getRegisterSets() == null ? null
						: new PipeStopGroupList(gp.getRegisterSets())),
				gp.getVitesse(), gp.getConstraints(), gp.getInfos(),
				gp.getState(), gp.getContact(), gp.getRendering(),
				gp.isPreferredViewedInverted(), false, null);

		return g;
	}

	private static Scale readGammeV4_V5(byte[] gammecontent)
			throws ScaleException {

		logger.debug("read V4 version"); //$NON-NLS-1$
		ScaleParserV4 gp = new ScaleParserV4();

		IniFileParser fp = new IniFileParser(new InputStreamReader(
				new ByteArrayInputStream(gammecontent),
				Charset.forName("UTF-8")), gp);
		try {
			fp.parse();
		} catch (Exception ex) {
			throw new ScaleException(ex);
		}

		Scale g = new Scale(gp.getName(), gp.getWidth(), gp.getEntrepiste(),
				gp.getLargeurpiste(), gp.getFirsttrack(), gp.getNbpistes(),
				gp.getNotemidi(), (gp.getRegisterSets() == null ? null
						: new PipeStopGroupList(gp.getRegisterSets())),
				gp.getVitesse(), gp.getConstraints(), gp.getInfos(),
				gp.getState(), gp.getContact(), gp.getRendering(),
				gp.isPreferredViewedInverted(), false, gp.getProperties());

		return g;
	}

	private static Scale readGammeV6(byte[] gammecontent) throws ScaleException {

		logger.debug("read V6 version"); //$NON-NLS-1$
		ScaleParserV6 gp = new ScaleParserV6();

		IniFileParser fp = new IniFileParser(new InputStreamReader(
				new ByteArrayInputStream(gammecontent),
				Charset.forName("UTF-8")), gp);
		try {
			fp.parse();
		} catch (Exception ex) {
			throw new ScaleException(ex);
		}

		Scale g = new Scale(gp.getName(), gp.getWidth(), gp.getEntrepiste(),
				gp.getLargeurpiste(), gp.getFirsttrack(), gp.getNbpistes(),
				gp.getNotemidi(), (gp.getRegisterSets() == null ? null
						: new PipeStopGroupList(gp.getRegisterSets())),
				gp.getVitesse(), gp.getConstraints(), gp.getInfos(),
				gp.getState(), gp.getContact(), gp.getRendering(),
				gp.isPreferredViewedInverted(), gp.isBookMovingRightToLeft(),
				gp.getProperties());

		return g;
	}

	// //////////////////////////////////////////////////////////////////////////////////
	// Ecriture de gamme

	private static void writeKeyValue(Writer w, String key, String value)
			throws IOException {

		String tmp = value;
		if (tmp != null) {
			tmp = tmp.replace("\n", "<EOL>"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		w.write(key + "=" + tmp + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void writeGamme(Scale gamme, File fichiergamme)
			throws IOException, ScaleException {

		writeGamme(gamme, new FileOutputStream(fichiergamme));
	}

	/**
	 * 
	 * write a scale with the latest scaleformat
	 * 
	 * @param gamme
	 *            scale to write
	 * @param stream
	 *            outputstream to write in
	 * 
	 * @throws IOException
	 * @throws ScaleException
	 */
	public static void writeGamme(Scale gamme, OutputStream stream)
			throws IOException, ScaleException {

		Writer w = new OutputStreamWriter(stream, Charset.forName("UTF-8"));
		try {
			w.write("# Gamme File " + gamme.getName() + " - " //$NON-NLS-1$ //$NON-NLS-2$
					+ SimpleDateFormat.getDateInstance().format(new Date())
					+ "\n"); //$NON-NLS-1$

			writeKeyValue(w, "version", "6"); //$NON-NLS-1$ //$NON-NLS-2$

			w.write("# Gamme description \n"); //$NON-NLS-1$

			writeKeyValue(w, "width", Double.toString(gamme.getWidth())); //$NON-NLS-1$
			writeKeyValue(w, "name", gamme.getName()); //$NON-NLS-1$
			writeKeyValue(w, "intertrack", Double.toString(gamme //$NON-NLS-1$
					.getIntertrackHeight()));
			writeKeyValue(w, "firsttrackoffset", Double.toString(gamme //$NON-NLS-1$
					.getFirstTrackAxis()));
			writeKeyValue(w, "perforationwidth", Double.toString(gamme //$NON-NLS-1$
					.getTrackWidth()));
			writeKeyValue(w, "speed", Double.toString(gamme.getSpeed())); //$NON-NLS-1$
			writeKeyValue(w, "tracknumber", Integer //$NON-NLS-1$
					.toString(gamme.getTrackNb()));
			writeKeyValue(w, "infos", gamme.getInformations()); //$NON-NLS-1$
			if (gamme.getState() != null)
				writeKeyValue(w, "state", gamme.getState()); //$NON-NLS-1$

			if (gamme.isBookMovingRightToLeft())
				writeKeyValue(w, "rightToLeft", "true");

			if (gamme.getContact() != null)
				writeKeyValue(w, "contact", gamme.getContact()); //$NON-NLS-1$

			writeKeyValue(w, "preferredviewedinverted",
					"" + gamme.isPreferredViewedInversed());

			if (gamme.getRendering() != null) {
				VirtualBookRendering vbr = gamme.getRendering();
				writeKeyValue(w, "rendering", vbr.getName()); //$NON-NLS-1$
			}

			w.write("\n\n\n"); //$NON-NLS-1$

			PipeStopGroupList rs = gamme.getPipeStopGroupList();
			if (rs != null) {
				w.write("# registers description \n"); //$NON-NLS-1$

				writeKeyValue(w, "pipestopgroupcount", Integer //$NON-NLS-1$
						.toString(rs.size()));

				for (int i = 0; i < rs.size(); i++) {
					PipeStopGroup r = rs.get(i);
					writeKeyValue(w, "pipestopgroup." + Integer.toString(i), r //$NON-NLS-1$
							.getName());

					PipeStop[] pipestops = r.getPipeStops();
					for (int j = 0; j < pipestops.length; j++) {
						writeKeyValue(w, "pipestopdefname." //$NON-NLS-1$
								+ Integer.toString(i), pipestops[j].getName());
						writeKeyValue(w, "pipestopdefregistercontrolled." //$NON-NLS-1$
								+ Integer.toString(i),
								"" + pipestops[j].isRegisteredControlled());

					}
				}
			}

			ConstraintList cl = gamme.getConstraints();
			if (cl != null) {
				w.write("# constraints description \n"); //$NON-NLS-1$

				for (Iterator it = cl.iterator(); it.hasNext();) {
					AbstractScaleConstraint c = (AbstractScaleConstraint) it
							.next();

					if (c instanceof ConstraintMinimumHoleLength) {

						writeKeyValue(
								w,
								"minimumholelength", Double //$NON-NLS-1$
										.toString(((ConstraintMinimumHoleLength) c)
												.getMinimumHoleLength()));
					} else if (c instanceof ConstraintMinimumInterHoleLength) {
						writeKeyValue(w,
								"minimuminterholelength", //$NON-NLS-1$
								Double.toString(((ConstraintMinimumInterHoleLength) c)
										.getMinimumInterHoleLength()));

					} else {
						throw new ScaleException(
								Messages.getString("ScaleIO.34") //$NON-NLS-1$
										+ c.getClass().getName());
					}

				}
			}

			// for the next release ....

			w.write("# properties \n");
			int propCount = 0;
			for (Iterator iterator = gamme.getAllProperties().entrySet()
					.iterator(); iterator.hasNext();) {
				Entry<String, String> e = (Entry<String, String>) iterator
						.next();
				writeKeyValue(w, "property." + (propCount++), e.getKey() + "|"
						+ e.getValue());
			}

			w.write("# track description \n"); //$NON-NLS-1$

			String currentregisterset = null;

			AbstractTrackDef[] d = gamme.getTracksDefinition();

			for (int i = 0; i < d.length; i++) {
				AbstractTrackDef td = d[i];
				if (td == null)
					continue;
				if (td instanceof NoteDef) {
					NoteDef nd = (NoteDef) td;

					if (nd.getRegisterSetName() == null) {
						if (currentregisterset != null) {
							writeKeyValue(w, "registerset", ""); //$NON-NLS-1$ //$NON-NLS-2$
						}
						currentregisterset = null;
					} else {
						if (!nd.getRegisterSetName().equals(currentregisterset)) {
							writeKeyValue(w, "registerset", nd //$NON-NLS-1$
									.getRegisterSetName());
						}
						currentregisterset = nd.getRegisterSetName();
					}

					// Ecriture de la piste ...
					writeKeyValue(w, Integer.toString(i),
							MidiHelper.midiLibelle(nd.getMidiNote() - 12)); // décalage
					// dans le
					// fichier
					// de gamme

				} else if (td instanceof PercussionDef) {
					PercussionDef pd = (PercussionDef) td;

					String tmp = "p" + pd.getPercussion(); //$NON-NLS-1$
					if (!Double.isNaN(pd.getRetard())) {
						tmp += "," + Double.toString(pd.getRetard()); //$NON-NLS-1$

						if (!Double.isNaN(pd.getLength())) {
							tmp += "," + Double.toString(pd.getLength()); //$NON-NLS-1$
						}
					}

					writeKeyValue(w, Integer.toString(i), tmp);

				} else if (td instanceof RegisterSetCommandResetDef) {
					RegisterSetCommandResetDef rscrd = (RegisterSetCommandResetDef) td;
					String tmp = "rr:" + rscrd.getRegisterSet(); //$NON-NLS-1$
					if (!Double.isNaN(rscrd.getRetard())) {
						tmp += "," + Double.toString(rscrd.getRetard()); //$NON-NLS-1$
						if (!Double.isNaN(rscrd.getLength()))
							tmp += "," + Double.toString(rscrd.getLength()); //$NON-NLS-1$
					}

					writeKeyValue(w, Integer.toString(i), tmp);

				} else if (td instanceof RegisterCommandStartDef) {
					RegisterCommandStartDef rsc = (RegisterCommandStartDef) td;
					String tmp = "rs:" + rsc.getRegisterSetName() + ":" //$NON-NLS-1$ //$NON-NLS-2$
							+ rsc.getRegisterInRegisterSet();
					if (!Double.isNaN(rsc.getRetard())) {
						tmp += "," + Double.toString(rsc.getRetard()); //$NON-NLS-1$
						if (!Double.isNaN(rsc.getLength()))
							tmp += "," + Double.toString(rsc.getLength()); //$NON-NLS-1$
					}
					writeKeyValue(w, Integer.toString(i), tmp);

				} else {
					throw new ScaleException(Messages.getString("ScaleIO.49") //$NON-NLS-1$
							+ td.getClass().getName());
				}

			}

		} finally {
			w.close();
		}
	}

	/**
	 * Ecriture de la gamme sous forme de fichier de gamme
	 * 
	 * @param gamme
	 *            gamme à écrire
	 * @param fichiergamme
	 *            le fichier de gamme à écrire
	 * @throws IOException
	 * @throws ScaleException
	 */
	private static void writeGammeV1(Scale gamme, File fichiergamme)
			throws IOException, ScaleException {

		Writer w = new FileWriter(fichiergamme);
		try {
			w.write("# Gamme File " + gamme.getName() + " - " //$NON-NLS-1$ //$NON-NLS-2$
					+ SimpleDateFormat.getDateInstance().format(new Date())
					+ "\n"); //$NON-NLS-1$

			writeKeyValue(w, "version", "1"); //$NON-NLS-1$ //$NON-NLS-2$

			w.write("# Gamme description \n"); //$NON-NLS-1$

			writeKeyValue(w, "width", Double.toString(gamme.getWidth())); //$NON-NLS-1$
			writeKeyValue(w, "name", gamme.getName()); //$NON-NLS-1$
			writeKeyValue(w, "intertrack", Double.toString(gamme //$NON-NLS-1$
					.getIntertrackHeight()));
			writeKeyValue(w, "firsttrackoffset", Double.toString(gamme //$NON-NLS-1$
					.getFirstTrackAxis()));
			writeKeyValue(w, "perforationwidth", Double.toString(gamme //$NON-NLS-1$
					.getTrackWidth()));
			writeKeyValue(w, "speed", Double.toString(gamme.getSpeed())); //$NON-NLS-1$
			writeKeyValue(w, "tracknumber", Integer //$NON-NLS-1$
					.toString(gamme.getTrackNb()));
			w.write("\n\n\n"); //$NON-NLS-1$

			String currentregisterset = null;

			AbstractTrackDef[] d = gamme.getTracksDefinition();

			for (int i = 0; i < d.length; i++) {
				AbstractTrackDef td = d[i];
				if (td instanceof NoteDef) {
					NoteDef nd = (NoteDef) td;

					if (nd.getRegisterSetName() == null) {
						if (currentregisterset != null) {
							writeKeyValue(w, "classification", ""); //$NON-NLS-1$ //$NON-NLS-2$
						}
						currentregisterset = null;
					} else {
						if (!nd.getRegisterSetName().equals(currentregisterset)) {
							writeKeyValue(w, "classification", nd //$NON-NLS-1$
									.getRegisterSetName());
						}
						currentregisterset = nd.getRegisterSetName();
					}

					// Ecriture de la piste ...
					writeKeyValue(w, Integer.toString(i),
							MidiHelper.midiLibelle(nd.getMidiNote()));

				} else if (td instanceof PercussionDef) {
					PercussionDef pd = (PercussionDef) td;

					String tmp = "p" + pd.getPercussion(); //$NON-NLS-1$
					if (!Double.isNaN(pd.getRetard())) {
						tmp += "," + Double.toString(pd.getRetard()); //$NON-NLS-1$

						if (!Double.isNaN(pd.getLength())) {
							tmp += "," + Double.toString(pd.getLength()); //$NON-NLS-1$
						}
					}

					writeKeyValue(w, Integer.toString(i), tmp);

				} else {
					throw new ScaleException("Unknown Track Def " //$NON-NLS-1$
							+ td.getClass().getName());
				}

			}

		} finally {
			w.close();
		}
	}

	/**
	 * Routine de test unitaire
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			Scale g = ScaleIO
					.readGamme(new File(
							"C:/Documents and Settings/Freydiere Patrice/workspace/APrint/gammes/52li.gamme")); //$NON-NLS-1$

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
}
