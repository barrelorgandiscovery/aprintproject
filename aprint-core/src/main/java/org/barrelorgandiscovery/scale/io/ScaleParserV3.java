package org.barrelorgandiscovery.scale.io;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.ConstraintList;
import org.barrelorgandiscovery.scale.ConstraintMinimumHoleLength;
import org.barrelorgandiscovery.scale.ConstraintMinimumInterHoleLength;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.PipeStop;
import org.barrelorgandiscovery.scale.PipeStopGroup;
import org.barrelorgandiscovery.scale.RegisterCommandStartDef;
import org.barrelorgandiscovery.scale.RegisterSetCommandResetDef;
import org.barrelorgandiscovery.scale.ScaleException;
import org.barrelorgandiscovery.tools.LineParser;
import org.barrelorgandiscovery.tools.MidiHelper;
import org.barrelorgandiscovery.virtualbook.rendering.VirtualBookRendering;
import org.barrelorgandiscovery.virtualbook.rendering.VirtualBookRenderingFactory;


public class ScaleParserV3 implements LineParser {

	private static final Logger logger = Logger.getLogger(ScaleParserV3.class);

	public ScaleParserV3() {
	}

	private String name = ""; //$NON-NLS-1$

	private double width = Double.NaN;

	private int nbpistes = 0;

	private String infos = null;
	/**
	 * tableau de définition des pistes
	 */
	private AbstractTrackDef[] notemidi;

	private double entrepiste = Double.NaN;

	private double firsttrack = Double.NaN;

	private double largeurpiste = Double.NaN;

	private double speed = Double.NaN;

	private String state = null;

	private String contact = null;

	private boolean preferredViewedInverted = false;

	/**
	 * liste des contraintes associées à la gamme ...
	 */
	private ConstraintList constraintlist = new ConstraintList();

	/**
	 * variable contenant le nom du jeu de registre utilisé pour la définition
	 * de la note
	 */
	private String currentregisterset = null;

	private VirtualBookRendering rendering = new VirtualBookRendering();

	/**
	 * tableau de définition des jeux de registres
	 */
	private PipeStopGroup[] registerset;

