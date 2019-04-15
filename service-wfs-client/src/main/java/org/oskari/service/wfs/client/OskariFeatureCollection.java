package org.oskari.service.wfs.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class OskariFeatureCollection implements SimpleFeatureCollection {

    private static final Logger LOG = LogFactory.getLogger(OskariFeatureCollection.class);

    private final String id;
    private final List<SimpleFeature> features;
    private final SimpleFeatureType schema;

    public OskariFeatureCollection(List<SimpleFeature> features) {
        this(UUID.randomUUID().toString(), features);
    }

    private OskariFeatureCollection(String id, List<SimpleFeature> features) {
        this.id = id;
        this.features = features;
        this.schema = determineSchema(features);
    }

    private SimpleFeatureType determineSchema(List<SimpleFeature> features) {
        Map<String, Integer> propertyNameToIndex = new HashMap<>();
        Map<Integer, String> indexToPropertyName = new HashMap<>();

        String typeName = "oskari";
        String namespaceURI = "http://oskari.org";
        
        String defaultGeometryName = null;
        CoordinateReferenceSystem crs = null; 
        
        List<AttributeType> attributeTypes = new ArrayList<>();
        List<Object> exampleValues = new ArrayList<>();
        
        for (SimpleFeature f : features) {
            SimpleFeatureType ft = f.getFeatureType();

            GeometryDescriptor geom = ft.getGeometryDescriptor();
            if (defaultGeometryName == null) {
                defaultGeometryName = geom.getLocalName();
                crs = ft.getGeometryDescriptor().getCoordinateReferenceSystem();
            } else {
                if (defaultGeometryName.equals(geom.getLocalName())) {
                    throw new IllegalArgumentException(
                            "Can not determine schema, two different default geometries");
                }
                if (!CRS.equalsIgnoreMetadata(crs, geom.getCoordinateReferenceSystem())) {
                    throw new IllegalArgumentException(
                            "Can not determine schema, two different coordinate reference systems");
                }
            }

            final int n = ft.getAttributeCount();
            for (int i = 0; i < n; i++) {
                AttributeDescriptor ad = ft.getDescriptor(i);
                String name = ad.getLocalName();
                AttributeType type = ad.getType();
                Object value = f.getAttribute(i);

                Integer idx = propertyNameToIndex.get(name);
                if (idx == null) {
                    // We don't have property with this name yet, add it
                    int index = attributeTypes.size();
                    attributeTypes.add(type);
                    exampleValues.add(value);
                    propertyNameToIndex.put(name, index);
                    indexToPropertyName.put(index, name);
                    continue;
                }

                AttributeType existingType = attributeTypes.get(idx);
                if (existingType.equals(type)) {
                    // No issue here!
                    continue;
                }

                if (value == null) {
                    // The value was null, maybe the type was difficult to
                    // interpret because the value was null.
                    // This can happen for example when reading JSON and the
                    // value is null - can't say much about the actual type
                    // Let's use the type we already have - it's probably better
                    continue;
                }
                
                // Okay, perhaps the previous time we encountered this property the value was null
                Object exampleValue = exampleValues.get(idx);
                if (exampleValue == null) {
                    // Great! Let's use this type and value instead
                    attributeTypes.set(idx, type);
                    exampleValues.set(idx, value);
                }
                
                // Don't know what to do here... flip for it?
                // Let's do nothing - first come first served!
                // But atleast log something about it.
                LOG.warn("Can't determine which type to use for property: {}, options: {}/{}, using the former",
                        name, existingType.getBinding().getSimpleName(), type.getBinding().getSimpleName());
            }
        }
        
        SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
        sftb.setName(typeName);
        sftb.setNamespaceURI(namespaceURI);
        sftb.setCRS(crs);
        
        for (int i = 0; i < attributeTypes.size(); i++) {
            AttributeType type = attributeTypes.get(i);
            String localName = indexToPropertyName.get(i);
            Name name = new NameImpl(localName);
            AttributeDescriptor ad = new AttributeDescriptorImpl(type, name, 0, 0, true, null);
            sftb.add(ad);
        }
        sftb.setDefaultGeometry(defaultGeometryName);
        
        return sftb.buildFeatureType();
    }
    
    @Override
    public SimpleFeatureType getSchema() {
        return schema;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public void accepts(FeatureVisitor visitor, ProgressListener progress) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReferencedEnvelope getBounds() {
        throw new UnsupportedOperationException();
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
        return new OskariFeatureCollection(features.stream()
                .filter(f -> filter.evaluate(f))
                .collect(Collectors.toList()));
    }

    @Override
    public SimpleFeatureCollection sort(SortBy order) {
        throw new UnsupportedOperationException();
    }

}
