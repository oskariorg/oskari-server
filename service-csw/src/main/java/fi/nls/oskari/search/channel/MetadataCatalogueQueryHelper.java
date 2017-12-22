package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.control.metadata.MetadataField;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.search.util.GeonetworkSpatialOperation;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.deegree.datatypes.QualifiedName;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.filterencoding.*;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.WKTAdapter;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.csw.discovery.GetRecords;
import org.deegree.ogcwebservices.csw.discovery.GetRecordsDocument;
import org.deegree.ogcwebservices.csw.discovery.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;

/**
 * Helper class for creating search queries for MetadataCatalogue
 */
public class MetadataCatalogueQueryHelper {

    private static final String GCO_NAMESPACE = "gco";
    private static final String GMD_NAMESPACE = "gmd";
    public final static String TARGET_SRS = "EPSG:4326";
    public final static String SPATIAL_OPERATOR = "INTERSECTS";

    private static final Logger log = LogFactory.getLogger(MetadataCatalogueQueryHelper.class);
    private final static char WILDCARD_CHARACTER = '*';

    private static final Map<String, Integer> opMap = new HashMap<String, Integer>();
    static {
        // only one needed at the moment
        opMap.put("COMP_EQUAL", OperationDefines.PROPERTYISEQUALTO);
    }

    public String getQueryPayload(SearchCriteria searchCriteria) {

        final GetRecords getRecs = getRecordsQuery(searchCriteria);
        if(getRecs == null) {
            // no point in making the query without GetRecords
            return null;
        }
        return getQueryPayload(getRecs);
    }

    public GetRecords getRecordsQuery(SearchCriteria searchCriteria) {

        final List<Operation> operations = getOperations(searchCriteria);
        Operation operation;

        if (operations.isEmpty()) {
            return null;
        } else if (operations.size() == 1) {
            operation = operations.get(0);
        } else {
            operation = new LogicalOperation(OperationDefines.AND, operations);
        }

        final ComplexFilter filter = new ComplexFilter(operation);
        try {
            final Map<String, URI> nsmap = new HashMap<String, URI>();
            nsmap.put(GMD_NAMESPACE, new URI("http://www.isotc211.org/2005/gmd"));
            nsmap.put(GCO_NAMESPACE, new URI("http://www.isotc211.org/2005/gco"));
            nsmap.put("csw", new URI("http://www.opengis.net/cat/csw/2.0.2"));

            final List<QualifiedName> typeNames = new ArrayList<QualifiedName>();
            typeNames.add(new QualifiedName("gmd:MD_Metadata"));

            final List<PropertyPath> elementNamesAsPropertyPaths = new ArrayList<PropertyPath>();

            final SortProperty[] sortProperties = SortProperty.create(null, nsmap);

            // we need "full" query to get locale mapping like #SW -> swe -> sv
            // to optimize we could try to do an "init" query to get mappings and use
            // "summary" query to get the data. Note! Since locale mappings are at "result item" level
            // this might lead to complications. Just using "full" for now, it's more XML to transfer and
            // parse but it's safe.
            final Query query = new Query("full", new ArrayList<QualifiedName>(),
                    new HashMap<String, QualifiedName>(),
                    elementNamesAsPropertyPaths, filter, sortProperties, typeNames,
                    new HashMap<String, QualifiedName>());

            final GetRecords getRecs = new GetRecords("0", "2.0.2", null, nsmap,
                    GetRecords.RESULT_TYPE.RESULTS, "application/xml", "http://www.isotc211.org/2005/gmd", 1,
                    10000, 0, null, query);
            return getRecs;
        } catch (Exception ex) {
            log.error(ex, "Error generating GetRecords document for CSW Query");
        }
        return null;
    }

    public String getQueryPayload(final GetRecords getRecs) {
        if(getRecs == null) {
            return null;
        }
        final StringWriter xml = new StringWriter();
        try {
            final GetRecordsDocument getRecsDoc = org.deegree.ogcwebservices.csw.discovery.XMLFactory.exportWithVersion(getRecs);
            final Properties p = new Properties();
            p.put("indent", "yes");
            // write the post data to postable string
            getRecsDoc.write(xml, p);
            xml.flush();
            return xml.toString();
        } catch (Exception ex) {
            log.error(ex, "Error generating payload for CSW Query");
        }
        finally {
            IOHelper.close(xml);
        }
        return null;
    }

