package org.barrelorgandiscovery.gui.aprintng;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicLabelUI;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.gui.explorer.ExplorerListener;
import org.barrelorgandiscovery.gui.explorer.JExplorer;

public class APrintNGWelcomeWithFilesPanel extends JPanel {

	private static final long serialVersionUID = 1417254944646219629L;

	APrintNG aprintng;
	IExtension[] extensions;

	public APrintNGWelcomeWithFilesPanel(APrintNG aprintng, IExtension[] extensions) throws Exception {
		this.aprintng = aprintng;
		this.extensions = extensions;

		initComponents();
	}

	/**
	 * This is the template for Classes.
	 *
	 *
	 * @since carbon 1.0
	 * @author Greg Hinkle, January 2002
	 * @version $Revision: 1.4 $($Author: dvoet $ / $Date: 2003/05/05 21:21:27 $)
	 * @copyright 2002 Sapient
	 */

	public static class VerticalLabelUI extends BasicLabelUI {
		static {
			labelUI = new VerticalLabelUI(false);
		}

		protected boolean clockwise;

		public VerticalLabelUI(boolean clockwise) {
			super();
			this.clockwise = clockwise;
		}

		public Dimension getPreferredSize(JComponent c) {
			Dimension dim = super.getPreferredSize(c);
			return new Dimension(dim.height, dim.width);
		}

		private static Rectangle paintIconR = new Rectangle();
		private static Rectangle paintTextR = new Rectangle();
		private static Rectangle paintViewR = new Rectangle();
		private static Insets paintViewInsets = new Insets(0, 0, 0, 0);

		public void paint(Graphics g, JComponent c) {

			JLabel label = (JLabel) c;
			String text = label.getText();
			Icon icon = (label.isEnabled()) ? label.getIcon() : label.getDisabledIcon();

			if ((icon == null) && (text == null)) {
				return;
			}

			FontMetrics fm = g.getFontMetrics();
			paintViewInsets = c.getInsets(paintViewInsets);

			paintViewR.x = paintViewInsets.left;
			paintViewR.y = paintViewInsets.top;

			// Use inverted height & width
			paintViewR.height = c.getWidth() - (paintViewInsets.left + paintViewInsets.right);
			paintViewR.width = c.getHeight() - (paintViewInsets.top + paintViewInsets.bottom);

			paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
			paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

			String clippedText = layoutCL(label, fm, text, icon, paintViewR, paintIconR, paintTextR);

			Graphics2D g2 = (Graphics2D) g;
			AffineTransform tr = g2.getTransform();
			if (clockwise) {
				g2.rotate(Math.PI / 2);
				g2.translate(0, -c.getWidth());
			} else {
				g2.rotate(-Math.PI / 2);
				g2.translate(-c.getHeight(), 0);
			}

			if (icon != null) {
				icon.paintIcon(c, g, paintIconR.x, paintIconR.y);
			}

			if (text != null) {
				int textX = paintTextR.x;
				int textY = paintTextR.y + fm.getAscent();

				if (label.isEnabled()) {
					paintEnabledText(label, g, clippedText, textX, textY);
				} else {
					paintDisabledText(label, g, clippedText, textX, textY);
				}
			}

			g2.setTransform(tr);
		}
	}

	protected void initComponents() throws Exception {

		APrintNGWelcomePanel aPrintNGWelcomePanel = new APrintNGWelcomePanel(aprintng, extensions);

		BorderLayout bl = new BorderLayout();
		setLayout(bl);

		JTabbedPane tp = new JTabbedPane();

		add(tp, BorderLayout.CENTER);

		JExplorer exp = new JExplorer();

		tp.addTab("Welcome", aPrintNGWelcomePanel);

		// Create vertical labels to render tab titles
		JLabel labTab1 = new JLabel("Welcome");
		labTab1.setUI(new VerticalLabelUI(false)); // true/false to make it upwards/downwards
		tp.setTabComponentAt(0, labTab1); // For component1

		tp.addTab("Explorer",
				new JScrollPane(exp, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		JLabel labTab2 = new JLabel("File Explorer");
		labTab2.setUI(new VerticalLabelUI(false));
		tp.setTabComponentAt(1, labTab2); // For component2

		tp.setTabPlacement(JTabbedPane.LEFT);
		tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		exp.addExplorerListener(new ExplorerListener() {
			
			@Override
			public void selectionChanged(FileObject[] fo) throws Exception {
				
			}
			
			@Override
			public void doubleClick(FileObject fo) throws Exception {
				aprintng.openFile((AbstractFileObject)fo);				
			}
		});

	}
}
