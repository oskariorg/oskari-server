package fi.nls.oskari.pojo;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles grid for WFS layer's. Contains bounds of tiles
 * 
 * Used for storing grid in SessionStore.
 * 
 * @see SessionStore
 */
public class Grid {
	private int rows;
	private int columns;
	private List<List<Double>> bounds;

	/**
	 * Constructs object without parameters
	 */
	public Grid() {
		bounds = new ArrayList<List<Double>>();
	}

	/**
	 * Gets rows
	 * 
	 * @return rows
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * Sets rows
	 * 
	 * @param rows
	 */
	public void setRows(int rows) {
		this.rows = rows;
	}

	/**
	 * Gets columns
	 * 
	 * @return columns
	 */
	public int getColumns() {
		return columns;
	}

	/**
	 * Sets columns
	 * 
	 * @param columns
	 */
	public void setColumns(int columns) {
		this.columns = columns;
	}

	/**
	 * Gets bounds
	 * 
	 * @return bounds
	 */
	public List<List<Double>> getBounds() {
		return bounds;
	}

	/**
	 * Sets bounds
	 * 
	 * @param bounds
	 */
	public void setBounds(List<List<Double>> bounds) {
		this.bounds = bounds;
	}

	/**
	 * Checks if bounds are on boundary
	 * Not a save algoritm, because of OpenLayers TileStrategy grid
	 * (border tile could be on a second column or a second last, etc...
	 * 
	 * @param index
	 * @return <code>true</code> if bounds of the given index are on the boundary; <code>false</code>
	 *         otherwise.
	 */
	@JsonIgnore
	public boolean isBoundsOnBoundary(int index) {
		if(index < columns) // first
			return true;
		else if(index >= (rows*columns) - columns) // last
			return true;
		else if(index % columns == 0) // left
			return true;
		else if((index+1) % columns == 0) // right
			return true;
		return false;
	}

    /**
     * Checks if bounds are on boundary, use coordinates
     *
     * @param location map location
     * @param bbox   grid tile bbox  left,bottom - right,top
     * @return <code>true</code> if bbox is not inside map location ; <code>false</code>
     *         otherwise.
     */
	@JsonIgnore
	public boolean isBoundsOnBoundary2(Location location, Double[] bbox) {

		if(bbox[0] < location.getLeft()  ){  // left check outside
			return true;
		}
		else if (bbox[2] > location.getRight()  ) {  // right check outside
			return true;
		}
		else if (bbox[1] < location.getBottom()  ) {  // bottom check outside
			return true;
		}
		else if (bbox[3] > location.getTop()  ) {  // top check outside
			return true;
		}
		return false;
	}
}
