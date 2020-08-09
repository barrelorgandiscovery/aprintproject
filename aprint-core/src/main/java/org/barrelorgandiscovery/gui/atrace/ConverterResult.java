package org.barrelorgandiscovery.gui.atrace;

import org.barrelorgandiscovery.issues.IssueCollection;

public class ConverterResult<T extends OptimizedObject> {

	public T[] result;

	public IssueCollection holeerrors = new IssueCollection();

}