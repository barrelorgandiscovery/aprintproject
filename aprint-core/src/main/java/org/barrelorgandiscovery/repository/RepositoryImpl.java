package org.barrelorgandiscovery.repository;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.instrument.InstrumentManager;
import org.barrelorgandiscovery.instrument.StorageInstrumentManager;
import org.barrelorgandiscovery.scale.ScaleManager;
import org.barrelorgandiscovery.scale.StorageScaleManager;
import org.barrelorgandiscovery.tools.streamstorage.FolderStreamStorage;
import org.barrelorgandiscovery.tools.streamstorage.StreamStorage;
import org.barrelorgandiscovery.tools.streamstorage.ZipStreamStorage;
import org.barrelorgandiscovery.virtualbook.transformation.StorageTransformationManager;
import org.barrelorgandiscovery.virtualbook.transformation.TransformationManager;


public class RepositoryImpl implements Repository {

	private static final Logger logger = Logger.getLogger(RepositoryImpl.class);

	/**
	 * Gestionnaire de gamme
	 */
	private ScaleManager gm;

	/**
	 * Gestionnaire de transpositions
	 */
	private TransformationManager tm;

	/**
	 * Gestionnaire d'instruments
	 */
	private InstrumentManager im;

	/**
	 * Name of the repository
	 */
	private String name = "unknown";
	
	/**
	 * label of the repository
	 */
	private String label;

	/**
	 * Constructeur
	 * 
	 * @param folder
	 * @throws Exception
	 */
	RepositoryImpl(Properties props) throws RepositoryException {
		try {

			if (props == null)
				throw new IllegalArgumentException();

			StreamStorage fis;

			String folderpath = props.getProperty("folder"); //$NON-NLS-1$
			if (folderpath == null) {
				logger.debug("utilisation du repository built in"); //$NON-NLS-1$
				this.name = "builtin";
				this.label = props.getProperty("label",name);

				InputStream zipstream = getClass().getClassLoader()
						.getResourceAsStream("gammes.zip"); //$NON-NLS-1$
				if (zipstream == null)
					throw new RepositoryException(
							"zip gamme resource not found"); //$NON-NLS-1$

				fis = new ZipStreamStorage(zipstream);

			} else {
				File folder = new File(folderpath);

				this.name = "Local :" + folderpath;
				this.label = props.getProperty("label",name);

				fis = new FolderStreamStorage(folder);
				if (!folder.exists() || !folder.isDirectory())
					throw new RepositoryException("Bad folder"); //$NON-NLS-1$
			}

			gm = new StorageScaleManager(fis);
			tm = new StorageTransformationManager(fis, gm);
			im = new StorageInstrumentManager(fis, gm);

		} catch (Exception ex) {
			throw new RepositoryException(ex);
		}
	}

	/**
	 * Récupère l'instance du gestionnaire de gammes
	 */
	public ScaleManager getScaleManager() {
		return gm;
	}

	/**
	 * Récupère l'instance du gestionnaire d'instrument
	 */
	public InstrumentManager getInstrumentManager() {
		return im;
	}

	/**
	 * Récupère l'instance du gestionnaire de transposition
	 */
	public TransformationManager getTranspositionManager() {
		return tm;
	}

	private String[] errormessages = null;

	public String[] getErrorMessages() {
		return errormessages;
	}

	public String getName() {
		return this.name;
	}
	
	@Override
	public String getLabel() {
		return label;
	}

}
