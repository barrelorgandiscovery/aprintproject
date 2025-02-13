/*
 * Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package gervill;

import javax.sound.midi.MidiChannel;

/**
 * A MidiChannel proxy object used for external access to synthesizer internal
 * channel objects.
 *
 * @author Karl Helgason
 */
public class SoftChannelProxy implements MidiChannel {

    private MidiChannel channel = null;

    public MidiChannel getChannel() {
        return channel;
    }

    public void setChannel(MidiChannel channel) {
        this.channel = channel;
    }

    public void allNotesOff() {
        if (channel == null)
            return;
        channel.allNotesOff();
    }

    public void allSoundOff() {
        if (channel == null)
            return;
        channel.allSoundOff();
    }

    public void controlChange(int controller, int value) {
        if (channel == null)
            return;
        channel.controlChange(controller, value);
    }

    public int getChannelPressure() {
        if (channel == null)
            return 0;
        return channel.getChannelPressure();
    }

    public int getController(int controller) {
        if (channel == null)
            return 0;
        return channel.getController(controller);
    }

    public boolean getMono() {
        if (channel == null)
            return false;
        return channel.getMono();
    }

    public boolean getMute() {
        if (channel == null)
            return false;
        return channel.getMute();
    }

    public boolean getOmni() {
        if (channel == null)
            return false;
        return channel.getOmni();
    }

    public int getPitchBend() {
        if (channel == null)
            return 8192;
        return channel.getPitchBend();
    }

    public int getPolyPressure(int noteNumber) {
        if (channel == null)
            return 0;
        return channel.getPolyPressure(noteNumber);
    }

    public int getProgram() {
        if (channel == null)
            return 0;
        return channel.getProgram();
    }

    public boolean getSolo() {
        if (channel == null)
            return false;
        return channel.getSolo();
    }

    public boolean localControl(boolean on) {
        if (channel == null)
            return false;
        return channel.localControl(on);
    }

    public void noteOff(int noteNumber) {
        if (channel == null)
            return;
        channel.noteOff(noteNumber);
    }

    public void noteOff(int noteNumber, int velocity) {
        if (channel == null)
            return;
        channel.noteOff(noteNumber, velocity);
    }

    public void noteOn(int noteNumber, int velocity) {
        if (channel == null)
            return;
        channel.noteOn(noteNumber, velocity);
    }

    public void programChange(int program) {
        if (channel == null)
            return;
        channel.programChange(program);
    }

    public void programChange(int bank, int program) {
        if (channel == null)
            return;
        channel.programChange(bank, program);
    }

    public void resetAllControllers() {
        if (channel == null)
            return;
        channel.resetAllControllers();
    }

    public void setChannelPressure(int pressure) {
        if (channel == null)
            return;
        channel.setChannelPressure(pressure);
    }

    public void setMono(boolean on) {
        if (channel == null)
            return;
        channel.setMono(on);
    }

    public void setMute(boolean mute) {
        if (channel == null)
            return;
        channel.setMute(mute);
    }

    public void setOmni(boolean on) {
        if (channel == null)
            return;
        channel.setOmni(on);
    }

    public void setPitchBend(int bend) {
        if (channel == null)
            return;
        channel.setPitchBend(bend);
    }

    public void setPolyPressure(int noteNumber, int pressure) {
        if (channel == null)
            return;
        channel.setPolyPressure(noteNumber, pressure);
    }

    public void setSolo(boolean soloState) {
        if (channel == null)
            return;
        channel.setSolo(soloState);
    }
}
