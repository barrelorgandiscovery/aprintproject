package org.barrelorgandiscovery.scale.importer;

import java.io.File;
import java.io.FileReader;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.PipeStopGroup;
import org.barrelorgandiscovery.scale.PipeStopGroupList;
import org.barrelorgandiscovery.scale.PipeStopListReference;
import org.barrelorgandiscovery.scale.Scale;

/**
 * Classe permettant l'import de gamme issues de midiboek
 * 
 * @author Freydiere Patrice
 */
public class MidiBoekGammeImporter {

	private static Logger logger = Logger
			.getLogger(MidiBoekGammeImporter.class);

	/**
	 * import midiboek file in scale file for APrint ...
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static Scale importScale(File file) throws Exception {

		// Ouverture du fichier ..

		class RegistersRead implements LineParser {

			/**
			 * As t on un chant ?
			 */
			private boolean hasChant;

			/**
			 * As t on un contre chant ?
			 */
			private boolean hasContreChant;

			/**
			 * Ligne courante pour la lecture des pistes ...
			 */
			int line = -1;

			int lineFile = -1;

			private AbstractTrackDef[] pistes = new AbstractTrackDef[300];

			private int nbpistes = -1;

			private double vitessedefilement = Double.NaN;

			private double entreaxe = Double.NaN;

			private double margedubas = Double.NaN;

			private double premierepiste = Double.NaN;

			private String name = null;

			public RegistersRead(String name) {
				this.name = name;
			}

			public void lineParsed(String cmd, String[] params)
					throws Exception {

				line++;
				lineFile++;

				String s = cmd.toLowerCase();
				if ("toets".equals(s)) {
					// chant ...
					hasChant = true;

					NoteDef nd = new NoteDef(Integer.parseInt(params[0]),
							PipeStopListReference.REGISTERSET_CHANT);

					pistes[line] = nd;

				} else if ("split".equals(s)) {
					line--;
				} else if ("tegen".equals(s)) {
					// contre chant ...
					hasContreChant = true;
					NoteDef nd = new NoteDef(Integer.parseInt(params[0]),
							PipeStopListReference.REGISTERSET_CONTRECHAMP);

					pistes[line] = nd;

				} else if ("resrv".equals(s)) {
					// la ligne est r�serv�e ...

				} else if ("snelh".equals(s)) {

					vitessedefilement = 1.0 * Integer.parseInt(params[0]) / 10.0; // en
					// mm/s
					logger.debug("vitesse :" + vitessedefilement + " mm/s");

				} else if ("leima".equals(s)) {

					// Axe de la premi�re piste

					premierepiste = 1.0 * Integer.parseInt(params[0]) / 10.0;
					logger.debug("premi�re piste :" + premierepiste);

				} else if ("ondma".equals(s)) {

					// Marge du bas ... on s'en sert pour la largeur du carton
					// ...

					margedubas = 1.0 * Integer.parseInt(params[0]) / 10.0;
					logger.debug("marge du bas :" + margedubas);

				} else if ("harta".equals(s)) {
					// entre axe ...

					boolean ishi = params.length > 1
							&& "h".equals(params[1].toLowerCase());

					entreaxe = 1.0 * Integer.parseInt(params[0])
							/ (ishi ? 100.0 : 10.0);
					logger.debug("entre axe :" + entreaxe);

				} else if ("ntype".equals(s)) {
					// type de note ... on s'en fout :-)

				} else if ("tbrdt".equals(s)) {

				} else if ("minle".equals(s)) {
					// Contraintes de perforation ...

				} else if ("slagw".equals(s)) {
					// Percussion ....

					double retard = Double.NaN;
					double longueur = Double.NaN;

					int midicode = Integer.parseInt(params[0]);

					if (params.length > 1
							&& !params[1].toLowerCase().equals("m")) {
						// lecture de la longueur
						longueur = 1.0 * Integer.parseInt(params[1]) / 10.0;
					}

					if (params.length > 2
							&& !params[2].toLowerCase().equals("m")) {

						if ("e".equals(params[2].toLowerCase())) {
							retard = longueur;
						} else {
							retard = -1.0 * Integer.parseInt(params[2]) / 10.0;
						}
					}

					PercussionDef p = new PercussionDef(midicode, retard,
							longueur);
					this.pistes[line] = p;

				} else if ("regis".equals(s)) {
					// Commande de registre ...
					// pas pour l'instant ...

				} else if ("klavp".equals(s)) {
					// nombre de touches ...
					nbpistes = Integer.parseInt(params[0]);
					logger.debug("nombre de touches " + nbpistes);

				} else if ("melod".equals(s)) {
					line--;
					logger.debug("melod not supported");
					throw new Exception(
							"Plusieures m�lodies , non support�e .... ");
				} else {
					throw new Exception("unsupported command " + s + " line "
							+ lineFile);
				}

			}

			/**
			 * Cette fonction cree la gamme � partir du parsing ...
			 * 
			 * @return
			 */
			public Scale toScale() throws Exception {

				// Calcul de la largeur du carton ...

				double largeur = nbpistes * entreaxe + margedubas
						+ premierepiste;

				// copie du tableau // BackPort en 1.5

				AbstractTrackDef[] tds = new AbstractTrackDef[nbpistes];
				for (int i = 0; i < nbpistes; i++) {
					tds[i] = this.pistes[i];
				}
				// ancienne version
				// Arrays.copyOfRange(this.pistes, 0,
				// nbpistes);

				PipeStopGroupList l = new PipeStopGroupList();
				if (hasChant)
					l.put(new PipeStopGroup(
							PipeStopListReference.REGISTERSET_CHANT, null));
				if (hasContreChant)
					l.put(new PipeStopGroup(
							PipeStopListReference.REGISTERSET_CONTRECHAMP, null));

				// TODO , Rendering ...

				Scale g = new Scale(this.name, largeur, entreaxe, entreaxe,
						premierepiste, nbpistes, tds, l, vitessedefilement,
						null, "Converted from Midiboek",
						Scale.GAMME_STATE_INPROGRESS, "Piet", null, false,
						false, null);

				return g;
			}

		}

		RegistersRead r = new RegistersRead(file.getName());
		MidiBoekFileParser fp = new MidiBoekFileParser(new FileReader(file), r);
		fp.parse();

		return r.toScale();
	}

	/**
	 * Routine de test
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		logger.debug("Start Importing ... ");

		logger.debug("resultat : "
				+ importScale(new File(
						"C:\\Projets\\APrint\\midiboek_gammes\\m_lim51.gam")));

	}

}
