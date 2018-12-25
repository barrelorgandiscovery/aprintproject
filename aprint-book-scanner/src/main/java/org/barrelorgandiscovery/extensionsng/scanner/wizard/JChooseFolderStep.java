package org.barrelorgandiscovery.extensionsng.scanner.wizard;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.scanner.PerfoScanFolder;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.prefs.IPrefsStorage;

import com.jeta.forms.components.panel.FormPanel;
import com.l2fprod.common.swing.JDirectoryChooser;

public class JChooseFolderStep extends BasePanelStep {

  /** */
  private static final long serialVersionUID = 5969685883805568698L;

  private static final String DEFAULTCURRENTFOLDER = "defaultcurrentfolder";

  private static Logger logger = Logger.getLogger(JChooseFolderStep.class);

  private IPrefsStorage ps;

  public JChooseFolderStep(IPrefsStorage ps) throws Exception {
    super("StepChooseFolder", null);
    this.ps = ps;
    assert ps != null;
    initComponents();
  }

  private JLabel folderLabel;

  private JImageFolderPreviewer imagefolderpreviewere = new JImageFolderPreviewer();

  protected void initComponents() throws Exception {

    InputStream is = getClass().getResourceAsStream("perfoscanchoose.jfrm");
    assert is != null;
    FormPanel fp = new FormPanel(is);

    AbstractButton b = fp.getButton("btnchoose");
    b.setText("Choose Folder ...");

    b.addActionListener(
        (e) -> {
          JDirectoryChooser c;
          try {
            Cursor old = JChooseFolderStep.this.getCursor();
            try {
              JChooseFolderStep.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
              c = new JDirectoryChooser();
              if (folderChoosen != null) {
                c.setCurrentDirectory(folderChoosen);
              }

            } finally {
              JChooseFolderStep.this.setCursor(old);
            }
            int result = c.showOpenDialog(JChooseFolderStep.this);
            File selectedFolder = c.getSelectedFile();
            if (selectedFolder != null
                && selectedFolder.isDirectory()
                && result == JFileChooser.APPROVE_OPTION) {
              logger.debug("open the folder " + selectedFolder);
              folderChoosen = selectedFolder;
              updateState();

              ps.setFileProperty(DEFAULTCURRENTFOLDER, folderChoosen);
              ps.save();
            }

          } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
          }
        });

    folderLabel = fp.getLabel("lblfolder");

    setLayout(new BorderLayout());
    add(fp, BorderLayout.CENTER);

    fp.getFormAccessor().replaceBean("preview", imagefolderpreviewere);

    folderChoosen = ps.getFileProperty(DEFAULTCURRENTFOLDER, null);
    updateState();
  }

  /** update state */
  private void updateState() {
    if (folderChoosen == null) {
      folderLabel.setText("< Choose Folder >");
    } else {
      folderLabel.setText(folderChoosen.getAbsolutePath());
      perfoScanFolder = new PerfoScanFolder(folderChoosen);
    }
    if (folderChoosen != null) {
      try {
        if (listener != null) listener.stepStatusChanged();
        imagefolderpreviewere.loadFolder(folderChoosen);
      } catch (Exception ex) {
        logger.error("preview not available :" + ex.getMessage(), ex);
      }
    }
  }

  @Override
  public String getLabel() {
    return "Choose Images folder";
  }

  private File folderChoosen = null;
  
  private PerfoScanFolder perfoScanFolder = null;

  private StepStatusChangedListener listener = null;

  @Override
  public void activate(
      Serializable state, WizardStates allStepsStates, StepStatusChangedListener stepListener)
      throws Exception {

    if (state != null && state instanceof File) {
      this.folderChoosen = (File) state;
    }

    if (folderChoosen != null) {
      perfoScanFolder = new PerfoScanFolder(folderChoosen);
    }
    this.listener = stepListener;
  }

  @Override
  public Serializable unActivateAndGetSavedState() throws Exception {
    return folderChoosen;
  }

  @Override
  public boolean isStepCompleted() { 
    return folderChoosen != null;
  }

  File getFolder() {
    return folderChoosen;
  }
}
