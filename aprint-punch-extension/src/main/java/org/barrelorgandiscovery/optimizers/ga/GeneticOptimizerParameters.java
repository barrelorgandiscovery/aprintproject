package org.barrelorgandiscovery.optimizers.ga;

import org.barrelorgandiscovery.optimizers.punch.PunchConverterOptimizerParameters;


public class GeneticOptimizerParameters extends PunchConverterOptimizerParameters {

	private double pageSize = 30;
	
	public double getPageSize() {
		return pageSize;
	}
	
	public void setPageSize(double pageSize) {
		this.pageSize = pageSize;
	}
	
}
