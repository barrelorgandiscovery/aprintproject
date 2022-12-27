package org.barrelorgandiscovery.tools.bugsreports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.tools.StreamsTools;

/**
 * Bug Reporter utility class
 */
public class BugReporter {

	private static final String REPORT_COMMAND = "upload.php";
	private static final String PING_COMMAND = "ping.php";
	private static File reportfile = null;
	private static String applicationname = null;

	public static void init(String applicationname) throws Exception {

		BugReporter.applicationname = applicationname;

		reportfile = File.createTempFile("log", "log");
		reportfile.deleteOnExit();
		BasicConfigurator.configure(getAppender());

	}

	/**
	 * get the log4J appender associated to the bug reporter
	 * 
	 * @return
	 */
	public static Appender getAppender() throws Exception {
		return new FileAppender(new PatternLayout("%r %-5p %C:%L [%t]: %m%n"), reportfile.getAbsolutePath());
	}

	private static ExecutorService bugReportSender = Executors.newSingleThreadExecutor();

	/**
	 * Send the log file to the developper using Http access.
	 */
	public static void sendBugReport() {
		bugReportSender.execute(new Runnable() {
			public void run() {
				try {

					HttpClient client = new HttpClient();

					// client.getHostConfiguration().setProxy("localhost",
					// 8888);

					GetMethod pingm = new GetMethod("http://www.barrel-organ-discovery.org/bugreport/" + PING_COMMAND);

					int pingstatus = client.executeMethod(pingm);

					if (pingstatus != 200)
						throw new Exception("bug server not found ...");

					String reportURL = pingm.getURI().toString();
					reportURL = reportURL.substring(0, reportURL.indexOf(PING_COMMAND)) + REPORT_COMMAND;

					File tempfile = File.createTempFile("report", "zip");

					FileOutputStream fos = new FileOutputStream(tempfile);

					// compactage des elements dans un fichier zip ...
					ZipOutputStream zipos = new ZipOutputStream(fos);
					try {
						// ecriture du log d'erreurs pour la session en cours
						// ...
						zipos.putNextEntry(new ZipEntry("logreport.log"));

						if (reportfile == null) {
							System.out.println(
									"can't send bug report, the bug report library might not have been properly initialized");
							return;
						}

						StreamsTools.copyStream(new FileInputStream(reportfile), zipos);
						zipos.closeEntry();

						zipos.putNextEntry(new ZipEntry("version"));
						StreamsTools.copyStream(APrintNG.class.getClassLoader().getResourceAsStream("aprintversion.properties"),
								zipos);
						zipos.closeEntry();

					} finally {
						zipos.close();
					}

					// send bug report

					PostMethod filePost = new PostMethod(reportURL);
					Part[] parts = { new StringPart("uid", System.getProperty("user.name")),
							new FilePart("file", "bugreport", tempfile) };
					filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));

					// filePost.setFollowRedirects(true);

					int status = client.executeMethod(filePost);

					if (status != 200) {
						System.err.println("Erreur dans l'envoi du rapport de bug -> code " + status);
						System.err.println("return from server :" + filePost.getResponseBodyAsString());
					} else {
						System.out.println("return from server :" + filePost.getResponseBodyAsString());
					}

				} catch (Exception ex) {
					// silently abord the problem ...
					ex.printStackTrace(System.err);
				}

			}
		});

	}

	/**
	 * Save the bug report in an external file
	 * 
	 * @param fileInWhichSaveTheBugReport
	 * @throws Exception
	 */
	public static void saveBugReport(File fileInWhichSaveTheBugReport) throws Exception {
		FileInputStream fileInputStream = new FileInputStream(reportfile);
		FileOutputStream fos = new FileOutputStream(fileInWhichSaveTheBugReport);
		try {
			StreamsTools.copyStream(fileInputStream, fos);
		} finally {
			fos.close();
		}
	}

	public static void saveBugReport(OutputStream fileInWhichSaveTheBugReport) throws Exception {
		FileInputStream fileInputStream = new FileInputStream(reportfile);
		StreamsTools.copyStream(fileInputStream, fileInWhichSaveTheBugReport);

	}

}
