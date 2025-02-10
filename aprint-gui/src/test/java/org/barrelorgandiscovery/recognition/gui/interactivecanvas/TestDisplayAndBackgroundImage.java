package org.barrelorgandiscovery.recognition.gui.interactivecanvas;

import java.awt.BorderLayout;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.barrelorgandiscovery.bookimage.ZipBookImage;
import org.barrelorgandiscovery.gui.aedit.CreationTool;
import org.barrelorgandiscovery.gui.aedit.ImageAndHolesVisualizationLayer;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.RectSelectTool;
import org.barrelorgandiscovery.images.books.tools.BookImageRecognitionTiledImage;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO.VirtualBookResult;

public class TestDisplayAndBackgroundImage {

	/**
	 * add hole in layer, for recognition
	 *
	 */
	private static class LayerHoleAdd extends CreationTool {
		public LayerHoleAdd(JEditableVirtualBookComponent virtualBookComponent, ImageAndHolesVisualizationLayer layer)
				throws Exception {
			super(virtualBookComponent, null, null, new CreationTool.CreationToolAction() {
				@Override
				public void handleAction(Hole n, boolean isRemove) {
					ArrayList<Hole> holes = layer.getHoles();
					if (holes == null) {
						holes = new ArrayList<>();
					}
					holes.add(n);
					layer.setHoles(holes);
					layer.setVisible(true);

					assert virtualBookComponent != null;
					virtualBookComponent.repaint();
				}
			});
		}
	}
	
	/**
	 * select rect in book, for recognition
	 *
	 */
	private static class RectHoleAdd extends RectSelectTool {
		public RectHoleAdd(JEditableVirtualBookComponent virtualBookComponent, ImageAndHolesVisualizationLayer layer)
				throws Exception {
			super(virtualBookComponent, null, new RectSelectTool.RectSelectToolListener() {

				@Override
				public void rectDrawn(double xmin, double ymin, double xmax, double ymax) {
					double w = xmax - xmin;
					double h = ymax - ymin;
					if (w < 0 || h < 0) {
						// skip
						return;
					}
					layer.setVisible(true);
					layer.getAdditionalShapes().add(new Rectangle2D.Double(xmin, ymin, w, h));
					virtualBookComponent.repaint();
				}
			});

		}

	}

	public static void main(String[] args) throws Exception {

		JFrame jframe = new JFrame();
		jframe.setSize(800, 600);

		JEditableVirtualBookComponent vbc = new JEditableVirtualBookComponent();

		File testFolder = new File("/home/use/projets/APrint/contributions/plf/2020-10_Essais saisie video");

		VirtualBookResult r = VirtualBookXmlIO
				.read(new File(testFolder, "45Limonaire_FondBlanc_recognition_20201122.book"));

		vbc.setVirtualBook(r.virtualBook);

		jframe.getContentPane().setLayout(new BorderLayout());
		jframe.getContentPane().add(vbc, BorderLayout.CENTER);

		ImageAndHolesVisualizationLayer vizLayer = new ImageAndHolesVisualizationLayer();
		vbc.addLayer(vizLayer);

		BookImageRecognitionTiledImage b = new BookImageRecognitionTiledImage(
				new ZipBookImage(new File(testFolder, "45Limonaire_FondBlanc.bookimage")));

		vizLayer.setTiledBackgroundimage(b);

		
		vbc.setCurrentTool(new RectHoleAdd(vbc, vizLayer));

		jframe.setVisible(true);

	}

}
