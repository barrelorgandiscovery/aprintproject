package org.barrelorgandiscovery.mcp.features;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Utility class to help generate feature classes from jSymbolic feature definitions.
 * This is a helper tool for porting features - not used at runtime.
 * 
 * @author APrint Development Team
 */
public class FeatureGenerator {
	
	/**
	 * Generate a feature class from a template.
	 */
	public static void generateFeature(String className, String code, String name, 
			String description, String implementation) {
		String template = 
			"package org.barrelorgandiscovery.mcp.features;\n\n" +
			"/**\n" +
			" * Feature: " + name + "\n" +
			" * \n" +
			" * " + description + "\n" +
			" * \n" +
			" * @author APrint Development Team\n" +
			" */\n" +
			"public class " + className + " extends VirtualBookFeatureExtractor {\n\n" +
			"\tpublic " + className + "() {\n" +
			"\t\tthis.code = \"" + code + "\";\n" +
			"\t\tthis.name = \"" + name + "\";\n" +
			"\t\tthis.description = \"" + description + "\";\n" +
			"\t}\n\n" +
			"\t@Override\n" +
			"\tpublic double[] extractFeature(VirtualBookFeatureContext context) throws Exception {\n" +
			implementation +
			"\t}\n" +
			"}\n";
		
		System.out.println(template);
	}
	
	/**
	 * Main method for generating features interactively or from file.
	 */
	public static void main(String[] args) {
		// This is a utility class for development
		System.out.println("Feature Generator - Use this to help port jSymbolic features");
	}
}

