package org.oskari.csw.request;

import fi.nls.oskari.service.ServiceRuntimeException;
import org.geotools.filter.v1_0.OGCConfiguration;
import org.geotools.xml.Encoder;
import org.opengis.filter.Filter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class GetRecords {

    private static final XMLOutputFactory xof = XMLOutputFactory.newFactory();
    private static final String CSW_URI = "http://www.opengis.net/cat/csw/2.0.2";
    private static final String GMD_URI = "http://www.isotc211.org/2005/gmd";

    private static final String CSW_VERSION = "2.0.2";
    private static final String CONSTRAINT_VERSION = "1.1.0";

    private GetRecords() {}

    /**
     * Builds a GetRecords request payload for CSW-service with the given filters.
     * @param filter required
     * @return
     */
    public static String createRequest(Filter filter) {
        if (filter == null) {
            throw new ServiceRuntimeException("Filter is required");
        }
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        XMLStreamWriter xsw = getWriter(stream);
        startDocument(xsw);
        writeRawXMLUnsafe(xsw, stream, getFilterAsString(filter));
        endDocument(xsw);
        return getAsString(stream);
    }

    /**
     * Nothing fancy. Just wrapping possible exception to a runtime exception.
     * @param stream
     * @return
     */
    private static XMLStreamWriter getWriter(OutputStream stream) {
        try {
            return xof.createXMLStreamWriter(stream);
        } catch (XMLStreamException e) {
            throw new ServiceRuntimeException("Couldn't create stream writer", e);
        }
    }

    /**
     *
     <csw:GetRecords xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" service="CSW" version="2.0.2">
     <csw:Query typeNames="csw:Record">
     <ElementSetName typeNames="csw:Record">full</ElementSetName>
     <csw:Constraint version="1.1.0">
     ...
     */
    private static void startDocument(XMLStreamWriter xsw) {
        try {
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
    }

    /**
     * Returns the generated XML.
                ...includes filters... and end document
             </csw:Constraint>
         </csw:Query>
     </csw:GetRecords>
     * @return
     */
    private static void endDocument(XMLStreamWriter xsw) {
        try {
            xsw.writeEndElement(); // Constraint
            xsw.writeEndElement(); // Query
            xsw.writeEndElement(); // GetRecords
            xsw.writeEndDocument();
            xsw.close();
        } catch (XMLStreamException e) {
            throw new ServiceRuntimeException("Couldn't create GetRecords request", e);
        }
    }

    /**
     * Serialize the filter to get a String that can be injected into the stream.
     * @param filter generated with Geotools
     * @return String that can be injected to XML document
     */
    private static String getFilterAsString(Filter filter) {
        if (filter == null) {
            throw new ServiceRuntimeException("Tried to serialize null-filter");
        }
        try {
            Encoder encoder = new Encoder(new OGCConfiguration());
            encoder.setOmitXMLDeclaration(true);
            return encoder.encodeAsString(filter, org.geotools.filter.v1_0.OGC.Filter);
        } catch (IOException e) {
            throw new ServiceRuntimeException("Couldn't serialize filter to string", e);
        }
    }

    /**
     * Injects any string in the middle of the XML document.
     *
     * Unsafe because injecting invalid XML-fragment as string makes the whole document invalid
     * and you can do it with this method.
     * @param xsw
     * @param stream
     * @param injectUnsafe XML fragment to inject in the middle of the stream.
     */
    private static void writeRawXMLUnsafe(XMLStreamWriter xsw, OutputStream stream, String injectUnsafe) {
        try {
            // Following line is very important!!
            // without it unescaped data will appear inside the previously opened tag.
            xsw.writeCharacters("");
            xsw.flush();

            OutputStreamWriter osw = new OutputStreamWriter(stream);
            osw.write(injectUnsafe);
            osw.flush();
        } catch (XMLStreamException | IOException e) {
            throw new ServiceRuntimeException("Couldn't write raw XML to GetRecords request", e);
        }
    }

    /**
     * Serialize stream to get a String
     * @param stream
     * @return
     */
    private static String getAsString(ByteArrayOutputStream stream) {
        return new String (stream.toByteArray(), Charset.forName("UTF-8"));
    }
}
