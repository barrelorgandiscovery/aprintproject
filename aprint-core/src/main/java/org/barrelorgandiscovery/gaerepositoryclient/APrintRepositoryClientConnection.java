package org.barrelorgandiscovery.gaerepositoryclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.ArrayList;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.xmlbeans.impl.util.Base64;
import org.barrelorgandiscovery.tools.StreamsTools;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 */
@Deprecated
public class APrintRepositoryClientConnection {

	public static final String APPLICATION_OCTET_STREAM = "application/octet-stream"; //$NON-NLS-1$
	private static Logger logger = Logger
			.getLogger(APrintRepositoryClientConnection.class);

	public APrintRepositoryClientConnection() {

	}

	private HttpClient httpclient;
	private String baseUrl = null;
	private String user = null;
	private String password = null;

	public void connect(String url, String user, String password)
			throws Exception {
		this.httpclient = new HttpClient();
		HttpConnectionManager cnxmgr = this.httpclient.getHttpConnectionManager();
		cnxmgr.getParams().setConnectionTimeout(60000);
		
		
		this.baseUrl = url;
		if (!"".equals(user) && user != null) {
			this.user = user;
			this.password = password;
		}

	}

	/**
	 * List instruments
	 * @return
	 * @throws Exception
	 */
	public RepositoryInstrument[] listInstruments() throws Exception {

		JSONArray array = (JSONArray) handleMessage("/instruments", METHOD_GET, //$NON-NLS-1$
				null);

		ArrayList<RepositoryInstrument> retvalue = new ArrayList<RepositoryInstrument>();

		for (int i = 0; i < array.length(); i++) {
			JSONObject o = (JSONObject) array.get(i);
			retvalue.add(new RepositoryInstrument((String) o.get("name"), //$NON-NLS-1$
					(Integer) o.get("id"), (Integer) o.get("version"), //$NON-NLS-1$ //$NON-NLS-2$
					((String) o.get("tags")).split(","))); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return retvalue.toArray(new RepositoryInstrument[0]);
	}

	/**
	 * send a stream to the server
	 * 
	 * @param streamsEndPoint
	 * @param is
	 * @param contentType
	 * @return
	 * @throws Exception
	 */
	public int putStream(String streamsEndPoint, InputStream is,
			String contentType) throws Exception {

		File tempFile = File.createTempFile("stream", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
		try {

			FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
			StreamsTools.copyStream(is, fileOutputStream);
			fileOutputStream.close();

			String md5 = "unknown"; //$NON-NLS-1$

			FileInputStream fisDigestCompute = new FileInputStream(tempFile);
			try {

				MessageDigest md5digest = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
				int cpt;
				byte[] buffer = new byte[4096];

				md5digest.reset();
				while ((cpt = fisDigestCompute.read(buffer)) != -1) {
					md5digest.update(buffer, 0, cpt);
				}

				md5 = new String(Base64.encode(md5digest.digest()));

				logger.debug("md5 :" + md5); //$NON-NLS-1$

			} finally {
				fisDigestCompute.close();
			}

			JSONObject newStream = new JSONObject();
			newStream.put("md5", md5); //$NON-NLS-1$
			newStream.put("size", tempFile.length()); //$NON-NLS-1$
			newStream.put("contenttype", contentType); //$NON-NLS-1$

			Object wrResult = handleMessage(streamsEndPoint + "/new", //$NON-NLS-1$
					METHOD_PUT, newStream);
			logger.debug("wr response :" + wrResult); //$NON-NLS-1$

			if (!(wrResult instanceof Long) && !(wrResult instanceof Integer))
				throw new Exception("Cannot create Stream"); //$NON-NLS-1$

			int retvalue = (Integer) wrResult;

			RandomAccessFile raf = new RandomAccessFile(tempFile, "r"); //$NON-NLS-1$
			try {

				long length = raf.length();

				int chunksize = 100000;
				long nbchunks = length / chunksize;

				if (chunksize * nbchunks < length)
					nbchunks += 1;

				byte[] b = new byte[chunksize];
				for (int i = 0; i < nbchunks; i++) {
					raf.seek(chunksize * i);
					int cpt = raf.read(b);

					JSONObject chunk = new JSONObject();
					java.util.Base64.Encoder be = java.util.Base64.getEncoder();
					// BASE64Encoder be = new BASE64Encoder();
					String encoded = null;
					if (cpt == b.length) {
						encoded = new String(be.encode(b));
					} else {
						byte[] nba = new byte[cpt];
						System.arraycopy(b, 0, nba, 0, cpt);
						encoded = new String(be.encode(nba));
					}
					chunk.put("content", encoded); //$NON-NLS-1$

					if (!"OK".equals(handleMessage(streamsEndPoint + "/" //$NON-NLS-1$ //$NON-NLS-2$
							+ retvalue + "/stream/chunks/" + i, METHOD_PUT, //$NON-NLS-1$
							chunk)))
						throw new Exception("cannot put chunk " + i); //$NON-NLS-1$

				}

			} finally {
				raf.close();
			}

			return retvalue;

		} finally {
			// tempFile.delete();
		}

	}

	private final static int METHOD_GET = 0;
	private final static int METHOD_PUT = 1;
	private final static int METHOD_DELETE = 2;

	protected Object handleMessage(String operation, int method,
			JSONObject message) throws Exception {

		HttpMethodBase m = null;
		String url = baseUrl + operation;

		url += getQueryAuthentication();

		logger.debug("sending to " + url); //$NON-NLS-1$

		if (method == METHOD_GET) {
			m = new GetMethod(url);

		} else if (method == METHOD_DELETE) {
			m = new DeleteMethod(url);
		} else if (method == METHOD_PUT) {

			EntityEnclosingMethod em;
			em = new PutMethod(url);

			if (message != null) {
				String jsonmessage = message.toString();
				em.setRequestEntity(new StringRequestEntity(jsonmessage));
			}
			m = em;
		}

		
		if (m == null)
			throw new Exception("unsupported method :" + method); //$NON-NLS-1$

		int status = httpclient.executeMethod(m);
		if (status != 200)
			throw new Exception("error in request " + status); //$NON-NLS-1$
		logger.debug("status " + status); //$NON-NLS-1$

		String responseBodyAsString = m.getResponseBodyAsString();

		logger.debug(responseBodyAsString);

		org.json.JSONTokener tokener = new org.json.JSONTokener(
				responseBodyAsString);

		JSONObject jsonresponse = new JSONObject(tokener);

		return jsonresponse.get("result"); //$NON-NLS-1$

	}

	/**
	 * Get the authentication part for connecting to the repository
	 * 
	 * @return
	 */
	private String getQueryAuthentication() {
		if (user != null) {
			return "?login=" + user + "&password=" + password; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return ""; //$NON-NLS-1$
	}

	public void getContentStream(RepositoryInstrument instrument,
			OutputStream os) throws Exception {

		logger.debug("getContentStream on " + instrument); //$NON-NLS-1$

		GetMethod gm = new GetMethod(baseUrl + "/instruments/" //$NON-NLS-1$
				+ instrument.getId() + "/content" + getQueryAuthentication()); //$NON-NLS-1$

		logger.debug("execute request ... on " + gm.getURI()); //$NON-NLS-1$
		int status = httpclient.executeMethod(gm);

		logger.debug("request done ! status :" + status + " " //$NON-NLS-1$ //$NON-NLS-2$
				+ gm.getStatusLine().toString());

		if (status != 200)
			throw new Exception("fail to get instrument content  ..."); //$NON-NLS-1$

		InputStream responseStream = gm.getResponseBodyAsStream();

		StreamsTools.copyStream(responseStream, os);

		logger.debug("content done"); //$NON-NLS-1$
	}

	/**
	 * Update instrument on repository
	 * 
	 * @param id
	 * @param name
	 * @param tags
	 * @throws Exception
	 */
	public void updateInstrument(long id, String name, String tags,
			Long contentstream, Long imagestream) throws Exception {

		PutMethod pm = new PutMethod(baseUrl + "/instruments/" + id //$NON-NLS-1$
				+ getQueryAuthentication());

		JSONObject updatedInstrument = new JSONObject();
		updatedInstrument.put("id", id); //$NON-NLS-1$
		updatedInstrument.put("name", name); //$NON-NLS-1$
		updatedInstrument.put("tags", tags); //$NON-NLS-1$
		if (contentstream != null)
			updatedInstrument.put("contentstreamid", contentstream); //$NON-NLS-1$

		if (imagestream != null)
			updatedInstrument.put("imagestreamid", imagestream); //$NON-NLS-1$

		String jsonmessage = updatedInstrument.toString();
		pm.setRequestEntity(new StringRequestEntity(jsonmessage));

		if (httpclient.executeMethod(pm) != 200) {
			throw new Exception("fail to update instrument"); //$NON-NLS-1$
		}

		org.json.JSONTokener tokener = new org.json.JSONTokener(pm
				.getResponseBodyAsString());

		JSONObject jsonresponse = new JSONObject(tokener);
		if (jsonresponse.has("error"))
			throw new Exception("error :" + jsonresponse.getString("error"));

	}

	public int createInstrument(String name, String tags) throws Exception {

		JSONObject createdInstrument = new JSONObject();

		createdInstrument.put("name", name); //$NON-NLS-1$
		createdInstrument.put("tags", tags); //$NON-NLS-1$

		return (Integer) handleMessage("/instruments/new", METHOD_PUT, //$NON-NLS-1$
				createdInstrument);

	}

	public void deleteInstrument(long id) throws Exception {

		JSONObject deletedInstrument = new JSONObject();

		handleMessage("/instruments/" + id, METHOD_DELETE, deletedInstrument); //$NON-NLS-1$

	}

	public static void main(String[] args) throws Exception {

		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure(new ConsoleAppender(new PatternLayout()));

		APrintRepositoryClientConnection c = new APrintRepositoryClientConnection();

		// c.connect("http://aprintrepository.appspot.com", null, null);

		c.connect("http://localhost:8080", "frett27", "frett27"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		long l = c.createInstrument("mon instrument", "montag"); //$NON-NLS-1$ //$NON-NLS-2$

		long imageStream = c
				.putStream(
						"/streams", //$NON-NLS-1$
						new FileInputStream(
								new File(
										"C:/Documents and Settings/Freydiere Patrice/Bureau/Projets/Musique Mécanique/gamme_50_limonaire.jpg")), //$NON-NLS-1$
						"image/jpeg"); //$NON-NLS-1$

		long contentStream = c
				.putStream(
						"/streams", //$NON-NLS-1$
						new FileInputStream(
								"C:/Documents and Settings/Freydiere Patrice/Mes documents/monrepertoiredegammes/38 t FROR.instrumentbundle"), //$NON-NLS-1$
						APPLICATION_OCTET_STREAM);

		c.updateInstrument(l, "mon instrument", "mon tag", contentStream, //$NON-NLS-1$ //$NON-NLS-2$
				imageStream);

		RepositoryInstrument[] listInstruments = c.listInstruments();
		System.out.println(listInstruments);

	}

}
