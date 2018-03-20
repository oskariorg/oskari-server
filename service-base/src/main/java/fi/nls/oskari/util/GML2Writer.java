package fi.nls.oskari.util;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.gml2.GMLConstants;

public class GML2Writer {

    /**
     * Requires that gml namespace is already bound!
     */
    public static void writeGeometry(XMLStreamWriter xsw, Geometry geometry)
            throws XMLStreamException {
        if (geometry instanceof Point) {
            writePoint(xsw, (Point) geometry);
        } else if (geometry instanceof LineString) {
            writeLineString(xsw, (LineString) geometry);
        } else if (geometry instanceof Polygon) {
            writePolygon(xsw, (Polygon) geometry);
        } else if (geometry instanceof MultiPoint) {
            writeMultiPoint(xsw, (MultiPoint) geometry);
        } else if (geometry instanceof MultiLineString) {
            writeMultiLineString(xsw, (MultiLineString) geometry);
        } else if (geometry instanceof MultiPolygon) {
            writeMultiPolygon(xsw, (MultiPolygon) geometry);
        } else if (geometry instanceof GeometryCollection) {
            writeMultiGeometry(xsw, (GeometryCollection) geometry);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static void writePoint(XMLStreamWriter xsw, Point geometry)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_POINT);
        writeSRID(xsw, geometry.getSRID());
        writeCoordinates(xsw, geometry.getCoordinates());
        xsw.writeEndElement();
    }

    private static void writeLineString(XMLStreamWriter xsw, LineString geometry)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_LINESTRING);
        writeSRID(xsw, geometry.getSRID());
        writeCoordinates(xsw, geometry.getCoordinates());
        xsw.writeEndElement();
    }

    private static void writePolygon(XMLStreamWriter xsw, Polygon geometry)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_POLYGON);
        writeSRID(xsw, geometry.getSRID());
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_OUTER_BOUNDARY_IS);
        writeLinearRing(xsw, (LinearRing) geometry.getExteriorRing());
        xsw.writeEndElement();
        for (int i = 0; i < geometry.getNumInteriorRing(); i++) {
            xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_INNER_BOUNDARY_IS);
            writeLinearRing(xsw, (LinearRing) geometry.getInteriorRingN(i));
            xsw.writeEndElement();
        }
        xsw.writeEndElement();
    }

    private static void writeMultiPoint(XMLStreamWriter xsw, MultiPoint geometry)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_MULTI_POINT);
        writeSRID(xsw, geometry.getSRID());
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_POINT_MEMBER);
            writePoint(xsw, (Point) geometry.getGeometryN(i));
            xsw.writeEndElement();
        }
        xsw.writeEndElement();
    }

    private static void writeMultiLineString(XMLStreamWriter xsw, MultiLineString geometry)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_MULTI_LINESTRING);
        writeSRID(xsw, geometry.getSRID());
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_LINESTRING_MEMBER);
            writeLineString(xsw, (LineString) geometry.getGeometryN(i));
            xsw.writeEndElement();
        }
        xsw.writeEndElement();
    }

    private static void writeMultiPolygon(XMLStreamWriter xsw, MultiPolygon geometry)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_MULTI_POLYGON);
        writeSRID(xsw, geometry.getSRID());
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_POLYGON_MEMBER);
            writePolygon(xsw, (Polygon) geometry.getGeometryN(i));
            xsw.writeEndElement();
        }
        xsw.writeEndElement();
    }

    private static void writeMultiGeometry(XMLStreamWriter xsw, GeometryCollection geometry)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_MULTI_GEOMETRY);
        writeSRID(xsw, geometry.getSRID());
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_GEOMETRY_MEMBER);
            writeGeometry(xsw, geometry.getGeometryN(i));
            xsw.writeEndElement();
        }
        xsw.writeEndElement();
    }

    private static void writeCoordinates(XMLStreamWriter xsw, Coordinate[] coordinates)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_COORDINATES);
        StringBuilder sb = new StringBuilder();
        for (Coordinate coordinate : coordinates) {
            sb.append(' ').append(coordinate.x).append(',').append(coordinate.y);
        }
        xsw.writeCharacters(sb.substring(1));
        xsw.writeEndElement();
    }

    private static void writeLinearRing(XMLStreamWriter xsw, LinearRing linearRing)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_LINEARRING);
        writeCoordinates(xsw, linearRing.getCoordinates());
        xsw.writeEndElement();
    }

    private static void writeSRID(XMLStreamWriter xsw, int srid)
            throws XMLStreamException {
        if (srid != 0) {
            xsw.writeAttribute("srsName", "http://www.opengis.net/def/crs/EPSG/0/" + srid);
        }
    }

}
