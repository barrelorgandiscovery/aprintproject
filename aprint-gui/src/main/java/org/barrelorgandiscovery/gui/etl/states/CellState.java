package org.barrelorgandiscovery.gui.etl.states;

/**
 * Differents etats de la cellule
 * 
 * @author pfreydiere
 * 
 */
public enum CellState {

	STEP_UNCONFIGURED("unconfigured", true), STEP_OK("configured", false), STEP_UNSCHEDULED(
			"unscheduled", false), MANDATORY_INPUTPARAMETER_UNCONNECTED(
			"parameterunconnected", true), INPUTPARAMETEROK("inparam", false), OUTPUTPARAMETEROK(
			"outparam", false);

	private String cellStyle;
	private boolean error;

	CellState(String cellStyle, boolean isError) {
		this.cellStyle = cellStyle;
		this.error = isError;
	}

	public String getCellStyle() {
		return cellStyle;
	}

	public boolean isError() {
		return error;
	}

}
