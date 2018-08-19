package org.barrelorgandiscovery.repository.httpxmlrepository;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.StreamStorageEditableInstrumentManager;
import org.barrelorgandiscovery.gaerepositoryclient.AbstractEditableInstrumentRepository;
import org.barrelorgandiscovery.tools.streamstorage.FolderStreamStorage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Repository associated to the getting of instruments on a remote site this
 * repository list the instrument present in a xml file and get only the wishes
 * instruments
 * 
 * @author pfreydiere
 * 
 * @since 2011.6.prerelease.145
 * 
 */
public class HttpXmlRepository extends AbstractEditableInstrumentRepository {

	private static Logger logger = Logger.getLogger(HttpXmlRepository.class);

	

	public static class InstrumentDefinition {

		public String label;
		public String filename;
		public String sha1;

	}

	private String root;
	private FolderStreamStorage fst;
	private String displayName;

	/**
	 * Constuct the HttpXmlRepository
	 * 
	 * @param folder
	 *            the main folder in which the instruments are stored
	 * @param httpRootUrl
	 *            the xml url for the instruments
	 * @param cacheFolder
	 *            the main cache folder for internal optimizations
	 * @throws Exception
	 */
	public HttpXmlRepository(File folder, String httpRootUrl, File cacheFolder,
			String displayName) throws Exception {
		this.displayName = displayName;
		
		fst = new FolderStreamStorage(folder);

		StreamStorageEditableInstrumentManager eis = new StreamStorageEditableInstrumentManager(
				fst);

		init(eis, "Http Repository" /*Name*/, displayName, cacheFolder);

		if (httpRootUrl == null)
			throw new Exception("bad httprooturl parameters");

		this.root = httpRootUrl;

	}

	@Override
	public String getName() {
		return super.getName() + " - " + displayName;
	}
	
	

	/**
	 * Return the instrument definition list
	 * 
	 * @return
	 * @throws Exception
	 */
	public InstrumentDefinition[] getInstruments() throws Exception {

		HttpClient c = new HttpClient();

		String httpurlinstrument = root + "/instruments.xml";

		GetMethod gm = new GetMethod(httpurlinstrument);
		try {
			int status = c.executeMethod(gm);

			if (status != 200)
				throw new Exception(
						"error in getting the instruments.xml file at "
								+ httpurlinstrument);

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder docb = dbf.newDocumentBuilder();

			Document doc = docb.parse(gm.getResponseBodyAsStream());

			ArrayList<InstrumentDefinition> retvalue = new ArrayList<InstrumentDefinition>();
			Element doce = doc.getDocumentElement();

			if (doce != null) {
				NodeList childs = doce.getChildNodes();
				for (int i = 0; i < childs.getLength(); i++) {
					Node item = childs.item(i);
					if (item instanceof Element) {
						Element ei = (Element) item;
						String label = ei.getAttribute("name");
						String file = ei.getAttribute("path");
						String sha1 = ei.getAttribute("sha1");

						InstrumentDefinition idef = new InstrumentDefinition();
						idef.filename = file;
						idef.sha1 = sha1;
						idef.label = label;

						retvalue.add(idef);

						if (logger.isDebugEnabled())
							logger.debug("instrument definition read :" + idef);

					}
				}
			}

			return retvalue.toArray(new InstrumentDefinition[0]);

		} finally {
			gm.releaseConnection();
		}
	}

	/**
	 * 
	 * @param names
	 * @throws Exception
	 */
	public void downloadInstruments(InstrumentDefinition[] instrumentsToDownload) throws Exception {

		HttpClient c = new HttpClient();

		for (int i = 0; i < instrumentsToDownload.length; i++) {

			String ftg = instrumentsToDownload[i].filename;
			logger.debug("get " + ftg);
			try {
				URL u = new URL(root + "/" + ftg);

				String f = u.getFile();

				GetMethod gm = new GetMethod(u.toString());
				try {
					int status = c.executeMethod(gm);

					if (status != 200)
						throw new Exception("error in getting instrument " + f);

					logger.debug("Saving instrument to local storage");
					fst.saveStream(
							ftg,
							StreamStorageEditableInstrumentManager.EDITABLE_INSTRUMENT_TYPE,
							gm.getResponseBodyAsStream());

					adapter.signalInstrumentChanged();
					
				} finally {
					gm.releaseConnection();
				}

				

			} catch (Exception ex) {
				logger.error(
						"Error in getting " + ftg + ": " + ex.getMessage(), ex);
			}
		}
		
	
		
		// fire instruments changed

		adapter.reloadEditableInstruments();
		fireInstrumentsChanged();
	}

	/**
	 * Return the http root url
	 * 
	 * @return
	 */
	public String getHttpRootUrl() {
		return this.root;
	}
}
