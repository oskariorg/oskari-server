package org.geotools.mif;

import java.io.IOException;

import org.geotools.api.data.FeatureReader;
import org.geotools.api.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.BasicFeatureTypes;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.mif.column.MIDColumn;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;

import org.locationtech.jts.geom.Geometry;

public class MIFFeatureSource extends ContentFeatureSource {

    public MIFFeatureSource(ContentEntry entry, Query query) {
        super(entry, query);
    }

    @Override
    public MIFDataStore getDataStore() {
        return (MIFDataStore) super.getDataStore();
    }

    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
        return new MIFFeatureReader(getState(), query);
    }

    @Override
    protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
        return null; // feature by feature scan required to establish bounds
    }

    @Override
    protected int getCountInternal(Query query) throws IOException {
        return -1; // feature by feature scan required to count records
    }

    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        MIFHeader header = getDataStore().readHeader();

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(entry.getName());
        builder.setDefaultGeometry(BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME);
        builder.setCRS(header.getCoordSys());
        builder.add(BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME, Geometry.class);

        for (MIDColumn column : header.getColumns()) {
            builder.add(column.getName(), column.getAttributeClass());
        }

        return builder.buildFeatureType();
    }

}
