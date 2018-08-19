package org.barrelorgandiscovery.tools;



public class TimeUtils {

	/**
	 * format a microsecond time stamp to a string Min:Secondes:Millis
	 * @param micro
	 * @return
	 */
	public static String toMinSecs(long micro)
	{
		long totalmillis = micro / 1000;
		
		long millis = totalmillis % 1000;
		
		long totalseconds = (totalmillis / 1000);
		
		long minutes = (totalseconds / 60);
		
		long seconds = totalseconds - (minutes * 60);
		
		return  String.format("%2d",minutes) + ":" + String.format("%2d",seconds) + ":" + String.format("%3d",millis); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	
}