	public void lineParsed(String key, String value, int line) throws Exception {
		try {

			if (key == null || key.length() == 0)
				return;

			if (value == null || value.length() == 0)
				return;

			// on passe en lowercase pour éviter les saisies difficiles ...
			key = key.toLowerCase();
			// value = value.toLowerCase();
			value = value.replace("<EOL>", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

			if ("width".equals(key)) { //$NON-NLS-1$
				width = Double.parseDouble(value);
			} else if ("name".equals(key)) { //$NON-NLS-1$
				logger.debug("read name:" + value); //$NON-NLS-1$
				name = value;
			} else if ("intertrack".equals(key)) { //$NON-NLS-1$
				entrepiste = Double.parseDouble(value);
			} else if ("firsttrackoffset".equals(key)) { //$NON-NLS-1$
				firsttrack = Double.parseDouble(value);
			} else if ("perforationwidth".equals(key)) { //$NON-NLS-1$
				largeurpiste = Double.parseDouble(value);
			} else if ("speed".equals(key)) { //$NON-NLS-1$
				speed = Double.parseDouble(value);
			} else if ("minimumholelength".equals(key)) { //$NON-NLS-1$
				logger.debug("lecture de la longueur minimum des trous " //$NON-NLS-1$
						+ value);
				ConstraintMinimumHoleLength c = new ConstraintMinimumHoleLength(
						Double.parseDouble(value));
				constraintlist.add(c);
			} else if ("minimuminterholelength".equals(key)) { //$NON-NLS-1$
				logger.debug("lecture de la longueur minimum entre deux trous " //$NON-NLS-1$
						+ value);
				ConstraintMinimumInterHoleLength c = new ConstraintMinimumInterHoleLength(
						Double.parseDouble(value));
				constraintlist.add(c);
			} else if ("version".equals(key)) { //$NON-NLS-1$
				logger.debug("version :" + value); //$NON-NLS-1$

			} else if ("preferredviewedinverted".equals(key)) { //$NON-NLS-1$
				this.preferredViewedInverted = Boolean.parseBoolean(value);
			} else if ("rendering".equals(key)) { //$NON-NLS-1$
				logger.debug("rendering " + value); //$NON-NLS-1$

				rendering = new VirtualBookRendering();

				VirtualBookRendering[] list = VirtualBookRenderingFactory
						.getRenderingList();
				for (int i = 0; i < list.length; i++) {
					VirtualBookRendering virtualBookRendering = list[i];
					String nameRendering = virtualBookRendering.getName();
					if (nameRendering.equals(value)) {
						rendering = virtualBookRendering;
						logger.debug(" " + nameRendering + " taken"); //$NON-NLS-1$ //$NON-NLS-2$
						break;
					}
				}

			} else if ("infos".equals(key)) { //$NON-NLS-1$
				logger
						.debug("lecture des informations associées à la gamme ..."); //$NON-NLS-1$

				infos = value;

			} else if ("minimuminterholelength".equals(key)) { //$NON-NLS-1$
				logger.debug("lecture de la longueur minimum entre les trous"); //$NON-NLS-1$
				ConstraintMinimumInterHoleLength c = new ConstraintMinimumInterHoleLength(
						Double.parseDouble(value));
				constraintlist.add(c);

			} else if ("tracknumber".equals(key)) { //$NON-NLS-1$
				nbpistes = Integer.parseInt(value);
				notemidi = new AbstractTrackDef[nbpistes];
				for (int i = 0; i < notemidi.length; i++)
					notemidi[i] = null; // pas de correspondance midi
			} else if ("registerset".equals(key) //$NON-NLS-1$
					|| "classification".equals(key)) { //$NON-NLS-1$
				if ("".equals(value)) { //$NON-NLS-1$
					value = null;
				} else {
					currentregisterset = value.toUpperCase();
				}
				// vérification de la déclaration préalable du jeu de registre

				// la vérification est réalisé par l'objet Gamme ...
				// une exception est levée s'il y a un problème dans la
				// définition
				// des registres ....

			} else if ("registersetcount".equals(key)) { //$NON-NLS-1$
				// nombre de jeux de registre
				logger.debug("read the number of registerset"); //$NON-NLS-1$
				registerset = new PipeStopGroup[Integer.parseInt(value)];

			} else if (key.startsWith("registersetcomposition.")) { //$NON-NLS-1$

				logger.debug("read registerset composition"); //$NON-NLS-1$
				int no = Integer.parseInt(key.substring(key.indexOf(".") + 1)); //$NON-NLS-1$

				logger.debug("registerset # " + no); //$NON-NLS-1$
				String register = value.toUpperCase();

				logger.debug("adding register " + register); //$NON-NLS-1$

				PipeStopGroup r = registerset[no];
				if (!r.exist(register)) {
					r.add(new PipeStop(register, true));
				}

			} else if (key.startsWith("registerset.")) { //$NON-NLS-1$
				logger.debug("read registerset name"); //$NON-NLS-1$

				int no = Integer.parseInt(key.substring(key.indexOf(".") + 1)); //$NON-NLS-1$

				PipeStopGroup s = new PipeStopGroup(value.toUpperCase(), null);
				registerset[no] = s;

			} else if ("contact".equals(key)) { //$NON-NLS-1$
				if ("".equals(value)) //$NON-NLS-1$
					value = null;

				this.contact = value;

			} else if ("state".equals(key)) { //$NON-NLS-1$
				if ("".equals(value)) //$NON-NLS-1$
					value = null;

				this.state = value;

			} else {

				logger.debug("reading trackdefinition for " + key + "->" //$NON-NLS-1$ //$NON-NLS-2$
						+ value);

				// c'est une note, on lit la note ...
				int nopiste = Integer.parseInt(key);

				AbstractTrackDef d = null;
				// on regarde si la définition est une percussion
				if (value.toLowerCase().startsWith("p")) { //$NON-NLS-1$

					// on decoupe les éléments de la ligne (pour la lecture du
					// retard et de la longueur)

					String[] values = value.split(","); //$NON-NLS-1$

					// c'est une percussion
					// on lit le nombre acollé au "P"
					String nopercu = values[0].substring(1);
					int codepercu = Integer.parseInt(nopercu);

					double retard = Double.NaN;
					double longueur = Double.NaN;

					if (values.length > 1) {
						retard = Double.parseDouble(values[1]);
					}

					if (values.length > 2) {
						longueur = Double.parseDouble(values[2]);
					}

					notemidi[nopiste] = new PercussionDef(codepercu, retard,
							longueur);

				} else if (value.toLowerCase().startsWith("r")) { //$NON-NLS-1$

					String[] values = value.split(","); //$NON-NLS-1$

					logger.debug("read the register definition"); //$NON-NLS-1$

					double retard = Double.NaN;
					double longueur = Double.NaN;

					if (values.length > 1) {
						retard = Double.parseDouble(values[1]);
					}

					if (values.length > 2) {
						longueur = Double.parseDouble(values[2]);
					}

					if (values[0].startsWith("rr")) { //$NON-NLS-1$
						logger.debug("read register reset"); //$NON-NLS-1$

						String registerset_name = values[0].substring(3); // rr:

						// check registers ...
						checkRegisterSetExist(registerset_name);

						notemidi[nopiste] = new RegisterSetCommandResetDef(
								registerset_name, retard, longueur);

					} else if (values[0].startsWith("rs")) { //$NON-NLS-1$
						logger.debug("register set"); //$NON-NLS-1$

						String tmp = values[0].substring(3); // rs:
						String[] parameters = tmp.split(":"); //$NON-NLS-1$

						String registerset_name = parameters[0];
						String register_name = parameters[1];

						// check registers ...
						checkRegisterExistInRegisterSet(registerset_name,
								register_name);

						notemidi[nopiste] = new RegisterCommandStartDef(
								registerset_name, register_name, retard,
								longueur);

					} else {
						throw new Exception("bad register command " + values[0]); //$NON-NLS-1$

					}

				} else {
					// c'est une note
					int codenotemidi = MidiHelper.midiCode(value);
					codenotemidi += 12; // décalage vis à vis de
										// l'implémentation

					NoteDef nd = new NoteDef(codenotemidi, currentregisterset);
					notemidi[nopiste] = nd;
				}

				logger.debug(nopiste + "-" + (d != null ? d.toString() : "")); //$NON-NLS-1$ //$NON-NLS-2$

			}
		} catch (Exception ex) {
			throw new ScaleException("Erreur ligne " + line, ex); //$NON-NLS-1$
		}
	}

	private boolean checkRegisterSetExist(String registerset) throws Exception {
		for (int i = 0; i < this.registerset.length; i++) {
			PipeStopGroup t = this.registerset[i];
			if (t == null)
				throw new Exception("error in getting registerset definition " //$NON-NLS-1$
						+ i);

			if (t.getName().equals(registerset))
				return true;

		}
		return false;
	}

	private boolean checkRegisterExistInRegisterSet(String registerset,
			String register) throws Exception {
		for (int i = 0; i < this.registerset.length; i++) {
			PipeStopGroup t = this.registerset[i];
			if (t == null)
				throw new Exception("error in getting registerset definition " //$NON-NLS-1$
						+ i);

			if (t.getName().equals(registerset)) {
				return t.exist(register);
			}
		}
		return false;
	}

	public double getEntrepiste() {
		return entrepiste;
	}

	public double getFirsttrack() {
		return firsttrack;
	}

	public double getLargeurpiste() {
		return largeurpiste;
	}

	public int getNbpistes() {
		return nbpistes;
	}

	public AbstractTrackDef[] getNotemidi() {
		return notemidi;
	}

	public double getWidth() {
		return width;
	}

	public String getName() {
		return name;
	}

	public double getVitesse() {
		return speed;
	}

	public PipeStopGroup[] getRegisterSets() {
		return registerset;
	}

	public ConstraintList getConstraints() {
		return constraintlist;
	}

	public String getInfos() {
		return infos;
	}

	public String getState() {
		return this.state;
	}

	public String getContact() {
		return this.contact;
	}

	public VirtualBookRendering getRendering() {
		return rendering;
	}

	public boolean isPreferredViewedInverted() {
		return preferredViewedInverted;
	}

}
