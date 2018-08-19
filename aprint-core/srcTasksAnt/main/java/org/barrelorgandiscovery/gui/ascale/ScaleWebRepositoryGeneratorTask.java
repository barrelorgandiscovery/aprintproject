package org.barrelorgandiscovery.gui.ascale;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.io.ScaleIO;
import org.barrelorgandiscovery.tools.StreamsTools;


/**
 * Outil de génération de la page web sur les gammes
 * 
 * @author Freydiere Patrice
 */
public class ScaleWebRepositoryGeneratorTask extends Task {

	private static final Logger logger = Logger
			.getLogger(ScaleWebRepositoryGeneratorTask.class);

	static {
		BasicConfigurator.configure(new ConsoleAppender(new SimpleLayout()));

	}

	public ScaleWebRepositoryGeneratorTask() {

	}

	private File templatefolder;

	public File getTemplatefolder() {
		return templatefolder;
	}

	public void setTemplatefolder(File templatefolder) {
		this.templatefolder = templatefolder;
	}

	private File outputfolder;

	public File getOutputfolder() {
		return outputfolder;
	}

	public void setOutputfolder(File outputfolder) {
		this.outputfolder = outputfolder;
	}

	private File scalesfolder;

	public File getScalesfolder() {
		return scalesfolder;
	}

	public void setScalesfolder(File scalesfolder) {
		this.scalesfolder = scalesfolder;
	}

	private String local = "fr"; //$NON-NLS-1$

	public String getLocale() {
		return local;
	}

	public void setLocale(String local) {
		this.local = local;
	}

	/**
	 * copie du contenu du fichier f dans le flux out ...
	 * 
	 * @param f
	 * @param out
	 */
	private static void CopyInto(File f, FileWriter out) throws IOException {

		LineNumberReader fr = new LineNumberReader(new FileReader(f));
		try {
			String s = null;
			while ((s = fr.readLine()) != null) {
				out.write(s);
			}
		} finally {
			fr.close();
		}
	}

