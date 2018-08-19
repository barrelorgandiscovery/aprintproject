package org.barrelorgandiscovery.gui.ascale;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.ScaleManager;
import org.barrelorgandiscovery.scale.StorageScaleManager;
import org.barrelorgandiscovery.tools.streamstorage.FolderStreamStorage;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;
import com.jeta.forms.gui.form.GridView;

public class JEquivalentScaleChooserPanel extends JPanel {

  /** serial */
  private static final long serialVersionUID = 7300563191181680537L;

  private static Logger logger = Logger.getLogger(JEquivalentScaleChooserPanel.class);

  private Scale foundEquivalentToThisScale;

  private ScaleComponent choosenScaleComponent;

  private JComboBox<ScaleDisplayer> cbChoice;

  public interface ScaleChooseListener {
    void scaleChanged(Scale e);
  }

  public JEquivalentScaleChooserPanel(Scale foundEquivalentToThisScale, String label) throws Exception {
    super();

    assert foundEquivalentToThisScale != null;
    this.foundEquivalentToThisScale = foundEquivalentToThisScale;

    initComponents(label);
  }

  /**
   * init visual components
   *
   * @throws Exception
   */
  protected void initComponents(String labelCurrentScale) throws Exception {

    FormPanel fp =
        new FormPanel(
            JEquivalentScaleChooserPanel.class.getResourceAsStream("equivalentscalechooser.jfrm"));

    JLabel label = (JLabel) fp.getComponentByName("intro");
    label.setText("Scale not found \" " + labelCurrentScale + "\", please select a replacement one");

    ScaleComponent foundScale = new ScaleComponent();
    foundScale.loadScale(foundEquivalentToThisScale);

    JScrollPane spFoundScale = new JScrollPane(foundScale);

    fp.getFormAccessor().replaceBean("scalecomponent", spFoundScale);
    spFoundScale.setBorder(new TitledBorder("Current Scale"));

    choosenScaleComponent = new ScaleComponent();

    JScrollPane spChoosenScale = new JScrollPane(choosenScaleComponent);

    FormAccessor gv = (FormAccessor) fp.getFormAccessor("equivalentgrid"); //$NON-NLS-1$
    GridView g = (GridView) gv;
    g.setBorder(new TitledBorder("New Scale"));

    gv.replaceBean("equivalent", spChoosenScale); //$NON-NLS-1$

    cbChoice =
        (JComboBox<JEquivalentScaleChooserPanel.ScaleDisplayer>)
            gv.getComponentByName("instrumentchoice"); //$NON-NLS-1$
    assert cbChoice != null;

    cbChoice.addItemListener(
        new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {

              Object o = e.getItem();
              logger.debug("Scale Changed " + o); //$NON-NLS-1$
              assert o instanceof ScaleDisplayer;
              if (o != null) {
                logger.debug("Send scale changed"); //$NON-NLS-1$
                ScaleDisplayer sd = (ScaleDisplayer) o;

                fireScaleChanged(sd.getScale());
              }
            }
          }
        });

    setLayout(new BorderLayout());
    add(fp, BorderLayout.CENTER);
  }

  public static class ScaleDisplayer {
    private Scale scale;
    private String label;

    public ScaleDisplayer(Scale s) {
      assert s != null;
      this.scale = s;
    }

    public ScaleDisplayer(Scale s, String label) {
      assert s != null;
      this.scale = s;
      this.label = label;
    }

    @Override
    public String toString() {
      if (this.label != null && !this.label.isEmpty()) {
        return this.label;
      }
      return scale.getName();
    }

    public Scale getScale() {
      return scale;
    }
  }

  private ScaleChooseListener currentListener;

  public void setCurrentListener(ScaleChooseListener currentListener) {
    this.currentListener = currentListener;
  }

  public ScaleChooseListener getCurrentListener() {
    return currentListener;
  }

  protected void fireScaleChanged(Scale newScale) {
    assert newScale != null;

    choosenScaleComponent.loadScale(newScale);
    if (currentListener != null) currentListener.scaleChanged(newScale);

    repaint();
  }

  public void defineScales(Map<String, Scale> scalesByLabels) {

    if (scalesByLabels == null) {
      scalesByLabels = new HashMap<>();
    }

    DefaultComboBoxModel<ScaleDisplayer> dm = new DefaultComboBoxModel<>();

    scalesByLabels
        .entrySet()
        .stream()
        .forEach(
            (e) -> {
              dm.addElement(new ScaleDisplayer(e.getValue(), e.getKey()));
            });

    cbChoice.setModel(dm);

    boolean visible = true;
    if (dm.getSize() > 0) {

      choosenScaleComponent.setVisible(true);
      // focus on the first
      cbChoice.setSelectedIndex(0);
      Scale firstScale = ((ScaleDisplayer) dm.getElementAt(0)).getScale();
      fireScaleChanged(firstScale);
    } else {
      visible = false;
    }

    choosenScaleComponent.setVisible(visible);
  }

  public void defineScales(Scale[] scales) {

    HashMap<String, Scale> r =
        Arrays.stream(scales)
            .collect(
                HashMap<String, Scale>::new,
                (p, e) -> {
                  p.put(e.getName(), e);
                },
                (a, b) -> {
                  a.putAll(b);
                });

    defineScales(r);
  }

  public Scale getSelectedScale() {
    ScaleDisplayer sd = (ScaleDisplayer) cbChoice.getSelectedItem();
    if (sd == null) return null;

    return sd.getScale();
  }

  public static void main(String[] args) throws Exception {

    // Lecture de gammes ...
    ScaleManager gm =
        new StorageScaleManager(
            new FolderStreamStorage(new File("C:\\projets\\APrint\\gammes"))); //$NON-NLS-1$

    JFrame j = new JFrame();
    JEquivalentScaleChooserPanel esc = new JEquivalentScaleChooserPanel(gm.getScale("20 Raffin"), "20 raffin");
    esc.defineScales(new Scale[] {gm.getScale("20 Raffin"), gm.getScale("42 Verbeeck")});

    j.getContentPane().add(esc, BorderLayout.CENTER);
    j.setSize(700, 500);
    j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    esc.setCurrentListener(
        new ScaleChooseListener() {

          @Override
          public void scaleChanged(Scale e) {
            System.out.println("selected scale :" + e);
          }
        });

    j.setVisible(true);
  }
}
