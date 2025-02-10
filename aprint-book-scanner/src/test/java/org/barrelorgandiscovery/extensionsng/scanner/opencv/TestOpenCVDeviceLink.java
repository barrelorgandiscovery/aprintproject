package org.barrelorgandiscovery.extensionsng.scanner.opencv;

public class TestOpenCVDeviceLink {
//
//	@BeforeClass
//	public void setup() {
//
//		// nu.pattern.OpenCV.loadShared();
//	}
//
//	@Test
//	public void testOpenVideoAndGetFrames() throws Exception {
//
//		Loader.load(opencv_java.class);
//
//		VideoCapture vcapture = new VideoCapture();
//
//		vcapture.open(
//				"C:\\projets\\APrint\\contributions\\plf\\2020-10_Essais saisie video\\carton ancien avec fond blanc.mp4");
//
//		boolean opened = vcapture.isOpened();
//		System.out.println("opened " + opened);
//		if (!opened) {
//			return;
//		}
//
//		JFrame jframe = new JFrame();
//		JLabel l = new JLabel();
//
//		jframe.getContentPane().setLayout(new BorderLayout());
//		jframe.getContentPane().add(l, BorderLayout.CENTER);
//
//		jframe.setSize(800, 600);
//		jframe.setVisible(true);
//
//		Mat m = new Mat();
//		boolean readReturn;
//
//		Mat d = new Mat();
//		
//		byte[] rgbcv = null;
//		BufferedImage gray = null;
//		
//		while ((readReturn = vcapture.read(m))) {
//			
//			System.out.println(m.width() + " x " + m.height() + " : " + m.depth());
//			System.out.println(readReturn);
//
//			System.out.println(m);
//
//			Imgproc.cvtColor(m, m, Imgproc.COLOR_RGB2RGBA, 0);
//			Imgproc.resize(m, d, new Size(m.width() / 2, m.height() / 2));
//
//			// Create an empty image in matching format
//			if (gray == null)  { gray = new BufferedImage(d.width(), d.height(), BufferedImage.TYPE_INT_RGB); }
//			if (rgbcv == null) { rgbcv = new byte[4 * d.height() * d.width()]; };
//
//			// Get the BufferedImage's backing array and copy the pixels directly into it
//			int[] data = ((DataBufferInt) gray.getRaster().getDataBuffer()).getData();
//			
//			d.get(0, 0, rgbcv);
//			// change image alignment
//			for (int i = 0; i < data.length; i++) {
//				final int value = rgbcv[i * 4] | (((int)rgbcv[i * 4 + 1]) & 0xFF) << 8  | ((int)rgbcv[i * 4 + 2] & 0xFF) << 16;
//				data[i] = value;
//			}
//			final BufferedImage b = gray;
//			SwingUtilities.invokeAndWait(() -> {
//				l.setIcon(new ImageIcon(b));
//			});
//
//		}
//
//	}
//
//	
//	/**
//	 * main 
//	 * @param args
//	 * @throws Exception
//	 */
//	public static void main(String[] args) throws Exception {
//		TestOpenCVDeviceLink t = new TestOpenCVDeviceLink();
//		t.setup();
//		t.testOpenVideoAndGetFrames();
//	}

}
