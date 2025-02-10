package org.barrelorgandiscovery.gui.aprint.instrumentchoice;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprint.PrintPreview;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.ascale.ScaleComponent;
import org.barrelorgandiscovery.gui.ascale.ScalePrintDocument;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Factory;
import org.barrelorgandiscovery.repository.RepositoryChangedListener;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.ui.tools.VerticalBagLayout;

import com.jeta.forms.components.panel.FormPanel;

public class JInstrumentChoice extends JPanel implements IInstrumentChoice {

	/**
	 * Loggeur
	 */
	private static Logger logger = Logger.getLogger(JInstrumentChoice.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 182147436234752673L;

	/**
	 * Reference au repository
	 */
	private Repository2 rep = null;

	private JPanel panelInstrumentChoice = null;

	private JPanel panel;

	private JLabel imageInstrument = null;

	private JLabel lbldescription = null;
	private JLabel lblnomgamme = null;
	private JLabel lblcontact = null;
	private JLabel lblinstrument = null;

	private JButton buttonImprimerGamme = null;

	private ScaleComponent gammecomponent = null;

	private JLabel scalePicture = new JLabel();

	private class InstrumentDisplayer {
		private Instrument ins = null;

		InstrumentDisplayer(Instrument ins) {
			this.ins = ins;
		}

		@Override
		public String toString() {
			return ins.getName();
		}

		public Instrument getInstrument() {
			return ins;
		}

	}

	private static ListCellRenderer<? super InstrumentDisplayer> createListRenderer() {
		return new DefaultListCellRenderer() {
			private Color background = new Color(0, 100, 255, 15);
			private Color defaultBackground = (Color) UIManager.get("List.background");

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (c instanceof JLabel) {
					JLabel label = (JLabel) c;
					InstrumentDisplayer emp = (InstrumentDisplayer) value;
					label.setText(String.format("%s", emp.toString()));
					if (!isSelected) {
						label.setBackground(index % 2 == 0 ? background : defaultBackground);
					}
				}
				return c;
			}
		};
	}

	private static boolean employeeFilter(InstrumentDisplayer emp, String str) {
		return emp.toString().toLowerCase().contains(str.toLowerCase());
	}

	private IInstrumentChoiceListener listener = null;

	/**
	 * Constructeur
	 * 
	 * @param repository le repository associé ...
	 */
	public JInstrumentChoice(Repository2 repository, IInstrumentChoiceListener listener) {

		assert repository != null;

		internalChangeRespositoryAndRegisterEvents(repository);

		this.listener = listener;

		initComponents();
	}

	private void internalChangeRespositoryAndRegisterEvents(Repository2 repository) {

		if (rep != null)
			rep.removeRepositoryChangedListener(internalRepositoryChangeListener);

		repository.addRepositoryChangedListener(internalRepositoryChangeListener);

		this.rep = repository;
	}

	private class InternalRepositoryChangeListener implements RepositoryChangedListener {

		public void instrumentsChanged() {
			reloadInstruments();
		}

		public void scalesChanged() {
			updateInstrumentInformations();
		}

		public void transformationAndImporterChanged() {

		}

	}

	private InternalRepositoryChangeListener internalRepositoryChangeListener = new InternalRepositoryChangeListener();

	/**
	 * Combo de choix des instruments ...
	 */
	private JComboBox choixInstrument;

	private JScrollPane gammescrollpane;

	/**
	 * Initialisation des composants ...
	 */
	private void initComponents() {

		FormPanel thepanel = null;

		panelInstrumentChoice = new JPanel();

		try {

			InputStream is = getClass().getResourceAsStream("APrintInstrumentChoice.jfrm"); //$NON-NLS-1$
			if (is == null)
				throw new Exception("form not found"); //$NON-NLS-1$
			thepanel = new FormPanel(is);
			panelInstrumentChoice = thepanel;
		} catch (Exception ex) {
			logger.error("panel construction", ex); //$NON-NLS-1$
		}

		choixInstrument = thepanel.getComboBox("instrumentList"); //$NON-NLS-1$
		choixInstrument.setEditable(true);
		Autocompletion.enable(choixInstrument);

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new TitledBorder(Messages.getString("JChoixInstrument.0"))); //$NON-NLS-1$

		panelInstrumentChoice.setLayout(new VerticalBagLayout());

		gammecomponent = new ScaleComponent();

		panel.add(panelInstrumentChoice);

		// gammescrollpane = new
		// JScrollPane(gammecomponent,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		// JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		// gammescrollpane.setAutoscrolls(true);
		// gammescrollpane.setMaximumSize(new Dimension(600,600));

		// panel.add(gammecomponent);
		panel.add(scalePicture);

		// ajout de l'image de l'instrument
		imageInstrument = thepanel.getLabel("previewInstrument"); //$NON-NLS-1$
		// imageInstrument.setAlignmentX(0.5f); // centre
		// imageInstrument.setBorder(BorderFactory.createEmptyBorder(3, 3, 3,
		// 3));

		// ajout des informations textuelles pour l'instrument
		lbldescription = thepanel.getLabel("labelDescription"); //$NON-NLS-1$

		lblnomgamme = thepanel.getLabel("labelGammeName"); //$NON-NLS-1$
		lblcontact = thepanel.getLabel("labelContactName"); //$NON-NLS-1$
		lblinstrument = thepanel.getLabel("labelInstrumentName"); //$NON-NLS-1$

		buttonImprimerGamme = new JButton();
		buttonImprimerGamme.setIcon(new ImageIcon(APrintNG.class.getResource("preview.png"))); //$NON-NLS-1$

		buttonImprimerGamme.setToolTipText(Messages.getString("JChoixInstrument.1")); //$NON-NLS-1$

