/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2008-2011 TOPP - www.openplans.org.
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
 */
package org.geoserver.wps.oskari.oskari;

import com.vividsolutions.jts.geom.*;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.process.gs.GSProcess;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.operation.MathTransform;

import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.NoSuchElementException;

/**
 * ZoneSectors a feature collection using a certain distance
 *
 * @author Gianni Barrotta - Sinergis
 * @author Andrea Di Nora - Sinergis
 * @author Pietro Arena - Sinergis
 * @author Andrea Aime - GeoSolutions
 *
 * @source $URL$
 */
@DescribeProcess(title = "zonesector", description = "Creates zones and sectors each feature in a collection by a fixed distance and zone count. Works in pure cartesian mode.")
public class ZoneSectorFeatureCollection implements GSProcess {
    @DescribeResult(description = "The zonesectored feature collection")
    public SimpleFeatureCollection execute(
            @DescribeParameter(name = "feature collection", description = "Feature collection") SimpleFeatureCollection features,
            @DescribeParameter(name = "width of the zonesector", description = "The width of the zonesector") Double distance,
            @DescribeParameter(name = "sector count", description = "The count of sectors, default=0, max=12") int sector_count,
            @DescribeParameter(name = "zone count", description = "The count of zones,default=5") int zone_count) {

        if (distance == null) {
            throw new IllegalArgumentException("ZoneSector distance was not specified");
        }

        if(zone_count < 1 ) zone_count = 5;
        if(sector_count > 12 ) sector_count = 12;

        Double min_distance = 0.0;
        Double max_distance = 0.0;


        ListFeatureCollection result = null;

        for (int i = 0; i < zone_count; i++) {
            max_distance = max_distance + distance;
            for (int k = 0; k < sector_count; k++) {
                SimpleFeatureCollection zone = new ZoneSectoredFeatureCollection(features, zone_count, k, sector_count, min_distance, max_distance);
                if (result == null) result = new ListFeatureCollection(zone.getSchema());
                this.appendFeatureCollection(result, zone);
            }
            min_distance = min_distance + distance;
        }

        return (SimpleFeatureCollection) result;

    }

    /**
     * Wrapper that will trigger the zonesector computation as features are requested
     */
    static class ZoneSectoredFeatureCollection extends SimpleProcessingCollection {

        Double min_distance;
        Double max_distance;

        int zone_count;
        int sector_count;
        int sector;
        String attribute;

        SimpleFeatureCollection delegate;


        public ZoneSectoredFeatureCollection(SimpleFeatureCollection delegate, int zone_count, int sector, int sector_count,
                                             Double min_distance,  Double max_distance) {
            this.min_distance = min_distance;
            this.max_distance = max_distance;
            this.zone_count = zone_count;
            this.sector_count = sector_count;
            this.sector = sector;
            this.delegate = delegate;
            this.attribute = null;



        }

        @Override
        public SimpleFeatureIterator features() {
            return new ZoneSectoredFeatureIterator(delegate, this.zone_count, this.sector, this.sector_count, this.min_distance, this.max_distance, getSchema());
        }

        @Override
        public ReferencedEnvelope getBounds() {
            if(attribute == null) {
                // in this case we just have to expand the original collection bounds
                ReferencedEnvelope re = delegate.getBounds();
                re.expandBy(max_distance);
                return re;
            } else {
                // unlucky case, we need to actually compute by hand...
                return getFeatureBounds();
            }
        }

        @Override
        protected SimpleFeatureType buildTargetFeatureType() {
            // create schema
            SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
            for (AttributeDescriptor descriptor : delegate.getSchema().getAttributeDescriptors()) {
                if (!(descriptor.getType() instanceof GeometryTypeImpl)
                        || (!delegate.getSchema().getGeometryDescriptor().equals(descriptor))) {
                   // tb.add(descriptor);  Add only sector id attribute
                } else {
                    AttributeTypeBuilder builder = new AttributeTypeBuilder();
                    builder.setBinding(MultiPolygon.class);
                    AttributeDescriptor attributeDescriptor = builder.buildDescriptor(descriptor
                            .getLocalName(), builder.buildType());
                    tb.add(attributeDescriptor);
                    if(tb.getDefaultGeometry() == null) {
                        tb.setDefaultGeometry(descriptor.getLocalName());
                    }
                    builder.setBinding(String.class);
                    AttributeDescriptor attribute2 = builder.buildDescriptor(
                            "sector_id", builder.buildType());
                    tb.add(attribute2);

                }
            }
            AttributeTypeBuilder builder = new AttributeTypeBuilder();

            tb.setDescription(delegate.getSchema().getDescription());
            tb.setCRS(delegate.getSchema().getCoordinateReferenceSystem());
            tb.setName(delegate.getSchema().getName());
            return tb.buildFeatureType();
        }

        @Override
        public int size() {
            return delegate.size();
        }

    }

    /**
     * ZoneSectors each feature as we scroll over the collection
     */
    static class ZoneSectoredFeatureIterator implements SimpleFeatureIterator {
        SimpleFeatureIterator delegate;