	@Override
	public void execute() throws BuildException {
		try {

			log("Verification des paramètres"); //$NON-NLS-1$

			if (scalesfolder == null)
				throw new BuildException("no scalesfolder defined"); //$NON-NLS-1$

			if (outputfolder == null)
				throw new BuildException("no outputfolder defined"); //$NON-NLS-1$

			if (templatefolder == null)
				throw new BuildException("no templatefolder defined"); //$NON-NLS-1$

			// Création du fichier index.html ....

			if ("en".equals(local)) //$NON-NLS-1$
				local = "en"; //$NON-NLS-1$

			Locale oldLocale = Locale.getDefault();
			try {

				if ("fr".equals(local)) { //$NON-NLS-1$
					Locale.setDefault(Locale.FRANCE);
				} else {
					Locale.setDefault(Locale.US);
				}

				System.out.println("current local : " + Locale.getDefault()); //$NON-NLS-1$
				Messages.initLocale(null);

				log("Ouverture du fichier de sortie"); //$NON-NLS-1$

				// Lecture des fichiers textes de template ...
				FileWriter fw = new FileWriter(new File(outputfolder,
						"index.html"), false); //$NON-NLS-1$
				try {
					log("Copie du header"); //$NON-NLS-1$
					// Ecriture du header ...
					CopyInto(new File(templatefolder, "header.template"), fw); //$NON-NLS-1$

					// Lecture des fichiers de gamme ....
					log("liste des fichier de gamme"); //$NON-NLS-1$
					String[] gammenames = scalesfolder
							.list(new FilenameFilter() {
								public boolean accept(File dir, String name) {
									if (name == null)
										return false;
									return name.endsWith(".gamme"); //$NON-NLS-1$
								}
							});

					fw
							.write("<h1>" + Messages.getString("ScaleWebRepositoryGeneratorTask.15") + "</h1><br/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

					int cpt = 0;

					fw.write("<table border=\"0\"><tr>"); //$NON-NLS-1$

					for (String g : gammenames) {
						cpt++;
						fw.write("<td>"); //$NON-NLS-1$
						try {

							Scale gamme = ScaleIO.readGamme(new File(
									scalesfolder, g));

							String state = gamme.getState();
							String contact = gamme.getContact();

							if (!Scale.GAMME_STATE_COMPLETED.equals(state))
								fw.write("<img src=\"ledyellow.png\">"); //$NON-NLS-1$
							else
								fw.write("<img src=\"ledgreen.png\">"); //$NON-NLS-1$

							fw
									.write("<a href=\"#" + g + "\">" + Messages.getString("ScaleWebRepositoryGeneratorTask.23") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											+ gamme.getName() + "</a>"); //$NON-NLS-1$

							if (contact != null)
								fw
										.write("<br><div style=\"font-size:smaller;\"><i>" + Messages.getString("ScaleWebRepositoryGeneratorTask.26") //$NON-NLS-1$ //$NON-NLS-2$
												+ contact + "</i></div>"); //$NON-NLS-1$

							fw.write("</td>"); //$NON-NLS-1$

						} catch (Exception ex) {
							throw new Exception(ex.getMessage(), ex);
						}
						if ((cpt % 3) == 0)
							fw.write("</tr><tr>"); //$NON-NLS-1$

					}

					fw.write("</tr></table>"); //$NON-NLS-1$

					cpt = 0;
					fw
							.write("<h1>" + Messages.getString("ScaleWebRepositoryGeneratorTask.32") + "</h1><br/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					fw.write("<table border=\"0\"><tr>"); //$NON-NLS-1$

					for (String g : gammenames) {
						cpt++;
						fw.write("<td>"); //$NON-NLS-1$
						try {
							log("Traitement de la gamme : " + g); //$NON-NLS-1$
							// Lecture de la gamme
							Scale gamme = ScaleIO.readGamme(new File(
									scalesfolder, g));

							ScaleComponent gc = new ScaleComponent();
							gc.loadScale(gamme);

							String pngfile = "tn-" + g + ".png"; //$NON-NLS-1$ //$NON-NLS-2$

							// write the full size scale ...

							log("Export sous forme d'image de la gamme ... "); //$NON-NLS-1$
							// Génération de l'image de la gamme ...
							Dimension preferredSize = gc.getPreferredSize();

							int width = (int) preferredSize.getWidth();
							int height = (int) preferredSize.getHeight();
							BufferedImage imfull = new BufferedImage(width,
									height, BufferedImage.TYPE_4BYTE_ABGR);
							Graphics2D g2dfs = imfull.createGraphics();
							try {

								gc.paint(g2dfs);

							} finally {
								g2dfs.dispose();
							}

							String fspngfile = "fs-" + g + ".png"; //$NON-NLS-1$ //$NON-NLS-2$
							// Ecriture du fichier image ...
							ImageIO.write(imfull, "PNG", new File(outputfolder, //$NON-NLS-1$
									fspngfile));

							// Ecriture du thumbnail ...
							BufferedImage im = new BufferedImage(214, 305,
									BufferedImage.TYPE_4BYTE_ABGR);

							Graphics2D graphic = im.createGraphics();
							try {

								graphic.scale(0.2, 0.2);

								RenderingHints renderHints = new RenderingHints(
										RenderingHints.KEY_ANTIALIASING,
										RenderingHints.VALUE_ANTIALIAS_ON);
								renderHints.put(RenderingHints.KEY_RENDERING,
										RenderingHints.VALUE_RENDER_QUALITY);
								renderHints
										.put(
												RenderingHints.KEY_INTERPOLATION,
												RenderingHints.VALUE_INTERPOLATION_BICUBIC);

								graphic.addRenderingHints(renderHints);
								graphic.drawImage(imfull, 0, 0, null);

							} finally {
								graphic.dispose();
							}

							ImageIO.write(im, "PNG", new File(outputfolder, //$NON-NLS-1$
									pngfile));

							// écriture du contenu dans le fichier de résultat
							fw
									.write("<a name=\"" //$NON-NLS-1$
											+ g
											+ "\">" + Messages.getString("ScaleWebRepositoryGeneratorTask.46") + "<b>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											+ gamme.getName()
											+ "</b></a><br> <div style=\"font-size:smaller\">" + Messages.getString("ScaleWebRepositoryGeneratorTask.49") + "<i> :" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											+ g + "</i></div> <br/>"); //$NON-NLS-1$

							fw
									.write("<div style=\"font-size:smaller\"><a href=\"mailto:frett27@free.fr?subject=Gamme " //$NON-NLS-1$
											+ g
											+ "\">" + Messages.getString("ScaleWebRepositoryGeneratorTask.14") + "</a></div><br/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

							fw.write("<a href=\"" + fspngfile + "\">" //$NON-NLS-1$ //$NON-NLS-2$
									+ "<img src=\"" + pngfile //$NON-NLS-1$
									+ "\" /></a><br/>"); //$NON-NLS-1$
							fw
									.write("<a href=\"" + g //$NON-NLS-1$
											+ "\"> " + Messages.getString("ScaleWebRepositoryGeneratorTask.62") + " </a><br/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

						} catch (Exception ex) {
							throw new BuildException(ex.getMessage(), ex);
						}

						fw.write("</td>"); //$NON-NLS-1$
						if (cpt % 3 == 0) {
							fw.write("</tr><tr>"); //$NON-NLS-1$
						}
					}

					fw.write("</tr></table>"); //$NON-NLS-1$

					// Ecriture du footer ...
					CopyInto(new File(templatefolder, "footer.template"), fw); //$NON-NLS-1$

				} finally {
					fw.close();
				}

			} finally {
				Locale.setDefault(oldLocale);
			}

			log("copy the images to the output folder ..."); //$NON-NLS-1$

			File[] listFiles = this.templatefolder.listFiles();
			for (int i = 0; i < listFiles.length; i++) {
				File file = listFiles[i];
				if (file.getName().endsWith(".png")) { //$NON-NLS-1$
					// copy the file to the output directory ...
					FileInputStream fis = new FileInputStream(file);
					try {
						FileOutputStream fos = new FileOutputStream(new File(
								outputfolder, file.getName()));
						try {
							StreamsTools.copyStream(fis, fos);
						} finally {
							fos.close();
						}
					} finally {
						fis.close();
					}
				}
			}

		} catch (Exception ex) {
			logger.error("execute", ex); //$NON-NLS-1$
			throw new BuildException(ex.getMessage(), ex);
		}

	}
}
