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

import javax.swing.plaf.ColorUIResource;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;

/**
 * A theme with metal blue primary colors and a light gray window background.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.9 $
 */

public class SkyKrupp extends AbstractSkyTheme {

    @Override
    public String getName() {
        return "Sky Krupp";
    }

    private final ColorUIResource primary1 = new ColorUIResource(54, 54, 90);
    private final ColorUIResource primary2 = new ColorUIResource(156, 156, 178);
    private final ColorUIResource primary3 = new ColorUIResource(197, 197, 221);

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
    public ColorUIResource getMenuItemSelectedBackground() {
        return getPrimary1();
    }
    @Override
    public ColorUIResource getMenuItemSelectedForeground() {
        return getWhite();
    }
    @Override
    public ColorUIResource getMenuSelectedBackground() {
        return getSecondary2();
    }

    @Override
    public ColorUIResource getFocusColor() {
        return PlasticLookAndFeel.getHighContrastFocusColorsEnabled()
            ? Colors.ORANGE_FOCUS
            : Colors.GRAY_DARK;
    }

}
