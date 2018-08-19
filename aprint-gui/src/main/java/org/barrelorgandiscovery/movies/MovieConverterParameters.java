package org.barrelorgandiscovery.movies;

/**
 * Parameters for movie Export function ...
 * 
 * @author Freydiere Patrice
 * 
 */
public class MovieConverterParameters {

	private int width = 600;
	private int height = 400;
	private boolean fastRendering = false;
	private boolean showTime = false;
	private boolean showComposition = false;
	private boolean showRegistration = false;

	public MovieConverterParameters() {

	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setFastRendering(boolean fastRendering) {
		this.fastRendering = fastRendering;
	}

	public boolean isFastRendering() {
		return fastRendering;
	}

	public void setShowTime(boolean showTime) {
		this.showTime = showTime;
	}

	public boolean isShowTime() {
		return showTime;
	}
	
	public boolean isShowComposition() {
		return showComposition;
	}
	
	public void setShowComposition(boolean showComposition) {
		this.showComposition = showComposition;
	}
	
	public void setShowRegistration(boolean showRegistration) {
		this.showRegistration = showRegistration;
	}
	
	public boolean isShowRegistration() {
		return showRegistration;
	}
	
	

}
