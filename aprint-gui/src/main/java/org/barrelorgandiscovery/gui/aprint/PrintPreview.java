package org.barrelorgandiscovery.gui.aprint;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.MatteBorder;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;

public class PrintPreview extends JFrame implements Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6227333326634817099L;

	private static Logger logger = Logger.getLogger(PrintPreview.class);

	protected JScrollPane displayArea;

	protected int m_wPage;

	protected int m_hPage;

	protected int width;

	protected int height;

	protected Printable m_target;

	protected JComboBox m_cbScale;

	protected PreviewContainer m_preview;

	protected PageFormat pp_pf = null;

	protected JButton formatButton;

	protected JButton shrinkButton;

	private static boolean bScallToFitOnePage = false;

	protected void getThePreviewPages() {
		m_wPage = (int) (pp_pf.getWidth());
		m_hPage = (int) (pp_pf.getHeight());
		int scale = getDisplayScale();
		width = (int) Math.ceil(m_wPage * scale / 100);
		height = (int) Math.ceil(m_hPage * scale / 100);

		int pageIndex = 0;
		try {
			while (true) {

				BufferedImage img = new BufferedImage(m_wPage, m_hPage,
						BufferedImage.TYPE_INT_RGB);

				Graphics g = img.getGraphics();
				g.setColor(Color.white);
				g.fillRect(0, 0, m_wPage, m_hPage);

				if (bScallToFitOnePage) {
					m_target.print(g, pp_pf, -1);
					PagePreview pp = new PagePreview(width, height, img);
					m_preview.add(pp);
					break;
				} else if (m_target.print(g, pp_pf, pageIndex) != Printable.PAGE_EXISTS)
					break;

				// sauvegarde de l'image dans un répertoire temporaire ...
				File f;
				try {
					f = File.createTempFile("printpreviewaprint", "img"); //$NON-NLS-1$ //$NON-NLS-2$
					f.deleteOnExit();
					ImageIO.write(img, "png", f); //$NON-NLS-1$
				} catch (IOException ex) {

					// erreur lors de la sauvegarde ...
					logger.error("erreur pendant la sauvegarde de l'image", ex); //$NON-NLS-1$
					break;
				}

				// lecture de l'image
				Image im = Toolkit.getDefaultToolkit().createImage(
						f.getAbsolutePath());

				PagePreview pp = new PagePreview(width, height, im);
				m_preview.add(pp);
				pageIndex++;
			}
		} catch (OutOfMemoryError om) {
			JOptionPane
					.showMessageDialog(
							this,
							Messages.getString("PrintPreview.0"), //$NON-NLS-1$
							Messages.getString("PrintPreview.1"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
		} catch (PrinterException e) {
			e.printStackTrace();
			System.err
					.println(Messages.getString("PrintPreview.2") + e.toString()); //$NON-NLS-1$
		}
	}

	protected void previewThePages(int orientation) {
		if (displayArea != null)
			displayArea.setVisible(false);

		m_preview = new PreviewContainer();

		getThePreviewPages();

		displayArea = new JScrollPane(m_preview);
		getContentPane().add(displayArea, BorderLayout.CENTER);
		setVisible(true);
		System.gc();
	}

	protected void createButtons(JToolBar tb, boolean shrink) {
		JButton bt = new JButton(
				Messages.getString("PrintPreview.3"), new ImageIcon("print.gif")); //$NON-NLS-1$ //$NON-NLS-2$
		ActionListener lst = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					PrinterJob prnJob = PrinterJob.getPrinterJob();
					prnJob.setPrintable(m_target, pp_pf);
					if (prnJob.printDialog()) {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.WAIT_CURSOR));

						// demande d'impression en 300 dpi ...
						// HashPrintRequestAttributeSet as = new
						// HashPrintRequestAttributeSet();
						// as.add(new PrinterResolution(300, 300,
						// ResolutionSyntax.DPI));
						//						
						prnJob.print();

						setCursor(Cursor
								.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
					dispose();
				} catch (PrinterException ex) {
					ex.printStackTrace();
					System.err
							.println(Messages.getString("PrintPreview.5") + ex.toString()); //$NON-NLS-1$
				}
			}
		};
		bt.addActionListener(lst);
		bt.setAlignmentY(0.5f);
		bt.setMargin(new Insets(4, 6, 4, 6));
		tb.add(bt);

		if (pp_pf.getOrientation() == PageFormat.PORTRAIT)
			formatButton = new JButton(Messages.getString("PrintPreview.6")); //$NON-NLS-1$
		else
			formatButton = new JButton(Messages.getString("PrintPreview.7")); //$NON-NLS-1$

		lst = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pp_pf.getOrientation() == PageFormat.PORTRAIT) {
					pp_pf.setOrientation(PageFormat.LANDSCAPE);
					previewThePages(PageFormat.LANDSCAPE);
					formatButton.setText(Messages.getString("PrintPreview.8")); //$NON-NLS-1$
				} else {
					pp_pf.setOrientation(PageFormat.PORTRAIT);
					previewThePages(PageFormat.PORTRAIT);
					formatButton.setText(Messages.getString("PrintPreview.9")); //$NON-NLS-1$
				}
			}
		};
		formatButton.addActionListener(lst);
		formatButton.setAlignmentY(0.5f);
		formatButton.setMargin(new Insets(4, 6, 4, 6));
		tb.add(formatButton);

		if (shrink) {
			shrinkButton = new JButton(Messages.getString("PrintPreview.10")); //$NON-NLS-1$

			lst = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					bScallToFitOnePage = !bScallToFitOnePage;
					previewThePages(pp_pf.getOrientation());
				}
			};
			shrinkButton.addActionListener(lst);
			shrinkButton.setAlignmentY(0.5f);
			shrinkButton.setMargin(new Insets(4, 6, 4, 6));
			tb.add(shrinkButton);
		}

		bt = new JButton(Messages.getString("PrintPreview.11")); //$NON-NLS-1$
		lst = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				dispose();
			}
		};
		bt.addActionListener(lst);
		bt.setAlignmentY(0.5f);
		bt.setMargin(new Insets(4, 6, 4, 6));
		tb.add(bt);
	}

	public int getDisplayScale() {
		String str = m_cbScale.getSelectedItem().toString();
		if (str.endsWith("%")) //$NON-NLS-1$
			str = str.substring(0, str.length() - 1);
		str = str.trim();
		int scale = 0;
		try {
			scale = Integer.parseInt(str);
		} catch (NumberFormatException ex) {
			return 25;
		}
		return scale;
	}

	public void setDisplayScale(int scale) {

		ComboBoxModel model = m_cbScale.getModel();
		int size = model.getSize();
		for (int i = 0; i < size; i++) {
			String str = model.getElementAt(i).toString();
			if (str.endsWith("%")) //$NON-NLS-1$
				str = str.substring(0, str.length() - 1);
			str = str.trim();

			if (Integer.toString(scale).equals(str)) {
				m_cbScale.setSelectedItem(model.getElementAt(i));
				return;
			}

		}

	}

	/**
	 * Class permettant l'aperçu avant impression
	 * 
	 * @param target
	 *            objet à imprimer
	 */
	public PrintPreview(Printable target) {
		this(target, Messages.getString("PrintPreview.13"), false); //$NON-NLS-1$
	}

	public PrintPreview(Printable target, PageFormat pf) {
		this(target, Messages.getString("PrintPreview.13"), false, pf); //$NON-NLS-1$
	}

	/**
	 * Class permettant l'aperçu avant impression
	 * 
	 * @param target
	 *            objet à imprimer
	 * @param title
	 */
	public PrintPreview(Printable target, String title) {
		this(target, title, false);
	}

	/**
	 * Class permettant l'aperçu avant impression
	 * 
	 * @param target
	 * @param title
	 * @param shrink
	 */
	public PrintPreview(Printable target, String title, boolean shrink) {
		this(target, title, shrink, null);
	}

	public PrintPreview(Printable target, String title, boolean shrink,
			PageFormat defaultpageformat) {
		super(title);
		setIconImage(new ImageIcon("teloptica.gif").getImage()); //$NON-NLS-1$
		bScallToFitOnePage = false; // reset to default
		PrinterJob prnJob = PrinterJob.getPrinterJob();
		pp_pf = defaultpageformat;

		if (pp_pf == null)
			pp_pf = prnJob.defaultPage();

		if (pp_pf.getHeight() == 0 || pp_pf.getWidth() == 0) {
			System.err.println(Messages.getString("PrintPreview.15")); //$NON-NLS-1$
			return;
		}

		setSize(600, 400);
		m_target = target;

		displayArea = null;
		m_preview = null;

		JToolBar tb = new JToolBar();
		createButtons(tb, shrink);

		String[] scales = { "10 %", "25 %", "50 %", "100 %", "200 %" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		m_cbScale = new JComboBox(scales);
		m_cbScale.setSelectedIndex(3);
		ActionListener lst = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread runner = new Thread(PrintPreview.this);
				runner.start();
			}
		};
		m_cbScale.addActionListener(lst);
		m_cbScale.setMaximumSize(m_cbScale.getPreferredSize());
		m_cbScale.setEditable(true);
		tb.addSeparator();
		tb.add(m_cbScale);
		getContentPane().add(tb, BorderLayout.NORTH);

		// previewThePages(PageFormat.PORTRAIT);
		previewThePages(pp_pf.getOrientation());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	public void run() {
		int scale = getDisplayScale();
		width = (int) (m_wPage * scale / 100);
		height = (int) (m_hPage * scale / 100);

		Component[] comps = m_preview.getComponents();
		for (int k = 0; k < comps.length; k++) {
			if (!(comps[k] instanceof PagePreview))
				continue;
			PagePreview pp = (PagePreview) comps[k];
			pp.setScaledSize(width, height);
		}
		m_preview.doLayout();
		m_preview.getParent().getParent().validate();
	}

	/**
	 * Classe interne
	 * 
	 */
	class PreviewContainer extends JPanel {
		/**
		 * No Identifiant pour la sérialisation
		 */
		private static final long serialVersionUID = 3432120445985995972L;

		protected int H_GAP = 16;

		protected int V_GAP = 10;

		public Dimension getPreferredSize() {
			int n = getComponentCount();
			if (n == 0)
				return new Dimension(H_GAP, V_GAP);
			Component comp = getComponent(0);
			Dimension dc = comp.getPreferredSize();
			int w = dc.width;
			int h = dc.height;

			Dimension dp = getParent().getSize();
			int nCol = Math.max((dp.width - H_GAP) / (w + H_GAP), 1);
			int nRow = n / nCol;
			if (nRow * nCol < n)
				nRow++;

			int ww = nCol * (w + H_GAP) + H_GAP;
			int hh = nRow * (h + V_GAP) + V_GAP;
			Insets ins = getInsets();
			return new Dimension(ww + ins.left + ins.right, hh + ins.top
					+ ins.bottom);
		}

		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		public void doLayout() {
			Insets ins = getInsets();
			int x = ins.left + H_GAP;
			int y = ins.top + V_GAP;

			int n = getComponentCount();
			if (n == 0)
				return;
			Component comp = getComponent(0);
			Dimension dc = comp.getPreferredSize();
			int w = dc.width;
			int h = dc.height;

			Dimension dp = getParent().getSize();
			int nCol = Math.max((dp.width - H_GAP) / (w + H_GAP), 1);
			int nRow = n / nCol;
			if (nRow * nCol < n)
				nRow++;

			int index = 0;
			for (int k = 0; k < nRow; k++) {
				for (int m = 0; m < nCol; m++) {
					if (index >= n)
						return;
					comp = getComponent(index++);
					comp.setBounds(x, y, w, h);
					x += w + H_GAP;
				}
				y += h + V_GAP;
				x = ins.left + H_GAP;
			}
		}
	}

	class PagePreview extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -577052596743623720L;

		protected int m_w;

		protected int m_h;

		protected Image m_img;

		public PagePreview(int w, int h, Image source) {
			m_w = w;
			m_h = h;
			m_img = source;
			setBackground(Color.white);
			setBorder(new MatteBorder(1, 1, 2, 2, Color.black));
		}

		public void setScaledSize(int w, int h) {
			m_w = w;
			m_h = h;
			repaint();
		}

		public Dimension getPreferredSize() {
			Insets ins = getInsets();
			return new Dimension(m_w + ins.left + ins.right, m_h + ins.top
					+ ins.bottom);
		}

		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		public void paint(Graphics g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			Graphics2D g2d = (Graphics2D) g;

			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);

			g.drawImage(m_img, 0, 0, m_w, m_h, this);
			paintBorder(g);
		}
	}
	
	
}

