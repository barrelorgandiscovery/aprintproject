package org.barrelorgandiscovery.tools.html;

import java.util.ArrayList;

import org.htmlparser.Tag;
import org.htmlparser.visitors.NodeVisitor;

public class LinkExtractor extends NodeVisitor {

	private ArrayList<String> links = new ArrayList<String>();

	public LinkExtractor() {

	}

	@Override
	public void visitTag(Tag tag) {
		super.visitTag(tag);

		if ("a".equalsIgnoreCase(tag.getTagName())) {
			String href = tag.getAttribute("href");
			links.add(href);
		}

	}

	public String[] getLinks() {
		return links.toArray(new String[0]);
	}

}
