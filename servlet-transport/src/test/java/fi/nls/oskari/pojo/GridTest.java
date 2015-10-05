package fi.nls.oskari.pojo;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GridTest {

	@Test
	public void testIsBoundsOnBoundary() {
		Grid grid = new Grid();
		grid.setRows(3);
		grid.setColumns(3);

		// first & last row, first of the row and last of the row
		assertTrue("first should be on boundary", grid.isBoundsOnBoundary(0));
		assertTrue("first row should be on boundary", grid.isBoundsOnBoundary(1));
		assertTrue("last should be on boundary", grid.isBoundsOnBoundary(2));
		assertTrue("first should be on boundary", grid.isBoundsOnBoundary(3));
		assertTrue("should not be on boundary", !grid.isBoundsOnBoundary(4));
		assertTrue("last should be on boundary", grid.isBoundsOnBoundary(5));
		assertTrue("first should be on boundary", grid.isBoundsOnBoundary(6));
		assertTrue("last row should be on boundary", grid.isBoundsOnBoundary(7));
		assertTrue("last should be on boundary", grid.isBoundsOnBoundary(8));
	}

}
