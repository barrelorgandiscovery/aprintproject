package org.barrelorgandiscovery.extensionsng.scanner.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
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
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.tools.ImageTools;

public class JImageFolderPreviewer extends JPanel {

  public JImageFolderPreviewer() throws Exception {
    initComponents();
  }

  private File folder;
  private ThumbnailDatabase thumbnailDatabase;

  public void loadFolder(File folder) throws Exception {
    this.folder = folder;
    this.thumbnailDatabase = new ThumbnailDatabase(folder, 150, 150);
    updateComponent();
  }

  private JList list;

  protected void initComponents() throws Exception {

    setLayout(new BorderLayout());
    list = new JList<>();

    list.setLayoutOrientation(JList.VERTICAL_WRAP);

    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setSelectionModel(new DefaultListSelectionModel());

    list.addListSelectionListener(
        new ListSelectionListener() {

          @Override
          public void valueChanged(ListSelectionEvent e) {
            // TODO Auto-generated method stub

          }
        });

    JScrollPane sp = new JScrollPane(list);
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    add(sp, BorderLayout.CENTER);
  }

  private void updateComponent() {

    if (folder != null && folder.isDirectory()) {
      File[] l = folder.listFiles();
      List<File> collectedList =
          Arrays.stream(l)
              .filter(
                  (e) -> {
                    if (e == null) {
                      return false;
                    }

                    return e.getName().endsWith(".jpg");
                  })
              .collect(Collectors.toList());
      DefaultListModel<Object> dm = new DefaultListModel<>();
      collectedList.forEach(
          (e) -> {
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

        Component c =
            list.getCellRenderer()
                .getListCellRendererComponent(
                    list, list.getModel().getElementAt(0), 0, false, false);

        Dimension preferredSizeOfOneTile = c.getPreferredSize();
        if (preferredSizeOfOneTile.height > 0) {
          // System.out.println("preferred height :" +
          // preferredSize.height);
          int newrowCount = getHeight() / preferredSizeOfOneTile.height;
          // System.out.println("new row count :" + newrowCount);
          list.setVisibleRowCount(newrowCount);
        }
      }
    }
    super.doLayout();
  }

  private static class ImageVerticalRenderer implements ListCellRenderer {

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

    public Component getListCellRendererComponent(
        JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

      try {

        File ins = (File) value;

        BufferedImage i = td.getOrCreate(ins);

        labelImage.setIcon(new ImageIcon(i));
        labelImage.setMaximumSize(new Dimension(200, 200));

        labelText.setText(ins.getName());

        p.setBackground(
            isSelected
                ? UIManager.getColor("Table.selectionBackground")
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

    File f =
        new File("C:\\projets\\APrint\\contributions\\patrice\\2018_josephine_90degres\\perfo");

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
