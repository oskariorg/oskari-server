/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2001-2007 TOPP - www.openplans.org.
 *
 *    Modified by Oskari.org 2014.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geoserver.wps.oskari;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.ProcessException;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.process.gs.GSProcess;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.ProgressListener;

import java.util.*;

/**
 * Oskari Rendering Transformation process which aggregates features into a set of
 * visually non-conflicting point features.
 * The created points have attributes which provide the total number of points
 * aggregated, as well as the number of unique point locations.
 * <p>
 * This is sometimes called "point clustering". 
 * The term stacking is used instead, since clustering has multiple
 * meanings in geospatial processing - it is also used to 
 * mean identifying groups defined by point proximity.
 * <p>
 * The stacking is defined by specifying a grid to aggregate to.
 * The grid cell size is specified in pixels relative to the requested output image size.
 * This makes it more intuitive to pick an appropriate grid size,
 * and ensures that the aggregation works at all zoom levels.
 * <p>
 * The output is a FeatureCollection containing the following attributes:
 * <ul>
 * <li><code>geom</code> - the point representing the cluster
 * <li><code>count</code> - the total number of points in the cluster
 * <li><code>countunique</code> - the number of unique point locations in the cluster
 * </ul>
 * Note that as required by the Rendering Transformation API, the output 
 * has the CRS of the input data. 
 * 
 */
@DescribeProcess(title = "Oskari Point Stacker", description = "Aggregates a collection of points over a grid into one point per grid cell.")
public class OskariPointStackerProcess implements GSProcess {
    
    public enum PreserveLocation { 
        /**
         * Preserves the original point location in case there is a single point in the cell
         */
        Single,
        /**
         * Preserves the original point location in case there are multiple points, but all with the same coordinates in the cell
         */
        Superimposed,
        /**
         * Default value, averages the point locations with the cell center to try and avoid conflicts among the symbolizers for the 
         */
        Never};

    public static final String ATTR_GEOM = "geom";
    public static final String ATTR_COUNT = "count";
    public static final String ATTR_COUNT_UNIQUE = "countunique";
    public static final String ATTR_NORM_COUNT = "normCount";
    public static final String ATTR_NORM_COUNT_UNIQUE = "normCountUnique";

    //TODO: add ability to pick index point selection strategy
    //TODO: add ability to set attribute name containing value to be aggregated
    //TODO: add ability to specify aggregation method (COUNT, SUM, AVG)
    //TODO: ultimately could allow aggregating multiple input attributes, with different methods for each
    //TODO: allow including attributes from input data (eg for use with points that are not aggregated)
    //TODO: expand query window to avoid edge effects?
    
    // no process state is defined, since RenderingTransformation processes must be stateless

