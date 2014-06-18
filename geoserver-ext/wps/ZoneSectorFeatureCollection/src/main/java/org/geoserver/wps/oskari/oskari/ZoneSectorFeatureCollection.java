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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.process.gs.GSProcess;
import org.geotools.process.gs.WrappingIterator;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.collection.DecoratingSimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.Converters;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

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
            @DescribeParameter(name = "zone count", description = "The count of zones,default=5") int zone_count) {

        if (distance == null) {
            throw new IllegalArgumentException("ZoneSector distance was not specified");
        } 

         if(zone_count < 1 ) zone_count = 5;
         Double min_distance = 0.0;
         Double max_distance = 0.0;
        
         ListFeatureCollection result = null;

         for (int i = 0; i < zone_count; i++) {
             max_distance= max_distance + distance;
            SimpleFeatureCollection zone = new ZoneSectoredFeatureCollection(features, zone_count, min_distance, max_distance);
            if(result == null) result = new ListFeatureCollection(zone.getSchema());
            this.appendFeatureCollection(result, zone);
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
        String attribute;
        
        SimpleFeatureCollection delegate;
       

        public ZoneSectoredFeatureCollection(SimpleFeatureCollection delegate, int zone_count,
                Double min_distance,  Double max_distance) {
            this.min_distance = min_distance;
            this.max_distance = max_distance;
            this.zone_count = zone_count;
            this.delegate = delegate;
            this.attribute = null;
           

            
        }

        @Override
        public SimpleFeatureIterator features() {
            return new ZoneSectoredFeatureIterator(delegate, this.zone_count, this.min_distance, this.max_distance, getSchema());
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
                    tb.add(descriptor);
                } else {
                    AttributeTypeBuilder builder = new AttributeTypeBuilder();
                    builder.setBinding(MultiPolygon.class);
                    AttributeDescriptor attributeDescriptor = builder.buildDescriptor(descriptor
                            .getLocalName(), builder.buildType());
                    tb.add(attributeDescriptor);
                    if(tb.getDefaultGeometry() == null) {
                        tb.setDefaultGeometry(descriptor.getLocalName());
                    }
                }
            }
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

        String attribute;

        int count;

        SimpleFeatureBuilder fb;

        SimpleFeature next;

        public ZoneSectoredFeatureIterator(SimpleFeatureCollection delegate, int zone_count,
                Double min_distance, Double max_distance, SimpleFeatureType schema) {
            this.delegate = delegate.features();
            this.min_distance = min_distance;
            this.max_distance = max_distance;
            this.collection = delegate;
            this.zone_count = zone_count;
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
                       
                        if(fDistance1 != null &&  fDistance2 != null && fDistance1 == 0.0) {
                            // Create buffer
                            value = ((Geometry) value).buffer(fDistance2);
                        } 
                        else if(fDistance1 != null &&  fDistance2 != null && fDistance1 != 0.0) {
                            // Create zone
                            Geometry minbuffer = ((Geometry) value).buffer(fDistance1);
                            Geometry maxbuffer = ((Geometry) value).buffer(fDistance2);
                            value = (Geometry) maxbuffer.difference(minbuffer);
                        }
                    }
                    fb.add(value);
                }
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
