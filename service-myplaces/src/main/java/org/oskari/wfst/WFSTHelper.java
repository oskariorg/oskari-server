package org.oskari.wfst;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class WFSTHelper {

    protected static final XMLOutputFactory XOF = XMLOutputFactory.newInstance();

    protected static final String WFS = "http://www.opengis.net/wfs";
    protected static final String OGC = "http://www.opengis.net/ogc";
    protected static final String OSKARI = "http://www.oskari.org";
    protected static final String PREFIX_OSKARI = "feature";

    protected static void writeStartTransaction(XMLStreamWriter xsw)
            throws XMLStreamException {
        xsw.writeStartDocument();
        xsw.writeStartElement("wfs", "Transaction", WFS);
        xsw.writeNamespace("wfs", WFS);
        xsw.writeNamespace("ogc", OGC);
        xsw.writeNamespace("gml", "http://www.opengis.net/gml");
        xsw.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        xsw.writeAttribute("service", "WFS");
        xsw.writeAttribute("version", "1.1.0");
    }

    protected static void writeProperty(XMLStreamWriter xsw, String name, long v)
            throws XMLStreamException {
        writeProperty(xsw, name, Long.toString(v));
    }

    protected static void writeProperty(XMLStreamWriter xsw, String name, String value)
            throws XMLStreamException {
        xsw.writeStartElement(WFS, "Property");
        writeTextElement(xsw, WFS, "Name", name);
        writeTextElement(xsw, WFS, "Value", value);
        xsw.writeEndElement();
    }

    protected static void writeTextElement(XMLStreamWriter xsw, String ns, String name, int v)
            throws XMLStreamException {
        writeTextElement(xsw, ns, name, Integer.toString(v));
    }

    protected static void writeTextElement(XMLStreamWriter xsw, String ns, String name, long v)
            throws XMLStreamException {
        writeTextElement(xsw, ns, name, Long.toString(v));
    }

    protected static void writeTextElement(XMLStreamWriter xsw, String ns, String name, String text)
            throws XMLStreamException {
        xsw.writeStartElement(ns, name);
        if (text != null && !text.isEmpty()) {
            xsw.writeCharacters(text);
        }
        xsw.writeEndElement();
    }

    protected static void writeFeatureIdFilter(XMLStreamWriter xsw, String fid)
            throws XMLStreamException {
        xsw.writeStartElement(OGC, "Filter");
        xsw.writeStartElement(OGC, "FeatureId");
        xsw.writeAttribute("fid", fid);
        xsw.writeEndElement();
        xsw.writeEndElement();
    }

}
