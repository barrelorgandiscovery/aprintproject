package org.barrelorgandiscovery.optimizers.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupedCutLine extends OptimizedObject {

	private List<CutLine> lines = new ArrayList<>();

	public GroupedCutLine(List<CutLine> lines) {
		this.lines = new ArrayList<>(lines);
		for (CutLine l : lines) {
			assert l instanceof CutLine;
		}
		assert lines.size() > 0;
	}

	public List<CutLine> getLinesByRefs() {
		return Collections.unmodifiableList(this.lines);
	}

	@Override
	public Extent getExtent() {
		assert lines.size() > 0;

		Extent current = null;
		for (CutLine c : lines) {
			if (current == null) {
				current = c.getExtent();
			} else {
				assert current != null;
				current = current.union(c.getExtent());
			}
		}

		return current;
	}

	@Override
	public double firstX() {
		assert lines.size() > 0;
		return lines.get(0).firstX();
	}

	@Override
	public double firstY() {
		assert lines.size() > 0;
		return lines.get(0).firstY();
	}

	@Override
	public double lastX() {
		assert lines.size() > 0;
		return lines.get(lines.size() - 1).lastX();
	}

	@Override
	public double lastY() {
		assert lines.size() > 0;
		return lines.get(lines.size() - 1).lastY();
	}

}