    @DescribeResult(name = "result", description = "Aggregated feature collection")
    public SimpleFeatureCollection execute(

            // process data
            @DescribeParameter(name = "data", description = "Input feature collection") SimpleFeatureCollection data,

            // process parameters
            @DescribeParameter(name = "cellSize", description = "Grid cell size to aggregate to, in pixels") Integer cellSize,
            @DescribeParameter(name = "normalize", description = "Indicates whether to add fields normalized to the range 0-1.", defaultValue="false") Boolean argNormalize,
            @DescribeParameter(name = "preserveLocation", description = "Indicates wheter to preserve the original location of points for single/superimposed points", defaultValue="Never", min=0) PreserveLocation preserveLocation,

            // output image parameters
            @DescribeParameter(name = "outputBBOX", description = "Bounding box for target image extent") ReferencedEnvelope outputEnv,
            @DescribeParameter(name = "outputWidth", description = "Target image width in pixels", minValue = 1) Integer outputWidth,
            @DescribeParameter(name = "outputHeight", description = "Target image height in pixels", minValue = 1) Integer outputHeight,
            

            ProgressListener monitor) throws ProcessException, TransformException {

        CoordinateReferenceSystem srcCRS = data.getSchema().getCoordinateReferenceSystem();
        CoordinateReferenceSystem dstCRS = outputEnv.getCoordinateReferenceSystem();
        MathTransform crsTransform = null;
        MathTransform invTransform = null;
        try {
            crsTransform = CRS.findMathTransform(srcCRS, dstCRS);
            invTransform = crsTransform.inverse();
        } catch (FactoryException e) {
            throw new ProcessException(e);
        }
        
        boolean normalize = false;
        if(argNormalize!=null){
            normalize = argNormalize;
        }

        // TODO: allow output CRS to be different to data CRS 
        // assume same CRS for now...
        double cellSizeSrc = cellSize * outputEnv.getWidth() / outputWidth;

        Collection<StackedPoint> stackedPts = stackPoints(data, crsTransform, cellSizeSrc,
                outputEnv.getMinX(), outputEnv.getMinY());

        SimpleFeatureType schema = createType(srcCRS, normalize);
        ListFeatureCollection result = new ListFeatureCollection(schema);
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(schema);

        GeometryFactory factory = new GeometryFactory(new PackedCoordinateSequenceFactory());

        double[] srcPt = new double[2];
        double[] dstPt = new double[2];

        // Find maxima of the point stacks if needed.
        int maxCount = 0;
        int maxCountUnique = 0;
        if(normalize){
            for (StackedPoint sp : stackedPts) {
                if(maxCount<sp.getCount()) maxCount = sp.getCount();
                if(maxCountUnique<sp.getCount()) maxCountUnique = sp.getCountUnique();
            }
        }

        for (StackedPoint sp : stackedPts) {
            // create feature for stacked point
            Coordinate pt = getStackedPointLocation(preserveLocation, sp);
            
            // transform back to src CRS, since RT rendering expects the output to be in the same CRS
            srcPt[0] = pt.x;
            srcPt[1] = pt.y;
            invTransform.transform(srcPt, 0, dstPt, 0, 1);
            Coordinate psrc = new Coordinate(dstPt[0], dstPt[1]);

            Geometry point = factory.createPoint(psrc);
            fb.add(point);
            fb.add(sp.getCount());
            fb.add(sp.getCountUnique());
            if(normalize){
                fb.add(((double)sp.getCount())/maxCount);
                fb.add(((double)sp.getCountUnique())/maxCountUnique);
            }
            fb.add(sp.getName());
            fb.add(sp.getAttention_text());
            fb.add(sp.getGeometryType());
            fb.add(sp.getStroke_width());
            fb.add(sp.getStroke_color());
            fb.add(sp.getFill_color());
            fb.add(sp.getDot_color());
            fb.add(sp.getDot_size());
            fb.add(sp.getDot_shape());
            fb.add(sp.getBorder_width());
            fb.add(sp.getBorder_color());
            fb.add(sp.getFill_pattern());
            fb.add(sp.getStroke_linejoin());
            fb.add(sp.getStroke_linecap());
            fb.add(sp.getStroke_dasharray());
            fb.add(sp.getBorder_linejoin());
            fb.add(sp.getBorder_dasharray());

            result.add(fb.buildFeature(null));
        }
        return result;
    }

    /**
     * Extract the geometry
     * @param preserveLocation
     * @param sp
     * @return
     */
    private Coordinate getStackedPointLocation(PreserveLocation preserveLocation, StackedPoint sp) {
        Coordinate pt;
        if (sp.getCount() == 1) {
            pt = sp.getOriginalLocation();
        } else {
            pt = sp.getLocation();
        }
        return pt;
    }

