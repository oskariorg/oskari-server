package org.oskari.map.userlayer.input;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.kml.v22.KML;
import org.geotools.kml.v22.KMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.xml.PullParser;
import org.geotools.geometry.jts.JTS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;

/**
 * Parse Google KML
 */
public class KMLParser implements FeatureCollectionParser {

    private static final Logger LOG = LogFactory.getLogger(KMLParser.class);

    public static final String SUFFIX = "KML";
    public static final String KML_NAME = "kml_name"; // GML also has name and kml_name is localized to user
    public static final String KML_DESC = "kml_desc"; // GML also has description and kml_desc is localized to user
    public static final String KML_GEOM = "the_geom";

    @Override
    @SuppressWarnings("unchecked")
    public SimpleFeatureCollection parse(File file, CoordinateReferenceSystem sourceCRS,
            CoordinateReferenceSystem targetCRS) throws ServiceException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            /*--- Geotools Parser example ---
            Parser p = new Parser(new KMLConfiguration());
            SimpleFeature doc = (SimpleFeature) p.parse(in);
            List <SimpleFeature> folders = (List<SimpleFeature>) doc.getAttribute("Feature");
            if ("folder".equals(folders.get(0).getType().getTypeName())) {
                List <SimpleFeature> features = (List<SimpleFeature>) folders.get(0).getAttribute("Feature");
                if ("placemark".equals(features.get(0).getType().getTypeName())){
                    // this is placemark
                }
            }
            ---*/
            // Note that this is the happy case where file structure is
            // <Document><Folder><PlaceMark>...</PlaceMark></Folder></Document>
            // Feature can be Document, Folder, PlaceMark, NetworkLink, gx:Tour, PhotoOverlay, ScreenOverlay, GroundOverlay
            // Containers (Document, Folder) can contain Features
            // --> have to loop Features and check the type and if Feature is container then loop it's features...

            // Using PullParser to parse Placemarks only
            PullParser parser = new PullParser(new KMLConfiguration(), in, KML.Placemark);
            SimpleFeature f;
            Set<String> extendedData = new HashSet<String>();
            Map<String, Object> untypedData;

            DefaultFeatureCollection fc = new DefaultFeatureCollection();
            while ((f = (SimpleFeature) parser.parse()) != null) {
                if (f.getUserData().get("UntypedExtendedData") != null) {
                    untypedData = (Map<String, Object>) f.getUserData().get("UntypedExtendedData");
                    untypedData.keySet().forEach((k) -> extendedData.add(k));
                }
                fc.add(f);
            }
            return processFeatures(targetCRS, fc, extendedData);
        } catch (XMLStreamException e) {
            throw new ServiceException("XMLStreamException occured", e);
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        } catch (SAXException e) {
            throw new ServiceException("Invalid KML file: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private DefaultFeatureCollection processFeatures (CoordinateReferenceSystem targetCRS, DefaultFeatureCollection fc_kml, Set<String> extendedData) throws ServiceException {
        try {
            DefaultFeatureCollection fc = new DefaultFeatureCollection();
            SimpleFeatureBuilder builder;
            SimpleFeature f;
            // KML always lon,lat 4326
            CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326", true);
            MathTransform transform = FeatureCollectionParsers.getTransform(sourceCRS, targetCRS);

            if (extendedData.isEmpty()){
               builder = getBasicBuilder(targetCRS);
            } else {
               builder = getExtendedBuilder(targetCRS, extendedData);
            }
            SimpleFeatureIterator iter = fc_kml.features();
            while (iter.hasNext()){
                f = iter.next();
                builder.set(KML_NAME, f.getAttribute("name"));
                // Basic builder
                if (extendedData.isEmpty()){
                    builder.set(KML_DESC, f.getAttribute("description"));
                // Extended builder
                } else {
                    Map<String, Object> untypedData = (Map<String, Object>) f.getUserData().get("UntypedExtendedData");
                    if (untypedData != null){
                        extendedData.forEach((k) -> {
                            if (k.equals("description")){
                                builder.set(KML_DESC, untypedData.get(k));
                                return;
                            }
                            builder.set(k, untypedData.get(k));
                        });
                    } else if (f.getAttribute("description") != null) {
                        builder.set(KML_DESC, f.getAttribute("description"));
                    }
                }
                Geometry geom = (Geometry) f.getDefaultGeometry();
                if (geom != null) {
                    builder.set(KML_GEOM, JTS.transform(geom, transform));
                }
                fc.add(builder.buildFeature(f.getID()));
            }
            return fc;
        } catch (Exception e) {
            throw new ServiceException("Failed to parse KML", e);
        }
    }
    private SimpleFeatureBuilder getBasicBuilder (CoordinateReferenceSystem targetCRS) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName( "BasciKMLBuilder" );
        builder.setNamespaceURI( "http://www.oskari.org" );
        builder.setCRS(targetCRS);
        builder.add(KML_NAME, String.class );
        builder.add(KML_DESC, String.class );
        builder.add(KML_GEOM, Geometry.class );
        return new SimpleFeatureBuilder(builder.buildFeatureType());
    }
    private SimpleFeatureBuilder getExtendedBuilder (CoordinateReferenceSystem targetCRS, Set<String> extendedData) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName( "ExtendedKMLBuilder" );
        builder.setNamespaceURI( "http://www.oskari.org" );
        builder.setCRS(targetCRS);
        builder.add(KML_NAME, String.class );
        builder.add(KML_DESC, String.class );
        extendedData.forEach((k) -> {
            if (k.equals("description")) return;
            builder.add(k, String.class);
        });
        builder.add(KML_GEOM, Geometry.class );
        return new SimpleFeatureBuilder(builder.buildFeatureType());
    }
}
