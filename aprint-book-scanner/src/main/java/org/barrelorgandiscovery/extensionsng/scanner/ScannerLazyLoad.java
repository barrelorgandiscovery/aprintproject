package org.barrelorgandiscovery.extensionsng.scanner;

import java.awt.BorderLayout;

import org.barrelorgandiscovery.extensionsng.scanner.wizard.scanormerge.JScanOrMergeWizard;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.aprintng.APrintNGInternalFrame;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.repository.Repository2;

/**
 * this class load the scan extension classes when needed, to speed up the
 * aprint startup
 * 
 * @author pfreydiere
 *
 */
public class ScannerLazyLoad {

	public static void lazyLoadScanner(IPrefsStorage extensionPreferences, Repository2 repository, APrintNG application)
			throws Exception {

		JScanOrMergeWizard jScanOrMergeWizard = new JScanOrMergeWizard(extensionPreferences, repository,
				application.getCurrentExtensions());
		APrintNGInternalFrame frame = new APrintNGInternalFrame(extensionPreferences);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(jScanOrMergeWizard, BorderLayout.CENTER);
		frame.setVisible(true);
	}

}
