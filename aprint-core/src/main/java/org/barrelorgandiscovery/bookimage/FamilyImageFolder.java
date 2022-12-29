package org.barrelorgandiscovery.bookimage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.ImageTools;

/**
 * Base class for listing images, using a filtering pattern
 * 
 * @author pfreydiere
 *
 */
public class FamilyImageFolder implements IFamilyImageSeeker {

	private static Logger logger = Logger.getLogger(FamilyImageFolder.class);

	protected static final int MAX_IMAGE_IN_FOLDER = 10_000;
	protected File folder;
	protected int count = 0;
	private File[] allFiles = null;
	private Pattern filePatternMatching;

	public FamilyImageFolder(File folder, Pattern filePatternMatching) {
		assert folder != null;
		assert folder.exists();
		assert folder.isDirectory();

		this.folder = folder;
		this.filePatternMatching = filePatternMatching;

		refreshListFiles(folder);

	}

	private void refreshListFiles(File folder) {
		// read all images
		assert folder != null;

		File[] allFiles = folder.listFiles();
		if (allFiles == null) {
			allFiles = new File[0];
		}

		if (filePatternMatching != null) {
			logger.debug("filter content");
			allFiles = folder.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					if (filePatternMatching.matcher(name).matches())
						return true;
					return false;
				}
			});

		}

		Arrays.sort(allFiles, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		this.allFiles = allFiles;
		this.count = this.allFiles.length;
	}

	/**
	 * load the image from index
	 * 
	 * @param sequence
	 * @return the full image
	 * @throws Exception
	 */
	public BufferedImage loadImage(int sequence) throws Exception {

		File f = allFiles[sequence];

		return ImageTools.loadImage(f.toURL());
	}

	/**
	 * count the number of images in folder assuming there are continuous
	 * 
	 * @return
	 */
	public int getImageCount() {
		return count;
	}

	/**
	 * get wurrent working folder
	 * 
	 * @return
	 */
	public File getFolder() {
		return folder;
	}

}