        SimpleFeatureCollection collection;

        Double min_distance;
        Double max_distance;


        int zone_count;
        int sector_count;
        int sector;

        String attribute;

        int count;

        SimpleFeatureBuilder fb;

        SimpleFeature next;

        public ZoneSectoredFeatureIterator(SimpleFeatureCollection delegate, int zone_count, int sector, int sector_count,
                                           Double min_distance, Double max_distance, SimpleFeatureType schema) {
            this.delegate = delegate.features();
            this.min_distance = min_distance;
            this.max_distance = max_distance;
            this.collection = delegate;
            this.zone_count = zone_count;
            this.sector_count = sector_count;
            this.sector = sector;
            this.attribute = null;
            fb = new SimpleFeatureBuilder(schema);

        }

        public void close() {
            delegate.close();
        }

        public boolean hasNext() {
            while (next == null && delegate.hasNext()) {
                SimpleFeature f = delegate.next();
                for (Object value : f.getAttributes()) {
                    if (value instanceof Geometry) {
                        Double fDistance1 = min_distance;
                        Double fDistance2 = max_distance;
                        //Create sector
                        double sectorside = ((Geometry) value).getLength() + (2.0d*fDistance2);

                        Geometry gsector = getSectorGeometry(((Geometry) ((Geometry) value).getCentroid()), sectorside, sector, sector_count);

                        if(fDistance1 != null &&  fDistance2 != null && fDistance1 == 0.0) {
                            // Create buffer
                            value = ((Geometry) value).buffer(fDistance2);
                            if(gsector != null) value = ((Geometry) value).intersection(gsector);
                        }
                        else if(fDistance1 != null &&  fDistance2 != null && fDistance1 != 0.0) {
                            // Create zone
                            Geometry minbuffer = ((Geometry) value).buffer(fDistance1);
                            Geometry maxbuffer = ((Geometry) value).buffer(fDistance2);
                            value =  maxbuffer.difference(minbuffer);
                            if(gsector != null) value = ((Geometry) value).intersection(gsector);
                        }
                        fb.add(value);  // Geometry
                    }

                }
                //Format distance
                double dkm = max_distance/1000.0d;
                String skm = new DecimalFormat("#0.0").format(dkm);
                String sunit = "km";
                if(dkm < 1.0d) {
                    sunit = "m";
                    skm = new DecimalFormat("#0").format(max_distance);
                }

                String sector_id=Integer.toString(sector+1)+"_"+skm+sunit;
                fb.add(sector_id);   // Sector id
                next = fb.buildFeature("zones." + count);
                count++;
                fb.reset();
            }
            return next != null;
        }

        public SimpleFeature next() throws NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException("hasNext() returned false!");
            }
            SimpleFeature result = next;
            next = null;
            return result;
        }

        /**
         *  Computes one sector based on sector number and total number of sectors (2-12)
         *  - use buffer quadrantSegments for getting sector points
         *  - buffer 1st point is in the east - we want from the north
         * @param gfeature    {Geometry}  feature for the sector center point
         * @param distance    {double}  buffer/sector distance
         * @param sector      {int} current sector # (1st index is 0)
         * @param sector_count {int} total number of sectors
         * @return {Geometry}  sector geometry
         */
        private Geometry getSectorGeometry(Geometry gfeature, double distance, int sector, int sector_count){
            try {
                GeometryFactory geometryFactory = new GeometryFactory();
                if (sector_count < 2) return null;
                int startInd = 4 * sector;
                int endInd = 4 * (sector+1);
                int midleInd = (startInd + endInd) / 2 ;
                Geometry sectorbuf = (gfeature).buffer(distance, sector_count);
                Coordinate center = sectorbuf.getCentroid().getCoordinate();
                // Rotate geometry - we want sectors clockwise from north
                AffineTransform affineTransform =
                        AffineTransform.getRotateInstance(Math.toRadians(90.0d), center.x,
                                center.y);
                MathTransform mathTransform = new AffineTransform2D(affineTransform);

                sectorbuf = JTS.transform(sectorbuf, mathTransform);
                Coordinate[] coords = sectorbuf.getCoordinates();
                Coordinate[] gsector = new Coordinate[5];
                gsector[0] = center;
                gsector[1] = coords[startInd];
                gsector[2] = coords[midleInd];
                gsector[3] = coords[endInd];
                gsector[4] = center;
                LinearRing ring = geometryFactory.createLinearRing(gsector);

                Polygon polygon = geometryFactory.createPolygon(ring, null);
                return (Geometry) polygon;
            }
            catch (Exception e)
            {
                System.out.println("Sector computation failed" + e);
                return null;
            }
        }


    }
    private void appendFeatureCollection(ListFeatureCollection result, SimpleFeatureCollection zone)
    {
        SimpleFeatureIterator iterator=zone.features();
        try {
            while( iterator.hasNext()  ){
                SimpleFeature feature = iterator.next();
                result.add(feature);
            }
        }
        finally {
            iterator.close();
        }
    }

}
