package org.barrelorgandiscovery.recognition.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.recognition.RecognitionProject;

public class JTileRecognitionImages extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8639426338853228937L;

	private static Logger logger = Logger
			.getLogger(JTileRecognitionImages.class);

	private static class RecoImage {
		public Image miniImage;
		public String name;
	}

	private class ImVerticalRenderer implements ListCellRenderer {

		private JLabel labelImage = new JLabel();
		private JLabel labelText = new JLabel();

		public JPanel p = new JPanel();

		public ImVerticalRenderer() {
			p.setLayout(new BorderLayout());
			p.add(labelImage, BorderLayout.CENTER);
			labelImage.setHorizontalAlignment(SwingConstants.CENTER);
			p.add(labelText, BorderLayout.SOUTH);
			labelText.setHorizontalAlignment(SwingConstants.CENTER);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			try {
				RecoImage recoImage = (RecoImage) value;

				labelImage.setIcon(new ImageIcon(recoImage.miniImage));
				labelImage.setMaximumSize(new Dimension(200, 200));

				labelText.setText(recoImage.name);

				p.setBackground(isSelected ? UIManager
						.getColor("Table.selectionBackground") : UIManager
						.getColor("Table.background"));

				return p;

			} catch (Exception ex) {
				ex.printStackTrace(System.err);
				return p;
			}
		}
	}

	private JList list;

	public JTileRecognitionImages() {
		initComponents();
	}

	protected void initComponents() {

		JScrollPane sp = new JScrollPane();

		list = new JList();
		sp.setViewportView(list);
		list.setLayoutOrientation(JList.VERTICAL_WRAP);
		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, sp);
		list.setCellRenderer(new ImVerticalRenderer());

		list.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				if (recognitionImageSelectListener != null) {

					JList l = (JList) e.getSource();

					if (l != null) {
						if (!e.getValueIsAdjusting())
							return;

						RecoImage v = (RecoImage) l.getSelectedValue();
						try {

							recognitionImageSelectListener
									.imageSelected(v.name);
						} catch (Throwable t) {
							logger.error(
									"error in selecting item :"
											+ t.getMessage(), t);
						}
					}

				}
			}
		});

	}

	private RecognitionProject recognitionProject;

	public void setRecognitionProject(RecognitionProject recognitionProject)
			throws Exception {
		this.recognitionProject = recognitionProject;
		reloadImages();
	}

	public RecognitionProject getRecognitionProject() {
		return recognitionProject;
	}

	private RecognitionImageSelectListener recognitionImageSelectListener = null;

	public void setRecognitionImageSelectListener(
			RecognitionImageSelectListener recognitionImageSelectListener) {
		this.recognitionImageSelectListener = recognitionImageSelectListener;
	}

	private void reloadImages() throws Exception {
		list.setModel(new DefaultListModel());
		if (recognitionProject != null) {
			String[] images = recognitionProject.listImageNames();
			DefaultListModel dlm = (DefaultListModel) list.getModel();
			for (int i = 0; i < images.length; i++) {
				String string = images[i];
				BufferedImage mi = recognitionProject.getMiniImage(string);
				RecoImage recoImage = new RecoImage();
				recoImage.miniImage = mi;
				recoImage.name = string;

				dlm.addElement(recoImage);
			}

			list.setPreferredSize(new Dimension(200 * images.length, 80));
			list.setVisibleRowCount(1);
		}

	}

	/**
	 * Get the current selected image element
	 * 
	 * @return
	 */
	public String getCurrentSelected() {
		RecoImage r = (RecoImage) list.getSelectedValue();
		if (r == null)
			return null;

		return r.name;
	}

	public String[] getAllSelected() {
		Object[] selectedValues = list.getSelectedValues();

		ArrayList<String> ret = new ArrayList<String>();

		for (int i = 0; i < selectedValues.length; i++) {
			Object object = selectedValues[i];
			RecoImage r = (RecoImage) object;
			if (r != null)
				ret.add(r.name);
		}
		return ret.toArray(new String[0]);

	}

}
