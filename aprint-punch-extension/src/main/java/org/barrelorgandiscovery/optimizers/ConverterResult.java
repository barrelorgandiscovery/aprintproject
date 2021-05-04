package org.barrelorgandiscovery.optimizers;

import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.optimizers.model.OptimizedObject;

public class ConverterResult<T extends OptimizedObject> {

	public T[] result;

	public IssueCollection holeerrors = new IssueCollection();

}