package org.barrelorgandiscovery.search;

import org.apache.lucene.document.Document;

public class ScoredDocument {
	
	public ScoredDocument(Document document, double score) {
		this.document = document;
		this.score = score;
	}
	
	public final Document document;
	public final double score;

}
