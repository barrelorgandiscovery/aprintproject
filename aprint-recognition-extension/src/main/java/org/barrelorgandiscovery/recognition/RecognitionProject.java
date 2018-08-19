package org.barrelorgandiscovery.recognition;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.tools.SystemExecutor;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO.VirtualBookResult;

/**
 * Model class for a recognition project
 * 
 * @author pfreydiere
 * 
 */
public class RecognitionProject {

	private static final String INSTRUMENT2 = "instrument";
	private static final String OFFSET2 = "offset";
	private static final String EDGES = "edges";
	private static final String HOLES = "holes";

	private static Logger logger = Logger.getLogger(RecognitionProject.class);

	private File folder;
	private Instrument instrument;
	private VirtualBook vb;

	private File exeFolder;

	/**
	 * Image Recognition project objet
	 * 
	 * @param folder
	 *            the folder in which the images and recognition will be stored
	 * @param scale
	 *            the scale of the instrument
	 * @throws Exception
	 */
	public RecognitionProject(File folder, Instrument instrument, File exeFolder)
			throws Exception {
		this.folder = folder;
		this.instrument = instrument;
		this.exeFolder = exeFolder;
		getExe(); // for checking the exe

		saveObject(INSTRUMENT2, instrument.getName(), INSTRUMENT2);

		vb = new VirtualBook(instrument.getScale());

	}

	/**
	 * init a recognition project from the folder and the repository collection
	 * this constructor assume that the project has already been created
	 * 
	 * @param folder
	 *            the folder image project
	 * @param repository
	 *            the instrument repository
	 * @throws Exception
	 */
	public RecognitionProject(File folder, Repository2 repository,
			File exeFolder) throws Exception {
		this.folder = folder;
		this.exeFolder = exeFolder;
		getExe(); // for checking the exe
		
		// read the instrument name
		String instrumentName = (String) readObject(INSTRUMENT2, INSTRUMENT2);
		if (instrumentName == null)
			throw new Exception("the projet has not yet been created");
		Instrument instrument = repository.getInstrument(instrumentName);
		if (instrument == null)
			throw new Exception("Instrument " + instrumentName + " not found");
		this.instrument = instrument;
		vb = new VirtualBook(instrument.getScale());
		readVirtualBook();
	}

