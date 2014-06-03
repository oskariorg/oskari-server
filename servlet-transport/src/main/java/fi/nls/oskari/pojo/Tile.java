package fi.nls.oskari.pojo;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Handles user's map's size
 * 
 * Used for storing map size in SessionStore.
 * 
 * @see SessionStore
 */
public class Tile {
	private int width;
	private int height;

	/**
	 * Gets width
	 * 
	 * @return width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets width
	 * 
	 * @param width
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Gets height
	 * 
	 * @return height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Sets height
	 * 
	 * @param height
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Print format
	 * 
	 * @return object description
	 */
	@JsonIgnore
	public String toString() {
		return "width: " + this.width + ", height: " + this.height;
	}
}