    /**
     * Computes the stacked points for the given data collection.
     * All geometry types are handled - for non-point geometries, the centroid is used.
     * 
     * @param data
     * @param cellSize
     * @param minX
     * @param minY
     * @return
     * @throws TransformException 
     */
    private Collection<StackedPoint> stackPoints(SimpleFeatureCollection data,
            MathTransform crsTransform, 
            double cellSize, double minX, double minY) throws TransformException {
        SimpleFeatureIterator featureIt = data.features();

        Map<Coordinate, StackedPoint> stackedPts = new HashMap<Coordinate, StackedPoint>();

        double[] srcPt = new double[2];
        double[] dstPt = new double[2];

        Coordinate indexPt = new Coordinate();
        try {
            while (featureIt.hasNext()) {
                SimpleFeature feature = featureIt.next();
                // get the point location from the geometry
                Geometry geom = (Geometry) feature.getDefaultGeometry();

                // Lines and polygons are not included
                if (!(geom.getGeometryType().equals("Point"))&&!(geom.getGeometryType().equals("MultiPoint"))) {
                    continue;
                }

                Coordinate[] ps = getRepresentativePoints(geom);

                for (int j=0; j<ps.length; j++) {
                    Coordinate p = ps[j];
                    // reproject data point to output CRS, if required
                    srcPt[0] = p.x;
                    srcPt[1] = p.y;
                    crsTransform.transform(srcPt, 0, dstPt, 0, 1);
                    Coordinate pout = new Coordinate(dstPt[0], dstPt[1]);

                    indexPt.x = pout.x;
                    indexPt.y = pout.y;
                    gridIndex(indexPt, cellSize);

                    StackedPoint stkPt = stackedPts.get(indexPt);
                    if (stkPt == null) {

                        double centreX = indexPt.x * cellSize + cellSize / 2;
                        double centreY = indexPt.y * cellSize + cellSize / 2;

                        stkPt = new StackedPoint(indexPt, new Coordinate(centreX, centreY));
                        if (feature.getAttribute("name") != null) {
                            stkPt.setName(String.valueOf(feature.getAttribute("name")));
                        } else {
                            stkPt.setName("");
                        }
                        if (feature.getAttribute("attention_text") != null) {
                            stkPt.setAttention_text(String.valueOf(feature.getAttribute("attention_text")));
                        } else {
                            stkPt.setAttention_text("");
                        }
                        stkPt.setGeometryType(geom.getGeometryType());
                        stkPt.setStroke_width(Integer.valueOf(String.valueOf(feature.getAttribute("stroke_width"))));
                        stkPt.setStroke_color(String.valueOf(feature.getAttribute("stroke_color")));
                        stkPt.setFill_color(String.valueOf(feature.getAttribute("fill_color")));
                        stkPt.setDot_color(String.valueOf(feature.getAttribute("dot_color")));
                        stkPt.setDot_size(Integer.valueOf(String.valueOf(feature.getAttribute("dot_size"))));
                        stkPt.setDot_shape(String.valueOf(feature.getAttribute("dot_shape")));
                        stkPt.setBorder_width(Integer.valueOf(String.valueOf(feature.getAttribute("border_width"))));
                        stkPt.setBorder_color(String.valueOf(feature.getAttribute("border_color")));
                        stkPt.setFill_pattern(Integer.valueOf(String.valueOf(feature.getAttribute("fill_pattern"))));
                        stkPt.setStroke_linejoin(String.valueOf(feature.getAttribute("stroke_linejoin")));
                        stkPt.setStroke_linecap(String.valueOf(feature.getAttribute("stroke_linecap")));
                        stkPt.setStroke_dasharray(String.valueOf(feature.getAttribute("stroke_dasharray")));
                        stkPt.setBorder_linejoin(String.valueOf(feature.getAttribute("border_linejoin")));
                        stkPt.setBorder_dasharray(String.valueOf(feature.getAttribute("border_dasharray")));

                        stackedPts.put(stkPt.getKey(), stkPt);
                    }
                    stkPt.add(pout);
                }
            }

        } finally {
            featureIt.close();
        }
        return stackedPts.values();
    }

    /**
     * Gets a point to represent the Geometry.
     * If the Geometry is a point, this is returned.
     * Otherwise, the centroid is used.
     * 
     * @param g the geometry to find a point for
     * @return a point representing the Geometry
     */
    private static Coordinate getRepresentativePoint(Geometry g)
    {
        if (g.getNumPoints() == 1) {
            return g.getCoordinate();
        }
        return g.getCentroid().getCoordinate();
    }
    
    /**
     * Gets points representing the geometry.
     *
     * @param g the geometry to find points for
     * @return points representing the geometry
     */
    private static Coordinate[] getRepresentativePoints(Geometry g)
    {
        Coordinate[] coordinates;
        int numPoints = g.getNumPoints();
        if (g.getGeometryType().equals("MultiPoint")) {
            coordinates = g.getCoordinates();
        } else {
            coordinates = new Coordinate[1];
            if (numPoints == 1) {
                coordinates[0] = g.getCoordinate();
            } else {
                coordinates[0] = g.getCentroid().getCoordinate();
            }
        }
        return coordinates;
    }

