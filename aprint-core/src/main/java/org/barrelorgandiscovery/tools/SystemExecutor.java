package org.barrelorgandiscovery.tools;

import java.io.InputStream;
import java.io.OutputStream;

public class SystemExecutor {

	/**
	 * Execute a command line with parameters and get the result in an outputStream
	 * @param cmdLine the command line and options
	 * @param out the output stream for the output
	 * @return the value of the process exit
	 * @throws Exception
	 */
	public static int execute(String[] cmdLine, OutputStream out)
			throws Exception {
		Process process = Runtime.getRuntime().exec(cmdLine);
		
		InputStream inputStream = process.getInputStream();

		byte[] buffer = new byte[2048];
		int cpt;
		while ((cpt = inputStream.read(buffer))!= -1)
		{
			out.write(buffer, 0, cpt);
		}
		
		return process.exitValue();
		
	}

}
