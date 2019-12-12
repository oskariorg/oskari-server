package org.geotools.mif;

import java.io.IOException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class MIFFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

    public static final String GEOM_COLUMN_NAME = "_geom";

    private ContentState state;
    private MIFHeader header;
    private MIFDataReader mif;
    private MIDReader mid;
    private SimpleFeatureBuilder builder;
    private SimpleFeature next;

    public MIFFeatureReader(ContentState contentState, Query query) throws IOException {
        this.state = contentState;
        MIFDataStore ds = (MIFDataStore) contentState.getEntry().getDataStore();
        this.header = ds.readHeader();
        this.mif = ds.readData(header);
        this.mid = ds.readMID(header);
        this.builder = new SimpleFeatureBuilder(getFeatureType());
    }

    @Override
    public SimpleFeatureType getFeatureType() {
        return state.getFeatureType();
    }

    @Override
    public boolean hasNext() throws IOException {
        if (next == null) {
            next = readFeature();
        }
        return next != null;
    }

    @Override
    public SimpleFeature next() throws IOException {
        SimpleFeature feature;
        if (next != null) {
            feature = next;
            next = null;
        } else {
            feature = readFeature();
        }
        return feature;
    }

    private SimpleFeature readFeature() throws IOException {
        if (mif == null) {
            throw new IOException("FeatureReader is closed; no additional features can be read");
        }
        if (!mif.hasNext()) {
            return null;
        }
        builder.reset();
        builder.set(GEOM_COLUMN_NAME, mif.next());
        mid.next(builder);
        return builder.buildFeature(null);
    }


    @Override
    public void close() throws IOException {
        if (mif != null) {
            mif.close();
            mif = null;
        }
        if (mid != null) {
            mid.close();
            mid = null;
        }
    }

}
