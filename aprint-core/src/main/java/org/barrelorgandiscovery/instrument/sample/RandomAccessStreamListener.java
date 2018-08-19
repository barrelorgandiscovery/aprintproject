package org.barrelorgandiscovery.instrument.sample;

/**
 * Asynchrone random file listener
 * @author Freydiere Patrice
 *
 */
public interface RandomAccessStreamListener {

	public boolean  dataReceived(byte[] chunk);

	public void endOfStream();

}
