/*
 * Library name : dxf
 * (C) 2006 Michaël Michaud
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
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;

/**
 * The ENTITIES section of a DXF file containing all data itself
 * @author Michaël Michaud
 * @version 0.5.0
 */
// History
public class DxfENTITIES {
    FeatureCollection entities;

    public DxfENTITIES() {
        entities = new FeatureDataset(DxfFile.DXF_SCHEMA);
    }
    
    public FeatureCollection getEntities() {
        return entities;
    }
    public void setEntities(FeatureCollection featureCollection) {
        this.entities = featureCollection;
    }

    public static DxfENTITIES readEntities(RandomAccessFile raf) throws IOException {
        DxfENTITIES dxfEntities = new DxfENTITIES();
        try {
            DxfGroup group = new DxfGroup(2, "ENTITIES");
            String nomVariable;
            while (!group.equals(DxfFile.ENDSEC)) {
                 if (group.getCode() == 0) {
                     if (group.getValue().equals("POINT")) {
                         group = DxfPOINT.readEntity(raf, dxfEntities.entities);
                     }
                     else if (group.getValue().equals("TEXT")) {
                         group = DxfTEXT.readEntity(raf, dxfEntities.entities);
                     }
                     else if (group.getValue().equals("LINE")) {
                         group = DxfLINE.readEntity(raf, dxfEntities.entities);
                     }
                     else if (group.getValue().equals("POLYLINE")) {
                         group = DxfPOLYLINE.readEntity(raf, dxfEntities.entities);
                     }
                     else if (group.getValue().equals("LWPOLYLINE")) {
                         group = DxfLWPOLYLINE.readEntity(raf, dxfEntities.entities);
                     }
                     else {group = DxfGroup.readGroup(raf);}
                 }
                 else {
                     //System.out.println("Group " + group.getCode() + " " + group.getValue() + " UNKNOWN");
                     group = DxfGroup.readGroup(raf);
                 }
            }
        } catch(IOException ioe) {throw ioe;}
        return dxfEntities;
    }

    public String toString() {
        Iterator it = entities.iterator();
        Feature feature;
        StringBuffer sb = new StringBuffer(DxfFile.SECTION.toString());
        sb.append(DxfFile.ENTITIES.toString());
        while (it.hasNext()) {
            feature = (Feature)it.next();
            sb.append(DxfENTITY.feature2Dxf(feature, "LAYER0", true));
        }
        sb.append(DxfFile.ENDSEC.toString());
        return sb.toString();
    }

    public void write(BufferedWriter bw, String defaultLayer) throws IOException {
        Iterator it = entities.iterator();
        Feature feature;
        StringBuffer sb = new StringBuffer(DxfFile.SECTION.toString());
        sb.append(DxfFile.ENTITIES.toString());
        try {
            bw.write(sb.toString());
            while (it.hasNext()) {
                feature = (Feature)it.next();
                bw.write(DxfENTITY.feature2Dxf(feature, defaultLayer, true));
            }
            sb.append(DxfFile.ENDSEC.toString());
            bw.write(DxfFile.ENDSEC.toString());
        }
        catch(IOException ioe) {throw ioe;}
    }

}
