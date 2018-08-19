package org.barrelorgandiscovery.extensionsng.perfo.ng.controlling;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.messages.Messages;

import com.jeta.forms.components.panel.FormPanel;

public class PositionPanel extends JPanel {
	
	private static Logger logger = Logger.getLogger(PositionPanel.class);

	private PositionPanelListener listener;

	public PositionPanel() throws Exception {
		initComponents();
	}

	public void setListener(PositionPanelListener listener) {
		this.listener = listener;
	}

	private JButton previous;
	private JButton next;
	private JButton playPause;
	private JLabel position;
	private JLabel timeLeft;
	private JLabel displacementmetersleft;
	private JLabel bookDisplacementLeft;
	private JLabel bookMetersDone;

	protected void initComponents() throws Exception {

		FormPanel panel = new FormPanel(getClass().getResourceAsStream(
				"positioncontrol.jfrm")); //$NON-NLS-1$

		previous = (JButton) panel.getButton("previous"); //$NON-NLS-1$
		assert previous != null;
		previous.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (listener != null)
					listener.previous();
			}
		});
		previous.setIcon(new ImageIcon(getClass().getResource("1leftarrow.png"))); //$NON-NLS-1$
		previous.setText(Messages.getString("PositionPanel.0")); //$NON-NLS-1$

		next = (JButton) panel.getButton("next"); //$NON-NLS-1$
		assert next != null;

		next.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (listener != null)
					listener.next();
			}
		});
		next.setIcon(new ImageIcon(getClass().getResource("1rightarrow.png"))); //$NON-NLS-1$
		next.setText(Messages.getString("PositionPanel.6")); //$NON-NLS-1$
		
		
		playPause = (JButton) panel.getButton("pause"); //$NON-NLS-1$
		assert playPause != null;
		playPause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (listener != null) {
						listener.pausePlay();
					}
				} catch (Exception ex) {
					logger.error(
							"error launching the stream :" + ex.getMessage(), //$NON-NLS-1$
							ex);
				}
			}
		});

		position = panel.getLabel("labelposition"); //$NON-NLS-1$

		timeLeft = panel.getLabel("timeleft"); //$NON-NLS-1$

		displacementmetersleft = panel.getLabel("metersleft"); //$NON-NLS-1$

		bookDisplacementLeft = panel.getLabel("bookmetersleft"); //$NON-NLS-1$
		
		bookMetersDone = panel.getLabel("bookmetersdone"); //$NON-NLS-1$
		
		
		panel.getLabel("lbltimeleft").setText(Messages.getString("PositionPanel.302")); //$NON-NLS-1$ //$NON-NLS-2$
		panel.getLabel("lblbookleft").setText(Messages.getString("PositionPanel.304")); //$NON-NLS-1$ //$NON-NLS-2$
		panel.getLabel("lbldisplacementleft").setText(Messages.getString("PositionPanel.307")); //$NON-NLS-1$ //$NON-NLS-2$
		panel.getLabel("lblbookmeterdone").setText(Messages.getString("PositionPanel.308")); //$NON-NLS-1$ //$NON-NLS-2$
		
		
		setPlayState(false);
		
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
	}

	/**
	 * update the panel state and informations
	 * 
	 * @param position
	 * @param enablePrevious
	 * @param enableNext
	 * @param timeLeft
	 * @param displacementmetersleft
	 */
	public void updateState(int position, int nbcommands, 
			boolean enablePrevious, boolean enableNext,
			String timeLeft, String displacementmetersleft, 
			String bookMetersLeft, String bookMetersDone) {

		this.position.setText(Integer.toString(position) + "/" + nbcommands); //$NON-NLS-1$
		this.previous.setEnabled(enablePrevious);
		this.next.setEnabled(enableNext);

		this.timeLeft.setText(timeLeft);
		this.displacementmetersleft.setText(displacementmetersleft);
		this.bookDisplacementLeft.setText(bookMetersLeft);
		this.bookMetersDone.setText(bookMetersDone);
	}

	/**
	 * define the punch play state
	 * @param playstate
	 */
	public void setPlayState(boolean playstate) {
	
		String label = Messages.getString("PositionPanel.12"); //$NON-NLS-1$
		Icon buttonImage = new ImageIcon(getClass().getResource("noatunplay.png")); //$NON-NLS-1$
		if (playstate) {
			label = Messages.getString("PositionPanel.14"); //$NON-NLS-1$
			buttonImage = new ImageIcon(getClass().getResource("noatunpause.png")); //$NON-NLS-1$
		}
		
		playPause.setIcon(buttonImage);
		playPause.setText(label);
	}
	
	
}
