package org.barrelorgandiscovery.tracetools.ga;

import org.barrelorgandiscovery.tracetools.punch.PunchConverterOptimizerParameters;


public class GeneticOptimizerParameters extends PunchConverterOptimizerParameters {

	private double pageSize = 30;
	
	public double getPageSize() {
		return pageSize;
	}
	
	public void setPageSize(double pageSize) {
		this.pageSize = pageSize;
	}
	
}
