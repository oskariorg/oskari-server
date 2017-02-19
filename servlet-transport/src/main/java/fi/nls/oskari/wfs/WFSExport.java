package fi.nls.oskari.wfs;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;

import fi.nls.oskari.service.ServiceRuntimeException;
import org.geotools.data.*;
import org.geotools.data.ogr.OGRDataStore;
import org.geotools.data.ogr.OGRDataStoreFactory;
import org.geotools.data.ogr.bridj.BridjOGRDataStoreFactory;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.*;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;

import org.geotools.referencing.CRS;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Feature export for WFS layer in the current location
 */
public class WFSExport {
    private static final String KEY = "WFSExport_";
    private static final String EXPORT_PATH = "export";
    private static final String GDAL_CODE_ESRI_SHAPE = "ESRI SHAPEFILE";
    private static final String GDAL_CODE_MAPINFO = "MAPINFO FILE";
    private static final String GDAL_CODE_GPX = "GPX";
    private static final String GDAL_CODE_KML = "KML";
    private static final Logger log = LogFactory.getLogger(WFSExport.class);
    private String format = null;
    private String crsName = null;

    private FeatureCollection<SimpleFeatureType, SimpleFeature> features;

    protected WFSExport() {

    }

    /**
     * @param format export file format
     */
    public WFSExport(
            String format, String crsName) {
        this.format = format;
        this.crsName = crsName;

    }


    /**
     * exports features to an export file
     *
     * @param {featureCollection} features
     * @return fileid
     */
    public String export(
            FeatureCollection<SimpleFeatureType, SimpleFeature> features) {
        String fileName = null;

        try {
            OGRDataStoreFactory factory = new BridjOGRDataStoreFactory();
            if (!factory.isAvailable()) {
                log.info("GDAL library is not found for data export -> install gdal -- http://www.gdal.org/");
                return fileName;
            }
            // OGR export doesn't work for features, which have object type properties -> trick:simplify to string type
            CoordinateReferenceSystem crs = CRS.decode(this.crsName, true);
            SimpleFeatureType schema = simplifyAttributes(features, crs, this.format);
            // Add CRS for geometry descriptor
            schema = DataUtilities.createSubType(schema, null, crs);
            // Modify features according to new schema
            FeatureCollection<SimpleFeatureType, SimpleFeature> sfeatures = modifiedFeatureCollection(schema, features);
            // Get target format -> some formats (e.g. GPX, KML) support only EPSG:4326 CRS
            // and transform in that case
            String targetCrs = getTargetCrsName(this.format, this.crsName);
            FeatureCollection<SimpleFeatureType, SimpleFeature> fc = ProjectionHelper.transformFeatureCollection(sfeatures, this.crsName, targetCrs);
            // export file
            File expFile = getExportFile(this.format, schema.getTypeName(), EXPORT_PATH);
            Map<String, String> connectionParams = new HashMap<String, String>();
            connectionParams.put("DriverName", this.format);
            connectionParams.put("DatasourceName", expFile.getAbsolutePath());

            fileName = expFile.getName();
            log.debug("WFS features export - file name: ", expFile.getAbsolutePath());

            OGRDataStore dataStore = (OGRDataStore) factory.createNewDataStore(connectionParams);

            // Get GDAL extra options
            String[] extra = getGdalOptions(this.format);

            //write export file(s)
            dataStore.createSchema((SimpleFeatureCollection) fc, true, extra);

            dataStore.dispose();

        } catch (Exception e) {
            log.error("Feature export failed: ", e.getMessage());
            throw new ServiceRuntimeException("WFS feature export failed for format "+this.format+ " - "+e.getMessage(), e.getCause());
        }

        return fileName;
    }

    /**
     * Workarounds for OGR/gdal output process, because of the limits in the output formats
     *
     * @param fc     geotools featureCollection <-- wfs GetFeature response
     * @param crs    source CRS
     * @param format output format code (http://www.gdal.org/ogr_formats.html)
     * @return
     * @throws Exception
     */
    private static SimpleFeatureType simplifyAttributes(FeatureCollection<SimpleFeatureType, SimpleFeature> fc, CoordinateReferenceSystem crs, String format) throws Exception {

        SimpleFeatureType schema = fc.getSchema();
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName(schema.getTypeName());
        tb.setNamespaceURI(schema.getName().getNamespaceURI());
        tb.setCRS(crs);

        for (int i = 0; i < schema.getAttributeCount(); i++) {
            AttributeDescriptor ad = schema.getDescriptor(i);
            ad = truncateFieldName(ad, format);
            ad = truncateObjectProperty(ad, schema);
            // mixed geometries in the source layer or unsupported geom types ? --> GPX supports only LINE, MULTILINE and POINT
            if (ad == schema.getGeometryDescriptor() && format.toUpperCase().equals("GPX")) {
                SimpleFeature first = DataUtilities.first(fc);
                if (first.getDefaultGeometry() instanceof MultiLineString) {
                    tb.add(ad.getLocalName(), MultiLineString.class, crs);
                } else if (first.getDefaultGeometry() instanceof LineString) {
                    tb.add(ad.getLocalName(), LineString.class, crs);
                } else if (first.getDefaultGeometry() instanceof Polygon || first.getDefaultGeometry() instanceof MultiPolygon) {
                    tb.add(ad.getLocalName(), LineString.class, crs);
                } else {
                    tb.add(ad.getLocalName(), Point.class, crs);
                }
            } else {
                tb.add(ad);
            }
        }

        tb.setDefaultGeometry(schema.getGeometryDescriptor().getLocalName());
        return tb.buildFeatureType();


    }

