package org.barrelorgandiscovery.recognition.gui.books.steps;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.recognition.gui.books.BackgroundTileImageProcessingThread;
import org.barrelorgandiscovery.recognition.gui.books.RecognitionTiledImage;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.ImageFileAndInstrument;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JTiledImageDisplayLayer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools.JViewingToolBar;
import org.barrelorgandiscovery.recognition.messages.Messages;
import org.barrelorgandiscovery.tools.Disposable;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import mmorpho.MorphoProcessor;
import mmorpho.StructureElement;
import trainableSegmentation.WekaSegmentation;

/**
 * model choice
 *
 * @author pfreydiere
 */
public class StepModelChooseChoice extends BasePanelStep implements Step, Disposable {

  /** */
  private static final long serialVersionUID = 1203739446221221981L;

  private static Logger logger = Logger.getLogger(StepModelChooseChoice.class);

  private JDisplay display;
  private JTiledImageDisplayLayer imageDisplayLayer;
  private JTiledImageDisplayLayer modelpreviewImage;

  private Model currentmodel;

  private static final char[] waiters = new char[] {'-', '\\', '|', '/'};
  private int currentWaiterCharIdx = 0;

  private ScheduledExecutorService waiter;

  /** custom recognition model */
  private Model customModel;

  /**
   * constructor
   *
   * @param id
   * @param parent
   * @throws Exception
   */
  public StepModelChooseChoice(String id, Step parent) throws Exception {
    super(id, parent);
    initComponents();

    waiter = Executors.newSingleThreadScheduledExecutor();

    waiter.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            try {
              if (SwingUtilities.isEventDispatchThread()) {
                changeProgressText();
              } else {
                SwingUtilities.invokeLater(
                    new Runnable() {
                      @Override
                      public void run() {
                        changeProgressText();
                      }
                    });
              }
            } catch (Exception ex) {

            }
          }
        },
        200,
        200,
        TimeUnit.MILLISECONDS);
  }

  protected void initComponents() throws Exception {

    display = new JDisplay();
    imageDisplayLayer = new JTiledImageDisplayLayer(display);
    display.addLayer(imageDisplayLayer);

    modelpreviewImage = new JTiledImageDisplayLayer(display);
    display.addLayer(modelpreviewImage);

    setLayout(new BorderLayout());

    FormPanel fp = new FormPanel(getClass().getResourceAsStream("modelchoice.jfrm")); //$NON-NLS-1$

    add(fp, BorderLayout.CENTER);

    FormAccessor formAccessor = fp.getFormAccessor();

    JPanel imagepreviewpanel = new JPanel();
    imagepreviewpanel.setLayout(new BorderLayout());
    imagepreviewpanel.add(display, BorderLayout.CENTER);

    JViewingToolBar vt = new JViewingToolBar(display);

    vt.addSeparator(new Dimension(50, 10));
    vt.add(new JLabel(Messages.getString("StepModelChooseChoice.0"))); //$NON-NLS-1$
    sl = new JSlider(JSlider.HORIZONTAL, 10, 90, 50);
    sl.setToolTipText(Messages.getString("StepModelChooseChoice.1")); //$NON-NLS-1$
    sl.setMaximumSize(new Dimension(100, 30));
    sl.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {

            double floatValue = 1.0 * sl.getValue() / 100;

            changeModelLayerTransparency(floatValue);
          }
        });
    vt.add(sl);

    vt.add(new JLabel(Messages.getString("StepModelChooseChoice.2"))); //$NON-NLS-1$
    progressBar = new JProgressBar(0, 100);
    progressBar.setPreferredSize(new Dimension(200, 40));
    vt.addSeparator();
    vt.add(progressBar);

    imagepreviewpanel.add(vt, BorderLayout.NORTH);

    formAccessor.replaceBean("bookview", imagepreviewpanel); //$NON-NLS-1$

    formAccessor.replaceBean("modellist", constructModelGUI()); //$NON-NLS-1$
  }

  private BackgroundTileImageProcessingThread<Void> backgroundThread;

  // event related events
  protected void modelChanged(final Model selectedModel) throws Exception {

    // launch recognition thread

    currentmodel = selectedModel;

    RecognitionTiledImage ti = (RecognitionTiledImage) imageDisplayLayer.getImageToDisplay();
    if (ti != null) {

      if (selectedModel != null) {

        if (backgroundThread != null) {
          backgroundThread.cancel();
        }

        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        lastText = Messages.getString("StepModelChooseChoice.3"); //$NON-NLS-1$
        changeProgressText();

        BackgroundTileImageProcessingThread<Void> c =
            new BackgroundTileImageProcessingThread<Void>(
                ti,
                new BackgroundTileImageProcessingThread.TiledProcessedListener() {
                  @Override
                  public <T> void tileProcessed(int index, T result) {
                    SwingUtilities.invokeLater(
                        new Runnable() {
                          @Override
                          public void run() {

                            progressBar.setValue(
                                (int) Math.ceil(backgroundThread.currentProgress() * 100));

                            if (currentStepListener != null) {
                              currentStepListener.stepStatusChanged();
                            }
                            display.repaint();
                          }
                        });
                  }
                },
                // nb of threads for processing
                1); // for huge images
        //(int) Math.ceil(Runtime.getRuntime().availableProcessors() / 2.0));

        backgroundThread = c;

        c.start(
            new BackgroundTileImageProcessingThread.TileProcessing<Void>() {
              @Override
              public Void process(int index, BufferedImage tile) throws Exception {

                File outputTile = ti.constructImagePath(index, selectedModel.getName());

                if (!outputTile.exists()) // already computed ?
                {
                  // compute
                  ImagePlus input = new ImagePlus();
                  input.setImage(tile);

                  WekaSegmentation ws = new WekaSegmentation(input);
                  ws.loadClassifier(selectedModel.createInputStream());

                  ImagePlus r = ws.applyClassifier(input);

                  ImageProcessor processor = r.getProcessor();

                  ByteProcessor bp = processor.convertToByteProcessor();

                  ImagePlus binary = new ImagePlus("", bp); //$NON-NLS-1$
                  ij.IJ.save(binary, outputTile.getAbsolutePath());
                }

                File outputTileBook =
                    ti.constructImagePath(index, selectedModel.getName() + "_book"); //$NON-NLS-1$
                if (!outputTileBook.exists()) {
                  ImagePlus handled = IJ.openImage(outputTile.getAbsolutePath());

                  // estimate the pixel size

                  StructureElement se =
                      new StructureElement(
                          StructureElement.SQARE, 0, 8.0f, StructureElement.OFFSET0);

                  MorphoProcessor mp = new MorphoProcessor(se);

                  mp.close(handled.getProcessor());

                  ij.IJ.save(handled, outputTileBook.getAbsolutePath());
                }

                return null;
              }
            });

        progressBar.setValue((int) (Math.ceil(c.currentProgress() * 100)));

        RecognitionTiledImage mo =
            new RecognitionTiledImage(
                (RecognitionTiledImage) imageDisplayLayer.getImageToDisplay());
        mo.setCurrentImageFamilyDisplay(selectedModel.getName());

        modelpreviewImage.setImageToDisplay(mo);

      } else {

        modelpreviewImage.setImageToDisplay(null);
      }

      if (currentStepListener != null) {
        currentStepListener.stepStatusChanged();
      }

      display.repaint();
    }

    // reinit the transparency
    sl.setValue(50);
    changeModelLayerTransparency(0.5);
  }

  private String lastText = Messages.getString("StepModelChooseChoice.6"); //$NON-NLS-1$

  private void changeProgressText() {

    BackgroundTileImageProcessingThread<Void> bt = backgroundThread;
    if (bt != null && bt.isRunning()) {
      currentWaiterCharIdx = (currentWaiterCharIdx + 1) % waiters.length;
    }
    progressBar.setString(lastText + "   " + waiters[currentWaiterCharIdx]); //$NON-NLS-1$
  }

  private JPanel constructModelGUI() throws Exception {
    JPanel contains = new JPanel();
    JPanel p = contains;

    BoxLayout bl = new BoxLayout(p, BoxLayout.X_AXIS);
    p.setLayout(bl);

    // construct models

    ButtonGroup bg = new ButtonGroup();

    Model[] list = ModelFactory.createModels();

    for (int i = 0; i < list.length; i++) {

      final Model model = list[i];

      JPanel modelPanel = new JPanel();
      modelPanel.setLayout(new BorderLayout());

      JRadioButton rb = new JRadioButton();
      rb.setText(model.getLabel());
      if (model.getModelImage() != null) {
        JLabel img = new JLabel();
        img.setIcon(new ImageIcon(model.getModelImage()));
        modelPanel.add(img, BorderLayout.CENTER);
      }

      rb.addActionListener(
          new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
              try {
                modelChanged(model);
              } catch (Exception ex) {
                logger.error("error in selected event :" + ex.getMessage(), ex); //$NON-NLS-1$
              }
            }
          });
      modelPanel.add(rb, BorderLayout.SOUTH);
      bg.add(rb);
      p.add(modelPanel);
    }

  

    // add custom model
    JRadioButton rb = new JRadioButton();
    rb.setText("Custom ..");
    JPanel customModelPanel = new JPanel();
    customModelPanel.setLayout(new BorderLayout());
    JButton btn = new JButton("open ...");
    btn.addActionListener(
        (e) -> {
          try {
        	  loadCustomModel();
        	  if (customModel != null) {
        		  rb.setText(customModel.getName());
        	  }
          } catch (Exception ex) {
            logger.error("error :" + ex.getMessage(), ex);
          }
        });

    // open the model choose option if activated

    customModelPanel.add(btn, BorderLayout.CENTER);
    customModelPanel.add(rb, BorderLayout.SOUTH);
    p.add(customModelPanel);
   
    rb.addActionListener(
        new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            try {
              if (customModel != null) {
                modelChanged(customModel);
              }
            } catch (Exception ex) {
              logger.error("error in selected event :" + ex.getMessage(), ex); //$NON-NLS-1$
            }
          }
        });

    return p;
  }

  @Override
  public String getLabel() {
    return Messages.getString("StepModelChooseChoice.9"); //$NON-NLS-1$
  }

  @Override
  public String getDetails() {
    return Messages.getString("StepModelChooseChoice.10"); //$NON-NLS-1$
  }

  StepStatusChangedListener currentStepListener;

  private JProgressBar progressBar;

  private JSlider sl;

  @Override
  public void activate(
      Serializable state, WizardStates allStepsStates, StepStatusChangedListener stepListener)
      throws Exception {

    currentStepListener = stepListener;

    // get image
    ImageFileAndInstrument d =
        allStepsStates.getPreviousStateImplementing(this, ImageFileAndInstrument.class);
    if (d != null) {
      File imageFile = d.diskFile;

      RecognitionTiledImage ti = new RecognitionTiledImage(imageFile);
      ti.constructTiles();

      imageDisplayLayer.setImageToDisplay(ti);
    }

    if (state != null) {
      assert state instanceof Model;
      modelChanged((Model) state);
    }
  }

  @Override
  public Serializable unActivateAndGetSavedState() throws Exception {

    if (backgroundThread != null) {
      backgroundThread.cancel();
    }
    return currentmodel;
  }

  @Override
  public boolean isStepCompleted() {
    boolean done = currentmodel != null && progressBar.getValue() >= 100;
    if (done) {
      lastText = Messages.getString("StepModelChooseChoice.11"); //$NON-NLS-1$
      changeProgressText();
    }
    return done;
  }

  private void changeModelLayerTransparency(double floatValue) {
    modelpreviewImage.setTransparency(floatValue);
    display.repaint();
  }

  /**
   * load a custom model into customModel element
   *
   * @throws Exception
   */
  private void loadCustomModel() throws Exception {

    JFileChooser fc = new JFileChooser();
    int showOpenDialog = fc.showOpenDialog(this);
    if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
      File file = fc.getSelectedFile();
      if (file != null) {
        customModel =
            new Model(
                file.getName(),
                file.getName(),
                new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB),
                file.toURL());
      }
    }
  }

  @Override
  public void dispose() {
    if (waiter != null) {
      waiter.shutdownNow();
      waiter = null;
    }

    if (backgroundThread != null) {
      backgroundThread.cancel();
      backgroundThread = null;
    }
  }

  @Override
  public Icon getPageImage() {
    return null;
  }
}
