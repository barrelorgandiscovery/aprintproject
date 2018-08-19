package org.barrelorgandiscovery.recognition.gui;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Properties;

import javax.swing.JFrame;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprintng.APrintNGInternalFrame;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.prefs.DummyPrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.recognition.BookEdges;
import org.barrelorgandiscovery.recognition.IntArrayHolder;
import org.barrelorgandiscovery.recognition.RecognitionProject;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Factory;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.SerializeTools;

public class EdgeEditingWindow extends APrintNGInternalFrame {

	private static Logger logger = Logger.getLogger(EdgeEditingWindow.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 3558116336792895410L;

	private BufferedImage bi;
	private BookEdges edges;
	private JEdgeDrawingComponent drawingComponent;

	public EdgeEditingWindow(IPrefsStorage prefsStorage, BufferedImage bi,
			BookEdges edges) throws Exception {
		super(prefsStorage);
		this.bi = bi;
		// copy the edges
		this.edges = (BookEdges) SerializeTools.deepClone(edges);

		initComponents();

	}

	protected void initComponents() throws Exception {

		drawingComponent = new JEdgeDrawingComponent();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(drawingComponent, BorderLayout.CENTER);
		drawingComponent.setImage(bi);
		drawingComponent.setEdges(edges);

		drawingComponent.addDrawingEdgeListener(new DrawingEdgeListener() {

			public void polylineDraw(IntArrayHolder path) {
				logger.debug("polylineDraw :" + path);
			}
		});

	}

	public IntArrayHolder getEdges() {
		return edges;
	}

	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new ConsoleAppender(new PatternLayout()));

		APrintProperties aPrintProperties = new APrintProperties(false);
		Repository2 rep = Repository2Factory.create(new Properties(),
				aPrintProperties);

		Instrument ins = rep.getInstrument("24 Thibouville");

		RecognitionProject rp = new RecognitionProject(
				new File(
						"C:\\Projets\\Ada\\Projets\\LectureCartons\\images\\numrisation49limonaireponcifs"),
				ins, new File(
						"C:\\Projets\\Ada\\Projets\\LectureCartons\\build"));

		String imageName = "PICT0332-BorderMaker.jpg";
		BufferedImage image = rp.getImage(imageName);
		BookEdges edges = rp.getEdges(imageName);

		BufferedImage bi = ImageTools.loadImage(image);

		EdgeEditingWindow edgeEditingWindow = new EdgeEditingWindow(
				new DummyPrefsStorage(), bi, edges);

		edgeEditingWindow.setSize(400, 500);
		edgeEditingWindow.setVisible(true);
		edgeEditingWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
}
