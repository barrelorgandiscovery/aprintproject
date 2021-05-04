package org.barrelorgandiscovery;

/**
 * interfce for getting job events, methods are called when job event occured
 * 
 * @author pfreydiere
 * 
 */
public interface JobEvent {

	void jobFinished(Object result);

	void jobError(Throwable ex);

	void jobAborted();

}
