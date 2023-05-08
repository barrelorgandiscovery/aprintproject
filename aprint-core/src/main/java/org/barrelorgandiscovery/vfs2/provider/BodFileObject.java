package org.barrelorgandiscovery.vfs2.provider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.vfs2.FileContentInfoFactory;
import org.apache.commons.vfs2.FileNotFoundException;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.GenericURLFileName;
import org.apache.commons.vfs2.provider.http4.Http4FileContentInfoFactory;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.protocol.HTTP;
import org.barrelorgandiscovery.tools.StreamsTools;

/**
 * A file object backed by Apache HttpComponents HttpClient.
 *
 * @param <FS> bodFileSystem subclass
 *
 * @since 2.3
 */
public class BodFileObject<FS extends BodFileSystem> extends AbstractFileObject<FS> {

	/**
	 * URL charset string.
	 */
	private final String urlCharset;

	/**
	 * Internal URI mapped to this {@code FileObject}. For example, the internal URI
	 * of {@code http4://example.com/a.txt} is {@code http://example.com/a.txt}.
	 */
	private final URI internalURI;

	/**
	 * The last executed HEAD {@code HttpResponse} object.
	 */
	private ResponseInformations lastHeadResponse;

	static class ResponseInformations {
		public Header contentLen;
		public Header lastModified;
	}

	ResponseInformations toResponseInformation(HttpResponse response) {
		ResponseInformations responseInformations = new ResponseInformations();
		responseInformations.contentLen = response.getFirstHeader(HTTP.CONTENT_LEN);
		responseInformations.lastModified = response.getFirstHeader("Last-Modified");
		return responseInformations;
	}

	/**
	 * Construct {@code Http4FileObject}.
	 *
	 * @param name       file name
	 * @param fileSystem file system
	 * @throws FileSystemException if any error occurs
	 */
	protected BodFileObject(final AbstractFileName name, final FS fileSystem) throws FileSystemException {
		this(name, fileSystem, BodFileSystemConfigBuilder.getInstance());
	}

	/**
	 * Construct {@code Http4FileObject}.
	 *
	 * @param name       file name
	 * @param fileSystem file system
	 * @param builder    {@code Http4FileSystemConfigBuilder} object
	 * @throws FileSystemException if any error occurs
	 */
	protected BodFileObject(final AbstractFileName name, final FS fileSystem, final BodFileSystemConfigBuilder builder)
			throws FileSystemException {
		super(name, fileSystem);
		final FileSystemOptions fileSystemOptions = fileSystem.getFileSystemOptions();
		urlCharset = builder.getUrlCharset(fileSystemOptions);
		final String pathEncoded = ((GenericURLFileName) name).getPathQueryEncoded(getUrlCharset());
		internalURI = URIUtils.resolve(fileSystem.getInternalBaseURI(), pathEncoded);
	}

	@Override
	protected void doDetach() throws Exception {
		lastHeadResponse = null;
	}

	@Override
	protected long doGetContentSize() throws Exception {
		if (lastHeadResponse == null) {
			return 0L;
		}

		final Header header = lastHeadResponse.contentLen;

		if (header == null) {
			// Assume 0 content-length
			return 0;
		}

		return Long.parseLong(header.getValue());
	}

	@Override
	protected InputStream doGetInputStream(final int bufferSize) throws Exception {
		URI callUrl = getHttpUri();
		final HttpGet getRequest = new HttpGet(callUrl);
		final HttpResponse httpResponse = executeHttpUriRequest(getRequest);
		final int status = httpResponse.getStatusLine().getStatusCode();

		if (status == HttpStatus.SC_NOT_FOUND) {
			throw new FileNotFoundException(getName());
		}

		if (status != HttpStatus.SC_OK) {
			throw new FileSystemException("vfs.provider.http/get.error", getName(), Integer.valueOf(status));
		}

		return new MonitoredHttpResponseContentInputStream(httpResponse, bufferSize);
	}

	@Override
	protected long doGetLastModifiedTime() throws Exception {
		FileSystemException.requireNonNull(lastHeadResponse, "vfs.provider.http/last-modified.error", getName());

		final Header header = lastHeadResponse.lastModified;

		FileSystemException.requireNonNull(header, "vfs.provider.http/last-modified.error", getName());

		return DateUtils.parseDate(header.getValue()).getTime();
	}

