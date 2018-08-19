package org.barrelorgandiscovery.tracetools.punch;

import java.io.Serializable;

public class PunchConverterOptimizerParameters implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7286062265432166322L;

	
	private double punchWidth = 3.0; // par défaut 3.0 mm

	private double overlap = 0.5; // 0.5 mm overlap

	private double notPunchedIfLessThan = 0.0;

	public double getPunchWidth() {
		return punchWidth;
	}

	public void setPunchWidth(double punchWidth) {
		this.punchWidth = punchWidth;
	}

	public double getOverlap() {
		return overlap;
	}

	public void setOverlap(double overlap) {
		this.overlap = overlap;
	}

	public double getNotPunchedIfLessThan() {
		return notPunchedIfLessThan;
	}

	public void setNotPunchedIfLessThan(double notPunchedIfLessThan) {
		this.notPunchedIfLessThan = notPunchedIfLessThan;
	}

	
	
}
