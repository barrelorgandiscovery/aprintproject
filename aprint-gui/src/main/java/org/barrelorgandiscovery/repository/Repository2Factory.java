package org.barrelorgandiscovery.repository;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository2Adapter;
import org.barrelorgandiscovery.editableinstrument.StreamStorageEditableInstrumentManager;
import org.barrelorgandiscovery.gaerepositoryclient.GAESynchronizedRepository2;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.repository.httpxmlrepository.HttpXmlRepository;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.tools.streamstorage.FolderStreamStorage;
import org.barrelorgandiscovery.tools.streamstorage.ReadOnlyOptimizedZipStream;
import org.barrelorgandiscovery.tools.streamstorage.StreamStorage;

/**
 * Factory for creating a repository ...
 * 
 * @author Freydiere Patrice
 */
public class Repository2Factory {

	private static final String BWEBREPOSITORY = "bwebrepository"; //$NON-NLS-1$
	private static final String HTTP_WWW_BARREL_ORGAN_DISCOVERY_ORG_BUILTDIN = "http://www.barrel-organ-discovery.org/builtin"; //$NON-NLS-1$
	public static final String PERSONAL_EDITABLE_INSTRUMENTS = "Personal Editable Instruments :"; //$NON-NLS-1$
	private static Logger logger = Logger.getLogger(Repository2Factory.class);

	/**
	 * Create a Repository2 using parameters
	 * 
	 * @param props
	 * @param aprintProperties
	 * @return
	 * @throws RepositoryException
	 */
	public static Repository2 create(Properties props,
			APrintProperties aprintProperties) throws RepositoryException {

		Repository2Collection col = new Repository2Collection(
				"repository collection"); //$NON-NLS-1$

		logger.debug("adding builtin repository ..."); //$NON-NLS-1$

		// InputStream zipstream = Repository2Factory.class.getClassLoader()
		//				.getResourceAsStream("gammes.zip"); //$NON-NLS-1$
		// if (zipstream == null)
		//			throw new RepositoryException("zip gamme resource not found"); //$NON-NLS-1$

		// try {
		// StreamStorage ss = new ReadOnlyOptimizedZipStream(
		// new ZipInputStream(zipstream));
		//
		// col.addRepository(new StorageRepositoryImpl(ss, "builtin"));
		// } catch (Exception ex) {
		// logger.error(ex.getMessage(), ex);
		// throw new RepositoryException(ex.getMessage(), ex);
		// }

		String type = props.getProperty("repositorytype", "folder"); //$NON-NLS-1$ //$NON-NLS-2$

		if ("folder".equals(type)) { //$NON-NLS-1$
			logger.debug("loading"); //$NON-NLS-1$
			String folder = props.getProperty("folder"); //$NON-NLS-1$

			File theFolder = null;

			if (folder != null) {
				// adding a folder repository ...

				theFolder = new File(folder);

			} else {
				logger.debug("create a private folder for editing ..."); //$NON-NLS-1$

				File privateFolder = new File(
						aprintProperties.getAprintFolder(), "private"); //$NON-NLS-1$
				privateFolder.mkdir();
				theFolder = privateFolder;

			}

			try {
				// adding folder repository
				addFolderRepositoryToCollection(col, theFolder);

			} catch (Exception ex) {
				logger.error(
						"fail to add personal repository :" + ex.getMessage(), //$NON-NLS-1$
						ex);
			}
		}

		File aprintFolder = aprintProperties.getAprintFolder();
		assert aprintFolder != null;

		

		{
			File f = new File(aprintFolder, BWEBREPOSITORY);
			if (!f.exists())
				f.mkdirs();
			
			File fcache = new File(aprintFolder, BWEBREPOSITORY + ".cache"); //$NON-NLS-1$
			if (!f.exists())
				f.mkdirs();
			
			try {
				HttpXmlRepository h = new HttpXmlRepository(f,
						HTTP_WWW_BARREL_ORGAN_DISCOVERY_ORG_BUILTDIN, fcache,
						Messages.getString("Repository2Factory.0")); //$NON-NLS-1$

				logger.info("Adding bod repository"); //$NON-NLS-1$
				col.addRepository(h);

			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}

		logger.debug("look for additional web repositories"); //$NON-NLS-1$

		File additionalWebRepositories = new File(aprintFolder,
				"additionalrepositories"); //$NON-NLS-1$
		if (!additionalWebRepositories.exists())
			additionalWebRepositories.mkdir();

		try {

			int c = aprintProperties.getAdditionalWebRepositoriesCount();
			for (int i = 0; i < c; i++) {
				String name = aprintProperties
						.getAdditionalWebRepositoriesName(i);

				String url = aprintProperties
						.getAdditionalWebRepositoriesUrl(i);
				String foldername = StringTools.convertToPhysicalName(name);
				logger.debug("adding additional web repository " + name //$NON-NLS-1$
						+ " at " + url + " in " + foldername); //$NON-NLS-1$ //$NON-NLS-2$

				assert name != null;
				assert url != null;

				File f = new File(additionalWebRepositories, foldername);
				if (!f.exists())
					f.mkdir();

				HttpXmlRepository h = new HttpXmlRepository(f, url, null, name);

				logger.debug("adding repository " + h.getName() //$NON-NLS-1$
						+ " associated to " + url); //$NON-NLS-1$
				col.addRepository(h);
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

		return col;

		// throw new RepositoryException("unsupported Repository type " + type);
		// //$NON-NLS-1$
	}

	private static void addFolderRepositoryToCollection(
			Repository2Collection col, File theFolder) throws Exception,
			RepositoryException {

		
		FolderStreamStorage folderStreamStorage = new FolderStreamStorage(
				theFolder);
//		col.addRepository(new StorageRepositoryImpl(folderStreamStorage,
//				"Personal :" + theFolder.getName()));

		StreamStorageEditableInstrumentManager eis = new StreamStorageEditableInstrumentManager(
				folderStreamStorage);

		File cacheFolder = new File(theFolder.getParentFile(),
				theFolder.getName() + ".cache"); //$NON-NLS-1$

		col.addRepository(new EditableInstrumentManagerRepository2Adapter(eis,
				PERSONAL_EDITABLE_INSTRUMENTS + theFolder.getName(),
				Messages.getString("Repository2Factory.19") + theFolder.getName(), //$NON-NLS-1$
				cacheFolder));
	}
}
