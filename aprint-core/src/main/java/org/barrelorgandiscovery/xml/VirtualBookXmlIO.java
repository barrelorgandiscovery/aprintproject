package org.barrelorgandiscovery.xml;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlOptions;
import org.barrelorgandiscovery.tools.StreamsTools;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.x2010.VirtualBookDocument;
import org.barrelorgandiscovery.virtualbook.x2010.VirtualBookMetadata;

public class VirtualBookXmlIO {

	private static Logger logger = Logger.getLogger(VirtualBookXmlIO.class);

	public static class VirtualBookResult {
		public VirtualBook virtualBook = null;
		public String preferredInstrumentName = null;
	}

	/**
	 * read a virtual book from a file
	 * 
	 * @param f
	 *            the file to read, this method use the same method with a
	 *            stream
	 * @return
	 * @throws Exception
	 */
	public static VirtualBookResult read(java.io.File f) throws Exception {
		long startTime = System.currentTimeMillis();
		try {
			FileInputStream fis = new FileInputStream(f);
			try {
				return read(fis);
			} finally {
				fis.close();
			}
		} finally {
			logger.debug("Perfs read :"
					+ (System.currentTimeMillis() - startTime));
		}
	}

	/**
	 * read a virtual book from a stream, the stream is not closed by the method
	 * 
	 * @param is
	 *            the stream to read
	 * @return a read result
	 * @throws Exception
	 */
	public static VirtualBookResult read(InputStream is) throws Exception {
		ByteArrayInputStream bi;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StreamsTools.copyStream(is, baos);
		bi = new ByteArrayInputStream(baos.toByteArray());

		try {
			return read2016(bi);
		} catch (Exception ex2016) {
			logger.debug(
					"error while reading 2016 new version :"
							+ ex2016.getMessage(), ex2016);
			bi.reset();
			try {
				return read2014(bi);

			} catch (Exception ex2014) {

				logger.debug(
						"error while reading 2014 new version :"
								+ ex2014.getMessage(), ex2014);
				bi.reset();
				try {

					return read2012(bi);

				} catch (Exception ex) {
					try {
						logger.debug("error while reading 2012 new version :"
								+ ex.getMessage(), ex);
						bi.reset();
						return read2010(bi);

					} catch (Exception ex2) {

						logger.error("Error trying to read in 2010 version : "
								+ ex.getMessage(), ex);

						throw new Exception("fail to load Virtual Book : "
								+ ex2.getMessage(), ex2);
					}

				}
			}
		}
	}

