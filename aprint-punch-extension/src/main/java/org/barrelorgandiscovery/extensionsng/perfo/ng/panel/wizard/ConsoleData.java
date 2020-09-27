package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;

/**
 * Class used for storing console data
 * 
 * @author Mike
 */
public final class ConsoleData {

	public static final Color DEFAULT_FOREGROUND = Color.LIGHT_GRAY;
	public static final Color DEFAULT_BACKGROUND = Color.BLACK;
	public static final Font DEFAULT_FONT = new Font("Courier New", Font.PLAIN, 18);

	private int capacity = 0;
	public int rows;
	public int columns;
	public Color[] background;
	public Color[] foreground;
	public Font[] font;
	public char[] text;

	ConsoleData() {
		// create empty console data
	}

	private void ensureCapacity(int newSize) {
		assert newSize >= 0;
		char[] newText = new char[newSize];
		Color[] newBackground = new Color[newSize];
		Color[] newForeground = new Color[newSize];
		Font[] newFont = new Font[newSize];

		Arrays.fill(newBackground, Color.black);
		Arrays.fill(newForeground, Color.white);
		Arrays.fill(newFont, DEFAULT_FONT);
		Arrays.fill(newText, ' ');

		if (newSize > 0) {
			int sizeToCopy = Math.min(newSize, text.length);
			System.arraycopy(text, 0, newText, 0, sizeToCopy);
			System.arraycopy(foreground, 0, newForeground, 0, sizeToCopy);
			System.arraycopy(background, 0, newBackground, 0, sizeToCopy);
			System.arraycopy(font, 0, newFont, 0, sizeToCopy);
		}

		text = newText;
		foreground = newForeground;
		background = newBackground;
		font = newFont;
		capacity = newSize;
	}

	void init(int columns, int rows) {
		resize(columns, rows);

	}

	public void resize(int columns, int rows) {
		ensureCapacity(rows * columns);
		this.rows = rows;
		this.columns = columns;
	}

	void init() {
		init(0, 0);
	}

	public boolean isOutOfConsole(int column, int row) {
		return column + row * columns >= text.length;
	}

	public void scrollUp() {

		for (int j = 1; j < rows; j++) {
			for (int i = 0; i < columns; i++) {
				final int dest = i + (j - 1) * columns;
				final int source = i + j * columns;

				text[dest] = text[source];
				foreground[dest] = foreground[source];
				background[dest] = background[source];
				font[dest] = font[source];
			}
		}
		for (int i = 0; i < columns; i++) {

			final int dest = i + (rows - 1) * columns;
			text[dest] = ' ';
		}

	}

	/**
	 * Sets a single character position
	 */
	public void setDataAt(int column, int row, char c, Color fg, Color bg, Font f) {
		int pos = column + row * columns;

		while (pos >= text.length) {
			scrollUp();
			row = row - 1;
			pos = column + row * columns;
		}

		assert pos < text.length;

		text[pos] = c;
		foreground[pos] = fg;
		background[pos] = bg;
		font[pos] = f;
	}

	public char getCharAt(int column, int row) {
		int offset = column + row * columns;
		return text[offset];
	}

	public Color getForegroundAt(int column, int row) {
		int offset = column + row * columns;
		return foreground[offset];
	}

	public Color getBackgroundAt(int column, int row) {
		int offset = column + row * columns;
		return background[offset];
	}

	public Font getFontAt(int column, int row) {
		int offset = column + row * columns;
		return font[offset];
	}

	public void fillArea(char c, Color fg, Color bg, Font f, int column, int row, int width, int height) {
		for (int q = Math.max(0, row); q < Math.min(row + height, rows); q++) {
			for (int p = Math.max(0, column); p < Math.min(column + width, columns); p++) {
				int offset = p + q * columns;
				text[offset] = c;
				foreground[offset] = fg;
				background[offset] = bg;
				font[offset] = f;
			}
		}
	}
}