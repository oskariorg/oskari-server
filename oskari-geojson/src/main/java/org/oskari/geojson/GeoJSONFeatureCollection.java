package org.oskari.geojson;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.geometry.BoundingBox;
import org.opengis.util.ProgressListener;

public class GeoJSONFeatureCollection implements SimpleFeatureCollection {

    private final List<SimpleFeature> features;
    private final SimpleFeatureType schema;
    private final String collectionId;
    private ReferencedEnvelope bounds;

    public GeoJSONFeatureCollection(List<SimpleFeature> features, SimpleFeatureType schema) {
        this(features, schema, null);
    }

    private GeoJSONFeatureCollection(List<SimpleFeature> features, SimpleFeatureType schema, String collectionId) {
        this.features = features;
        this.schema = schema;
        this.collectionId = collectionId != null ? collectionId : "featureCollection";
    }

    @Override
    public SimpleFeatureType getSchema() {
        return schema;
    }

    @Override
    public String getID() {
        return collectionId;
    }

    @Override
    public void accepts(FeatureVisitor visitor, ProgressListener progress) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReferencedEnvelope getBounds() {
        if (bounds == null) {
            bounds = new ReferencedEnvelope();
            features.stream()
                    .map(f -> f.getBounds())
                    .filter(BoundingBox::isEmpty)
                    .forEach(bbox -> bounds.include(bbox));
        }
        return bounds;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof SimpleFeature)) {
            return false;
        }
        String id = ((SimpleFeature) o).getID();
        if (id == null) {
            return false;
        }
        for (SimpleFeature f : features) {
            if (id.equals(f.getID())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> o) {
        for (Object a : o) {
            if (!contains(a)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return features.isEmpty();
    }

    @Override
    public int size() {
        return features.size();
    }

    @Override
    public Object[] toArray() {
        return features.toArray();
    }

    @Override
    public <O> O[] toArray(O[] a) {
        return features.toArray(a);
    }

    @Override
    public SimpleFeatureIterator features() {
        final Iterator<SimpleFeature> it = features.iterator();
        return new SimpleFeatureIterator() {

            @Override
            public SimpleFeature next() throws NoSuchElementException {
                return it.next();
            }

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public void close() {
                // NOP
            }
        };
    }

    @Override
    public SimpleFeatureCollection subCollection(Filter filter) {
        List<SimpleFeature> filtered =  features.stream()
                .filter(f -> filter.evaluate(f))
                .collect(Collectors.toList());
        return new GeoJSONFeatureCollection(filtered, schema);
    }

    @Override
    public SimpleFeatureCollection sort(SortBy order) {
        throw new UnsupportedOperationException();
    }

}
