package org.barrelorgandiscovery.virtualbook.transformation.importer;

import org.barrelorgandiscovery.issues.AbstractIssue;

/**
 * Base class for midi conversion problem
 * 
 * @author use
 * 
 */
public abstract class MidiConversionProblem {

	public abstract AbstractIssue toIssue();

}
