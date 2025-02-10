package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.generic.lasergcode;

/**
 * Generate specific laser GCode
 * 
 * @author pfreydiere
 *
 */
public class GenericLazerGCodeParameterGeneration {

	public String homingCommands = "$H";
	public String startBookPrecommands = "M4";

	public String displacementPreCommand = "";
	public String displacementCommandPattern = "G0 X%1$f Y%2$f";
	public String displacementPostCommand = "";

	public String cuttingPreCommand = "";
	public String cuttingToCommandPattern = "G1 X%1$f Y%2$f F%3$d";
	public String cuttingPostCommand = "";

	public String powerChangeCommand = "S%1$d";

	public String endBookPrecommands = "M5";

}
