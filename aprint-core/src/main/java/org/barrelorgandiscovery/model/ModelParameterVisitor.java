package org.barrelorgandiscovery.model;

public interface ModelParameterVisitor {

	public void visit(ModelParameter parameter);

	public void visit(ModelValuedParameter modelValuedParameter);

}
