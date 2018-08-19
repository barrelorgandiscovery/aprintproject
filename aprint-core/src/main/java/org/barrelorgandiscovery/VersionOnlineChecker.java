package org.barrelorgandiscovery;

import java.util.Locale;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.VersionTools;

public class VersionOnlineChecker {

	private static Logger logger = Logger.getLogger(VersionOnlineChecker.class);

	/**
	 * This function check on the website the new available version
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String newVersion() {

		try {

			HttpClient client = new HttpClient();
			// client.getHostConfiguration().setProxy("localhost", 8888);
			
			GetMethod gm = new GetMethod(
					"http://www.barrel-organ-discovery.org/version.php");
			
			NameValuePair[]  nvp = new NameValuePair[] {
					
					new NameValuePair("currentVersion", VersionTools.getVersion()),
					new NameValuePair("os.name", System.getProperty("os.name", "unknown")),
					new NameValuePair("os.arch", System.getProperty("os.arch", "unknown")),
					new NameValuePair("user.name", System.getProperty("user.name", "unknown")),
					new NameValuePair("user.language", Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry())
					
					
			};
			
			gm.setQueryString(nvp);
			
			int status = client.executeMethod(gm);
			if (status == 200) {

				String version = gm.getResponseBodyAsString();
				logger.debug("version of website :" + version);

				return version;
			}

			

		} catch (Exception ex) {
			System.out.println("can't check soft version :" + ex.getMessage());
		}
		
		return null;

	}
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("version : " + newVersion());
		
	}

}