	/**
	 * read the specific 2014 virtualbook version, using a stream the stream is
	 * not closed by this method
	 * 
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public static VirtualBookResult read2014(InputStream is) throws Exception {

		org.barrelorgandiscovery.virtualbook.x2014.VirtualBookDocument v = org.barrelorgandiscovery.virtualbook.x2014.VirtualBookDocument.Factory
				.parse(new BufferedInputStream(is));
		logger.debug("virtualbook document read !");

		VirtualBookResult r = new VirtualBookResult();
		r.virtualBook = XMLConverter2014.fromVirtualBookDocument(v);

		org.barrelorgandiscovery.virtualbook.x2014.VirtualBookMetadata xmlmt = v
				.getVirtualBook().getMetadata();
		if (xmlmt != null) {
			r.preferredInstrumentName = xmlmt.getDesignedInstrumentName();
		}

		return r;

	}

	/**
	 * read the specific 2016 virtualbook version, using a stream the stream is
	 * not closed by this method
	 * 
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public static VirtualBookResult read2016(InputStream is) throws Exception {

		org.barrelorgandiscovery.virtualbook.x2016.VirtualBookDocument v = org.barrelorgandiscovery.virtualbook.x2016.VirtualBookDocument.Factory
				.parse(new BufferedInputStream(is));
		logger.debug("virtualbook document read !");

		VirtualBookResult r = new VirtualBookResult();
		r.virtualBook = XMLConverter2016.fromVirtualBookDocument(v);

		org.barrelorgandiscovery.virtualbook.x2016.VirtualBookMetadata xmlmt = v
				.getVirtualBook().getMetadata();
		if (xmlmt != null) {
			r.preferredInstrumentName = xmlmt.getDesignedInstrumentName();
		}

		return r;

	}

	/**
	 * read the specific 2012 virtualbook version, using a stream the stream is
	 * not closed by this method
	 * 
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public static VirtualBookResult read2012(InputStream is) throws Exception {

		org.barrelorgandiscovery.virtualbook.x2012.VirtualBookDocument v = org.barrelorgandiscovery.virtualbook.x2012.VirtualBookDocument.Factory
				.parse(new BufferedInputStream(is));
		logger.debug("virtualbook document read !");

		VirtualBookResult r = new VirtualBookResult();
		r.virtualBook = XMLConverter2012.fromVirtualBookDocument(v);

		org.barrelorgandiscovery.virtualbook.x2012.VirtualBookMetadata xmlmt = v
				.getVirtualBook().getMetadata();
		if (xmlmt != null) {
			r.preferredInstrumentName = xmlmt.getDesignedInstrumentName();
		}

		return r;

	}

	/**
	 * Read in 2010 version of the virtual book specification
	 * 
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public static VirtualBookResult read2010(InputStream is) throws Exception {

		VirtualBookDocument v = VirtualBookDocument.Factory
				.parse(new BufferedInputStream(is));
		logger.debug("virtualbook document read !");

		VirtualBookResult r = new VirtualBookResult();
		r.virtualBook = XMLConverter.fromVirtualBookDocument(v);

		VirtualBookMetadata xmlmt = v.getVirtualBook().getMetadata();
		if (xmlmt != null) {
			r.preferredInstrumentName = xmlmt.getDesignedInstrumentName();
		}

		return r;

	}

	public static void write(OutputStream os, VirtualBook vb,
			String preferredInstrument) throws Exception {
		
		if (logger.isDebugEnabled())
			logger.debug("saving " + vb + " with preferred instrument :"
					+ preferredInstrument);

		org.barrelorgandiscovery.virtualbook.x2016.VirtualBookDocument virtualBookDocument = XMLConverter2016
				.toVirtualBookDocument(vb);

		org.barrelorgandiscovery.virtualbook.x2016.VirtualBookMetadata xmlmetadata = virtualBookDocument
				.getVirtualBook().getMetadata();
		assert xmlmetadata != null;

		xmlmetadata.setDesignedInstrumentName(preferredInstrument);

		XmlOptions options = new XmlOptions();
		options.setSavePrettyPrint();

		virtualBookDocument.save(os, options);
	}
	
	/**
	 * Write in the current 2014 version of book specification
	 * 
	 * @param os
	 * @param vb
	 * @param preferredInstrument
	 * @throws Exception
	 */
	public static void write_2014(OutputStream os, VirtualBook vb,
			String preferredInstrument) throws Exception {

		if (logger.isDebugEnabled())
			logger.debug("saving " + vb + " with preferred instrument :"
					+ preferredInstrument);

		org.barrelorgandiscovery.virtualbook.x2014.VirtualBookDocument virtualBookDocument = XMLConverter2014
				.toVirtualBookDocument(vb);

		org.barrelorgandiscovery.virtualbook.x2014.VirtualBookMetadata xmlmetadata = virtualBookDocument
				.getVirtualBook().getMetadata();
		assert xmlmetadata != null;

		xmlmetadata.setDesignedInstrumentName(preferredInstrument);

		XmlOptions options = new XmlOptions();
		options.setSavePrettyPrint();

		virtualBookDocument.save(os, options);

	}

	/**
	 * Write in the current 2012 version of book specification
	 * 
	 * @param os
	 * @param vb
	 * @param preferredInstrument
	 * @throws Exception
	 */
	public static void write_2012(OutputStream os, VirtualBook vb,
			String preferredInstrument) throws Exception {

		if (logger.isDebugEnabled())
			logger.debug("saving " + vb + " with preferred instrument :"
					+ preferredInstrument);

		org.barrelorgandiscovery.virtualbook.x2012.VirtualBookDocument virtualBookDocument = XMLConverter2012
				.toVirtualBookDocument(vb);

		org.barrelorgandiscovery.virtualbook.x2012.VirtualBookMetadata xmlmetadata = virtualBookDocument
				.getVirtualBook().getMetadata();
		assert xmlmetadata != null;

		xmlmetadata.setDesignedInstrumentName(preferredInstrument);

		XmlOptions options = new XmlOptions();
		options.setSavePrettyPrint();

		virtualBookDocument.save(os, options);

	}

	/**
	 * Write book for the 2010 specification
	 * 
	 * @param os
	 * @param vb
	 * @param preferredInstrument
	 * @throws Exception
	 */
	public static void write_2010(OutputStream os, VirtualBook vb,
			String preferredInstrument) throws Exception {

		if (logger.isDebugEnabled())
			logger.debug("saving " + vb + " with preferred instrument :"
					+ preferredInstrument);

		VirtualBookDocument virtualBookDocument = XMLConverter
				.toVirtualBookDocument(vb);

		VirtualBookMetadata xmlmetadata = virtualBookDocument.getVirtualBook()
				.getMetadata();
		assert xmlmetadata != null;

		xmlmetadata.setDesignedInstrumentName(preferredInstrument);

		XmlOptions options = new XmlOptions();
		options.setSavePrettyPrint();

		virtualBookDocument.save(os, options);

	}

	/*
	 * public static void saxRead(InputStream is) throws Exception { }
	 */

}