    /**
     * Computes the grid index for a point for the grid determined by the cellsize.
     * 
     * @param griddedPt the point to grid, and also holds the output value
     * @param cellSize the grid cell size
     */
    private void gridIndex(Coordinate griddedPt, double cellSize) {
        
        // TODO: is there any situation where this could result in too much loss of precision?  
        /**
         * The grid is based at the origin of the entire data space, 
         * not just the query window.
         * This makes gridding stable during panning.
         * 
         * This should not lose too much precision for any reasonable coordinate system and map size.
         * The worst case is a CRS with small ordinate values, and a large cell size.
         * The worst case tested is a map in degrees, zoomed out to show about twice the globe - works fine.
         */
        // Use longs to avoid possible overflow issues (e.g. for a very small cell size)
        long ix = (long) ((griddedPt.x) / cellSize);
        long iy = (long) ((griddedPt.y) / cellSize);
        
        griddedPt.x = ix;
        griddedPt.y = iy;
    }

    private SimpleFeatureType createType(CoordinateReferenceSystem crs, boolean stretch) {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.add(ATTR_GEOM, Point.class, crs);
        tb.add(ATTR_COUNT, Integer.class);
        tb.add(ATTR_COUNT_UNIQUE, Integer.class);
        if(stretch){
            tb.add(ATTR_NORM_COUNT, Double.class);
            tb.add(ATTR_NORM_COUNT_UNIQUE, Double.class);
        }
        tb.add("name",String.class);
        tb.add("attention_text",String.class);
        tb.add("geometryType",String.class);
        tb.add("stroke_width",Integer.class);
        tb.add("stroke_color",String.class);
        tb.add("fill_color",String.class);
        tb.add("dot_color",String.class);
        tb.add("dot_size",Integer.class);
        tb.add("dot_shape",String.class);
        tb.add("border_width",Integer.class);
        tb.add("border_color",String.class);
        tb.add("fill_pattern",Integer.class);
        tb.add("stroke_linejoin",String.class);
        tb.add("stroke_linecap",String.class);
        tb.add("stroke_dasharray",String.class);
        tb.add("border_linejoin",String.class);
        tb.add("border_dasharray",String.class);
        tb.setName("stackedPoint");
        SimpleFeatureType sfType = tb.buildFeatureType();
        return sfType;
    }

    private static class StackedPoint {
        private Coordinate key;
        private Coordinate centerPt;
        private Coordinate location = null;
        private int count = 0;
        private Set<Coordinate> uniquePts;
        private String name;
        private String attention_text;
        private String geometryType;
        private int stroke_width;
        private String stroke_color;
        private String fill_color;
        private String dot_color;
        private int dot_size;
        private String dot_shape;
        private int border_width;
        private String border_color;
        private int fill_pattern;
        private String stroke_linejoin;
        private String stroke_linecap;
        private String stroke_dasharray;
        private String border_linejoin;
        private String border_dasharray;

        /**
         * Creates a new stacked point grid cell.
         * The center point of the cell is supplied
         * so that it may be used as or influence the
         * location of the final display point
         * 
         * @param key a key for the grid cell (using integer ordinates to avoid precision issues)
         * @param centerPt the center point of the grid cell 
         */
        public StackedPoint(Coordinate key, Coordinate centerPt) {
            this.key = new Coordinate(key);
            this.centerPt = centerPt;
        }

        public Coordinate getKey() {
            return key;
        }

        public Coordinate getLocation() {
            return location;
        }

        public int getCount() {
            return count;
        }

        public int getCountUnique() {
            if (uniquePts == null)
                return 1;
            return uniquePts.size();
        }

        public String getBorder_dasharray() {
            return border_dasharray;
        }

        public void setBorder_dasharray(String border_dasharray) {
            this.border_dasharray = border_dasharray;
        }

        public String getBorder_linejoin() {
            return border_linejoin;
        }

        public void setBorder_linejoin(String border_linejoin) {
            this.border_linejoin = border_linejoin;
        }

        public String getStroke_dasharray() {
            return stroke_dasharray;
        }