		buttonImprimerGamme.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					Instrument ins = getCurrentInstrument();
					if (ins == null) {
						logger.warn("no selected instrument"); //$NON-NLS-1$
						return;
					}
					new PrintPreview(new ScalePrintDocument(ins.getScale()));

				} catch (Exception ex) {
					logger.error("imprimergammeinstrument", ex); //$NON-NLS-1$
					JMessageBox.showMessage(null, Messages.getString("JChoixInstrument.5")); //$NON-NLS-1$
				}
			}
		});

		// scale panel

		reloadInstruments();
		updateInstrumentInformations();

		// Ajout de la gestion des �v�nements ..
		choixInstrument.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {

					Object o = e.getItem();
					logger.debug("Instrument Changed " + o); //$NON-NLS-1$

					if (o != null && listener != null) {
						logger.debug("Send instrument changed"); //$NON-NLS-1$
						updateInstrumentInformations();
						listener.instrumentChanged(getCurrentInstrument());
					}
				}
			}
		});

		// ajout du composant de choix dans le composant ...
		add(panel);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.gui.aprint.instrumentchoice.IInstrumentChoice#
	 * setRepository(fr.freydierepatrice.repository.Repository2)
	 */
	public void setRepository(Repository2 newrep) {

		internalChangeRespositoryAndRegisterEvents(newrep);

		reloadInstruments();
		updateInstrumentInformations();
	}

	/**
	 * internal method for update the component after an instrument change
	 */
	private void updateInstrumentInformations() {
		// changement de l'image ...
		Instrument ins = getCurrentInstrument();

		if (ins == null)
			return;

		Image iimage = ins.getThumbnail();
		if (iimage != null) {
			imageInstrument.setIcon(new ImageIcon(iimage));
		} else {
			imageInstrument.setIcon(new ImageIcon(new BufferedImage(100, 134, BufferedImage.TYPE_4BYTE_ABGR)));
		}

		lbldescription.setText(null);
		if (ins.getDescriptionUrl() != null)
			lbldescription.setText(ins.getDescriptionUrl());

		assert ins.getScale() != null;
		lblnomgamme.setText(ins.getScale().getName());
		lblcontact.setText(ins.getScale().getContact());
		lblinstrument.setText(ins.getName());

		gammecomponent.loadScale(ins.getScale());

		Dimension size = gammecomponent.getPreferredSize();
		gammecomponent.setSize(size);

		// Create a picture of the scale ...
		BufferedImage bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = bi.createGraphics();
		try {
			gammecomponent.paint(g);
		} finally {
			g.dispose();
		}

		scalePicture.setIcon(new ImageIcon(bi));

		// gammecomponent.revalidate();
		// gammescrollpane.revalidate();
		// gammescrollpane.repaint();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.gui.aprint.instrumentchoice.IInstrumentChoice#
	 * reloadInstruments()
	 */
	public void reloadInstruments() {
		// Recharge les instruments du repository ...

		// RAZ de la liste des instruments
		choixInstrument.removeAllItems();

		if (rep != null) {

			Instrument[] instruments = rep.listInstruments();
			Arrays.sort(instruments, new Comparator<Instrument>() {

				public int compare(Instrument o1, Instrument o2) {

					String name1 = o1.getName();
					String name2 = o2.getName();
					if (name1 == null)
						return 0;

					return name1.compareTo(name2);

				}
			});

			for (int i = 0; i < instruments.length; i++) {
				choixInstrument.addItem(new InstrumentDisplayer(instruments[i]));
			}

			// Sélection du premier instrument
			if (instruments.length > 0)
				choixInstrument.setSelectedIndex(0);
		}
	}

	public boolean selectInstrument(String instrumentName) {
		if (instrumentName == null)
			return false;

		for (int i = 0; i < choixInstrument.getItemCount(); i++) {
			InstrumentDisplayer id = (InstrumentDisplayer) choixInstrument.getItemAt(i);
			if (instrumentName.equals(id.getInstrument().getName())) {
				choixInstrument.setSelectedIndex(i);
				return true;
			}
		}
		return false;
	}

	private String currentInstrumentFilter = null;

	public String getInstrumentFilter() {
		return currentInstrumentFilter;
	}

	public void setInstrumentFilter(String filter) {
		if (filter == null || "".equals(filter.trim())) {
			currentInstrumentFilter = null;
			return;
		}

		currentInstrumentFilter = filter.trim().toLowerCase();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.gui.aprint.instrumentchoice.IInstrumentChoice#
	 * getCurrentInstrument()
	 */
	public Instrument getCurrentInstrument() {

		InstrumentDisplayer o = ((InstrumentDisplayer) choixInstrument.getSelectedItem());

		if (o == null)
			return null;

		return o.getInstrument();
	}

	public static void main(String[] args) throws Exception {

		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure(new LF5Appender());

		APrintProperties properties = new APrintProperties(false);
		Properties p = new Properties();
		p.setProperty("repositorytype", "folder");

		p.setProperty("folder", "/home/use/aprintstudio/private");

		Repository2 repository2 = Repository2Factory.create(p, properties);

		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());
		JInstrumentChoice instrumentChoice = new JInstrumentChoice(repository2, new IInstrumentChoiceListener() {
			@Override
			public void instrumentChanged(Instrument newInstrument) {
				System.out.println("selected instrument :" + newInstrument);
			}
		});
		f.getContentPane().add(instrumentChoice, BorderLayout.CENTER);

		JButton validationButton = new JButton("Ok");
		validationButton.addActionListener( (e) -> {
			System.out.println(instrumentChoice.getCurrentInstrument());
		});
		f.getContentPane().add(validationButton, BorderLayout.SOUTH);

		f.setSize(new Dimension(500, 400));
		f.setVisible(true);

	}

}
