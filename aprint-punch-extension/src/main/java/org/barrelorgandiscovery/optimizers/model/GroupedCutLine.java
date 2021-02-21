package org.barrelorgandiscovery.optimizers.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.barrelorgandiscovery.optimizers.model.visitor.OptimizedObjectVisitor;

/**
 * grouped cut line are lines that are always groupped, with no optimization in between.
 * Why ? because we don't want to displace much the lazer head.
 * 
 * @author pfreydiere
 *
 */
public class GroupedCutLine extends OptimizedObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 662132798318799494L;
	
	
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
	
	@Override
	public void accept(OptimizedObjectVisitor visitor) {
		visitor.enter(this);
		visitor.visit(this);
		visitor.exit(this);
	}
	
}

