package org.barrelorgandiscovery.scale.importer;

public interface LineParser {

	public void lineParsed(String cmd, String[] params) throws Exception;

}
