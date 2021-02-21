package org.barrelorgandiscovery.optimizers.model.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.barrelorgandiscovery.optimizers.model.CutLine;
import org.barrelorgandiscovery.optimizers.model.GroupedCutLine;
import org.barrelorgandiscovery.optimizers.model.OptimizedObject;
import org.barrelorgandiscovery.optimizers.model.Punch;

public class CoordinateTransformOptimizedObjects extends OptimizedObjectVisitor {

	private final Function<Double, Double> xtransfo;
	private final Function<Double, Double> ytransfo;

	public CoordinateTransformOptimizedObjects(final Function<Double, Double> xtransfo, final Function<Double, Double> ytransfo) {
		this.xtransfo = xtransfo;
		this.ytransfo = ytransfo;
	}

	public List<OptimizedObject> result = new ArrayList<OptimizedObject>();

	@Override
	public void visit(CutLine cutLine) {
		result.add(newCutLine(cutLine));
	}

	private CutLine newCutLine(CutLine cutline) {
		return new CutLine(xtransfo.apply(cutline.x1), ytransfo.apply(cutline.y1), xtransfo.apply(cutline.x2),
				ytransfo.apply(cutline.y2), cutline.powerFraction, cutline.speedFraction);
	}

	@Override
	public void enter(GroupedCutLine groupedCutLine) {

	}

	@Override
	public void exit(GroupedCutLine groupedCutLine) {

	}

	@Override
	public void visit(GroupedCutLine groupedCutLine) {

		List<CutLine> cutlines = groupedCutLine.getLinesByRefs().stream().map(this::newCutLine)
				.collect(Collectors.toList());
		GroupedCutLine newgroupedCutLine = new GroupedCutLine(cutlines);
		newgroupedCutLine.userInformation = groupedCutLine.userInformation;
		result.add(newgroupedCutLine);

	}

	@Override
	public void visit(Punch punch) {
		result.add(new Punch(xtransfo.apply(punch.x), ytransfo.apply(punch.y)));
	}

}
