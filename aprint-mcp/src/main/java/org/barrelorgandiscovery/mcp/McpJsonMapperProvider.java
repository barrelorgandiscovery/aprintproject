package org.barrelorgandiscovery.mcp;

import java.util.ServiceLoader;

import org.apache.log4j.Logger;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.McpJsonMapperSupplier;

/**
 * Provider for McpJsonMapper that handles ClassLoader issues with ChildFirstClassLoader.
 * 
 * This class ensures that ServiceLoader uses the correct ClassLoader to avoid conflicts
 * with APrint's child-first class loading policy used in extensions.
 * 
 * The problem: ServiceLoader.load() uses Thread.currentThread().getContextClassLoader()
 * by default, which can cause "not a subtype" errors when ChildFirstClassLoader is involved.
 * 
 * Solution: Explicitly use the ClassLoader of McpJsonMapperSupplier.class to ensure
 * all classes are loaded from the same classloader.
 * 
 * @author APrint Development Team
 */
public class McpJsonMapperProvider {
	
	private static final Logger logger = Logger.getLogger(McpJsonMapperProvider.class);
	
	private static volatile McpJsonMapper instance;
	
	/**
	 * Get the McpJsonMapper instance (singleton pattern).
	 * 
	 * Uses the ClassLoader of McpJsonMapperSupplier.class to avoid conflicts
	 * with ChildFirstClassLoader used in APrint extensions.
	 * 
	 * @return The McpJsonMapper instance
	 * @throws IllegalStateException if no McpJsonMapperSupplier is found on the classpath
	 */
	public static McpJsonMapper get() {
		if (instance == null) {
			synchronized (McpJsonMapperProvider.class) {
				if (instance == null) {
					try {
						// Use the ClassLoader of McpJsonMapperSupplier.class instead of
						// Thread.currentThread().getContextClassLoader() to avoid conflicts
						// with ChildFirstClassLoader
						ClassLoader cl = McpJsonMapperSupplier.class.getClassLoader();
						
						logger.debug("Loading McpJsonMapperSupplier using ClassLoader: " + cl);
						
						instance = ServiceLoader.load(McpJsonMapperSupplier.class, cl)
							.stream()
							.findFirst()
							.map(ServiceLoader.Provider::get)
							.map(McpJsonMapperSupplier::get)
							.orElseThrow(() -> new IllegalStateException(
								"No McpJsonMapperSupplier found on classpath. " +
								"Make sure mcp-json-jackson2 is included in dependencies."));
						
						logger.info("McpJsonMapper loaded successfully: " + instance.getClass().getName());
						
					} catch (Exception e) {
						logger.error("Failed to load McpJsonMapper", e);
						throw new IllegalStateException(
							"Failed to load McpJsonMapper: " + e.getMessage(), e);
					}
				}
			}
		}
		return instance;
	}
	
	/**
	 * Reset the singleton instance (useful for testing).
	 */
	static void reset() {
		synchronized (McpJsonMapperProvider.class) {
			instance = null;
		}
	}
}

