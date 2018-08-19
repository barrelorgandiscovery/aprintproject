package org.barrelorgandiscovery.gui.aprintng;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;

/**
 * Class for displaying memory, and memory consumption of the application, this
 * component create a Scheduled Thread to handle the update of the progress bar
 * 
 * @author use
 * 
 */
public class JMemoryProgressBar extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7123551748820959359L;

	private static Logger logger = Logger.getLogger(JMemoryProgressBar.class);

	private ScheduledExecutorService ses = Executors
			.newSingleThreadScheduledExecutor();
	private JProgressBar pb = new JProgressBar();
	private ScheduledFuture<?> future;

	public JMemoryProgressBar() {

		setLayout(new BorderLayout());

		update();

		add(new JLabel(Messages.getString("JMemoryProgressBar.0")), BorderLayout.WEST); //$NON-NLS-1$
		add(pb, BorderLayout.CENTER);

		JButton gc = new JButton("Gc"); //$NON-NLS-1$
		gc.setToolTipText(Messages.getString("JMemoryProgressBar.2")); //$NON-NLS-1$
		gc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.gc();
			}
		});

		add(gc, BorderLayout.EAST);

		future = ses.scheduleWithFixedDelay(new Runnable() {
			public void run() {

				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {

							update();
						}
					});

				} catch (Exception ex) {
					logger.error(
							"error in updating progressbar :" + ex.getMessage(), //$NON-NLS-1$
							ex);
				}

			}

		}, 2, 2, TimeUnit.SECONDS);

		pb.setMinimumSize(new Dimension(50, 20));

	}

	private void update() {

		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();

		long freeMemory = runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory());
		
		

		long used = maxMemory - freeMemory;

		pb.setMaximum(100);
		pb.setValue((int) (100.0 * used / maxMemory));
		pb.setIndeterminate(false);
		pb.setStringPainted(true);
		pb.setString("" + pb.getValue() + " % / " + (used / (1024 * 1024)) //$NON-NLS-1$ //$NON-NLS-2$
				+ " mb"); //$NON-NLS-1$
		pb.repaint();

	}

}