        public void setStroke_dasharray(String stroke_dasharray) {
            this.stroke_dasharray = stroke_dasharray;
        }

        public String getStroke_linecap() {
            return stroke_linecap;
        }

        public void setStroke_linecap(String stroke_linecap) {
            this.stroke_linecap = stroke_linecap;
        }

        public String getStroke_linejoin() {
            return stroke_linejoin;
        }

        public void setStroke_linejoin(String stroke_linejoin) {
            this.stroke_linejoin = stroke_linejoin;
        }

        public int getFill_pattern() {
            return fill_pattern;
        }

        public void setFill_pattern(int fill_pattern) {
            this.fill_pattern = fill_pattern;
        }

        public String getBorder_color() {
            return border_color;
        }

        public void setBorder_color(String border_color) {
            this.border_color = border_color;
        }

        public int getBorder_width() {
            return border_width;
        }

        public void setBorder_width(int border_width) {
            this.border_width = border_width;
        }

        public String getDot_shape() {
            return dot_shape;
        }

        public void setDot_shape(String dot_shape) {
            this.dot_shape = dot_shape;
        }

        public int getDot_size() {
            return dot_size;
        }

        public void setDot_size(int dot_size) {
            this.dot_size = dot_size;
        }

        public String getDot_color() {
            return dot_color;
        }

        public void setDot_color(String dot_color) {
            this.dot_color = dot_color;
        }

        public String getFill_color() {
            return fill_color;
        }

        public void setFill_color(String fill_color) {
            this.fill_color = fill_color;
        }

        public String getStroke_color() {
            return stroke_color;
        }

        public void setStroke_color(String stroke_color) {
            this.stroke_color = stroke_color;
        }

        public int getStroke_width() {
            return stroke_width;
        }

        public void setStroke_width(int stroke_width) {
            this.stroke_width = stroke_width;
        }

        public String getGeometryType() {
            return geometryType;
        }

        public void setGeometryType(String geometryType) {
            this.geometryType = geometryType;
        }

        public String getAttention_text() {
            return attention_text;
        }

        public void setAttention_text(String attention_text) {
            this.attention_text = attention_text;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void add(Coordinate pt) {
            count++;
            /**
             * Only create set if this is the second point seen
             * (and assum the first pt is in location)
             */
            if (uniquePts == null) {
                uniquePts = new HashSet<Coordinate>();
            }
            uniquePts.add(pt);

            pickNearestLocation(pt);
            //pickCenterLocation(pt);
        }
        
        /**
         * The original location of the points, in case they are all superimposed (or there is a single
         * point), otherwise null
         * @return
         */
        public Coordinate getOriginalLocation() {
            if(uniquePts != null && uniquePts.size() == 1) {
                return uniquePts.iterator().next();
            } else {
                return null;
            }
        }

        /**
         * Picks the location as the point
         * which is nearest to the center of the cell.
         * In addition, the nearest location is averaged with the cell center.
         * This gives the best chance of avoiding conflicts.
         * 
         * @param pt
         */
        private void pickNearestLocation(Coordinate pt) {
            // strategy - pick most central point
            if (location == null) {
                location = average(centerPt, pt);
                return;
            }
            if (pt.distance(centerPt) < location.distance(centerPt)) {
                location = average(centerPt, pt);
            }
        }
        
        /**
         * Picks the location as the centre point of the cell.
         * This does not give a good visualization - the gridding is very obvious
         * 
         * @param pt
         */
        private void pickCenterLocation(Coordinate pt) {
            // strategy - pick first point
            if (location == null) {
                location = new Coordinate(pt);
                return;
            }
            location = centerPt;
        }

        /**
         * Picks the first location encountered as the cell location.
         * This is sub-optimal, since if the first point is near the cell
         * boundary it is likely to collide with neighbouring points.
         * 
         * @param pt
         */
        private void pickFirstLocation(Coordinate pt) {
            // strategy - pick first point
            if (location == null) {
                location = new Coordinate(pt);
            }
        }

        private static Coordinate average(Coordinate p1, Coordinate p2)
        {
            double x = (p1.x + p2.x) / 2;
            double y = (p1.y + p2.y) / 2;
            return new Coordinate(x, y);
        }
    }
}
