package org.barrelorgandiscovery.vfs2.provider;

import org.apache.commons.vfs2.provider.FileNameParser;
import org.apache.commons.vfs2.provider.GenericURLFileNameParser;

public class BodFileNameParser extends GenericURLFileNameParser {

	private static final int DEFAULT_PORT = 80;

	private static final BodFileNameParser INSTANCE = new BodFileNameParser();

	public static FileNameParser getInstance() {
		return INSTANCE;
	}

	public BodFileNameParser() {
		super(DEFAULT_PORT);
	}

}
