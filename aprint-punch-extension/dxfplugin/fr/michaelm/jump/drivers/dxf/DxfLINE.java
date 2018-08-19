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
import java.io.IOException;
import java.util.List;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.feature.FeatureCollection;

/**
 * LINE DXF entity.
 * This class has a static method reading a DXF LINE and adding the new
 * feature to a FeatureCollection
 * @author Micha�l Michaud
 * @version 0.5.0
 */
// History
public class DxfLINE extends DxfENTITY {

    public DxfLINE() {super("DEFAULT");}

    public static DxfGroup readEntity(RandomAccessFile raf, FeatureCollection entities)
                                                            throws IOException {
        Feature feature = new BasicFeature(DxfFile.DXF_SCHEMA);
        feature.setAttribute("LTYPE", "BYLAYER");
        feature.setAttribute("ELEVATION", new Double(0.0));
        feature.setAttribute("THICKNESS", new Double(0.0));
        feature.setAttribute("COLOR", new Integer(256)); // equivalent to BYLAYER
        feature.setAttribute("TEXT", "");
        feature.setAttribute("TEXT_HEIGHT", new Double(0.0));
        feature.setAttribute("TEXT_STYLE", "STANDARD");
        double x1=Double.NaN, y1=Double.NaN, z1=Double.NaN;
        double x2=Double.NaN, y2=Double.NaN, z2=Double.NaN;
        DxfGroup group;
        try {
            while (null != (group = DxfGroup.readGroup(raf)) && group.getCode()!=0) {
                if (group.getCode()==8) feature.setAttribute("LAYER", group.getValue());
                else if (group.getCode()==6) feature.setAttribute("LTYPE", group.getValue());
                else if (group.getCode()==39) feature.setAttribute("THICKNESS", new Double(group.getDoubleValue()));
                else if (group.getCode()==62) feature.setAttribute("COLOR", new Integer(group.getIntValue()));
                else if (group.getCode()==10) x1 = group.getDoubleValue();
                else if (group.getCode()==20) y1 = group.getDoubleValue();
                else if (group.getCode()==30) z1 = group.getDoubleValue();
                else if (group.getCode()==11) x2 = group.getDoubleValue();
                else if (group.getCode()==21) y2 = group.getDoubleValue();
                else if (group.getCode()==31) z2 = group.getDoubleValue();
                else {}
            }
            if (x1!=Double.NaN && y1!=Double.NaN && x2!=Double.NaN && y2!=Double.NaN) {
                feature.setGeometry(new LineString(
                    new Coordinate[]{new Coordinate(x1,y1,z1),new Coordinate(x2,y2,z2)}, DPM, 0));
                entities.add(feature);
            }
            //System.out.println("\t" + feature.getAttribute("LAYER").toString() +
            //                    "\t" + feature.getAttribute("GEOMETRY").toString());
        } catch (IOException ioe) {throw ioe;}
        return group;
    }


}
