package org.barrelorgandiscovery.virtualbook;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.ImageTools;

/**
 * Metadata informations about the virtual book
 * 
 * @author Freydiere Patrice
 * 
 */
public class VirtualBookMetadata implements Serializable {

	private static Logger logger = Logger.getLogger(VirtualBookMetadata.class);
	
	private String name; // for editing in bean info ...

	private String author;
	private String arranger;
	private String description;
	private String genre;

	private Date creationDate;
	private Date lastModifiedDate;

	private transient Image cover;

	// light weight image for displaying on the screen
	private transient Image optimizedImage;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getArranger() {
		return arranger;
	}

	public void setArranger(String arranger) {
		this.arranger = arranger;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public void setCover(Image cover) {
		this.cover = cover;
		this.optimizedImage = null;
		if (cover != null) {
			try {
			BufferedImage loadedCover = ImageTools.loadImage(cover);
			optimizedImage = ImageTools.crop(1000, 1000, loadedCover);
			} catch(Exception ex)
			{
				logger.error("fail to load the cover image");
			}
		}
	}

	public Image getCover() {
		return cover;
	}

	/**
	 * Return an adapted image for displaying (optimize the display)
	 * 
	 * @param width
	 * @return
	 */
	public Image getCoverForDisplay(int width) {
		if (cover == null)
			return null;

		if (optimizedImage != null) {
			if (width < optimizedImage.getWidth(null)) {
				return optimizedImage;
			}
		}

		return cover;
	}

}
