/*
 * Library name : dxf
 * (C) 2006 Micha�l Michaud
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * michael.michaud@free.fr
 *
 */

package fr.michaelm.jump.drivers.dxf;

import java.io.RandomAccessFile;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Dxf section between the HEADER and the TABLES sections.
 * Not mandatory for DXF 12, don't use it.
 */
public class DxfCLASSES {

    public DxfCLASSES() {}

    public static DxfCLASSES readClasses(RandomAccessFile raf) throws IOException {
        DxfCLASSES classes = new DxfCLASSES();
        try {
            DxfGroup group = null;
            while (null != (group = DxfGroup.readGroup(raf)) &&
                                !group.equals(DxfFile.ENDSEC)) {}
        } catch(IOException ioe) {throw ioe;}
        return classes;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(DxfFile.SECTION.toString());
        sb.append(DxfFile.CLASSES.toString());
        sb.append(DxfFile.ENDSEC.toString());
        return sb.toString();
    }
}
