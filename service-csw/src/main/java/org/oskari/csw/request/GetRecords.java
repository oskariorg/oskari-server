package org.oskari.csw.request;

import fi.nls.oskari.service.ServiceRuntimeException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.v1_1.OGCConfiguration;
import org.geotools.xml.Encoder;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class GetRecords {

    private FilterFactory filterFactory;
    private XMLStreamWriter xsw;
    private List<Filter> filters = new ArrayList<>();

    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();
    private final XMLOutputFactory xof = XMLOutputFactory.newFactory();
    private static final String CSW_URI = "http://www.opengis.net/cat/csw/2.0.2";
    private static final String GMD_URI = "http://www.isotc211.org/2005/gmd";

    private static final String CSW_VERSION = "2.0.2";
    private static final String CONSTRAINT_VERSION = "1.1.0";

    /**
     *
     <csw:GetRecords xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" service="CSW" version="2.0.2">
         <csw:Query typeNames="csw:Record">
            <ElementSetName typeNames="csw:Record">full</ElementSetName>
            <csw:Constraint version="1.1.0">
                ...
     */
    public GetRecords() {
        try {
            xsw = xof.createXMLStreamWriter(stream);
            xsw.writeStartDocument();
            xsw.writeStartElement("csw", "GetRecords", CSW_URI);
            xsw.writeNamespace("csw", CSW_URI);
            xsw.writeNamespace("gmd", GMD_URI);
            xsw.writeNamespace("gml", "http://www.opengis.net/gml");
            xsw.writeNamespace("ows", "http://www.opengis.net/ows");
            xsw.writeNamespace("ogc", "http://www.opengis.net/ogc");
            xsw.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            xsw.writeAttribute("service", "CSW");
            xsw.writeAttribute("version", CSW_VERSION);

            xsw.writeAttribute("maxRecords", "10000");
            xsw.writeAttribute("startPosition", "1");

            xsw.writeAttribute("resultType", "results"); // or "validate" or "hits"
            xsw.writeAttribute("outputFormat", "application/xml");
            xsw.writeAttribute("outputSchema", "http://www.isotc211.org/2005/gmd");

            xsw.writeStartElement(CSW_URI, "Query");
            //xsw.writeAttribute("typeNames", "csw:Record");
            xsw.writeAttribute("typeNames", "gmd:MD_Metadata");

            // we need "full" query to get locale mapping like #SW -> swe -> sv
            // to optimize we could try to do an "init" query to get mappings and use
            // "summary" query to get the data. Note! Since locale mappings are at "result item" level
            // this might lead to complications. Just using "full" for now, it's more XML to transfer and
            // parse but it's safe.
            xsw.writeStartElement(CSW_URI, "ElementSetName");
            xsw.writeCharacters("full");
            xsw.writeEndElement(); // ElementSetName


            xsw.writeStartElement(CSW_URI, "Constraint");
            xsw.writeAttribute("version", CONSTRAINT_VERSION);

        } catch (XMLStreamException e) {
            throw new ServiceRuntimeException("Couldn't create GetRecords request", e);
        }
        filterFactory = CommonFactoryFinder.getFilterFactory();
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }
    public void addEqualFilter(String name, String value) {
        Expression _property = filterFactory.property(name);
        filters.add(filterFactory.equals(_property, filterFactory.literal(value)));
    }

    /**
     * Returns the generated XML.
                ...includes filters... and end document
             </csw:Constraint>
         </csw:Query>
     </csw:GetRecords>
     * @return
     */
    public String getXML() {
        Filter filter;
        if (filters.isEmpty()) {
            throw new ServiceRuntimeException("Can't create GetRecords request without filters");
        } else if (filters.size() == 1) {
            filter = filters.get(0);
        } else {
            filter = filterFactory.and(filters);
        }

        try {
            writeRawXML(getFilterAsString(filter));
            xsw.writeEndElement(); // Constraint
            xsw.writeEndElement(); // Query
            xsw.writeEndElement(); // GetRecords
            xsw.writeEndDocument();
            xsw.close();
            return new String (stream.toByteArray(), Charset.forName("UTF-8"));
        } catch (XMLStreamException e) {
            throw new ServiceRuntimeException("Couldn't create GetRecords request", e);
        }
    }

    private static String getFilterAsString(Filter filter) {
        if (filter == null) {
            return null;
        }
        try {
            Encoder encoder = new Encoder(new OGCConfiguration());
            encoder.setOmitXMLDeclaration(true);
            return encoder.encodeAsString(filter, org.geotools.filter.v1_0.OGC.Filter);
        } catch (IOException e) {
            throw new ServiceRuntimeException("Couldn't create GetRecords request", e);
        }
    }

    private void writeRawXML(String injectUnsafe) {
        try {
            // Following line is very important!!
            // without it unescaped data will appear inside the previously opened tag.
            xsw.writeCharacters("");
            xsw.flush();

            OutputStreamWriter osw = new OutputStreamWriter(stream);
            osw.write(injectUnsafe);
            osw.flush();
        } catch (XMLStreamException | IOException e) {
            throw new ServiceRuntimeException("Couldn't create GetRecords request", e);
        }
    }

}
