package org.barrelorgandiscovery.extensionsng.perfo.ng.controlling;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jeta.forms.components.panel.FormPanel;

/**
 * this XY panel, manage a x/y offset in the settings
 * when the x/y changed, the listener is called for a 
 * 
 * @author pfreydiere
 *
 */
public class XYPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4105650312829819912L;

	public interface XYListener {
		public void xyChanged(double x, double y);
	}

	public XYPanel() throws Exception {
		initComponents();
	}

	private XYListener listener;
	private JLabel lx;
	private JLabel ly;
	private JButton bxm;
	private JButton bxp;
	private JButton bym;
	private JButton byp;
	
	private JButton bxmm;
	private JButton bxpp;
	private JButton bymm;
	private JButton bypp;
	

	protected void initComponents() throws Exception {

		FormPanel xypanel = new FormPanel(getClass().getResourceAsStream(
				"xypanel.jfrm")); //$NON-NLS-1$

		bxm = (JButton) xypanel.getComponentByName("xm"); //$NON-NLS-1$
		bxm.setIcon(new ImageIcon(getClass().getResource("1uparrow.png"))); //$NON-NLS-1$
		bxm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				xoffset -= .1;
				updatePanel();
				firexyChanged();
			}
		});
		bxm.setText("-0.1"); //$NON-NLS-1$

		bxp = (JButton) xypanel.getComponentByName("xp"); //$NON-NLS-1$
		bxp.setIcon(new ImageIcon(getClass().getResource("kdevelop_down.png"))); //$NON-NLS-1$
		bxp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				xoffset += .1;
				updatePanel();
				firexyChanged();
			}
		});
		bxp.setText("+0.1"); //$NON-NLS-1$


		bym = (JButton) xypanel.getComponentByName("ym"); //$NON-NLS-1$
		bym.setIcon(new ImageIcon(getClass().getResource("1leftarrow.png"))); //$NON-NLS-1$
		bym.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				yoffset -= .1;
				updatePanel();
				firexyChanged();
			}
		});
		bym.setText("-0.1"); //$NON-NLS-1$

		
		byp = (JButton) xypanel.getComponentByName("yp"); //$NON-NLS-1$
		byp.setIcon(new ImageIcon(getClass().getResource("1rightarrow.png"))); //$NON-NLS-1$
		byp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				yoffset += .1;
				updatePanel();
				firexyChanged();
			}
		});
		byp.setText("+0.1"); //$NON-NLS-1$

		
		///////////////////////////////////////////////////////
		
		bxmm = (JButton) xypanel.getComponentByName("xmm"); //$NON-NLS-1$
		bxmm.setIcon(new ImageIcon(getClass().getResource("2uparrow.png"))); //$NON-NLS-1$
		bxmm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				xoffset -= 1;
				updatePanel();
				firexyChanged();
			}
		});
		bxmm.setText("-1"); //$NON-NLS-1$


		bxpp = (JButton) xypanel.getComponentByName("xpp"); //$NON-NLS-1$
		bxpp.setIcon(new ImageIcon(getClass().getResource("2downarrow.png"))); //$NON-NLS-1$
		bxpp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				xoffset += 1;
				updatePanel();
				firexyChanged();
			}
		});
		bxpp.setText("+1"); //$NON-NLS-1$


		bymm = (JButton) xypanel.getComponentByName("ymm"); //$NON-NLS-1$
		bymm.setIcon(new ImageIcon(getClass().getResource("2leftarrow.png"))); //$NON-NLS-1$
		bymm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				yoffset -= 1;
				updatePanel();
				firexyChanged();
			}
		});
		bymm.setText("-1"); //$NON-NLS-1$


		bypp = (JButton) xypanel.getComponentByName("ypp"); //$NON-NLS-1$
		bypp.setIcon(new ImageIcon(getClass().getResource("2rightarrow.png"))); //$NON-NLS-1$
		bypp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				yoffset += 1;
				updatePanel();
				firexyChanged();
			}
		});
		bypp.setText("+1"); //$NON-NLS-1$


		lx = (JLabel) xypanel.getComponentByName("x"); //$NON-NLS-1$
		assert lx != null;
		ly = (JLabel) xypanel.getComponentByName("y"); //$NON-NLS-1$
		assert ly != null;

		setLayout(new BorderLayout());
		add(xypanel, BorderLayout.CENTER);
	}

	private double xoffset = 0.0;
	private double yoffset = 0.0;

	/**
	 * define the panel values
	 * 
	 * @param xoffset
	 * @param yoffset
	 */
	public void setOffsets(double xoffset, double yoffset) {
		this.xoffset = xoffset;
		this.yoffset = yoffset;
		updatePanel();
	}

	protected void updatePanel() {
		lx.setText(String.format("%1$+1.2f", xoffset)); //$NON-NLS-1$
		ly.setText(String.format("%1$+1.2f", yoffset)); //$NON-NLS-1$
	}

	public double getXOffset() {
		return xoffset;
	}

	public double getYOffset() {
		return yoffset;
	}

	public void setXYListener(XYListener listener) {
		this.listener = listener;
	}

	protected void firexyChanged() {
		if (listener == null)
			return;

		listener.xyChanged(xoffset, yoffset);
	}

}
