package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.control.metadata.MetadataField;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.util.GeonetworkSpatialOperation;
import fi.nls.oskari.util.IOHelper;
import org.deegree.datatypes.QualifiedName;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.filterencoding.*;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.WKTAdapter;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.csw.discovery.*;

import java.io.StringWriter;
import java.net.URI;
import java.util.*;

/**
 * Helper class for creating search queries for MetadataCatalogue
 */
public class MetadataCatalogueQueryHelper {

    private static final String GCO_NAMESPACE = "gco";
    private static final String GMD_NAMESPACE = "gmd";

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

            final Query query = new Query("summary", new ArrayList<QualifiedName>(),
                    new HashMap<String, QualifiedName>(),
                    elementNamesAsPropertyPaths, filter, sortProperties, typeNames,
                    new HashMap<String, QualifiedName>());

            final GetRecords getRecs = new GetRecords("0", "2.0.2", null, nsmap,
                    GetRecords.RESULT_TYPE.RESULTS, "application/xml", "csw:IsoRecord", 1,
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

        final Object param = searchCriteria.getParam(field.getProperty());
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
                                          final String searchElementName) {
        if (searchCriterion == null || searchCriterion.isEmpty()) {
            return null;
        }
        // FIXME: really create for each call?
        final CoordinateSystem crs = CRSFactory.createDummyCRS("EPSG:4326");
        try {
            Geometry geom = WKTAdapter.wrap(searchCriterion, crs);
            GeonetworkSpatialOperation op = new GeonetworkSpatialOperation(
                    OperationDefines.INTERSECTS,
                    new PropertyName(new QualifiedName(searchElementName)),
                    geom,
                    searchCriterion);
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
}