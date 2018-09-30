package org.barrelorgandiscovery.perfo.gui;

import java.awt.BorderLayout;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import com.jeta.forms.components.panel.FormPanel;

/**
 * Touch NumericValue component
 *
 * @author pfreydiere
 */
public class JNumericValue extends JPanel {

  private static Logger logger = Logger.getLogger(JNumericValue.class);

  public JNumericValue() throws Exception {
    initComponents();
  }

  private double value;

  private JLabel label;

  private ChangeListener changeListener;

  protected void initComponents() throws Exception {
    FormPanel fp = new FormPanel(getClass().getResourceAsStream("numericvalue.jfrm"));
    AbstractButton bup = fp.getButton("up");
    bup.setText("");
    bup.setIcon(new ImageIcon(getClass().getResource("1uparrow.png")));
    bup.addActionListener(
        (e) -> {
          value += 1.0;
          valueChanged();
        });

    AbstractButton bbigup = fp.getButton("bigup");
    bbigup.setText("");
    bbigup.setIcon(new ImageIcon(getClass().getResource("2uparrow.png")));
    bbigup.addActionListener(
        (e) -> {
          value += 10.0;
          valueChanged();
        });

    AbstractButton bdown = fp.getButton("down");
    bdown.setText("");
    bdown.setIcon(new ImageIcon(getClass().getResource("1downarrow.png")));
    bdown.addActionListener(
        (e) -> {
          value -= 1.0;
          valueChanged();
        });

    AbstractButton bbigdown = fp.getButton("bigdown");
    bbigdown.setText("");
    bbigdown.setIcon(new ImageIcon(getClass().getResource("2downarrow.png")));
    bbigdown.addActionListener(
        (e) -> {
          value -= 10.0;
          valueChanged();
        });

    label = fp.getLabel("lbl");
    
    
    setLayout(new BorderLayout());
    add(fp, BorderLayout.CENTER);
  }

  private void valueChanged() {
    label.setText(String.format("%2.2f", value));
    // fire event
    fireChangeEvent();
  }

  public void setValue(double value) {
	  double oldvvalue = this.value;
	  this.value = value;
	  valueChanged();
	  if (oldvvalue != value) {
		  fireChangeEvent();
	  }
  }

  public double getValue() {
    return value;
  }

  protected void fireChangeEvent() {
    if (changeListener != null) {
      try {
        changeListener.stateChanged(new ChangeEvent(this));
      } catch (Exception ex) {
        logger.error(ex.getMessage(), ex);
      }
    }
  }

  public void setChangeListener(ChangeListener changeListener) {
    this.changeListener = changeListener;
  }

  public ChangeListener getChangeListener() {
    return changeListener;
  }
}
