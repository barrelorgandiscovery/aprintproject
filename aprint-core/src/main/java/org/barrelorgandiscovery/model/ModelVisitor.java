package org.barrelorgandiscovery.model;

public abstract class ModelVisitor {

	public abstract void visit(Model model,ModelParameter parameter);
	
	public abstract void visit(Model model,ModelStep step);
	
	public abstract void visit(Model model,ModelLink link);
	
	
}
