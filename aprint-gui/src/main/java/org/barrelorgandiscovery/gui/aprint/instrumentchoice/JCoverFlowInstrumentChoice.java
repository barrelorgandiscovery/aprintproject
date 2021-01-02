package org.barrelorgandiscovery.gui.aprint.instrumentchoice;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Collection;
import org.barrelorgandiscovery.repository.Repository2Factory;
import org.barrelorgandiscovery.repository.RepositoryChangedListener;
import org.barrelorgandiscovery.ui.animation.GradientPanel;
import org.barrelorgandiscovery.ui.animation.ImageFlow;
import org.barrelorgandiscovery.ui.animation.ImageFlowItem;
import org.barrelorgandiscovery.ui.animation.StackLayout;


public class JCoverFlowInstrumentChoice extends JPanel implements
		IInstrumentChoice {

	private static Logger logger = Logger
			.getLogger(JCoverFlowInstrumentChoice.class);

	private class InternalRepositoryChangeListener implements
			RepositoryChangedListener {

		public void instrumentsChanged() {
			reloadInstruments();
		}

		public void scalesChanged() {
			reloadInstruments();

		}

		public void transformationAndImporterChanged() {

		}

	}

	private static class InstrumentItemFlow extends ImageFlowItem {

		private Instrument instrument = null;

		public InstrumentItemFlow(Instrument instrument, String displayName)
				throws Exception {
			super(instrument.getThumbnail(), displayName);
			this.instrument = instrument;
		}

		private Instrument getInstrument() {
			return this.instrument;
		}

	}

	private JPanel imageFlowPanel;
	private ImageFlow imageFlow;

	private IInstrumentChoiceListener choiceListener;

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		imageFlow.setEnabled(enabled);
		logger.debug("enabled " + enabled);
	}

	/**
	 * Create a cover flow associated to the repository
	 * @param repository2 the repository, must not be null
	 * @param instrumentListener
	 */
	public JCoverFlowInstrumentChoice(Repository2 repository2,
			IInstrumentChoiceListener instrumentListener) {
		
		assert repository2 != null;
		
		imageFlowPanel = new JPanel(new StackLayout());
		imageFlowPanel.add(new GradientPanel(), StackLayout.BOTTOM);

		internalChangeRepositoryAndRegisterEvents(repository2);

		setLayout(new BorderLayout());

		add(imageFlowPanel, BorderLayout.CENTER);

		this.choiceListener = instrumentListener;

	}

	public Instrument getCurrentInstrument() {

		if (imageFlow != null) {
			Object selectedValue = imageFlow.getSelectedValue();
			if (selectedValue == null)
				return null;

			return ((InstrumentItemFlow) selectedValue).getInstrument();
		}

		return null;
	}

	public boolean selectInstrument(String instrumentName) {
		if (instrumentName == null)
			return false;
		
		List<ImageFlowItem> items = imageFlow.getItems();
		if (items != null)
		{
			for(int i = 0 ; i < items.size() ; i ++)
			{
				
				InstrumentItemFlow fi = (InstrumentItemFlow)items.get(i);
				if (instrumentName.equals( fi.getInstrument().getName()))
				{
					imageFlow.setAvatarIndex(i);
					return true;
				}
				
			}
		}
		
		return false;
	}
	
	/**
	 * get the first existing instrument, or null if none
	 * @return
	 */
	public Instrument getFirstInstrument() {
		List<ImageFlowItem> items = imageFlow.getItems();
		if (items != null && items.size() > 0) {
			InstrumentItemFlow iflow = (InstrumentItemFlow) items.get(0);
			if (iflow != null) {
				return iflow.instrument;
			}
		}
		
		return null;
	}
	
	
	
	public void reloadInstruments() {

		Instrument lastInstrumentBeforeReload = getCurrentInstrument();
		logger.debug("last instrument Before Reload :"
				+ lastInstrumentBeforeReload);

		List<ImageFlowItem> array = new ArrayList<ImageFlowItem>();
		Instrument[] listInstruments = rep.listInstruments();
		if (listInstruments != null) {
			instrumentSuivant: for (int i = 0; i < listInstruments.length; i++) {
				Instrument instrument = listInstruments[i];
				try {

					String instrumentDisplayName = instrument.getName();

					if (tokenizedKeyWords.length > 0) {
						for (int j = 0; j < tokenizedKeyWords.length; j++) {
							if (!"".equals(tokenizedKeyWords[j])
									&& instrumentDisplayName.toLowerCase()
											.indexOf(tokenizedKeyWords[j]) == -1) {
								logger.debug("filter this instrument");
								continue instrumentSuivant;
							}
						}

					}

					logger.debug("adding instrument " + instrumentDisplayName);

					if (rep instanceof Repository2Collection) {
						Repository2Collection rc = (Repository2Collection) rep;
						Repository2 findRepositoryAssociatedTo = rc
								.findRepositoryAssociatedTo(instrument);
						if (findRepositoryAssociatedTo != null) {
							String repositoryname = findRepositoryAssociatedTo
									.getName();
							if (repositoryname != null
									&& repositoryname.length() > 10)
								repositoryname = repositoryname
										.substring(0, 10)
										+ "..."; //$NON-NLS-1$
							instrumentDisplayName += " (" + repositoryname //$NON-NLS-1$
									+ ")"; //$NON-NLS-1$
						}
					}

					array.add(new InstrumentItemFlow(instrument,
							instrumentDisplayName));
				} catch (Exception ex) {
					logger.error("instrument cannot be loaded ...", ex); //$NON-NLS-1$
				}
			}
		}

		if (imageFlow != null) {
			imageFlowPanel.remove(imageFlow);
		}

		// get the name of the last selected instrument ...
		String lastInstrumentName = null;
		if (lastInstrumentBeforeReload != null) {
			lastInstrumentName = lastInstrumentBeforeReload.getName();
			logger.debug("last instrument name : " + lastInstrumentName);
		}

		imageFlow = new ImageFlow((List<ImageFlowItem>) array);
		imageFlow.setSigma(0.4);
		imageFlow.setSpacing(0.3);

		imageFlow.setEnabled(this.isEnabled());

		imageFlowPanel.add(imageFlow, StackLayout.TOP);

		imageFlow.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {

				logger.debug("value changed on the image Flow " + e);
				
				Object selectedValue = imageFlow.getSelectedValue();

				if (choiceListener == null)
				{
					return;
				}
				
				logger.debug(selectedValue);
				if (selectedValue == null) {
					choiceListener.instrumentChanged(null);
					return;
				}

				choiceListener
						.instrumentChanged(((InstrumentItemFlow) selectedValue)
								.getInstrument());
			}
		});

		// find the index of the last instrument to focus on it !!

		if (lastInstrumentName != null) {
			for (int i = 0; i < array.size(); i++) {
				InstrumentItemFlow itf = (InstrumentItemFlow) array.get(i);
				if (itf.getInstrument().getName().equals(lastInstrumentName)) {
					imageFlow.setSelectedIndex(i);
					logger.debug("reselect item :" + i);
					break;
				}
			}
		} else {
			logger.debug("no selected instrument ...");
		}

		imageFlowPanel.revalidate();
		
	}

	private String currentInstrumentFilter = null;
	private String[] tokenizedKeyWords = new String[0];

	public void setInstrumentFilter(String filter) {

		if (filter == null || "".equals(filter.trim())) {
			currentInstrumentFilter = null;
			tokenizedKeyWords = new String[0];
			return;
		}

		currentInstrumentFilter = filter.trim().toLowerCase();

		tokenizedKeyWords = filter.split(" ");
		for (int i = 0; i < tokenizedKeyWords.length; i++) {
			tokenizedKeyWords[i] = tokenizedKeyWords[i].trim();
			logger.debug("keyword :" + tokenizedKeyWords[i]);
		}

		logger.debug("current instrument filter :" + currentInstrumentFilter);

	}

	public String getInstrumentFilter() {
		return currentInstrumentFilter;
	}

	public void setRepository(Repository2 newrep) {
		internalChangeRepositoryAndRegisterEvents(newrep);
	}

	private Repository2 rep;

	private InternalRepositoryChangeListener internalRepositoryChangeListener = new InternalRepositoryChangeListener();

	private void internalChangeRepositoryAndRegisterEvents(
			Repository2 repository) {

		if (rep != null)
			rep
					.removeRepositoryChangedListener(internalRepositoryChangeListener);

		repository
				.addRepositoryChangedListener(internalRepositoryChangeListener);

		this.rep = repository;
		reloadInstruments();
	}
	
	
	public static void main(String[] args) throws Exception {
		
		JFrame f = new JFrame();
		

		Properties p = new Properties();
		p.setProperty("repositorytype", "folder"); //$NON-NLS-1$ //$NON-NLS-2$
		p.setProperty("folder", //$NON-NLS-1$
				"C:\\users\\use\\aprintstudio\\private"); //$NON-NLS-1$

		APrintProperties aprintproperties = new APrintProperties(true);

		Repository2 repository = Repository2Factory.create(p, aprintproperties);

		
		JCoverFlowInstrumentChoice jCoverFlowInstrumentChoice = new JCoverFlowInstrumentChoice(repository, new IInstrumentChoiceListener() {
			
			public void instrumentChanged(Instrument newInstrument) {
				
				
			}
		});
		
		
		
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new BorderLayout());
		jPanel.add(jCoverFlowInstrumentChoice, BorderLayout.CENTER);
		
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(jPanel, BorderLayout.CENTER);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(500,200);
		
		jCoverFlowInstrumentChoice.selectInstrument("31t Raffin");
		
		
		f.setVisible(true);
		
		jCoverFlowInstrumentChoice.setRepository(repository);
				
		
	}
	

}
