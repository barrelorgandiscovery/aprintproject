/*
 * Copyright (c) 2001-2010 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jgoodies.looks.windows;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.plaf.ComponentUI;

import com.jgoodies.looks.BorderStyle;
import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;

/**
 * The JGoodies Windows look and feel implemenation of {@code MenuBarUI}.<p>
 *
 * Can handle optional {@code Border} types as specified by the
 * {@code BorderStyle} or {@code HeaderStyle} client properties.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.9 $
 */
public final class WindowsMenuBarUI extends com.sun.java.swing.plaf.windows.WindowsMenuBarUI {

	private PropertyChangeListener listener;


	public static ComponentUI createUI(JComponent b) {
		return new WindowsMenuBarUI();
	}


	// Handling Special Borders *********************************************************

	@Override
    protected void installDefaults() {
		super.installDefaults();
		installSpecialBorder();
	}


	@Override
    protected void installListeners() {
		super.installListeners();
		listener = createBorderStyleListener();
		menuBar.addPropertyChangeListener(listener);
	}


	@Override
    protected void uninstallListeners() {
		menuBar.removePropertyChangeListener(listener);
		super.uninstallListeners();
	}


	private PropertyChangeListener createBorderStyleListener() {
		return new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent e) {
				String prop = e.getPropertyName();
				if (prop.equals(Options.HEADER_STYLE_KEY) ||
				    prop.equals(WindowsLookAndFeel.BORDER_STYLE_KEY)) {
				   WindowsMenuBarUI.this.installSpecialBorder();
				}
			}

		};
	}


	/**
	 * Installs a special border, if either a look-dependent
	 * {@code BorderStyle} or a look-independent
	 * {@code HeaderStyle} has been specified.
	 * A look specific BorderStyle shadows
	 * a HeaderStyle.<p>
	 *
	 * Specifying a HeaderStyle is recommend.
	 */
	private void installSpecialBorder() {
		String suffix;
		BorderStyle borderStyle = BorderStyle.from(menuBar,
												WindowsLookAndFeel.BORDER_STYLE_KEY);
		if (borderStyle == BorderStyle.EMPTY) {
            suffix = "emptyBorder";
        } else if (borderStyle == BorderStyle.ETCHED) {
            suffix = "etchedBorder";
        } else if (borderStyle == BorderStyle.SEPARATOR) {
            suffix = "separatorBorder";
        } else if (HeaderStyle.from(menuBar) == HeaderStyle.BOTH) {
            suffix = "headerBorder";
        } else {
            return;
        }

		LookAndFeel.installBorder(menuBar, "MenuBar." + suffix);
	}

}