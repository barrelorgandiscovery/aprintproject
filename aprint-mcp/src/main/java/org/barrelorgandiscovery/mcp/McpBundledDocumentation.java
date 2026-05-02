package org.barrelorgandiscovery.mcp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Classpath-backed documentation shipped with {@code aprint-mcp} for MCP resources and tools.
 */
public final class McpBundledDocumentation {

	public static final String URI_GROOVY_SCRIPTING = "aprint://docs/groovy-scripting.md";

	public static final String CLASSPATH_GROOVY_SCRIPTING = "org/barrelorgandiscovery/mcp/docs/groovy-scripting-aprint-studio.md";

	private McpBundledDocumentation() {
	}

	/**
	 * Loads UTF-8 text from the current class loader (same as {@link McpBundledDocumentation}'s).
	 */
	public static String loadClasspathUtf8(String classpathRelativePath) throws IOException {
		ClassLoader cl = McpBundledDocumentation.class.getClassLoader();
		try (InputStream is = cl.getResourceAsStream(classpathRelativePath)) {
			if (is == null) {
				throw new IOException("Classpath resource not found: " + classpathRelativePath);
			}
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
