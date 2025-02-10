package org.barrelorgandiscovery.scale.importer;

/**
 * internal line parser
 * 
 * @author pfreydiere
 *
 */
public interface LineParser {

	/**
	 * inform a line has been parsed
	 * @param cmd midiboek line
	 * @param params associated parameters
	 * @throws Exception
	 */
	public void lineParsed(String cmd, String[] params) throws Exception;

}
