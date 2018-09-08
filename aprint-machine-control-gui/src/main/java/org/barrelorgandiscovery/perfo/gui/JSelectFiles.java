package org.barrelorgandiscovery.perfo.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;

import javax.swing.AbstractButton;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import org.barrelorgandiscovery.perfo.PunchProcess;

import com.jeta.forms.components.panel.FormPanel;

public class JSelectFiles extends JPanel implements IPunchMachinePanelActivate {

  private PunchProcess punchProcess;
  private Navigation navigation;

  private JList fileList;

  public JSelectFiles(PunchProcess punchProcess, Navigation navigation) throws Exception {
    super();
    this.punchProcess = punchProcess;
    this.navigation = navigation;
    initComponents();
  }

  protected void initComponents() throws Exception {

    FormPanel p = new FormPanel(getClass().getResourceAsStream("selectfiles.jfrm"));

    AbstractButton cancelbtn = p.getButton("cancel");
    cancelbtn.addActionListener(
        (e) -> {
          navigation.navigateTo(this, PunchScreen.Hello);
        });

    AbstractButton punchButton = p.getButton("punch");
    punchButton.addActionListener(
        (e) -> {
          navigation.navigateTo(this, PunchScreen.Punch);
        });

    setLayout(new BorderLayout());
    add(p, BorderLayout.CENTER);

    JList list = new JList<>();
    list.setCellRenderer(
        new ListCellRenderer<File>() {
          @Override
          public Component getListCellRendererComponent(
              JList<? extends File> list,
              File value,
              int index,
              boolean isSelected,
              boolean cellHasFocus) {

            JCheckBox cb = new JCheckBox(value.getName());
            cb.setSelected(isSelected);
            return cb;
          }
        });

    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    list.setSelectionModel(
        new DefaultListSelectionModel() {
          @Override
          public void setSelectionInterval(int index0, int index1) {
            if (super.isSelectedIndex(index0)) {
              super.removeSelectionInterval(index0, index1);
            } else {
              super.addSelectionInterval(index0, index1);
            }
          }
        });
    this.fileList = list;

    JScrollPane sp = new JScrollPane(fileList);
    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    sp.getVerticalScrollBar().setPreferredSize(new Dimension(30, 0));
    p.getFormAccessor().replaceBean("fileselect", sp);
  }

  @Override
  public void activate() {

    punchProcess.searchForFile();

    File[] files = punchProcess.getFiles();

    this.fileList.setListData(files);

    assert files != null;
  }

  public File[] getSelectedFiles() {

    Object[] selectedValues = this.fileList.getSelectedValues();
    if (selectedValues == null) {
      return new File[0];
    }
    File[] f = new File[selectedValues.length];
    for (int i = 0; i < selectedValues.length; i++) {
      f[i] = (File) selectedValues[i];
    }
    return f;
  }
}
