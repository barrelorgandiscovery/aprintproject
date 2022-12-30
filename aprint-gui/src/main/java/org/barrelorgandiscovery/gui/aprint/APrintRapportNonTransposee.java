package org.barrelorgandiscovery.gui.aprint;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.barrelorgandiscovery.gui.ascale.ScaleComponent;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.TimeUtils;
import org.barrelorgandiscovery.virtualbook.Hole;


public class APrintRapportNonTransposee extends JDialog {

	private static final long serialVersionUID = 5222290318496722374L;
	
	private JTextArea text;

	public APrintRapportNonTransposee(Frame owner, ArrayList<Hole> holes,
			Scale origine) {
		super(owner, Messages.getString("APrintRapportNonTransposee.0"), true); //$NON-NLS-1$
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		// Lecture des éléments non transposés ...
		text = new JTextArea();

		AbstractTrackDef[] gammetracks = origine.getTracksDefinition();

		StringBuffer sb = new StringBuffer();
		// construction de la chaine de caractères ...
		for (int i = 0; i < holes.size(); i++) {

			Hole h = holes.get(i);

			AbstractTrackDef td = gammetracks[h.getTrack()];

			sb.append(ScaleComponent.getTrackLibelle(td) + " " //$NON-NLS-1$
					+ TimeUtils.toMinSecs(h.getTimestamp())
					+ Messages.getString("APrintRapportNonTransposee.2")); //$NON-NLS-1$
			
		}
		text.setText(sb.toString());

		JScrollPane sp = new JScrollPane(text);
		getContentPane().add(sp);

		setSize(new Dimension(500, 500));
		// pack();
	}

}
