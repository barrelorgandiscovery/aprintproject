package org.barrelorgandiscovery.tools.html;

import org.apache.log4j.Logger;
import org.htmlparser.Parser;
import org.htmlparser.lexer.Lexer;

public class HTMLParsingTools {

	private static Logger logger = Logger.getLogger(HTMLParsingTools.class);
	
	public static String[] parseHTMLAndExtractLinks(String htmlFragment)
			throws Exception {

		if (htmlFragment == null || htmlFragment.isEmpty())
			return new String[0];
		
		if (logger.isDebugEnabled())
		{
			logger.debug("parsing fragment :" + htmlFragment);
		}
		
		Lexer l = new Lexer(htmlFragment);
		Parser p = new Parser(l);

		LinkExtractor le = new LinkExtractor();
		p.visitAllNodesWith(le);

		return le.getLinks();

	}

}
