package org.barrelorgandiscovery.gui.ascale;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.RegisterCommandStartDef;


/**
 * Classe utilitaire de test des panneaux ...
 * 
 * @author Freydiere Patrice
 */
public class AbstractTrackDefComponentTester extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7521913916822235361L;

	private NoteDefComponent notedefcomponent;

	private PercussionDefComponent percussiondefcomponent;

	private RegisterStartDefComponent registerdefcomponent;

	public AbstractTrackDefComponentTester() throws Exception {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		notedefcomponent = new NoteDefComponent();
		notedefcomponent.load(new NoteDef(69, "Accompagnement")); //$NON-NLS-1$
		notedefcomponent
				.setTrackDefComponentListener(new TrackDefComponentListener() {
					public void trackDefChanged(AbstractTrackDef td) {
						System.out.println(td.toString());
					}
				});

		percussiondefcomponent = new PercussionDefComponent();
		percussiondefcomponent.load(new PercussionDef(40, Double.NaN,
				Double.NaN));
		percussiondefcomponent
				.setTrackDefComponentListener(new TrackDefComponentListener() {
					public void trackDefChanged(AbstractTrackDef td) {
						System.out.println(td.toString());
					}
				});

		registerdefcomponent = new RegisterStartDefComponent();
		registerdefcomponent.load(new RegisterCommandStartDef("Bourdons", //$NON-NLS-1$
				"toto", Double.NaN, Double.NaN)); //$NON-NLS-1$
		registerdefcomponent
				.setTrackDefComponentListener(new TrackDefComponentListener() {
					public void trackDefChanged(AbstractTrackDef td) {
						System.out.println(td.toString());
					}
				});

		setLayout(new GridBagLayout());

		getContentPane().add(
				notedefcomponent,
				new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

		getContentPane().add(
				percussiondefcomponent,
				new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
		getContentPane().add(
				registerdefcomponent,
				new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

		pack();
	}

	public static void main(String[] args) throws Exception{
		BasicConfigurator.configure(new LF5Appender());
		new AbstractTrackDefComponentTester().setVisible(true);
	}

}
