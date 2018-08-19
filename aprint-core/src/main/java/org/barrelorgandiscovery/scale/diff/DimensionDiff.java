package org.barrelorgandiscovery.scale.diff;

public class DimensionDiff extends AbstractDiffElement {

	private String propertyName;
	private double source;
	private double destination;

	public DimensionDiff(String propertyName, double source, double destination) {
		assert propertyName != null;

		this.propertyName = propertyName;
		this.source = source;
		this.destination = destination;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	public double getSource() {
		return source;
	}

	public double getDestination() {
		return destination;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	

}
