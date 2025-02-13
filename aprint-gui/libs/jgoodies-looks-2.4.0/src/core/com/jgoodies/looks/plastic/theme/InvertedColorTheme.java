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

package com.jgoodies.looks.plastic.theme;

import java.awt.Color;

import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;

import com.jgoodies.looks.plastic.PlasticTheme;

/**
 * The abstract superclass of all inverted Plastic themes,
 * that have light foreground and dark window background colors.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.9 $
 */
public abstract class InvertedColorTheme extends PlasticTheme {

    private final ColorUIResource softWhite =
        new ColorUIResource(154, 154, 154);

    private final ColorUIResource primary1 = new ColorUIResource(83, 83, 61);
    //90,  90,  66);// Dunkel: Rollbalkenrahmen-Dunkel

    private final ColorUIResource primary2 = new ColorUIResource(115, 107, 82);
    //132, 123,  90);// Mittel: Rollbalkenhintergrund

    private final ColorUIResource primary3 = new ColorUIResource(156, 156, 123);
    //148, 140, 107); //181, 173, 148); // Hell:   Ordnerfl�che, Selektion, Rollbalken-Hoch, Men�hintergrund

    private final ColorUIResource secondary1 = new ColorUIResource(32, 32, 32);
    // Abw�rts  (dunkler)73,  59,  23);

    private final ColorUIResource secondary2 = new ColorUIResource(96, 96, 96);
    // Aufw�rts (heller)136, 112,  46);

    private final ColorUIResource secondary3 = new ColorUIResource(84, 84, 84);
    // Fl�che   134, 104,  22);

    @Override
    public ColorUIResource getSimpleInternalFrameBackground() {
        return getWhite();
    }

    @Override
    public void addCustomEntriesToTable(UIDefaults table) {
        super.addCustomEntriesToTable(table);
        Object[] uiDefaults = {
                "TextField.ineditableForeground",
                getSoftWhite(),
                "Plastic.brightenStop",
                new Color(255, 255, 255, 20),
                "Plastic.ltBrightenStop",
                new Color(255, 255, 255, 16),
                "SimpleInternalFrame.activeTitleBackground",
                getPrimary2()
                };
        table.putDefaults(uiDefaults);
    }

    @Override
    public ColorUIResource getControlDisabled() {
        return getSoftWhite();
    }

    @Override
    public ColorUIResource getControlHighlight() {
        return getSoftWhite();
    }

    @Override
    public ColorUIResource getControlInfo() {
        return getWhite();
    }

    @Override
    public ColorUIResource getInactiveSystemTextColor() {
        return getSoftWhite();
    }

    @Override
    public ColorUIResource getMenuDisabledForeground() {
        return getSoftWhite();
    }

    @Override
    public ColorUIResource getMenuItemSelectedBackground() {
        return getPrimary3();
    }

    @Override
    public ColorUIResource getMenuItemSelectedForeground() {
        return getBlack();
    }

    @Override
    public ColorUIResource getMenuSelectedBackground() {
        return getPrimary2();
    }

    @Override
    public ColorUIResource getMenuSelectedForeground() {
        return getWhite();
    }

    @Override
    protected ColorUIResource getPrimary1() {
        return primary1;
    }

    @Override
    protected ColorUIResource getPrimary2() {
        return primary2;
    }

    @Override
    protected ColorUIResource getPrimary3() {
        return primary3;
    }
    @Override
    public ColorUIResource getPrimaryControlHighlight() {
        return getSoftWhite();
    }

    @Override
    protected ColorUIResource getSecondary1() {
        return secondary1;
    }

    @Override
    protected ColorUIResource getSecondary2() {
        return secondary2;
    }

    @Override
    protected ColorUIResource getSecondary3() {
        return secondary3;
    }

    @Override
    public ColorUIResource getSeparatorBackground() {
        return getSoftWhite();
    }

    protected ColorUIResource getSoftWhite() {
        return softWhite;
    }

    @Override
    public ColorUIResource getTitleTextColor() {
        return getControlInfo();
    }

    @Override
    public ColorUIResource getToggleButtonCheckColor() {
        return getWhite();
    }

    @Override
    public ColorUIResource getFocusColor() {
        return Colors.GRAY_FOCUS;
    }

//    public FontUIResource getControlTextFont() {
//        return getFont();
//    }
//
//    public FontUIResource getMenuTextFont() {
//        return getFont();
//    }
//
//    public FontUIResource getWindowTitleFont() {
//        return getFont();
//    }
//
//    public FontUIResource getSystemTextFont() {
//        return getFont();
//    }
//
//    public FontUIResource getUserTextFont() {
//        return getFont();
//    }

}
