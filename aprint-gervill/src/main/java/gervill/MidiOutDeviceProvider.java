/*
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
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

import javax.sound.midi.MidiDevice;
import javax.sound.midi.spi.MidiDeviceProvider;


/**
 * MIDI output device provider.
 *
 * @author Kara Kytle
 * @author Florian Bomers
 */
public class MidiOutDeviceProvider extends AbstractMidiDeviceProvider {

    /** Cache of info objects for all MIDI output devices on the system. */
    static Info[] infos = null;

    /** Cache of open MIDI output devices on the system. */
    static MidiDevice[] devices = null;

    private static boolean enabled;

    // STATIC

    static {
        // initialize
        Platform.initialize();
        enabled = Platform.isMidiIOEnabled();
    }

    // CONSTRUCTOR

    /**
     * Required public no-arg constructor.
     */
    public MidiOutDeviceProvider() {
        if (Printer.trace) Printer.trace("MidiOutDeviceProvider: constructor");
    }

    // implementation of abstract methods in AbstractMidiDeviceProvider

    AbstractMidiDeviceProvider.Info createInfo(int index) {
        if (!enabled) {
            return null;
        }
        return new MidiOutDeviceInfo(index, MidiOutDeviceProvider.class);
    }

    MidiDevice createDevice(AbstractMidiDeviceProvider.Info info) {
        if (enabled && (info instanceof MidiOutDeviceInfo)) {
            return new MidiOutDevice(info);
        }
        return null;
    }

    int getNumDevices() {
        if (!enabled) {
            if (Printer.debug)Printer.debug("MidiOutDevice not enabled, returning 0 devices");
            return 0;
        }
        return nGetNumDevices();
    }

    MidiDevice[] getDeviceCache() { return devices; }
    void setDeviceCache(MidiDevice[] devices) { this.devices = devices; }
    Info[] getInfoCache() { return infos; }
    void setInfoCache(Info[] infos) { this.infos = infos; }


    // INNER CLASSES

    /**
     * Info class for MidiOutDevices.  Adds the
     * provider's Class to keep the provider class from being
     * unloaded.  Otherwise, at least on JDK1.1.7 and 1.1.8,
     * the provider class can be unloaded.  Then, then the provider
     * is next invoked, the static block is executed again and a new
     * instance of the device object is created.  Even though the
     * previous instance may still exist and be open / in use / etc.,
     * the new instance will not reflect that state...
     */
    static class MidiOutDeviceInfo extends AbstractMidiDeviceProvider.Info {
        private Class providerClass;

        private MidiOutDeviceInfo(int index, Class providerClass) {
            super(nGetName(index), nGetVendor(index), nGetDescription(index), nGetVersion(index), index);
            this.providerClass = providerClass;
        }

    } // class MidiOutDeviceInfo


    // NATIVE METHODS

    private static native int nGetNumDevices();
    private static native String nGetName(int index);
    private static native String nGetVendor(int index);
    private static native String nGetDescription(int index);
    private static native String nGetVersion(int index);
}
