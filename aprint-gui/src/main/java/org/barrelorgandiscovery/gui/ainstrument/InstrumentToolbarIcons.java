package org.barrelorgandiscovery.gui.ainstrument;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Scales patch / toolbar PNGs to a compact size and shifts cold (blue) accents
 * toward green for a consistent instrument-editor toolbar look.
 */
public final class InstrumentToolbarIcons {

	/** Icon size for sound-mapping and percussion toolbars */
	public static final int TOOLBAR_ICON_PX = 20;

	private static final Map<String, ImageIcon> CACHE = new ConcurrentHashMap<>();

	private InstrumentToolbarIcons() {
	}

	/**
	 * Loads a classpath PNG next to {@code base}, applies a blue→green accent
	 * shift, scales to {@link #TOOLBAR_ICON_PX}, and caches the result.
	 */
	public static ImageIcon patchToolbarIcon(Class<?> base, String resourceName) {
		String cacheKey = base.getName() + '#' + resourceName + '#' + TOOLBAR_ICON_PX;
		return CACHE.computeIfAbsent(cacheKey, k -> loadPatchIcon(base, resourceName));
	}

	private static ImageIcon loadPatchIcon(Class<?> base, String resourceName) {
		URL url = base.getResource(resourceName);
		if (url == null) {
			return new ImageIcon();
		}
		try {
			BufferedImage raw = ImageIO.read(url);
			if (raw == null) {
				return new ImageIcon(url);
			}
			BufferedImage tinted = shiftBlueAccentsTowardGreen(raw);
			BufferedImage scaled = scaleSmooth(tinted, TOOLBAR_ICON_PX);
			return new ImageIcon(scaled);
		} catch (IOException e) {
			return new ImageIcon(url);
		}
	}

	private static BufferedImage scaleSmooth(BufferedImage src, int size) {
		BufferedImage dst = new BufferedImage(size, size,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dst.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(src, 0, 0, size, size, null);
		} finally {
			g.dispose();
		}
		return dst;
	}

	/**
	 * Nudges pixels that read as blue/cyan (typical patch icon ink) toward green.
	 */
	private static BufferedImage shiftBlueAccentsTowardGreen(BufferedImage src) {
		int w = src.getWidth();
		int h = src.getHeight();
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int rgb = src.getRGB(x, y);
				int a = (rgb >>> 24) & 0xff;
				int r = (rgb >> 16) & 0xff;
				int g = (rgb >> 8) & 0xff;
				int b = rgb & 0xff;
				if (a < 10) {
					out.setRGB(x, y, rgb);
					continue;
				}
				if (b > r + 12 && b > g + 4) {
					int boost = Math.min(60, (b - g) / 2);
					int nr = clamp(r + boost / 3);
					int ng = clamp(g + boost + 10);
					int nb = clamp(b - boost);
					out.setRGB(x, y, (a << 24) | (nr << 16) | (ng << 8) | nb);
				} else {
					out.setRGB(x, y, rgb);
				}
			}
		}
		return out;
	}

	private static int clamp(int v) {
		return Math.max(0, Math.min(255, v));
	}
}
