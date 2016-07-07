package fi.nls.oskari.pojo;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.List;

/**
 * Handles user's active WFS layers
 * 
 * Used for storing layers in SessionStore.
 * 
 * @see SessionStore
 */
public class Layer {
	private String id;
	private String styleName;
	private boolean visible = true;
	private List<String> highlightedFeatureIds = null;
    private List<List<Double>> tiles = null;

	/**
	 * Style named "none" has special meaning:
	 * Tiles should not be rendered when style is none.
	 * @return
     */
	public boolean hasVisibleStyle() {
		return !"oskari_none".equals(styleName);
	}
	/**
	 * Constructs object with full information
	 * 
	 * @param id
	 * @param styleName
	 */
	public Layer(String id, String styleName) {
		this.id = id;
		this.styleName = styleName;
	}

	/**
	 * Constructs object without parameters
	 */
	public Layer() {
	}

	/**
	 * Gets id
	 * 
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets id
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets style name
	 * 
	 * @return style name
	 */
	public String getStyleName() {
		return styleName;
	}

	/**
	 * Sets style id
	 * 
	 * @param styleName
	 */
	public void setStyleName(String styleName) {
		this.styleName = styleName;
	}

	/**
	 * Checks if layer is visible
	 * 
	 * @return <code>true</code> if layer is visible; <code>false</code>
	 *         otherwise.
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Sets if visible
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Gets highlighted feature ids
	 * 
	 * @return highlighted feature ids
	 */
	@JsonIgnore
	public List<String> getHighlightedFeatureIds() {
		return highlightedFeatureIds;
	}

	/**
	 * Sets highlighted feature ids
	 * 
	 * @param highlightedFeatureIds
	 */
	public void setHighlightedFeatureIds(List<String> highlightedFeatureIds) {
		this.highlightedFeatureIds = highlightedFeatureIds;
	}

    /**
     * Gets tiles (bounding boxes)
     *
     * @return tiles
     */
    @JsonIgnore
    public List<List<Double>> getTiles() {
        return tiles;
    }

    /**
     * Sets tiles (bounding boxes)
     *
     * @param tiles
     */
    public void setTiles(List<List<Double>> tiles) {
        this.tiles = tiles;
    }

    /**
     * Checks if bounds are found in tiles
     *
     * @param bounds
     * @return <code>true</code> if found in tiles; <code>false</code>
     *         otherwise.
     */
    @JsonIgnore
    public boolean isTile(List<Double> bounds) {
        boolean result;
        for (List<Double> tile : this.tiles) {
            result = true;
            for (int i = 0; i < tile.size(); i++) {
                if(bounds.size() <= i) {
                    result = false;
                    break;
                }
                if(Double.compare(tile.get(i), bounds.get(i)) != 0) {
                    result = false;
                    break;
                }
            }
            if(result) return true;
        }
        return false;
    }

	/**
	 * Print format
	 * 
	 * @return object description
	 */
	@JsonIgnore
	public String toString() {
		return "id: " + this.id + ", style name: " + this.styleName;
	}
}
