package org.barrelorgandiscovery.ui.animation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.ICancelTracker;

/**
 * The Class InfiniteProgressPanel. This Progress Panel is used with the
 * GlassPane, and forbid the user to make things while the program is working
 */
public class InfiniteProgressPanel extends JComponent implements MouseListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3511102138388769386L;

	/** The logger. */
	private static Logger logger = Logger
			.getLogger(InfiniteProgressPanel.class);

	/** The ticker. */
	protected Area[] ticker = null;

	/** The animation. */
	protected AtomicReference<Thread> animation = new AtomicReference<Thread>();

	/** The started. */
	protected boolean started = false;

	/** The alpha level. */
	protected int alphaLevel = 0;

	/** The ramp delay. */
	protected int rampDelay = 300;

	/** The shield. */
	protected float shield = 0.70f;

	/** The text. */
	protected String text = "";

	/** The bars count. */
	protected int barsCount = 30;

	/** The fps. */
	protected float fps = 5.0f;

	/** The hints. */
	protected RenderingHints hints = null;

	protected ICancelTracker cancelTracker = null;

	/**
	 * Instantiates a new infinite progress panel.
	 */
	public InfiniteProgressPanel() {
		this("");
	}

	/**
	 * Instantiates a new infinite progress panel.
	 * 
	 * @param text
	 *            the text
	 */
	public InfiniteProgressPanel(String text) {
		this(text, 30);
	}

	/**
	 * Instantiates a new infinite progress panel.
	 * 
	 * @param text
	 *            the text
	 * @param barsCount
	 *            the bars count
	 */
	public InfiniteProgressPanel(String text, int barsCount) {
		this(text, barsCount, 0.70f);
	}

	/**
	 * Instantiates a new infinite progress panel.
	 * 
	 * @param text
	 *            the text
	 * @param barsCount
	 *            the bars count
	 * @param shield
	 *            the shield
	 */
	public InfiniteProgressPanel(String text, int barsCount, float shield) {
		this(text, barsCount, shield, 15.0f);
	}

	/**
	 * Instantiates a new infinite progress panel.
	 * 
	 * @param text
	 *            the text
	 * @param barsCount
	 *            the bars count
	 * @param shield
	 *            the shield
	 * @param fps
	 *            the fps
	 */
	public InfiniteProgressPanel(String text, int barsCount, float shield,
			float fps) {
		this(text, barsCount, shield, fps, 300);
	}

	/**
	 * Instantiates a new infinite progress panel.
	 * 
	 * @param text
	 *            the text
	 * @param barsCount
	 *            the bars count
	 * @param shield
	 *            the shield
	 * @param fps
	 *            the fps
	 * @param rampDelay
	 *            the ramp delay
	 */
	public InfiniteProgressPanel(String text, int barsCount, float shield,
			float fps, int rampDelay) {
		this.text = text;
		this.rampDelay = rampDelay >= 0 ? rampDelay : 0;
		this.shield = shield >= 0.0f ? shield : 0.0f;
		this.fps = fps > 0.0f ? fps : 15.0f;
		this.barsCount = barsCount > 0 ? barsCount : 14;

		this.hints = new RenderingHints(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		this.hints.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		this.hints.put(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	/**
	 * This method should be invoked from swing thread.
	 * 
	 * @param text
	 *            the text
	 */
	public void setText(String text) {
		this.text = text;
		repaint();
	}

	/**
	 * Gets the text.
	 * 
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	private JButton cancelButton = null;

	/**
	 * Should be invoked from swing thread ..
	 * 
	 * @param text
	 *            the text
	 */
	public void start(final String text) {

		Thread t = animation.getAndSet(null);
		if (t != null) {
			t.interrupt();
		}

		if (text != null || !"".equals(text))
			setText(text);
		addMouseListener(InfiniteProgressPanel.this);
		setVisible(true);

		ticker = buildTicker();

		if (cancelTracker != null) {

			if (cancelButton == null) {
				cancelButton = new JButton("Cancel");
				cancelButton.setBounds(0, 0, 80, 30);
				cancelButton.setSize(80, 30);
				
				cancelButton.setLocation((getWidth() - cancelButton.getWidth()) / 2,
						(int) (getHeight() *3 /4));

				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (cancelTracker != null)
							cancelTracker.cancel();
					}
				});
			}

			add(cancelButton);

		} else {

			if (cancelButton != null) {
				this.remove(cancelButton);
				this.cancelButton = null;
			}

		}

		t = new Thread(new Animator(true));
		t.start();
		animation.set(t);

	}

	/**
	 * Should be invoked from swing thread.
	 */
	public void start() {
		start(null);
	}

	/**
	 * Should be invoked from swing thread.
	 */
	public void stop() {

		Thread t = animation.getAndSet(null);

		if (t != null) {
			t.interrupt();
			t = new Thread(new Animator(false));
			t.start();

			removeMouseListener(InfiniteProgressPanel.this);

			animation.set(t);
		}
	}

	public boolean isStarted() {
		return started;
	}

	/**
	 * Interrupt.
	 */
	public void interrupt() {
		Thread t = animation.getAndSet(null);
		if (t != null) {
			t.interrupt();

			removeMouseListener(InfiniteProgressPanel.this);
			setVisible(false);
			repaint();

		}
	}

	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		if (started) {
			int width = getWidth();
			int height = getHeight();

			double maxY = 0.0;

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHints(hints);

			g2.setColor(new Color(255, 255, 255, (int) (alphaLevel * shield)));
			g2.fillRect(0, 0, getWidth(), getHeight());

			synchronized (ticker) {
				for (int i = 0; i < ticker.length; i++) {
					int channel = 224 - 128 / (i + 1);
					g2
							.setColor(new Color(channel, channel, channel,
									alphaLevel ));
					g2.fill(ticker[i]);
					Rectangle2D bounds = ticker[i].getBounds2D();
					if (bounds.getMaxY() > maxY)
						maxY = bounds.getMaxY();
				}
			}

			if (text != null && text.length() > 0) {
				FontRenderContext context = g2.getFontRenderContext();
				TextLayout layout = new TextLayout(text, getFont(), context);
				Rectangle2D bounds = layout.getBounds();
				g2.setColor(getForeground());
				layout.draw(g2, (float) (width - bounds.getWidth()) / 2,
						(float) (maxY + layout.getLeading() + 2 * layout
								.getAscent()));

				maxY += layout.getLeading() + 2 * layout.getAscent() + 10;
			}


		}
	}

	public void setCancelTracker(ICancelTracker cancelTracker) {
		this.cancelTracker = cancelTracker;
	}

	public ICancelTracker getCancelTracker() {
		return this.cancelTracker;
	}

	/**
	 * Builds the ticker.
	 * 
	 * @return the area[]
	 */
	private Area[] buildTicker() {
		Area[] ticker = new Area[barsCount];
		Point2D.Double center = new Point2D.Double((double) getWidth() / 2,
				(double) getHeight() / 2);
		double fixedAngle = 2.0 * Math.PI / ((double) barsCount);

		for (double i = 0.0; i < (double) barsCount; i++) {
			Area primitive = buildPrimitive();

			AffineTransform toCenter = AffineTransform.getTranslateInstance(
					center.getX(), center.getY());
			AffineTransform toBorder = AffineTransform.getTranslateInstance(
					45.0, -6.0);
			AffineTransform toCircle = AffineTransform.getRotateInstance(-i
					* fixedAngle, center.getX(), center.getY());

			AffineTransform toWheel = new AffineTransform();
			toWheel.concatenate(toCenter);
			toWheel.concatenate(toBorder);

			primitive.transform(toWheel);
			primitive.transform(toCircle);

			ticker[(int) i] = primitive;
		}

		return ticker;
	}

	/**
	 * Builds the primitive.
	 * 
	 * @return the area
	 */
	private Area buildPrimitive() {
		Rectangle2D.Double body = new Rectangle2D.Double(3, 0, 30, 6);
		Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 6, 6);
		Ellipse2D.Double tail = new Ellipse2D.Double(30, 0, 6, 6);

		Area tick = new Area(body);
		tick.add(new Area(head));
		tick.add(new Area(tail));

		return tick;
	}

	/**
	 * The Class Animator.
	 */
	protected class Animator implements Runnable {

		/** The ramp up. */
		private boolean rampUp = true;

		/**
		 * Instantiates a new animator.
		 * 
		 * @param rampUp
		 *            the ramp up
		 */
		protected Animator(boolean rampUp) {
			this.rampUp = rampUp;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			Point2D.Double center = new Point2D.Double((double) getWidth() / 2,
					(double) getHeight() / 2);
			double fixedIncrement = 2.0 * Math.PI / ((double) barsCount);
			AffineTransform toCircle = AffineTransform.getRotateInstance(
					fixedIncrement, center.getX(), center.getY());

			long start = System.currentTimeMillis();
			if (rampDelay == 0)
				alphaLevel = rampUp ? 255 : 0;

			started = true;
			boolean inRamp = rampUp;

			while (!Thread.interrupted()) {
				if (!inRamp) {
					synchronized (ticker) {
						for (int i = 0; i < ticker.length; i++)
							ticker[i].transform(toCircle);
					}
				}

				repaint();

				if (rampUp) {
					if (alphaLevel < 255) {
						alphaLevel = (int) (255 * (System.currentTimeMillis() - start) / rampDelay);
						if (alphaLevel >= 255) {
							alphaLevel = 255;
							inRamp = false;
							break;
						}
					}
				} else if (alphaLevel > 0) {
					alphaLevel = (int) (255 - (255 * (System
							.currentTimeMillis() - start) / rampDelay));
					if (alphaLevel <= 0) {
						alphaLevel = 0;
						break;
					}
				}

				try {
					Thread.sleep(inRamp || (!inRamp && alphaLevel != 255) ? 10
							: (int) (1000 / fps));
				} catch (InterruptedException ie) {
					break;
				}
				Thread.yield();
			}

			if (!rampUp) {
				started = false;
				repaint();
				setVisible(false);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}
}