package org.barrelorgandiscovery.extensionsng.scanner.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.tools.Disposable;

/**
 * this class permit to view a folder and display associated images
 * 
 * @author pfreydiere
 *
 */
public class JImageFolderPreviewer extends JPanel implements Disposable {

	private static Logger logger = Logger.getLogger(JImageFolderPreviewer.class);

	ExecutorService executor;
	
	public JImageFolderPreviewer() throws Exception {
		this.executor = Executors.newSingleThreadExecutor();
		initComponents();
	}

	@Override
	public void dispose() {
		if (executor != null) {
			executor.shutdownNow();
			executor = null;
		}
	}

	private File folder;

	private ThumbnailDatabase thumbnailDatabase;

	public void loadFolder(File folder) throws Exception {
		this.folder = folder;
		this.thumbnailDatabase = new ThumbnailDatabase(folder, 150, 150);
		updateComponent();
	}
	
	private Pattern filePatternFiltering = null;
	public void setFilePattern(Pattern filePattern) {
		this.filePatternFiltering = filePattern;
		updateComponent();
	}

	private JList list;

	protected void initComponents() throws Exception {

		setLayout(new BorderLayout());
		list = new JList<>();

		list.setLayoutOrientation(JList.VERTICAL_WRAP);

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectionModel(new DefaultListSelectionModel());

		list.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {

			}
		});

		JScrollPane sp = new JScrollPane(list);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(sp, BorderLayout.CENTER);
	}

	private void updateComponent() {

		if (folder != null && folder.isDirectory()) {
			File[] l = folder.listFiles();
			List<File> collectedList = Arrays.stream(l).filter((e) -> {
				if (e == null) {
					return false;
				}
				String filename = e.getName();
				boolean isImage = filename.endsWith(".jpg") || e.getName().endsWith(".png");
				
				boolean filtered = true;
				if (filePatternFiltering != null) {
					Matcher matcher = filePatternFiltering.matcher(filename);
					filtered =  matcher.matches();
				}
				
				return isImage && filtered;
			}).collect(Collectors.toList());
			DefaultListModel<Object> dm = new DefaultListModel<>();
			collectedList.forEach((e) -> {
				dm.addElement(e);
			});
			list.setModel(dm);
			list.setCellRenderer(new ImageVerticalRenderer(thumbnailDatabase));
		}
	}

	@Override
	public void doLayout() {

		// System.out.println("Width :" + getWidth());
		// System.out.println("Height :" + getHeight());
		//
		// System.out.println("typical cell value :" +
		// l.getPrototypeCellValue());

		// récupération de la dimension d'un élément (en largeur)
		// preferred Size est calculée en fonction des éléments à l'intérieur
		// on récupère (aux inset près), la taille en largeur
		// d'une element pour ajuster le nombre de colonnes

		if (list != null && list.getModel() != null) {
			int instrumentNumber = list.getModel().getSize();
			if (instrumentNumber > 0) {

				Component c = list.getCellRenderer().getListCellRendererComponent(list, list.getModel().getElementAt(0),
						0, false, false);

				Dimension preferredSizeOfOneTile = c.getPreferredSize();
				if (preferredSizeOfOneTile.height > 0) {
					int newrowCount = getHeight() / preferredSizeOfOneTile.height;
					list.setVisibleRowCount(newrowCount);
				}
			}
		}
		super.doLayout();
	}
	
	private static BufferedImage EMPTY_IMAGE = new BufferedImage(100,100,BufferedImage.TYPE_4BYTE_ABGR);

	private class ImageVerticalRenderer implements ListCellRenderer {

		private JLabel labelImage = new JLabel();
		private JLabel labelText = new JLabel();
		
		
		public JPanel p = new JPanel();

		private ThumbnailDatabase td;

		public ImageVerticalRenderer(ThumbnailDatabase td) {
			assert td != null;
			this.td = td;
			BorderLayout bl = new BorderLayout();
			bl.setHgap(3);
			p.setLayout(bl);
			labelImage.setAlignmentY(BOTTOM_ALIGNMENT);
			p.add(labelImage, BorderLayout.CENTER);
			labelImage.setHorizontalAlignment(SwingConstants.CENTER);
			p.add(labelText, BorderLayout.SOUTH);
			labelText.setHorizontalAlignment(SwingConstants.CENTER);
			// small space in the bottom
			p.setBorder(new EmptyBorder(2, 2, 10, 2));
		}
		
		

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			try {

				File ins = (File) value;

				BufferedImage i = EMPTY_IMAGE;
				if (td.thumbnailExists(ins)) {
					 i = td.getOrCreate(ins);
				} else {
					//push to be computed
					JImageFolderPreviewer.this.executor.submit( () -> {
						try {
							td.getOrCreate(ins);
						
							SwingUtilities.invokeLater( () -> {
								JImageFolderPreviewer.this.repaint();
							});
							logger.debug("image generated");
						
						} catch(Exception ex) {
							logger.error(ex.getMessage(), ex);
						}
						
					});
					
				}
				
				labelImage.setIcon(new ImageIcon(i));
				labelImage.setMaximumSize(new Dimension(200, 200));

				labelText.setText(ins.getName());

				p.setBackground(isSelected ? UIManager.getColor("Table.selectionBackground")
						: UIManager.getColor("Table.background"));

				return p;

			} catch (Exception ex) {
				ex.printStackTrace(System.err);
				return p;
			}
		}
	}

	// test method
	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		File f = new File("C:\\projets\\APrint\\contributions\\patrice\\2018_josephine_90degres\\perfo");

		JFrame frame = new JFrame();
		frame.setSize(800, 600);

		frame.getContentPane().setLayout(new BorderLayout());
		JImageFolderPreviewer imagepreviewer = new JImageFolderPreviewer();

		frame.getContentPane().add(imagepreviewer, BorderLayout.CENTER);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		imagepreviewer.loadFolder(f);
	}
}
