package org.barrelorgandiscovery.playsubsystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository2Adapter;
import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.extensions.SimpleExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.InitNGExtensionPoint;
import org.barrelorgandiscovery.playsubsystem.registry.IPlaySubSystemRegistryExtensionPoint;
import org.barrelorgandiscovery.playsubsystem.registry.PlaySubSystemDef;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Collection;
import org.barrelorgandiscovery.repository.Repository2Factory;

public class SynthExtension implements IExtension, IPlaySubSystemRegistryExtensionPoint, InitNGExtensionPoint {

	private Logger logger = Logger.getLogger(SynthExtension.class);

	@Override
	public String getName() {
		return "Native Synthetizer Extension";
	}

	APrintNG host;

	@Override
	public void init(APrintNG f) {
		host = f;
	}

	@Override
	public ExtensionPoint[] getExtensionPoints() {
		ArrayList<ExtensionPoint> exts = new ArrayList<>();
		try {
			SimpleExtensionPoint a = new SimpleExtensionPoint(InitNGExtensionPoint.class, this);
			exts.add(a);

			SimpleExtensionPoint e = new SimpleExtensionPoint(IPlaySubSystemRegistryExtensionPoint.class, this);

			exts.add(e);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return exts.toArray(new ExtensionPoint[0]);

	}

	/**
	 * @param editableRepository
	 * @return
	 */
	protected EditableInstrumentManagerRepository2Adapter findEditableInstrumentManagerRepository(
			List<EditableInstrumentManagerRepository2Adapter> editableRepository) {
		EditableInstrumentManagerRepository2Adapter eira = null;
		for (Iterator iterator = editableRepository.iterator(); iterator.hasNext();) {
			EditableInstrumentManagerRepository2Adapter editableInstrumentManagerRepository2Adapter = (EditableInstrumentManagerRepository2Adapter) iterator
					.next();

			logger.debug("evaluating " //$NON-NLS-1$
					+ editableInstrumentManagerRepository2Adapter.getName());
			if ((Repository2Factory.PERSONAL_EDITABLE_INSTRUMENTS + "private") //$NON-NLS-1$
					.equals(editableInstrumentManagerRepository2Adapter.getName())) {
				eira = editableInstrumentManagerRepository2Adapter;
				break;
			}
		}
		return eira;
	}

	@Override
	public PlaySubSystemDef[] getPlaySubSystems() {

		ArrayList<PlaySubSystemDef> pl = new ArrayList<PlaySubSystemDef>();

		Repository2 ra = (Repository2) host.getRepository();

		if (ra instanceof Repository2Collection) {
			Repository2Collection col = (Repository2Collection) ra;
			List<EditableInstrumentManagerRepository2Adapter> editableRepository = col
					.findRepository(EditableInstrumentManagerRepository2Adapter.class);

			EditableInstrumentManagerRepository2Adapter eira = findEditableInstrumentManagerRepository(
					editableRepository);

			if (eira != null) {
				PlaySubSystemDef pss = new PlaySubSystemDef();
				pss.name = "Synthetizer Extension";

				pss.playSubSystem = new SynthPlaySubSystem(eira);

				pl.add(pss);
			}
		}

		return pl.toArray(new PlaySubSystemDef[0]);
	}
}
