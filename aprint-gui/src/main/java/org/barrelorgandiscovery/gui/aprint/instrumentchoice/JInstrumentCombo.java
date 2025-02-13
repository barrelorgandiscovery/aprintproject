package org.barrelorgandiscovery.gui.aprint.instrumentchoice;

import java.awt.BorderLayout;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Factory;
import org.barrelorgandiscovery.repository.RepositoryChangedListener;

public class JInstrumentCombo extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1581972416838379690L;

	private static Logger logger = Logger.getLogger(JInstrumentCombo.class);

	/**
	 * instrument repository
	 */
	private Repository2 rep;

	private IInstrumentChoiceListener choiceListener;

	/**
	 * constructor,
	 * 
	 * @param repository2 the instrument repository
	 */
	public JInstrumentCombo(Repository2 repository2) {
		this.rep = repository2;

		rep.addRepositoryChangedListener(new RepositoryChangedListener() {

			public void transformationAndImporterChanged() {

			}

			public void scalesChanged() {

			}

			public void instrumentsChanged() {
				updateContent();
			}
		});
		initComponents();

		cb.addItemListener((item) -> {
			if (choiceListener != null) {
				try {
				choiceListener.instrumentChanged(this.getSelectedInstrument());
				} catch(Throwable t) {
					logger.error(t.getMessage(), t);
				}
			}
		});
	}

	public void setChoiceListener(IInstrumentChoiceListener choiceListener) {
		this.choiceListener = choiceListener;
	}
	
	/**
	 * the combo box
	 */
	private JComboBox cb;

	private JLabel text;

	/**
	 * init the internal components
	 */
	protected void initComponents() {
		setLayout(new BorderLayout());

		text = new JLabel("Select Instrument :");
		add(text, BorderLayout.WEST);

		cb = new JComboBox();

		updateContent();

		add(cb, BorderLayout.CENTER);
	}
	
	public void setLabel(String label) {
		text.setText(label);
	}

	/**
	 * internal class for diplay instrument label in combobox
	 * 
	 * @author use
	 *
	 */
	private static class InstrumentDisplayer {
		public String label;
		public Instrument instrument;

		@Override
		public String toString() {
			return label;
		}
	}

	/**
	 * update the combobox content
	 */
	private void updateContent() {
		Instrument[] instruments = rep.listInstruments();
		Vector<InstrumentDisplayer> ids = new Vector<InstrumentDisplayer>();
		for (int i = 0; i < instruments.length; i++) {
			Instrument instrument = instruments[i];
			InstrumentDisplayer id = new InstrumentDisplayer();
			id.label = instrument.getName();

			id.instrument = instrument;
			ids.add(id);
		}

		DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel(ids);
		cb.setModel(defaultComboBoxModel);

	}

	/**
	 * Get the selected instrument
	 * 
	 * @return the selected instrument or null if none
	 */
	public Instrument getSelectedInstrument() {

		InstrumentDisplayer id = (InstrumentDisplayer) cb.getSelectedItem();
		if (id == null)
			return null;

		return id.instrument;
	}

	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		JFrame f = new JFrame();

		Repository2 r2 = Repository2Factory.create(new Properties(), new APrintProperties("aprintstudio", true));

		f.getContentPane().add(new JInstrumentCombo(r2));

		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
