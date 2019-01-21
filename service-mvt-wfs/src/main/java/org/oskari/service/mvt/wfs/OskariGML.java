package org.oskari.service.mvt.wfs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.xml.DOMParser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.util.XmlHelper;
import net.opengis.wfs.FeatureCollectionType;

public class OskariGML {

    /**
     * Remove problematic attribute numberOfFeatures, some servers
     * write "unknown" which causes the parser to fail miserably when it
     * expects a xsd:nonNegativeInteger
     */
    public SimpleFeatureCollection decodeFeatureCollection(InputStream in, String username, String password)
            throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbf = XmlHelper.newDocumentBuilderFactory();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(in);
        Element root = doc.getDocumentElement();
        root.removeAttribute("numberOfFeatures");
        root.removeAttribute("wfs:numberOfFeatures");
        DOMParser parser = new DOMParser(new OskariGMLConfiguration(username, password), doc);
        Object obj = parser.parse();
        return toFeatureCollection(obj);
    }

    /**
     * Convert parse results into a SimpleFeatureCollection.
     *
     * @param obj SimpleFeatureCollection, Collection<?>, SimpleFeature, etc...
     * @return SimpleFeatureCollection of the results
     */
    private SimpleFeatureCollection toFeatureCollection(Object obj) {
        if (obj == null) {
            return null; // not available?
        }
        if (obj instanceof SimpleFeatureCollection) {
            return (SimpleFeatureCollection) obj;
        }
        if (obj instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) obj;
            SimpleFeatureCollection simpleFeatureCollection = simpleFeatureCollection(collection);
            return simpleFeatureCollection;
        }
        if (obj instanceof SimpleFeature) {
            SimpleFeature feature = (SimpleFeature) obj;
            return DataUtilities.collection(feature);
        }
        if (obj instanceof FeatureCollectionType) {
            FeatureCollectionType collectionType = (FeatureCollectionType) obj;
            for (Object entry : collectionType.getFeature()) {
                SimpleFeatureCollection collection = toFeatureCollection(entry);
                if (entry != null) {
                    return collection;
                }
            }
            return null; // nothing found
        } else {
            throw new ClassCastException(
                    obj.getClass()
                    + " produced when FeatureCollection expected"
                    + " check schema use of AbstractFeatureCollection");
        }
    }

    /**
     * Go through collection contents and morph contents into SimpleFeatures as required.
     *
     * @param collection
     * @return SimpleFeatureCollection
     */
    private SimpleFeatureCollection simpleFeatureCollection(Collection<?> collection) {
        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();
        SimpleFeatureType schema = null;
        for (Object obj : collection) {
            if (schema == null) {
                schema = simpleType(obj);
            }
            SimpleFeature feature = simpleFeature(obj, schema);
            featureCollection.add(feature);
        }
        return featureCollection;
    }

    protected SimpleFeatureType simpleType(Object obj) {
        if (obj instanceof SimpleFeature) {
            SimpleFeature feature = (SimpleFeature) obj;
            return feature.getFeatureType();
        }
        if (obj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) obj;
            SimpleFeatureTypeBuilder build = new SimpleFeatureTypeBuilder();
            build.setName("Unknown");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = (String) entry.getKey();
                Object value = entry.getValue();
                Class<?> binding = value == null ? Object.class : value.getClass();
                if (value instanceof Geometry) {
                    Geometry geom = (Geometry) value;
                    Object srs = geom.getUserData();
                    if (srs instanceof CoordinateReferenceSystem) {
                        build.add(key, binding, (CoordinateReferenceSystem) srs);
                    } else if (srs instanceof Integer) {
                        build.add(key, binding, (Integer) srs);
                    } else if (srs instanceof String) {
                        build.add(key, binding, (String) srs);
                    } else {
                        build.add(key, binding);
                    }
                } else {
                    build.add(key, binding);
                }
            }
            SimpleFeatureType schema = build.buildFeatureType();
            return schema;
        }
        if (obj instanceof Geometry) {
            Geometry geom = (Geometry) obj;
            Class<?> binding = geom.getClass();
            Object srs = geom.getUserData();

            SimpleFeatureTypeBuilder build = new SimpleFeatureTypeBuilder();
            build.setName("Unknown");
            if (srs instanceof CoordinateReferenceSystem) {
                build.add("the_geom", binding, (CoordinateReferenceSystem) srs);
            } else if (srs instanceof Integer) {
                build.add("the_geom", binding, (Integer) srs);
            } else if (srs instanceof String) {
                build.add("the_geom", binding, (String) srs);
            } else {
                build.add("the_geom", binding);
            }
            build.setDefaultGeometry("the_geom");
            SimpleFeatureType schema = build.buildFeatureType();
            return schema;
        }
        return null;
    }

    /**
     * Morph provided obj to a SimpleFeature if possible.
     *
     * @param obj
     * @param schema
     * @return SimpleFeature, or null if not possible
     */
    protected SimpleFeature simpleFeature(Object obj, SimpleFeatureType schema) {
        if (schema == null) {
            schema = simpleType(obj);
        }

        if (obj instanceof SimpleFeature) {
            return (SimpleFeature) obj;
        }
        if (obj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) obj;
            Object values[] = new Object[schema.getAttributeCount()];
            for (int i = 0; i < schema.getAttributeCount(); i++) {
                AttributeDescriptor descriptor = schema.getDescriptor(i);
                String key = descriptor.getLocalName();
                Object value = map.get(key);

                values[i] = value;
            }
            SimpleFeature feature = SimpleFeatureBuilder.build(schema, values, null);
            return feature;
        }
        if (obj instanceof Geometry) {
            Geometry geom = (Geometry) obj;
            SimpleFeatureBuilder build = new SimpleFeatureBuilder(schema);
            build.set(schema.getGeometryDescriptor().getName(), geom);

            SimpleFeature feature = build.buildFeature(null);
            return feature;
        }
        return null; // not available as a feature!
    }

}
