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
import java.util.List;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.FeatureCollection;


/**
 * POLYLINE DXF entity.
 * This class has a static method reading a DXF POLYLINE and adding the new
 * feature to a FeatureCollection
 * @author Michaël Michaud
 * @version 0.5.0
 */
// History
public class DxfPOLYLINE extends DxfENTITY {

    public DxfPOLYLINE() {super("DEFAULT");}

    public static DxfGroup readEntity(RandomAccessFile raf, FeatureCollection entities)
                                                            throws IOException {
        Feature feature = new BasicFeature(entities.getFeatureSchema());
        String geomType = "LineString";
        CoordinateList coordList = new CoordinateList();
        feature.setAttribute("LTYPE", "BYLAYER");
        feature.setAttribute("ELEVATION", new Double(0.0));
        feature.setAttribute("THICKNESS", new Double(0.0));
        feature.setAttribute("COLOR", new Integer(256)); // equivalent to BYLAYER
        feature.setAttribute("TEXT", "");
        feature.setAttribute("TEXT_HEIGHT", new Double(0.0));
        feature.setAttribute("TEXT_STYLE", "STANDARD");
        double x=Double.NaN, y=Double.NaN, z=Double.NaN;
        DxfGroup group = DxfFile.ENTITIES;
        try {
            while (!group.equals(DxfFile.ENDSEC)) {
                if (group.getCode()==8) {
                    feature.setAttribute("LAYER", group.getValue());
                    group = DxfGroup.readGroup(raf);
                }
                else if (group.getCode()==6) {
                    feature.setAttribute("LTYPE", group.getValue());
                    group = DxfGroup.readGroup(raf);
                }
                else if (group.getCode()==38) {
                    feature.setAttribute("ELEVATION", new Double(group.getDoubleValue()));
                    group = DxfGroup.readGroup(raf);
                }
                else if (group.getCode()==39) {
                    feature.setAttribute("THICKNESS", new Double(group.getDoubleValue()));
                    group = DxfGroup.readGroup(raf);
                }
                else if (group.getCode()==62) {
                    feature.setAttribute("COLOR", new Integer(group.getIntValue()));
                    group = DxfGroup.readGroup(raf);
                }
                else if (group.getCode()==70) {
                    if ((group.getIntValue()&1)==1) geomType = "Polygon";
                    group = DxfGroup.readGroup(raf);
                }
                else if (group.equals(VERTEX)) {
                    group = DxfVERTEX.readEntity(raf, coordList);
                }
                else if (group.equals(SEQEND)) {group = DxfGroup.readGroup(raf);}
                else if (group.getCode()==0) {
                    // 0 group different from VERTEX and different from SEQEND
                    break;
                }
                else {group = DxfGroup.readGroup(raf);}
            }
            if (geomType.equals("LineString")) {
                feature.setGeometry(new LineString(coordList.toCoordinateArray(), DPM, 0));
                entities.add(feature);
            }
            else if (geomType.equals("Polygon")) {
                coordList.closeRing();
                feature.setGeometry(new Polygon(new LinearRing(coordList.toCoordinateArray(), DPM, 0), DPM, 0));
                entities.add(feature);
            }
            else {}
            //System.out.println("\t" + feature.getAttribute("LAYER").toString() +
            //                    "\t" + feature.getAttribute("GEOMETRY").toString());
        } catch (IOException ioe) {throw ioe;}
        return group;
    }

}
