package org.barrelorgandiscovery.gui.ascale.constraints;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.barrelorgandiscovery.messages.Messages;

/**
 * Shared layout helpers and schematic drawings for scale constraint editors.
 */
public final class ConstraintSketches {

	private ConstraintSketches() {
	}

	/**
	 * Prepare a read-only description that wraps to the container width (uses a
	 * narrow column count so preferred width does not stretch the parent panel).
	 */
	public static void configureWrappingDescription(JTextArea ta) {
		ta.setEditable(false);
		ta.setOpaque(false);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setColumns(1);
		ta.setRows(3);
		ta.setTabSize(4);
		ta.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
	}

	/**
	 * Scrolls the description vertically only; width follows the constraint panel.
	 * Minimum width is zero so long unwrapped text does not inflate parent
	 * {@link javax.swing.JComponent#getMinimumSize()}.
	 */
	public static JScrollPane wrapDescription(JTextArea ta) {
		ta.setMinimumSize(new Dimension(0, 0));
		JScrollPane sp = new JScrollPane(ta);
		sp.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		sp.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		sp.setVerticalScrollBarPolicy(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp.setMinimumSize(new Dimension(0, 0));
		sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
		return sp;
	}

	/**
	 * Like {@link #wrapDescription(JTextArea)} but sets a modest preferred size so
	 * the help block lays out correctly; width still grows with the parent. Minimum
	 * width stays 0 (see {@link #wrapDescription(JTextArea)}).
	 */
	public static JScrollPane wrapHelpIntro(JTextArea ta, int preferredWidthPx,
			int preferredHeightPx) {
		JScrollPane sp = wrapDescription(ta);
		sp.setPreferredSize(new Dimension(preferredWidthPx, preferredHeightPx));
		return sp;
	}

	/**
	 * Stacks wrapped description and an optional schematic (for new constraint
	 * types, pass {@code null} for diagram).
	 */
	public static JPanel buildNorthSection(JTextArea description,
			JComponent diagramOrNull) {
		JPanel stack = new JPanel();
		stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
		stack.setAlignmentX(Component.LEFT_ALIGNMENT);
		JScrollPane descScroll = wrapDescription(description);
		descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
		stack.add(descScroll);
		if (diagramOrNull != null) {
			stack.add(Box.createVerticalStrut(8));
			diagramOrNull.setAlignmentX(Component.LEFT_ALIGNMENT);
			stack.add(diagramOrNull);
		}
		return stack;
	}

	/** Schematic: one hole on the track; bracket shows “minimum length”. */
	public static JComponent minimumHoleLengthDiagram() {
		JPanel canvas = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				try {
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
					int w = getWidth();
					int h = getHeight();
					int midY = h / 2 - 4;
					g2.setColor(new Color(90, 90, 90));
					g2.setStroke(new BasicStroke(1.2f));
					g2.drawLine(16, midY, w - 16, midY);

					int holeW = Math.max(40, Math.min(140, w - 80));
					int holeX = (w - holeW) / 2;
					int holeH = 20;
					g2.setColor(new Color(195, 215, 245));
					g2.fillRoundRect(holeX, midY - holeH / 2, holeW, holeH, 6, 6);
					g2.setColor(new Color(70, 90, 130));
					g2.drawRoundRect(holeX, midY - holeH / 2, holeW, holeH, 6, 6);

					int dimY = midY + holeH / 2 + 14;
					g2.setColor(new Color(60, 60, 60));
					g2.setStroke(new BasicStroke(1f));
					drawBracketDimension(g2, holeX, holeX + holeW, dimY);

					g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 10f));
					String lab = Messages.getString(
							"MinimumHoleLengthConstraintComponent.diagramAxisLabel"); //$NON-NLS-1$
					int tw = g2.getFontMetrics().stringWidth(lab);
					g2.drawString(lab, (w - tw) / 2, dimY + 14);
				} finally {
					g2.dispose();
				}
			}
		};
		canvas.setOpaque(true);
		canvas.setBackground(new Color(248, 250, 255));
		canvas.setPreferredSize(new Dimension(280, 92));
		canvas.setMinimumSize(new Dimension(160, 88));
		canvas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

		JLabel cap = new JLabel(Messages.getString(
				"MinimumHoleLengthConstraintComponent.diagramCaption")); //$NON-NLS-1$
		cap.setFont(cap.getFont().deriveFont(Font.PLAIN, 11f));
		cap.setHorizontalAlignment(SwingConstants.CENTER);
		cap.setForeground(new Color(70, 70, 70));

		JPanel wrap = new JPanel(new BorderLayout(0, 4));
		wrap.setBorder(BorderFactory.createTitledBorder(
				Messages.getString(
						"MinimumHoleLengthConstraintComponent.diagramTitle"))); //$NON-NLS-1$
		wrap.add(canvas, BorderLayout.CENTER);
		wrap.add(cap, BorderLayout.SOUTH);
		wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
		return wrap;
	}

	/** Schematic: two holes; bracket shows minimum gap. */
	public static JComponent minimumInterHoleDiagram() {
		JPanel canvas = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				try {
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
					int w = getWidth();
					int h = getHeight();
					int midY = h / 2 - 2;
					g2.setColor(new Color(90, 90, 90));
					g2.setStroke(new BasicStroke(1.2f));
					g2.drawLine(12, midY, w - 12, midY);

					int holeW = 28;
					int gap = Math.max(24, Math.min(80, (w - 2 * holeW - 40) / 2));
					int total = holeW + gap + holeW;
					int startX = (w - total) / 2;

					drawHole(g2, startX, midY, holeW, 18);
					drawHole(g2, startX + holeW + gap, midY, holeW, 18);

					int dimY = midY + 22;
					g2.setColor(new Color(60, 60, 60));
					g2.setStroke(new BasicStroke(1f));
					int g0 = startX + holeW;
					int g1 = startX + holeW + gap;
					drawBracketDimension(g2, g0, g1, dimY);

					g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 10f));
					String lab = Messages.getString(
							"MinimumInnerHoleLengthComponent.diagramAxisLabel"); //$NON-NLS-1$
					int tw = g2.getFontMetrics().stringWidth(lab);
					g2.drawString(lab, (w - tw) / 2, dimY + 14);
				} finally {
					g2.dispose();
				}
			}

			private void drawHole(Graphics2D g2, int cx, int midY, int holeW,
					int holeH) {
				int x = cx;
				g2.setColor(new Color(195, 215, 245));
				g2.fillRoundRect(x, midY - holeH / 2, holeW, holeH, 5, 5);
				g2.setColor(new Color(70, 90, 130));
				g2.drawRoundRect(x, midY - holeH / 2, holeW, holeH, 5, 5);
			}
		};
		canvas.setOpaque(true);
		canvas.setBackground(new Color(248, 250, 255));
		canvas.setPreferredSize(new Dimension(280, 100));
		canvas.setMinimumSize(new Dimension(160, 92));
		canvas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));

		JLabel cap = new JLabel(Messages.getString(
				"MinimumInnerHoleLengthComponent.diagramCaption")); //$NON-NLS-1$
		cap.setFont(cap.getFont().deriveFont(Font.PLAIN, 11f));
		cap.setHorizontalAlignment(SwingConstants.CENTER);
		cap.setForeground(new Color(70, 70, 70));

		JPanel wrap = new JPanel(new BorderLayout(0, 4));
		wrap.setBorder(BorderFactory.createTitledBorder(
				Messages.getString(
						"MinimumInnerHoleLengthComponent.diagramTitle"))); //$NON-NLS-1$
		wrap.add(canvas, BorderLayout.CENTER);
		wrap.add(cap, BorderLayout.SOUTH);
		wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
		return wrap;
	}

	private static void drawBracketDimension(Graphics2D g2, int x0, int x1,
			int y) {
		int tick = 5;
		g2.draw(new Line2D.Float(x0, y - tick, x0, y + tick));
		g2.draw(new Line2D.Float(x1, y - tick, x1, y + tick));
		g2.draw(new Line2D.Float(x0, y, x1, y));
		// small inward arrows at ends
		g2.drawLine(x0, y, x0 + 4, y - 3);
		g2.drawLine(x0, y, x0 + 4, y + 3);
		g2.drawLine(x1, y, x1 - 4, y - 3);
		g2.drawLine(x1, y, x1 - 4, y + 3);
	}
}
