package org.barrelorgandiscovery.gui.etl;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.gui.script.groovy.IScriptConsole;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.model.ModelStepRegistry;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.tools.FileNameExtensionFilter;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

import com.mxgraph.swing.util.mxGraphActions;

import groovy.swing.impl.DefaultAction;

public class JModelEditorPanel extends JPanel {

  /** */
  private static final long serialVersionUID = -2209700181189894883L;

  private static final String MODEL_FILE_SUFFIX = "model"; //$NON-NLS-1$
  private static final String USER_DEFAULTDIRLOCATION = "defaultdirlocation"; //$NON-NLS-1$

  private static Logger logger = Logger.getLogger(JModelEditorPanel.class);

  private Repository2 repository2;
  private ModelStepRegistry registry;
  private Map<String, Object> context;

  private String lastModelFileName = null;

  private IScriptConsole console;

  /** user preference storage */
  private IPrefsStorage prefs;

  private ModelEditor modelEditor;
  private AsyncJobsManager asyncJobManager;

  public JModelEditorPanel(
      ModelStepRegistry registry,
      Repository2 repository2,
      AsyncJobsManager asyncJobManager,
      Map<String, Object> context,
      IPrefsStorage prefs)
      throws Exception {

    this.registry = registry;
    assert repository2 != null;
    this.repository2 = repository2;
    this.context = context;
    this.prefs = prefs;
    assert asyncJobManager != null;
    this.asyncJobManager = asyncJobManager;

    initComponents();
  }

  protected void initComponents() throws Exception {

    modelEditor = new ModelEditor(registry, repository2, asyncJobManager, context);

    setLayout(new BorderLayout());
    add(modelEditor, BorderLayout.CENTER);

    // add toolbox

    JToolBar tb = new JToolBar();
    JButton load = new JButton();
    load.setToolTipText(Messages.getString("JModelEditorPanel.2")); //$NON-NLS-1$
    load.setIcon(ImageTools.loadIcon(getClass(), "fileopen.png")); //$NON-NLS-1$
    tb.add(load);
    load.addActionListener(
        e -> {
          load();
        });

    JButton save = new JButton();
    save.setToolTipText(Messages.getString("JModelEditorPanel.9")); //$NON-NLS-1$
    save.setIcon(ImageTools.loadIcon(getClass(), "filesave.png")); //$NON-NLS-1$
    tb.add(save);

    save.addActionListener(
        e -> {
          save();
        });

    tb.addSeparator();

    JButton undo = new JButton();
    undo.setToolTipText(Messages.getString("JModelEditorPanel.19")); //$NON-NLS-1$
    undo.setIcon(ImageTools.loadIcon(getClass(), "undo.png")); //$NON-NLS-1$

    undo.addActionListener(
        e -> {
          modelEditor.undo();
        });
    tb.add(undo);

    JButton redo = new JButton();
    redo.setToolTipText(Messages.getString("JModelEditorPanel.21")); //$NON-NLS-1$
    redo.setIcon(ImageTools.loadIcon(getClass(), "redo.png")); //$NON-NLS-1$

    redo.addActionListener(
        e -> {
          modelEditor.redo();
        });
    tb.add(redo);

    tb.addSeparator();
    tb.add(
        new JButton(
            modelEditor.bind(
                Messages.getString("JModelEditorPanel.23"), //$NON-NLS-1$
                mxGraphActions.getZoomInAction(), //$NON-NLS-1$
                ImageTools.loadIcon(getClass(), "viewmag+.png")))); //$NON-NLS-1$
    tb.add(
        new JButton(
            modelEditor.bind(
                Messages.getString("JModelEditorPanel.25"), //$NON-NLS-1$
                mxGraphActions.getZoomOutAction(), //$NON-NLS-1$
                ImageTools.loadIcon(getClass(), "viewmag-.png")))); //$NON-NLS-1$
    tb.add(
        new JButton(
            modelEditor.bind(
                Messages.getString("JModelEditorPanel.27"), //$NON-NLS-1$
                mxGraphActions.getZoomActualAction(), //$NON-NLS-1$
                ImageTools.loadIcon(getClass(), "viewmagfit.png")))); //$NON-NLS-1$

    tb.addSeparator();
    JButton btnExecute =
        (JButton)
            tb.add(
                new JButton(
                    new DefaultAction() {
                      @Override
                      public void actionPerformed(ActionEvent event) {
                        try {

                          if (console != null) {
                            console.appendOutput(Messages.getString("JModelEditorPanel.100"), null); //$NON-NLS-1$
                          }

                          List<String> errors = validateState();

                          if (errors != null && errors.size() > 0) {

                            JMessageBox.showMessage(
                                JModelEditorPanel.this,
                                Messages.getString("JModelEditorPanel.101")); //$NON-NLS-1$
                            
                            fireShowConsole();

                            return;
                          }

                          modelEditor.execute(null);

                        } catch (Exception ex) {
                          logger.error(ex.getMessage(), ex);
                          ex.printStackTrace();
                        }
                      }
                    }));

    btnExecute.setToolTipText(Messages.getString("JModelEditorPanel.29")); //$NON-NLS-1$
    btnExecute.setIcon(ImageTools.loadIcon(getClass(), "player_play.png")); //$NON-NLS-1$
    add(tb, BorderLayout.NORTH);
  }

