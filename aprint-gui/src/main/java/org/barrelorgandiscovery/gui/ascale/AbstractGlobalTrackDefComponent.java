package org.barrelorgandiscovery.gui.ascale;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.AbstractTrackDef;


public abstract class AbstractGlobalTrackDefComponent extends JComponent {

	private static Logger logger = Logger
			.getLogger(AbstractGlobalTrackDefComponent.class);

	@SuppressWarnings("unchecked")
	private Class[] editedtrackedtabbed = new Class[] { NoteDefComponent.class,
			PercussionDefComponent.class, RegisterStartDefComponent.class,
			NullTrackDefComponent.class, RegisterResetDefComponent.class };
	protected List<AbstractTrackDefComponent> tdc = new ArrayList<AbstractTrackDefComponent>();
	protected InstrumentPipeStopDescriptionComponent registersetcomponent;
	private TrackDefComponentListener listener;

	protected void initInternal(InstrumentPipeStopDescriptionComponent rsc) {
		if (rsc == null)
			throw new IllegalArgumentException();
		this.registersetcomponent = rsc;
	}

	protected void initComponent() {

		setLayout(new BorderLayout());

		// Création des tabs ...
		for (int i = 0; i < editedtrackedtabbed.length; i++) {
			try {
				AbstractTrackDefComponent c = (AbstractTrackDefComponent) editedtrackedtabbed[i]
						.newInstance();

				// Ajout de l'écouteur ...

				tdc.add(c);

				// Ajout du composant dans les panneaux ...

				addTrackDefComponentInGui(c);

				c.setTrackDefComponentListener(new TrackDefComponentListener() {

					public void trackDefChanged(AbstractTrackDef td) {
						fireTrackDefChanged(td);

					}
				});

				// informe du composant d'édition des registres ...

				c.informRegisterSetComponent(registersetcomponent);

			} catch (Exception ex) {
				logger.error("initComponent", ex); //$NON-NLS-1$
			}
		}

	}

	protected void addTrackDefComponentInGui(AbstractTrackDefComponent c) {

	}

	public AbstractGlobalTrackDefComponent() {
		super();
	}

	public void setTrackDefComponentListener(TrackDefComponentListener listener) {
		this.listener = listener;
	}

	private void fireTrackDefChanged(AbstractTrackDef newTrackDef) {
		if (listener != null) {
			logger.debug("fireTrackDefChanged"); //$NON-NLS-1$
			listener.trackDefChanged(newTrackDef);
		}
	}

	/**
	 * Edite l'élément donné
	 * 
	 * @param td
	 */
	public void edit(AbstractTrackDef td) {

		// Recherche du tab éditant l'élément ...

		logger.debug("edit " + td); //$NON-NLS-1$

		for (int i = 0; i < tdc.size(); i++) {
			AbstractTrackDefComponent evaluatedTrackDefComponent = tdc.get(i);
			if (evaluatedTrackDefComponent.getEditedTrackDef() == null) {
				if (td == null)
					activateCurrentTrackDef(i);

			} else {
				if (td != null
						&& evaluatedTrackDefComponent.getEditedTrackDef() == td
								.getClass()) {
					logger.debug("activateCurrentTrackDef " + evaluatedTrackDefComponent);
					activateCurrentTrackDef(i);
					logger.debug("load current trackDef");
					evaluatedTrackDefComponent.load(td);
					return;
				}
			}
		}

	}

	protected abstract void activateCurrentTrackDef(int index) ;
	
	
}