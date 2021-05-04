package org.barrelorgandiscovery.optimizers.model.visitor;

import org.barrelorgandiscovery.optimizers.model.CutLine;
import org.barrelorgandiscovery.optimizers.model.GroupedCutLine;
import org.barrelorgandiscovery.optimizers.model.Punch;

public abstract class OptimizedObjectVisitor {

	public abstract void visit(CutLine cutLine);
	
	public abstract void enter(GroupedCutLine groupedCutLine);
	public abstract void exit(GroupedCutLine groupedCutLine);
	public abstract void visit(GroupedCutLine groupedCutLine);
	
	public abstract void visit(Punch punch);
}
