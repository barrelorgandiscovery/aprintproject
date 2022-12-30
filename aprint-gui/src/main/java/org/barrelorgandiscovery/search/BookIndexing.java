package org.barrelorgandiscovery.search;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.barrelorgandiscovery.gui.APrintConstants;
import org.barrelorgandiscovery.gui.ProgressIndicator;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.VirtualBookMetadata;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO.VirtualBookResult;

/**
 * Service class for indexing the book on the file system ...
 * 
 * @author Freydiere Patrice
 * 
 */
public class BookIndexing implements Disposable {

	public static final String INSTRUMENT_FIELD = "instrument";

	public static final String DESCRIPTION_FIELD = "description";

	public static final String ARRANGER_FIELD = "arranger";

	public static final String AUTHOR_FIELD = "author";

	public static final String FILEREF_FIELD = "fileref";

	public static final String GENRE_FIELD = "genre";

	public static final String NAME_FIELD = "name";

	public static final String SCALE_FIELD = "scale";

	public static final String ALL = "all";

	private static Logger logger = Logger.getLogger(BookIndexing.class);

	private APrintProperties props = null;
	
	private Analyzer analyzer = null;
	private Directory luceneDirectory;
	private File searchFolder = null;

	public BookIndexing(APrintProperties props) throws Exception {
		this.props = props;

		analyzer = new StandardAnalyzer(Version.LUCENE_30);

		searchFolder = new File(props.getAprintFolder(), "searchindex");
		if (!searchFolder.exists()) {
			searchFolder.mkdir();
		}

		luceneDirectory = FSDirectory.open(searchFolder);
	}

	public void dispose() {
		try {
			luceneDirectory.close();
		} catch (Throwable t) {
			logger.error("error in release the directory :" + t.getMessage(), t);
		}
	}

	private VirtualBookResult readVirtualBook(File file) throws Exception {

		BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
		try {
			return VirtualBookXmlIO.read(bufferedInputStream);
		} finally {
			bufferedInputStream.close();
		}
	}

	private int recurseIndexFiles(File f, IndexWriter index, ProgressIndicator indicator, int nbindexedFile)
			throws Exception {

		if (logger.isDebugEnabled()) {
			logger.debug("file examined :" + f.getAbsolutePath());
		}

		if (f.isDirectory()) {

			File[] subFiles = f.listFiles();
			if (subFiles != null) {
				for (int i = 0; i < subFiles.length; i++) {
					File file = subFiles[i];
					nbindexedFile = recurseIndexFiles(file, index, indicator, nbindexedFile + 1);
				}
			}

		} else {
			if (f.getName().toLowerCase().endsWith("." + APrintConstants.BOOK)) {

				try {
					Document doc = new Document();

					if ((nbindexedFile % 10) == 0) {
						if (indicator != null)
							indicator.progress(0, ".. " + nbindexedFile);
					}
					VirtualBookResult vbr = readVirtualBook(f);
					VirtualBook vb = vbr.virtualBook;
					if (vb.getMetadata() != null) {
						VirtualBookMetadata m = vb.getMetadata();

						logger.debug("adding file :" + f.getAbsolutePath());

						StringBuilder all = new StringBuilder();

						doc.add(new Field(SCALE_FIELD, vb.getScale().getName(), Field.Store.YES, Field.Index.ANALYZED));
						all.append(vb.getScale().getName()).append(" ");

						if (vb.getName() != null) {
							doc.add(new Field(NAME_FIELD, vb.getName(), Field.Store.YES, Field.Index.ANALYZED));
							all.append(vb.getName()).append(" ");
						}

						if (m.getGenre() != null) {
							doc.add(new Field(GENRE_FIELD, m.getGenre(), Field.Store.YES, Field.Index.ANALYZED));
							all.append(m.getGenre()).append(" ");
						}

						doc.add(new Field(FILEREF_FIELD, f.toURL().toString(), Field.Store.YES, Field.Index.NO));

						String prepare = f.getAbsolutePath();
						prepare = StringTools.join(prepare.split("_"), " ");
						prepare = StringTools.join(prepare.split("-"), " ");
						prepare = StringTools.join(prepare.split("."), " ");

						all.append(f.getAbsolutePath()).append(" ").append(prepare).append(" ");

						if (m.getAuthor() != null) {
							doc.add(new Field(AUTHOR_FIELD, m.getAuthor(), Field.Store.YES, Field.Index.ANALYZED));
							all.append(m.getAuthor()).append(" ");
						}
						if (m.getArranger() != null) {
							doc.add(new Field(ARRANGER_FIELD, m.getArranger(), Field.Store.YES, Field.Index.ANALYZED));
							all.append(m.getArranger()).append(" ");
						}
						if (m.getDescription() != null) {
							doc.add(new Field(DESCRIPTION_FIELD, m.getDescription(), Field.Store.YES,
									Field.Index.ANALYZED));
							all.append(m.getDescription()).append(" ");

						}
						if (vbr.preferredInstrumentName != null) {
							doc.add(new Field(INSTRUMENT_FIELD, vbr.preferredInstrumentName, Field.Store.YES,
									Field.Index.ANALYZED));
							all.append(vbr.preferredInstrumentName).append(" ");

						}

						doc.add(new Field(ALL, all.toString(), Field.Store.YES, Field.Index.ANALYZED));
						index.addDocument(doc);

					} else {
						logger.debug("no metadata for " + f.getAbsolutePath());
					}
				} catch (Throwable t) {
					logger.warn("file :" + f.getAbsolutePath() + " cannot be indexed");
				}
			}
		}

		return nbindexedFile;

	}

	public void index(File directory, ProgressIndicator indicator) throws Exception {

		if (directory == null || !directory.isDirectory()) {
			return;
		}

		// To store an index on disk, use this instead:
		// Directory directory = FSDirectory.open("/tmp/testindex");
		IndexWriter iwriter = new IndexWriter(luceneDirectory, analyzer, true, new IndexWriter.MaxFieldLength(25000));
		try {

			recurseIndexFiles(directory, iwriter, indicator, 0);

		} finally {
			iwriter.close();
		}
	}

	public ScoredDocument[] search(String search) throws Exception {

		/*
		 * if (search == null || "".equals(search)) return new ScoredDocument[0];
		 */
		// Now search the index:
		IndexSearcher isearcher = new IndexSearcher(luceneDirectory, true); // read-only=true
		try {
			// Parse a simple query that searches for "text":
			QueryParser parser = new QueryParser(Version.LUCENE_30, ALL, analyzer);
			Query query = null;
			if ("".equals(search) || search == null) {
				query = new MatchAllDocsQuery();
			} else {
				query = parser.parse(search);
			}
			ScoreDoc[] hits = isearcher.search(query, null, 500).scoreDocs;

			// Iterate through the results:
			ArrayList<ScoredDocument> retvalue = new ArrayList<>();
			for (int i = 0; i < hits.length; i++) {
				retvalue.add(new ScoredDocument(isearcher.doc(hits[i].doc), hits[i].score));
			}

			return retvalue.toArray(new ScoredDocument[0]);

		} finally {
			isearcher.close();
		}
	}

}
