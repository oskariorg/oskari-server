package org.geotools.mif;

import java.io.IOException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.type.BasicFeatureTypes;
import org.geotools.mif.util.NOPFilter;
import org.geotools.mif.util.TransformFilter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;

public class MIFFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

    private ContentState state;
    private MIFDataReader mif;
    private MIDReader mid;
    private CoordinateSequenceFilter transform;
    private SimpleFeatureBuilder builder;
    private SimpleFeature next;
    private int row;

    public MIFFeatureReader(ContentState contentState, Query query) throws IOException {
        this.state = contentState;
        MIFDataStore ds = (MIFDataStore) contentState.getEntry().getDataStore();
        MIFHeader header = ds.readHeader();
        this.transform = getTransform(header);
        this.mif = ds.openData();
        this.mid = ds.openMID(header);
        this.builder = new SimpleFeatureBuilder(getFeatureType());
        this.row = 0;
    }

    private CoordinateSequenceFilter getTransform(MIFHeader header) {
        double[] a = header.getTransform();
        double sx = a[0];
        double sy = a[1];
        double tx = a[2];
        double ty = a[3];
        if (sx == 1 && sy == 1 && tx == 0 && ty == 0) {
            return new NOPFilter();
        }
        return new TransformFilter(sx, sy, tx, ty);
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
            close();
            return null;
        }

        Geometry geom = mif.next();
        geom.apply(transform);

        builder.reset();
        builder.set(BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME, geom);
        mid.next(builder);

        return buildFeature();
    }

    protected SimpleFeature buildFeature() {
        row += 1;
        return builder.buildFeature(state.getEntry().getTypeName() + "." + row);
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
        builder = null;
        next = null;
    }

}