    /**
     * Creates a new featurecollection
     *
     * @param modSchema new schema
     * @param fc        source featureC>ollection
     * @return
     * @throws Exception
     */
    private static FeatureCollection modifiedFeatureCollection(SimpleFeatureType modSchema, FeatureCollection<SimpleFeatureType, SimpleFeature> fc) throws Exception {

        FeatureCollection<SimpleFeatureType, SimpleFeature> fcnew = new DefaultFeatureCollection(fc.getID(), modSchema);

        FeatureIterator<SimpleFeature> iterator = fc.features();
        try {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                // simplify attributes
                feature = simplifyFeatureAttributes(feature);
                SimpleFeature changed = DataUtilities.reType(modSchema, feature);
                if (fcnew instanceof Collection) {
                    Collection<SimpleFeature> collection = (Collection) fcnew;
                    collection.add(changed);
                }
            }
        } finally {
            iterator.close();
        }

        return fcnew;

    }

    /**
     * Get export file name and path
     *
     * @param format
     * @param typename
     * @param path
     * @return
     * @throws java.io.IOException
     */
    private static File getExportFile(String format, String typename, String path) throws IOException {

        String suffix = "." + format.toLowerCase();
        // Is the result a fileset e.g. Mapinfo, Shape
        if (format.toUpperCase().equals(GDAL_CODE_ESRI_SHAPE) || format.toUpperCase().equals(GDAL_CODE_MAPINFO)) {
            suffix = "";
        }
        File tmpFile = File.createTempFile(typename, suffix, new File("./" + path));
        tmpFile.delete();

        return tmpFile;
    }

    /**
     * Get target CRS name for the export data
     *
     * @param format  gdal format code
     * @param crsName source CRS name
     * @return
     */
    private static String getTargetCrsName(String format, String crsName) {

        if (format.toUpperCase().equals(GDAL_CODE_GPX) || format.toUpperCase().equals(GDAL_CODE_KML)) {
            return "EPSG:4326";
        }
        return crsName;
    }

    /**
     * Set extra options for OGR/gdal export process (http://www.gdal.org/ogr_formats.html
     *
     * @param format
     * @return
     */
    private static String[] getGdalOptions(String format) {

        String[] extra = null;
        if (format.equals(GDAL_CODE_GPX)) {
            extra = new String[]{"GPX_USE_EXTENSIONS=YES"};
        }
        return extra;
    }

    /**
     * Stringify complex attribute object values, which are not supported in Ogr/gdal
     *
     * @param fea source feature
     * @return
     */
    private static SimpleFeature simplifyFeatureAttributes(SimpleFeature fea) {

        List<Object> orgAttributes = fea.getAttributes();
        List<Object> newAttributes = new ArrayList<Object>();

        for (Object attr : orgAttributes) {
            if (attr instanceof ReferencedEnvelope) {
                newAttributes.add(((ReferencedEnvelope) attr).toString());
            } else {
                newAttributes.add(attr);
            }
        }
        fea.setAttributes(newAttributes);

        return fea;
    }

    /**
     * Truncate property name for shape file - OGR library can't do it
     * Maximum field name length in SHP is 10 ch ?
     *
     * @param ad
     * @param format
     * @return
     */
    private static AttributeDescriptor truncateFieldName(AttributeDescriptor ad, String format) {

        if (format.toUpperCase().equals(GDAL_CODE_ESRI_SHAPE)) {
            // field name truncate doesn't work in ogr -> do yourself
            if (ad.getLocalName().length() > 10) {
                //create the builder
                AttributeTypeBuilder builder = new AttributeTypeBuilder();
                builder.init(ad.getType());
                //set truncated name for Shapefile
                builder.setName(ad.getLocalName().substring(0, 9));
                //build the descriptor
                ad = builder.buildDescriptor(ad.getLocalName().substring(0, 9));
            }
        }
        return ad;
    }

    /**
     * Trick  object type property class to String class in the schema for ORG process
     * - skip  geometry descriptor property
     *
     * @param ad
     * @param schema
     * @return
     */
    private static AttributeDescriptor truncateObjectProperty(AttributeDescriptor ad, SimpleFeatureType schema) {

        if (Object.class.equals(ad.getType().getBinding()) ||
                Envelope.class.equals(ad.getType().getBinding()) ||
                ReferencedEnvelope.class.equals(ad.getType().getBinding()) ||
                HashMap.class.equals(ad.getType().getBinding()) ||
                        (Geometry.class.equals(ad.getType().getBinding()) && ad != schema.getGeometryDescriptor())) {
            AttributeType at = new AttributeTypeImpl(new NameImpl("String"), String.class, false,
                    false, Collections.EMPTY_LIST, null, null);
            AttributeDescriptorImpl newDescriptor = new AttributeDescriptorImpl(
                    at, new NameImpl(ad.getLocalName()), ad.getMinOccurs(),
                    ad.getMaxOccurs(), ad.isNillable(),
                    ad.getDefaultValue());
            return newDescriptor;
        }
        return ad;
    }
}
