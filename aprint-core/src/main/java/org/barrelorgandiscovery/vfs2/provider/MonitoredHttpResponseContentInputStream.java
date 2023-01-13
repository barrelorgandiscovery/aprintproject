package org.barrelorgandiscovery.vfs2.provider;

import java.io.IOException;

import org.apache.commons.vfs2.util.MonitorInputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;

public class MonitoredHttpResponseContentInputStream extends MonitorInputStream {


    private final HttpResponse httpResponse;

    public MonitoredHttpResponseContentInputStream(final HttpResponse httpResponse) throws IOException {
        super(httpResponse.getEntity().getContent());
        this.httpResponse = httpResponse;
    }

    public MonitoredHttpResponseContentInputStream(final HttpResponse httpResponse, final int bufferSize) throws IOException {
        super(httpResponse.getEntity().getContent(), bufferSize);
        this.httpResponse = httpResponse;
    }

    /**
     * Prevent closing the stream itself if the httpResponse is closeable.
     * Closing the stream may consume all remaining data no matter how large (VFS-805).
     */
    @Override
    protected void closeSuper() throws IOException {
        if (!(httpResponse instanceof CloseableHttpResponse)) {
            super.closeSuper();
        }
    }

    @Override
    protected void onClose() throws IOException {
        if (httpResponse instanceof CloseableHttpResponse) {
            ((CloseableHttpResponse) httpResponse).close();
        }
    }
}