    private Operation getOperationForField(SearchCriteria searchCriteria, MetadataField field) {
        return getOperationForField(searchCriteria, field, false);
    }

    private Operation getOperationForField(SearchCriteria searchCriteria, MetadataField field, boolean recursion) {
        final String[] values = getValuesForField(searchCriteria, field);
        if(values == null || (!recursion && field.getShownIf() != null)) {
            // FIXME: not too proud of the shownIf handling
            // shownIf is meant to link fields for frontend but it also means we need special handling for it in here
            // another field should have this one linked as dependency so we skip the actual field handling by default
            return null;
        }
        final Map<String, String> deps = field.getDependencies();
        log.debug("Field dependencies:", deps);
        final List<Operation> multiOp = new ArrayList<Operation>();
        for(String value: values) {
            Operation op = getOperation(field, value);
            final String dep = deps.get(value);
            if(dep != null) {
                final MetadataField depField = MetadataCatalogueChannelSearchService.getField(dep);
                Operation depOp = getOperationForField(searchCriteria, depField, true);
                if(depOp != null) {
                    List<Operation> combination = new ArrayList<Operation>(2);
                    combination.add(op);
                    combination.add(depOp);
                    op = new LogicalOperation(OperationDefines.AND, combination);
                }
            }
            addOperation(multiOp, op);
        }
        if(multiOp.isEmpty()) {
            return null;
        }

        if(field.isMulti() && multiOp.size() > 1) {
            // combine to one OR-statement if we have a multivalue field with more than one selection
            Operation op = new LogicalOperation(OperationDefines.OR, multiOp);
            return op;
        }
        return multiOp.get(0);
    }

    private String[] getValuesForField(SearchCriteria searchCriteria, MetadataField field) {
        if(searchCriteria == null || field == null) {
            return null;
        }

        Object param = searchCriteria.getParam(field.getProperty());
        if(param == null) {
            param = field.getDefaultValue();
        }
        if(param == null) {
            return null;
        }
        log.debug("Got value for metadata field:", field.getProperty(), "=", param);
        if(field.isMulti()) {
            return (String[]) param;
        }
        else {
            return new String[]{(String) param};
        }
    }

    private List<Operation> getOperations(SearchCriteria searchCriteria) {
        final List<Operation> list = new ArrayList<Operation>();

        // user input
        addOperation(list, getLikeOperation(searchCriteria.getSearchString(), "csw:anyText"));

        final List<Operation> theOrList = new ArrayList<Operation>();
        for(MetadataField field : MetadataCatalogueChannelSearchService.getFields()) {
            final Operation operation = getOperationForField(searchCriteria, field);
            if(operation == null) {
                continue;
            }
            // add must matches to toplevel list
            if(field.isMustMatch()) {
                addOperation(list, operation);
            }
            // others to OR-list
            else {
                addOperation(theOrList, operation);
            }
        }
        if(theOrList.size() == 1) {
            addOperation(list, theOrList.get(0));
        } else if (theOrList.size() > 1) {
            addOperation(list, new LogicalOperation(OperationDefines.OR, theOrList));
        }

        return list;
    }
    private Operation getOperation(MetadataField field, String value) {
        if(field.getFilterOp() == null) {
            return getLikeOperation(value, field.getFilter());
        }
        else if(field.getFilterOp().equals(SPATIAL_OPERATOR)) {
            return getSpatialOperation(value, field.getFilter(), field.getFilterOp());
        }
        else {
            return getCompOperation(value, field.getFilter(), opMap.get(field.getFilterOp()));
        }
    }


