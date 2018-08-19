package org.barrelorgandiscovery.recognition.gui.books.steps;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

public class Model implements Serializable {

	private String name; // model name
	private String label;
	transient private BufferedImage modelImage; // image model, must be 128x128
	private URL modelUrl;

	public Model(String name, String label, BufferedImage modelImage, URL stream) {
		this.name = name;
		this.label = label;
		this.modelImage = modelImage;
		this.modelUrl = stream;
	}

	public InputStream createInputStream() throws Exception {
		return modelUrl.openStream();
	}

	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public Image getModelImage() {
		return modelImage;
	}

}