	/**
	 * List Images in the project
	 */
	public String[] listImageNames() throws Exception {
		String[] names = folder.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jpg");
			}
		});

		Arrays.sort(names, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return StringTools.compare(o1, o2);
			}
		});
		return names;
	}

	/**
	 * book image
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public BufferedImage getImage(String name) throws Exception {
		return ImageTools.loadImage(Toolkit.getDefaultToolkit().createImage(
				new File(folder, name).toURL()));
	}

	/**
	 * mini image, generated with the stamps (edges and recognized holes)
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public BufferedImage getMiniImage(String name) throws Exception {
		BufferedImage bi = ImageTools.crop(50, 50, getImage(name));

		BufferedImage bords = ImageTools.loadImage(Toolkit.getDefaultToolkit()
				.createImage(getClass().getResource("kpager.png")));

		BufferedImage holes = ImageTools.loadImage(Toolkit.getDefaultToolkit()
				.createImage(getClass().getResource("kcmmidi.png")));

		Graphics2D g2d = bi.createGraphics();
		try {

			if (getEdges(name) != null) {
				g2d.drawImage(bords, bi.getWidth() - bords.getWidth(), 0, null);
			}

			if (getHoles(name) != null) {
				g2d.drawImage(holes, bi.getWidth() - holes.getWidth(),
						bi.getHeight() - holes.getHeight(), null);
			}

		} finally {
			g2d.dispose();
		}

		return bi;
	}

	private File getExe() throws Exception {
		File f = new File(exeFolder, "RawLectureCarton.exe");
		if (!f.exists())
			throw new Exception("Exe RawLectureCarton.exe not found in "
					+ f.getAbsolutePath());
		
		return f;
	}

	/**
	 * Launch the recognition and get the edge information plus atomatic reading
	 * informations
	 * 
	 * @param name
	 * @throws Exception
	 */
	public void recognize(String name) throws Exception {

		// prepare the execution of the Ada process

		Scale scale = instrument.getScale();

		String[] cmdline = new String[] {
				getExe().getAbsolutePath(),
				"" + (scale.getWidth() / 10.0),
				"" + scale.getIntertrackHeight(), "" + scale.getTrackWidth(),
				"" + scale.getFirstTrackAxis(),

				"" + scale.getTrackNb(),
				"\"" + new File(folder, name).getAbsolutePath() + "\"" };

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		SystemExecutor.execute(cmdline, baos);

		if (logger.isDebugEnabled())
			logger.debug(new String(baos.toByteArray()));

		// read BAOS Stream to get Edges definition
		// ---BORDS
		// ---ENDBORDS

		// edges are necessary for the image recognition
		BookEdges be = new BookEdges();

		try {

			LineNumberReader reader = new LineNumberReader(
					new InputStreamReader(new ByteArrayInputStream(
							baos.toByteArray())));

			String line;
			// micro parser
			int state = 0;

			while ((line = reader.readLine()) != null) {

				switch (state) {

				case 0:
					if ("---BORDS".equals(line))
						state = 1;
					break;
				case 1:

					if ("---ENDBORDS".equals(line)) {
						state = 3;
						break;
					}
					// parsing number ...
					line = line.trim();
					StringTokenizer st = new java.util.StringTokenizer(line,
							" ");

					int pos = Integer.parseInt(st.nextToken());
					int length = Integer.parseInt(st.nextToken());

					be.addElement(pos, length);

				default:

				}

			}

			if (logger.isDebugEnabled()) {
				logger.debug("edges :" + be.toString());
			}

			setEdges(name, be);
		} catch (Exception ex) {
			logger.error("fail to read the edges :" + ex.getMessage(), ex);
		}

		try {
			// read image content and set the Holes ...
			PPMImageReader ppmReader = new PPMImageReader(new File(folder, name
					+ ".LU.ppm"));
			try {

				BufferedImage bi = ppmReader.readFullImage();
				BufferedImage biimage = getImage(name);

				int computeMeanLength = be.computeMeanLength();

				double widthfactor = 1.0 * biimage.getHeight()
						/ computeMeanLength;

				double width = scale.getWidth();
				double xResolution = width / computeMeanLength / widthfactor;

				ArrayList<Hole> holes = new ArrayList<Hole>();
				long startedTime = -1;
				for (int j = 0; j < bi.getHeight(); j++) {
					startedTime = -1;

					for (int i = 0; i < bi.getWidth(); i++) {

						int rgb = bi.getRGB(i, j);

						if ((rgb & 0xFFFFFF) == 0) {
							// noir
							if (startedTime >= 0) {

								int track = j;
								if (scale.isPreferredViewedInversed()) {
									track = scale.getTrackNb() - track - 1;
								}

								Hole h = new Hole(track, startedTime,
										scale.mmToTime(i * xResolution)
												- startedTime);
								holes.add(h);
								logger.debug("hole added :" + h);
								startedTime = -1;
							}

						} else {
							// blanc
							if (startedTime < 0) {
								startedTime = (long) scale.mmToTime(i
										* xResolution);
							}

						}

					} // i in width

				}// j in height

				setHoles(name, holes);

			} finally {
				ppmReader.close();
			}

		} catch (Exception ex) {
			logger.error(
					"fail to read the recognized image :" + ex.getMessage(), ex);
		}
	}

	/**
	 * internal Save object in a file
	 * 
	 * @param name
	 * @param object
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void saveObject(String name, Serializable object, String suffix)
			throws Exception {
		File f = constructXXFiles(name, suffix);

		if (object == null) {
			f.delete();
			return;
		}

		FileOutputStream fos = new FileOutputStream(f);
		try {
			DataOutputStream dataOutputStream = new DataOutputStream(fos);
			ObjectOutputStream oos = new ObjectOutputStream(dataOutputStream);
			oos.writeObject(object);
		} finally {
			fos.close();
		}
	}

	/**
	 * internal read object function
	 * 
	 * @param name
	 * @param suffix
	 * @return
	 * @throws Exception
	 */
	private Serializable readObject(String name, String suffix)
			throws Exception {

		File f = constructXXFiles(name, suffix);
		if (!f.exists())
			return null;

		FileInputStream fis = new FileInputStream(f);
		try {
			DataInputStream dataInputStream = new DataInputStream(fis);
			ObjectInputStream ois = new ObjectInputStream(dataInputStream);
			return (Serializable) ois.readObject();
		} finally {
			fis.close();
		}

	}

	/**
	 * define edge informations for this image
	 * 
	 * @param name
	 * @param be
	 * @throws Exception
	 */
	public void setEdges(String name, BookEdges be) throws Exception {

		saveObject(name, be, EDGES);
	}

	/**
	 * Get Edge informations for this image
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public BookEdges getEdges(String name) throws Exception {
		try {
			return (BookEdges) readObject(name, EDGES);
		} catch (Throwable ex) {
			logger.debug("fail to load edges " + ex.getMessage(), ex);
			return null;
		}
	}

	/**
	 * set the associated holes
	 * 
	 * @param name
	 * @param holes
	 * @throws Exception
	 */
	public void setHoles(String name, ArrayList<Hole> holes) throws Exception {
		saveObject(name, holes, HOLES);
	}

	/**
	 * get the associated holes
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Hole> getHoles(String name) throws Exception {
		return (ArrayList<Hole>) readObject(name, HOLES);
	}

	/**
	 * construct a file associated to the main image for storing objects
	 * 
	 * @param name
	 * @param prefix
	 * @return
	 */
	private File constructXXFiles(String name, String prefix) {
		return new File(folder, name + "." + prefix);
	}

	/**
	 * Compute a wrap image from the book edges
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public BufferedImage computeWarpImage(String name) throws Exception {

		BufferedImage image = getImage(name);
		if (image == null)
			return null;

		BookEdges be = getEdges(name);
		if (be == null)
			return null;

		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);

		BufferedImage bi = new BufferedImage(imageWidth, imageHeight,
				BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < imageWidth; i++) {
			for (int j = 0; j < imageHeight; j++) {

				int[] e = be.getElement(i);
				int start = e[0];
				int length = e[1];
				int rgb = image.getRGB(i, start
						+ (int) (length * (1.0 * j / imageHeight)));

				bi.setRGB(i, j, rgb);

			}
		}
		return bi;
	}

	public void setImageOffset(String name, Double offset) throws Exception {
		saveObject(name, offset, OFFSET2);
	}

	public Double getImageOffset(String name) throws Exception {
		return (Double) readObject(name, OFFSET2);
	}

	public void setHolesXOffset(String name, Double xscale) throws Exception {
		saveObject(name, xscale, "xscale");
	}

	public Double getHolesXOffset(String name) throws Exception {
		return (Double) readObject(name, "xscale");
	}

	public VirtualBook getVirtualBook() {
		return vb;
	}

	private void readVirtualBook() throws Exception {
		File fb = constructVirtualBookFile();
		if (!fb.exists())
			return;
		VirtualBookResult r = VirtualBookXmlIO.read(fb);
		this.vb = r.virtualBook;
	}

	/**
	 * @return
	 */
	protected File constructVirtualBookFile() {
		return new File(folder, "vb.book");
	}

	public void saveVirtualBook() throws Exception {

		VirtualBookXmlIO.write(
				new FileOutputStream(constructVirtualBookFile()), vb,
				instrument.getName());

	}

	/**
	 * get the project instrument
	 * 
	 * @return the instrument
	 */
	public Instrument getInstrument() {
		return instrument;
	}
}