	@Override
	protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception {
		throw new UnsupportedOperationException("Not implemented.");
	}

	URI getHttpUri() {
		return URI.create(getInternalURI().toString().replaceAll("bod://", "http://"));
	}

	FileType fileType = null;

	@Override
	protected FileType doGetType() throws Exception {

		if (fileType != null) {
			return fileType;
		}

		URI callUri = getHttpUri();
		HttpHead head = new HttpHead(callUri);
		head.setProtocolVersion(HttpVersion.HTTP_1_0);
		var httpResponse = executeHttpUriRequest(head);
		try {

			lastHeadResponse = toResponseInformation(httpResponse);
			final int status = httpResponse.getStatusLine().getStatusCode();

			if (status == HttpStatus.SC_OK
					|| status == HttpStatus.SC_METHOD_NOT_ALLOWED /* method is not allowed, but resource exist */) {
				fileType = FileType.FILE;
				return fileType;
			}

			if (status == HttpStatus.SC_NOT_FOUND || status == HttpStatus.SC_GONE) {
				fileType = FileType.IMAGINARY;
				return fileType;

			}

			try {
				getChildren();
				fileType = FileType.FOLDER;
				return fileType;

			} catch (Exception ex) {
				// continue
			}

			throw new FileSystemException("vfs.provider.http/head.error", getName(), Integer.valueOf(status));
		} finally {
			httpResponse.close();
		}
	}

	@Override
	protected boolean doIsWriteable() throws Exception {
		return false;
	}

	@Override
	protected String[] doListChildren() throws Exception {

		URI contentListUri = URI.create(getHttpUri() + "/CONTENT");

		final HttpGet getRequest = new HttpGet(contentListUri);
		final CloseableHttpResponse httpResponse = executeHttpUriRequest(getRequest);
		try {
			final int status = httpResponse.getStatusLine().getStatusCode();

			if (status == HttpStatus.SC_NOT_FOUND) {

				throw new FileNotFoundException(getName());
			}

			if (status != HttpStatus.SC_OK) {
				throw new FileSystemException("vfs.provider.http/get.error", getName(), Integer.valueOf(status));
			}

			String content = StreamsTools
					.fullyReadUTF8StringFromStream(new MonitoredHttpResponseContentInputStream(httpResponse, 4096));

			ArrayList<String> al = new ArrayList<String>();
			(new StringTokenizer(content, "\n").asIterator()).forEachRemaining((s) -> al.add((String) s));

			return al.toArray(new String[al.size()]);

		} finally {
			httpResponse.close();
		}
	}

	/**
	 * Execute the request using the given {@code httpRequest} and return a
	 * {@code HttpResponse} from the execution.
	 *
	 * @param httpRequest {@code HttpUriRequest} object
	 * @return {@code HttpResponse} from the execution
	 * @throws IOException if IO error occurs
	 *
	 * @since 2.5.0
	 */
	protected CloseableHttpResponse executeHttpUriRequest(final HttpUriRequest httpRequest) throws IOException {

		final HttpClient httpClient = getAbstractFileSystem().getHttpClient();

		// System.out.println("execute : " + httpRequest);
		final HttpClientContext httpClientContext = getAbstractFileSystem().getHttpClientContext();
		CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpRequest, httpClientContext);
		// System.out.println("  response :" + response.getStatusLine());
		return response;

	}

	@Override
	protected FileContentInfoFactory getFileContentInfoFactory() {
		return new Http4FileContentInfoFactory();
	}

	/**
	 * Return the internal {@code URI} object mapped to this file object.
	 *
	 * @return the internal {@code URI} object mapped to this file object
	 */
	protected URI getInternalURI() {
		return internalURI;
	}

	/**
	 * Return the last executed HEAD {@code HttpResponse} object.
	 *
	 * @return the last executed HEAD {@code HttpResponse} object
	 * @throws IOException if IO error occurs
	 */
	ResponseInformations getLastHeadResponse() throws IOException {
		if (lastHeadResponse != null) {
			return lastHeadResponse;
		}
		HttpHead head = new HttpHead(getHttpUri());
		CloseableHttpResponse response = executeHttpUriRequest(head);
		try {
			return toResponseInformation(response);
		} finally {
			response.close();
		}
	}

	/**
	 * Return URL charset string.
	 * 
	 * @return URL charset string
	 */
	protected String getUrlCharset() {
		return urlCharset;
	}

}
