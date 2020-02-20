package org.barrelorgandiscovery.gaerepositoryclient;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository2Adapter;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.gaerepositoryclient.synchroreport.MessageSynchroElement;
import org.barrelorgandiscovery.gaerepositoryclient.synchroreport.SynchroElement;
import org.barrelorgandiscovery.gaerepositoryclient.synchroreport.SynchronizationReport;
import org.barrelorgandiscovery.gui.ascale.ScaleComponent;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.tools.streamstorage.FolderStreamStorage;
import org.barrelorgandiscovery.tools.streamstorage.StreamStorage;

@Deprecated
public class GAESynchronizedRepository2 extends
		AbstractEditableInstrumentRepository implements Disposable {

	private static Logger logger = Logger
			.getLogger(GAESynchronizedRepository2.class);

	private URL repositoryRootUrl = null;

	private String user = null;

	private String password = null;

	protected StreamStorage folderStreamStorage;

	public GAESynchronizedRepository2(File syncFolder, URL repositoryRootURL,
			String user, String password) throws Exception {

		logger.debug("construct GAESynchronizedRepository2 to " //$NON-NLS-1$
				+ repositoryRootURL);
		logger.debug("user : " + user); //$NON-NLS-1$

		changeRepositoryConnectionInfos(repositoryRootURL, user, password);

		folderStreamStorage = new FolderStreamStorage(syncFolder);

		init(new VersionedEditableInstrumentManager(folderStreamStorage),
				Messages.getString("GAESynchronizedRepository2.2"), 
				Messages.getString("GAESynchronizedRepository2.2"),
				new File(syncFolder.getAbsolutePath() + ".cache")); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public void changeRepositoryConnectionInfos(URL repositoryRootURL,
			String user, String password) {
		this.repositoryRootUrl = repositoryRootURL;
		this.user = user;
		this.password = password;
	}

	private class LocalInstrument {

		/**
		 * identifiant de l'instrument
		 */
		public long id;
		/**
		 * version de l'instrument
		 */
		public long version;
		/**
		 * nom de l'instrument tel que dans le fichier instrumentbundle
		 */
		public String name;
		/**
		 * nom complet du fichier instrument
		 */

		public String streamName;

		/**
		 * Status of the local instrument (new, updated, deleted, fetched)
		 */
		public String status;
	}

	/**
	 * Synchronise le repository
	 * 
	 * @param feedBack
	 * @throws Exception
	 */
	public SynchronizationReport synchronizeRepository(
			SynchronizationFeedBack feedBack) throws Exception {

		SynchronizationReport report = new SynchronizationReport();

		// get all the instruments definition from the repository

		if (feedBack != null)
			feedBack.inform(
					Messages.getString("GAESynchronizedRepository2.0"), 0); //$NON-NLS-1$

		APrintRepositoryClientConnection c = new APrintRepositoryClientConnection();

		c.connect(repositoryRootUrl.toString(), user, password);

		RepositoryInstrument[] listInstruments = c.listInstruments();

		if (feedBack != null)
			feedBack.inform(
					Messages.getString("GAESynchronizedRepository2.47"), 0); //$NON-NLS-1$

		HashMap<Long, RepositoryInstrument> repositoryInstrumentsByID = new HashMap<Long, RepositoryInstrument>();
		for (int i = 0; i < listInstruments.length; i++) {
			RepositoryInstrument repositoryInstrument = listInstruments[i];
			repositoryInstrumentsByID.put(repositoryInstrument.getId(),
					repositoryInstrument);
		}

		// get all local instruments definition

		if (feedBack != null)
			feedBack.inform(
					Messages.getString("GAESynchronizedRepository2.3"), 0); //$NON-NLS-1$

		HashMap<String, DecodedInstrumentRef> readDecodedStreamReferences = getVersionedManager()
				.readDecodedStreamReferences();

		logger.debug("list instruments to update ..."); //$NON-NLS-1$

		for (Iterator iterator = readDecodedStreamReferences.entrySet()
				.iterator(); iterator.hasNext();) {
			Entry<String, DecodedInstrumentRef> entry = (Entry<String, DecodedInstrumentRef>) iterator
					.next();

			DecodedInstrumentRef decoded = entry.getValue();
			String editableInstrumentReference = entry.getKey();

			logger.debug("check instrument :" + decoded); //$NON-NLS-1$

			if ("U".equals(decoded.status)) { //$NON-NLS-1$
				logger.debug("instrument has been locally updated, update the repository .."); //$NON-NLS-1$

				if (feedBack != null)
					feedBack.inform(
							Messages.getString("GAESynchronizedRepository2.8") + editableInstrumentReference, 0.0); //$NON-NLS-1$

				RepositoryInstrument ri = repositoryInstrumentsByID
						.get(decoded.instrumentID);
				if (ri == null) {

					logger.error("instrument " + ri.getName() //$NON-NLS-1$
							+ " not found in repository"); //$NON-NLS-1$

					report.add(new MessageSynchroElement(SynchroElement.ERROR,
							Messages.getString("GAESynchronizedRepository2.48"))); //$NON-NLS-1$

					continue;
				}

				try {
					int stream = c.putStream("/streams", folderStreamStorage //$NON-NLS-1$
							.openStream(decoded.fullStreamName),
							c.APPLICATION_OCTET_STREAM);

					int imageStream;

					imageStream = pushInstrumentThumbnail(c,
							editableInstrumentReference);

					c.updateInstrument((Long) decoded.instrumentID,
							decoded.name, "", (long) stream, (long) imageStream); //$NON-NLS-1$

					logger.debug("removing local modifications"); //$NON-NLS-1$

					folderStreamStorage
							.deleteStream(
									StringTools
											.removeExtension(
													decoded.fullStreamName,
													VersionedEditableInstrumentManager.SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE),
									VersionedEditableInstrumentManager.SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE);

				} catch (Exception ex) {
					logger.error("error while updating an instrument :" //$NON-NLS-1$
							+ ex.getMessage(), ex);

					report.add(new MessageSynchroElement(SynchroElement.ERROR,
							Messages.getString("GAESynchronizedRepository2.50") //$NON-NLS-1$
									+ decoded.name));

				}

			} else if ("N".equals(decoded.status)) { //$NON-NLS-1$

				if (feedBack != null)
					feedBack.inform(
							Messages.getString("GAESynchronizedRepository2.15") + editableInstrumentReference, 0.0); //$NON-NLS-1$

				try {

					logger.debug("put instrument stream"); //$NON-NLS-1$
					int stream = c.putStream("/streams", folderStreamStorage //$NON-NLS-1$
							.openStream(decoded.fullStreamName),
							c.APPLICATION_OCTET_STREAM);

					int imageStream = pushInstrumentThumbnail(c,
							editableInstrumentReference);

					logger.debug("creating instrument definition"); //$NON-NLS-1$
					int instrumentid = c.createInstrument(decoded.name, ""); //$NON-NLS-1$

					logger.debug("update instrument"); //$NON-NLS-1$

					c.updateInstrument(instrumentid, decoded.name, "", //$NON-NLS-1$
							(long) stream, (long) imageStream);

					logger.debug("removing local modifications"); //$NON-NLS-1$
					folderStreamStorage
							.deleteStream(
									StringTools
											.removeExtension(
													decoded.fullStreamName,
													VersionedEditableInstrumentManager.SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE),
									VersionedEditableInstrumentManager.SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE);

				} catch (Exception ex) {
					logger.error("error while creating an instrument :" //$NON-NLS-1$
							+ ex.getMessage(), ex);

					report.add(new MessageSynchroElement(SynchroElement.ERROR,
							Messages.getString("GAESynchronizedRepository2.52") //$NON-NLS-1$
									+ decoded.name));
				}

			} else if ("D".equals(decoded.status)) { //$NON-NLS-1$

				if (feedBack != null)
					feedBack.inform(
							Messages.getString("GAESynchronizedRepository2.24") + editableInstrumentReference, 0.0); //$NON-NLS-1$

				try {
					logger.debug("deleting instrument " + decoded.instrumentID); //$NON-NLS-1$
					c.deleteInstrument(decoded.instrumentID);

					logger.debug("removing local modifications"); //$NON-NLS-1$
					folderStreamStorage
							.deleteStream(
									StringTools
											.removeExtension(
													decoded.fullStreamName,
													VersionedEditableInstrumentManager.SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE),
									VersionedEditableInstrumentManager.SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE);
				} catch (Exception ex) {
					logger.error("error while deleting instrument :" //$NON-NLS-1$
							+ ex.getMessage(), ex);

					report.add(new MessageSynchroElement(SynchroElement.ERROR,
							Messages.getString("GAESynchronizedRepository2.54") //$NON-NLS-1$
									+ decoded.name));
				}
			}

		} // for

		logger.debug("local modification pushed to the repository ..."); //$NON-NLS-1$

		if (feedBack != null)
			feedBack.inform(
					Messages.getString("GAESynchronizedRepository2.55"), 0.0); //$NON-NLS-1$

		logger.debug("fetching the new instruments from repository ..."); //$NON-NLS-1$

		listInstruments = c.listInstruments();

		logger.debug("reading local instruments"); //$NON-NLS-1$
		readDecodedStreamReferences = getVersionedManager()
				.readDecodedStreamReferences();

		HashMap<Long, DecodedInstrumentRef> localInstrumentsByID = new HashMap<Long, DecodedInstrumentRef>();
		for (Iterator iterator = readDecodedStreamReferences.entrySet()
				.iterator(); iterator.hasNext();) {
			Entry<String, DecodedInstrumentRef> entry = (Entry<String, DecodedInstrumentRef>) iterator
					.next();
			DecodedInstrumentRef d = entry.getValue();
			localInstrumentsByID.put(d.instrumentID, d);
		}

		ArrayList<DecodedInstrumentRef> streamsToDelete = new ArrayList<DecodedInstrumentRef>();

		for (int i = 0; i < listInstruments.length; i++) {
			RepositoryInstrument repositoryInstrument = listInstruments[i];

			if (feedBack != null)
				feedBack.inform(
						Messages.getString("GAESynchronizedRepository2.30") //$NON-NLS-1$
								+ listInstruments[i].getName(), (double) i
								/ listInstruments.length);

			if (localInstrumentsByID.containsKey(repositoryInstrument.getId())) {
				// local instrument exist ..
				logger.debug("local instrument exist ... "); //$NON-NLS-1$

				DecodedInstrumentRef decodedInstrumentRef = localInstrumentsByID
						.get(repositoryInstrument.getId());

				if (decodedInstrumentRef.version >= repositoryInstrument
						.getVersion()
						&& "F".equals(decodedInstrumentRef.status)) { //$NON-NLS-1$
					// nothing to do ..
					logger.debug("instrument  " + decodedInstrumentRef.name //$NON-NLS-1$
							+ " up to date"); //$NON-NLS-1$
					localInstrumentsByID
							.remove(decodedInstrumentRef.instrumentID);
					continue;
				} else if ("U".equals(decodedInstrumentRef.status) //$NON-NLS-1$
						|| "N".equals(decodedInstrumentRef.status)) { //$NON-NLS-1$
					localInstrumentsByID
							.remove(decodedInstrumentRef.instrumentID);
					continue;
				}

				logger.debug("stream " + decodedInstrumentRef.fullStreamName //$NON-NLS-1$
						+ " mark for remove"); //$NON-NLS-1$
				streamsToDelete.add(decodedInstrumentRef);

				localInstrumentsByID.remove(decodedInstrumentRef.instrumentID);

			}

			if (feedBack != null)
				feedBack.inform(
						Messages.getString("GAESynchronizedRepository2.37") //$NON-NLS-1$
								+ listInstruments[i].getName(), (double) i
								/ listInstruments.length);

			logger.debug("try to import instrument " //$NON-NLS-1$
					+ repositoryInstrument.getId());

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			try {

				c.getContentStream(repositoryInstrument, baos);

				// write to local ...

				if (feedBack != null)
					feedBack.inform(
							Messages.getString("GAESynchronizedRepository2.39") //$NON-NLS-1$
									+ listInstruments[i].getName(), (double) i
									/ listInstruments.length);

				folderStreamStorage
						.saveStream(
								"F_" //$NON-NLS-1$
										+ repositoryInstrument.getId()
										+ "_" //$NON-NLS-1$
										+ repositoryInstrument.getVersion()
										+ "_" //$NON-NLS-1$
										+ StringTools
												.convertToPhysicalName(repositoryInstrument
														.getName()),
								VersionedEditableInstrumentManager.SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE,
								new ByteArrayInputStream(baos.toByteArray()));

				logger.debug("instrument " + repositoryInstrument //$NON-NLS-1$
						+ " synchronized ... "); //$NON-NLS-1$
			} catch (Exception ex) {

				logger.error("error while saving instrument :" //$NON-NLS-1$
						+ ex.getMessage(), ex);

				report.add(new MessageSynchroElement(SynchroElement.ERROR,
						Messages.getString("GAESynchronizedRepository2.59") //$NON-NLS-1$
								+ repositoryInstrument.getName()));
			}

		}

		for (Iterator iterator = localInstrumentsByID.entrySet().iterator(); iterator
				.hasNext();) {
			Entry<Long, DecodedInstrumentRef> decodedInstrumentRef2 = (Entry<Long, DecodedInstrumentRef>) iterator
					.next();
			streamsToDelete.add(decodedInstrumentRef2.getValue());
		}

		for (Iterator iterator = streamsToDelete.iterator(); iterator.hasNext();) {
			DecodedInstrumentRef decodedInstrumentRef = (DecodedInstrumentRef) iterator
					.next();

			try {

				String fullStreamName = decodedInstrumentRef.fullStreamName;
				folderStreamStorage
						.deleteStream(
								StringTools
										.removeExtension(
												fullStreamName,
												VersionedEditableInstrumentManager.SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE),
								VersionedEditableInstrumentManager.SYNCHRONIZED_EDITABLE_INSTRUMENT_TYPE);

			} catch (Exception ex) {
				logger.error("fail to remove " + ex.getMessage(), ex); //$NON-NLS-1$
			}

		}

		if (feedBack != null)
			feedBack.inform(
					Messages.getString("GAESynchronizedRepository2.46"), 1.0); //$NON-NLS-1$

		getVersionedManager().fireInstrumentsChanged();

		return report;
	}

	private VersionedEditableInstrumentManager getVersionedManager() {
		return (VersionedEditableInstrumentManager) getManager();
	}

	private int pushInstrumentThumbnail(APrintRepositoryClientConnection c,
			String editableInstrumentReference) throws Exception, IOException {
		int imageStream;
		IEditableInstrument loadEditableInstrument = getManager()
				.loadEditableInstrument(editableInstrumentReference);
		try {
			Image img = loadEditableInstrument.getInstrumentPicture();

			if (img == null) {
				// create a thumbnail of the scale ...
				BufferedImage scaleImage = ScaleComponent
						.createScaleImage(loadEditableInstrument.getScale());
				img = scaleImage;
			}

			ByteArrayOutputStream imageByteArrayOutputStream;
			File tempFile = File.createTempFile("temp", "png"); //$NON-NLS-1$ //$NON-NLS-2$
			try {

				ImageIO.write((BufferedImage) img, "PNG", new FileOutputStream( //$NON-NLS-1$
						tempFile));
				BufferedImage r = ImageTools.loadImageAndCrop(tempFile, 250,
						250);

				imageByteArrayOutputStream = new ByteArrayOutputStream();
				ImageIO.write(r, "PNG", imageByteArrayOutputStream); //$NON-NLS-1$

			} finally {
				tempFile.delete();
			}

			imageStream = c.putStream("/streams", new ByteArrayInputStream( //$NON-NLS-1$
					imageByteArrayOutputStream.toByteArray()), "image/png"); //$NON-NLS-1$

		} finally {
			loadEditableInstrument.dispose();
		}
		return imageStream;
	}

}
