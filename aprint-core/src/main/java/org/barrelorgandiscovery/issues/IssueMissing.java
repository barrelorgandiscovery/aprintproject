package org.barrelorgandiscovery.issues;

import org.barrelorgandiscovery.gui.ascale.ScaleComponent;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.tools.HashCodeUtils;
import org.barrelorgandiscovery.tools.TimeUtils;
import org.barrelorgandiscovery.virtualbook.Region;

/**
 * une ou plusieurs notes manques ...
 * 
 * @author Freydiere Patrice
 */
public class IssueMissing extends AbstractSpatialIssue {

	private long start;
	private long length;
	private AbstractTrackDef pistedef;
	private Region extent;
	private int interpolatepos;

	public IssueMissing(long start, long length, AbstractTrackDef pisteorigine,
			int interpolatepos) {
		super(0); // pas de libellé associé
		this.start = start;
		this.length = length;
		this.pistedef = pisteorigine;

		this.interpolatepos = interpolatepos;

		if (interpolatepos > 0) {
			this.extent = new Region(start, start + length, interpolatepos,
					interpolatepos + 1);

		} else {
			this.extent = new Region(start, start + length, 0, 0);
		}
	}

	public long getStart() {
		return start;
	}

	public long getLength() {
		return length;
	}

	public AbstractTrackDef getPiste() {
		return pistedef;
	}

	@Override
	public Region getExtent() {
		return extent;
	}

	public int getInterpolatePos() {
		return interpolatepos;
	}

	@Override
	public int hashCode() {
		int hash = HashCodeUtils.SEED;
		hash = HashCodeUtils.hash(hash, start);
		hash = HashCodeUtils.hash(hash, length);
		hash = HashCodeUtils.hash(hash, pistedef.hashCode());
		hash = HashCodeUtils.hash(hash, extent.hashCode());
		hash = HashCodeUtils.hash(hash, interpolatepos);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj.getClass() != getClass())
			return false;

		IssueMissing n = (IssueMissing) obj;
		return start == n.start && length == n.length
				&& pistedef.equals(n.pistedef) && extent.equals(n.extent)
				&& interpolatepos == n.interpolatepos;
	}

	@Override
	public String toLabel() {
		return TimeUtils.toMinSecs(getStart())
				+ Messages.getString("JIssuePresenter.1") //$NON-NLS-1$
				+ ScaleComponent.getTrackLibelle(getPiste());
	}

}
