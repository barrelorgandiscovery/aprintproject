package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import java.awt.BorderLayout;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressPanelWithText extends JPanel {

	private JProgressBar pb;
	private JLabel text;

	public ProgressPanelWithText() throws Exception {
		initcomponents();
	}

	protected void initcomponents() throws Exception {
		pb = new JProgressBar();
		pb.setModel(new DefaultBoundedRangeModel(0, 1, 0, 100));

		text = new JLabel();
		text.setText("Ready");

		setLayout(new BorderLayout());
		add(pb, BorderLayout.NORTH);
		add(text, BorderLayout.SOUTH);

	}

	public void reset() {
		setProgress(0.0);
		setText("Ready");
	}
	
	public void setProgress(double progress) {
		pb.setValue((int) (progress * 100));
	}

	public void setText(String text) {
		this.text.setText(text);
	}

}