    private Operation getLikeOperation(final String searchCriterion,
                                       final String searchElementName) {
        if (searchCriterion == null || searchCriterion.isEmpty()) {
            return null;
        }
        PropertyIsLikeOperation op = new PropertyIsLikeOperation(
                new PropertyName(new QualifiedName(searchElementName)),
                new Literal(searchCriterion), WILDCARD_CHARACTER, '?', '/');
        return op;
    }

    private Operation getCompOperation(final String searchCriterion,
                                       final String searchElementName,
                                       final int operationId) {
        if (searchCriterion == null || searchCriterion.isEmpty()) {
            return null;
        }
        PropertyIsCOMPOperation op = new PropertyIsCOMPOperation(
                operationId,
                new PropertyName(new QualifiedName(searchElementName)),
                new Literal(searchCriterion),
                false);
        return op;
    }

    private Operation getSpatialOperation(final String searchCriterion,
                                          final String searchElementName, final String operation ) {
        if (searchCriterion == null || searchCriterion.isEmpty()) {
            return null;
        }

        final CoordinateSystem crs = CRSFactory.createDummyCRS(TARGET_SRS);
        try {
            String polygon = parseWKTPolygon(searchCriterion);
            Geometry geom = WKTAdapter.wrap(polygon, crs);
            GeonetworkSpatialOperation op = new GeonetworkSpatialOperation(
                    OperationDefines.INTERSECTS,
                    new PropertyName(new QualifiedName(searchElementName)),
                    geom,
                    polygon);
            return op;
        } catch (GeometryException e) {
            log.error(e, "Error creating spatial operation!");
        }
        return null;
    }

    private void addOperation(final List<Operation> list, final Operation op) {
        if(op != null) {
            list.add(op);
        }
    }

    private String getSRS(JSONObject geojson) {
        JSONObject crs = geojson.optJSONObject("crs");
        if(crs != null) {
            // old ol2 impl: { ..., "crs":{"type":"name","properties":{"name":"EPSG:3067"}}}
            return crs.optJSONObject("properties").optString("name");
        }
        // new ol4 drawtools impl: { ..., "crs":"EPSG:3067"}
        String srs = geojson.optString("crs");
        if(srs != null) {
            return srs;
        }
        return "EPSG:4326";
    }

    /* "{"type":"FeatureCollection","features":[{"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[382186.81433571,6677985.8855768],[382186.81433571,6682065.8855768],[391446.81433571,6682065.8855768],[391446.81433571,6677985.8855768],[382186.81433571,6677985.8855768]]]}}],"crs":{"type":"name","properties":{"name":"EPSG:3067"}}}" */
    private String parseWKTPolygon(final String searchCriterion) {
        try {
            JSONObject geojson = JSONHelper.createJSONObject(searchCriterion);
            String sourceSRS = getSRS(geojson);
            geojson.remove("crs");
            FeatureCollection fc = null;
            StringBuilder sb = new StringBuilder("POLYGON((");
            FeatureJSON fjs = new FeatureJSON();
            fc = fjs.readFeatureCollection(new ByteArrayInputStream(
                    searchCriterion.getBytes("utf-8")));
            ReferencedEnvelope env = fc.getBounds();
            //Transform to target crs
            Point minb = ProjectionHelper.transformPoint(env.getMinX(), env.getMinY(), sourceSRS, TARGET_SRS);
            Point maxb = ProjectionHelper.transformPoint(env.getMaxX(), env.getMaxY(), sourceSRS, TARGET_SRS);

            sb.append(minb.getLonToString()+" ");
            sb.append(minb.getLatToString());
            sb.append(",");
            sb.append(minb.getLonToString()+" ");
            sb.append(maxb.getLatToString());
            sb.append(",");
            sb.append(maxb.getLonToString()+" ");
            sb.append(maxb.getLatToString());
            sb.append(",");
            sb.append(maxb.getLonToString()+" ");
            sb.append(minb.getLatToString());
            sb.append(",");
            sb.append(minb.getLonToString()+" ");
            sb.append(minb.getLatToString());
            sb.append("))");
            return sb.toString();

        }
        catch (Exception e){
            log.error(e, "Error parsing coverage geometry");
        }

       return null;
    }
}