  public void load() {
    try {

      File f = prefs.getFileProperty(USER_DEFAULTDIRLOCATION, new File(".")); //$NON-NLS-1$
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setCurrentDirectory(f);
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

      fileChooser.setFileFilter(
          new FileNameExtensionFilter("model", MODEL_FILE_SUFFIX)); //$NON-NLS-1$
      int ret =
          fileChooser.showDialog(this, Messages.getString("JModelEditorPanel.6")); //$NON-NLS-1$
      if (ret == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile != null && selectedFile.exists()) {
          prefs.setFileProperty(USER_DEFAULTDIRLOCATION, selectedFile.getParentFile());
          prefs.save();
          modelEditor.loadGraph(selectedFile); // new
          // File("c:\\temp\\test.xml")
          lastModelFileName = selectedFile.getName();
          logger.debug("open file"); //$NON-NLS-1$
        }
      }

    } catch (Exception ex) {
      logger.error("error when loading graph model :" + ex.getMessage(), ex); //$NON-NLS-1$
      // ex.printStackTrace(System.err);
      BugReporter.sendBugReport();
    }
  }

  public void newFromTemplate(URL template) throws Exception {
    logger.debug("new model from template"); //$NON-NLS-1$

    if (template == null) {
      modelEditor.newGraph();
      return;
    }
    InputStream os = template.openStream();

    if (os == null) {
      throw new Exception("template " + template + Messages.getString("JModelEditorPanel.102")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    try {
      modelEditor.load(os);
    } finally {
      os.close();
    }
  }

  public List<String> validateState() throws Exception {

    List<String> errors = modelEditor.validateState();
    if (errors != null && errors.size() > 0) {
      if (console != null) {
    	AtomicBoolean showConsole = new AtomicBoolean(false); 
        errors
            .stream()
            .forEach(
                e -> {
                  try {
                    console.appendOutput(Messages.getString("JModelEditorPanel.103") + e + "\n", null); //$NON-NLS-1$ //$NON-NLS-2$
                    showConsole.set(true);
                  } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                  }
                });
        if (showConsole.get()) {
        	fireShowConsole();
        }
      }
    }
    
    return errors;
    
  }

  public void save() {
    try {

      File f = prefs.getFileProperty(USER_DEFAULTDIRLOCATION, new File(".")); //$NON-NLS-1$
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setCurrentDirectory(f);
      if (lastModelFileName != null) {
        fileChooser.setSelectedFile(new File(f, lastModelFileName));
      }
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fileChooser.setFileFilter(
          new FileNameExtensionFilter("model", MODEL_FILE_SUFFIX)); //$NON-NLS-1$
      int ret =
          fileChooser.showDialog(this, Messages.getString("JModelEditorPanel.13")); //$NON-NLS-1$
      if (ret == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile != null) {

          if (!selectedFile.getName().endsWith("." + MODEL_FILE_SUFFIX)) { //$NON-NLS-1$
            selectedFile =
                new File(
                    selectedFile.getParentFile(),
                    selectedFile.getName() + "." + MODEL_FILE_SUFFIX); //$NON-NLS-1$
          }

          if (selectedFile.exists()) {
            // ask overwrite
            if (JOptionPane.showConfirmDialog(this, Messages.getString("JModelEditorPanel.16")) //$NON-NLS-1$
                != JOptionPane.YES_OPTION) { //$NON-NLS-1$
              return;
            }
          }

          prefs.setFileProperty(USER_DEFAULTDIRLOCATION, selectedFile.getParentFile());
          prefs.save();

          modelEditor.saveGraph(selectedFile);
          logger.debug("file saved"); //$NON-NLS-1$
        }
      }

    } catch (Exception ex) {
      logger.error("error when loading graph model :" + ex.getMessage(), ex); //$NON-NLS-1$
      // ex.printStackTrace(System.err);
      BugReporter.sendBugReport();
    }
  }

  public void setConsole(IScriptConsole console) {
    this.console = console;
    this.modelEditor.setConsole(console);
  }

  public IScriptConsole getConsole() {
    return console;
  }
  
  private IConsoleShowListener consoleShowListener;
  public void setConsoleShowListener(IConsoleShowListener consoleShowListener) {
	  this.consoleShowListener = consoleShowListener;
  }
  
  protected void fireShowConsole() {
	  if (consoleShowListener != null) {
		  consoleShowListener.showConsole();
	  }
  }
  
}
