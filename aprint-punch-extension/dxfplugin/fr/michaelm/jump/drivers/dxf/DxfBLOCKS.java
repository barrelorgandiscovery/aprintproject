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
import java.io.IOException;
import java.util.Map;
import java.util.List;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;


/**
 * A DXF block contains a block of geometries. The dxf driver can read entities
 * inside a block, but it will not remember that the entities are in a same
 * block.
 * @author Michaël Michaud
 * @version 0.5.0
 */
// History
public class DxfBLOCKS {
    //final static FeatureSchema DXF_SCHEMA = new FeatureSchema();
    FeatureCollection entities;

    public DxfBLOCKS() {
        /*
        DXF_SCHEMA.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        DXF_SCHEMA.addAttribute("LAYER", AttributeType.STRING);
        DXF_SCHEMA.addAttribute("LTYPE", AttributeType.STRING);
        DXF_SCHEMA.addAttribute("THICKNESS", AttributeType.DOUBLE);
        DXF_SCHEMA.addAttribute("COLOR", AttributeType.INTEGER);
        DXF_SCHEMA.addAttribute("TEXT", AttributeType.STRING);
        */
        entities = new FeatureDataset(DxfFile.DXF_SCHEMA);
    }

    public static DxfBLOCKS readBlocks(RandomAccessFile raf) throws IOException {
        DxfBLOCKS blocks = new DxfBLOCKS();
        try {
            DxfGroup group = null;
            String nomVariable;
            while (null != (group = DxfGroup.readGroup(raf)) &&
                   !group.equals(DxfFile.ENDSEC)) {
            }
        } catch(IOException ioe) {throw ioe;}
        return blocks;
    }

    public static DxfBLOCKS readEntities(RandomAccessFile raf) throws IOException {
        DxfBLOCKS dxfEntities = new DxfBLOCKS();
        try {
            DxfGroup group = new DxfGroup(2, "BLOCKS");
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
                     else if (group.getValue().equals("TEXT")) {
                         group = DxfTEXT.readEntity(raf, dxfEntities.entities);
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

}
