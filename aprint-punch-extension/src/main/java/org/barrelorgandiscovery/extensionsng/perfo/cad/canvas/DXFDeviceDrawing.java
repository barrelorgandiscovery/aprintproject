package org.barrelorgandiscovery.extensionsng.perfo.cad.canvas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.StreamsTools;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;

import fr.michaelm.jump.drivers.dxf.DxfFile;

public class DXFDeviceDrawing extends DeviceDrawing {

	private static Logger logger = Logger.getLogger(DXFDeviceDrawing.class);

	private static final String LAYER_ATTRIBUTE_NAME = "LAYER";

	private static final String SHAPE_ATTRIBUTE_NAME = "SHAPE";

	private FeatureSchema fs;

	private FeatureCollection fc;

	public DXFDeviceDrawing() {
		fs = new FeatureSchema();
		fs.addAttribute(SHAPE_ATTRIBUTE_NAME, AttributeType.GEOMETRY);
		fs.addAttribute(LAYER_ATTRIBUTE_NAME, AttributeType.STRING);

		fc = new FeatureDataset(fs);
	}

	private Feature toFeature(Geometry g, String layerName) {
		Feature feature = FeatureUtil.toFeature(g, fs);
		if (layerName != null) {
			feature.setAttribute(LAYER_ATTRIBUTE_NAME, layerName);
		}
		return feature;
	}

	@Override
	protected void addObject(Geometry g) {
		String currentLayer = getCurrentLayer();
		fc.add(toFeature(g, currentLayer));
	}

	public void write(File file, String[] layers) throws Exception {
		FileWriter fw = new FileWriter(file);
		try {

			DxfFile.write(fc, layers, fw, 3, false);

		} finally {
			fw.close();
		}

	}

	@Override
	public void write(OutputStream outStream, String[] layers) throws Exception {
		assert outStream != null;

		File tmpFile = File.createTempFile("dxftmp", ".tmp");
		logger.debug("start writing temp file " + tmpFile);
		try {
			FileWriter fw = new FileWriter(tmpFile);
			try {
				DxfFile.write(fc, layers, fw, 3, false);

				FileInputStream isfile = new FileInputStream(tmpFile);
				try {
					StreamsTools.copyStream(isfile, outStream);
				} finally {
					isfile.close();
				}
			} finally {
				fw.close();
			}
		} finally {
			tmpFile.delete();
		}

	}

	@Override
	public boolean ignoreReference() {
		return false;
	}
	
}
