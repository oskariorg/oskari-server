package fi.nls.oskari.util;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.gml2.GMLConstants;

public class GML3Writer {

    public static void writeGeometry(XMLStreamWriter xsw, Geometry geometry)
            throws XMLStreamException {
        writeGeometry(xsw, geometry, true);
    }

    public static void writeGeometry(XMLStreamWriter xsw, Geometry geometry, boolean xyOrder)
                throws XMLStreamException {
        if (geometry instanceof Point) {
            writePoint(xsw, (Point) geometry, xyOrder);
        } else if (geometry instanceof LineString) {
            writeLineString(xsw, (LineString) geometry, xyOrder);
        } else if (geometry instanceof Polygon) {
            writePolygon(xsw, (Polygon) geometry, xyOrder);
        } else if (geometry instanceof MultiPoint) {
            writeMultiPoint(xsw, (MultiPoint) geometry, xyOrder);
        } else if (geometry instanceof MultiLineString) {
            writeMultiLineString(xsw, (MultiLineString) geometry, xyOrder);
        } else if (geometry instanceof MultiPolygon) {
            writeMultiPolygon(xsw, (MultiPolygon) geometry, xyOrder);
        } else if (geometry instanceof GeometryCollection) {
            writeMultiGeometry(xsw, (GeometryCollection) geometry, xyOrder);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static void writePoint(XMLStreamWriter xsw, Point geometry, boolean xyOrder)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_POINT);
        writeSRID(xsw, geometry.getSRID());
        writePos(xsw, geometry.getCoordinate(), xyOrder);
        xsw.writeEndElement();
    }

    private static void writeLineString(XMLStreamWriter xsw, LineString geometry, boolean xyOrder)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_LINESTRING);
        writeSRID(xsw, geometry.getSRID());
        writePosList(xsw, geometry.getCoordinates(), xyOrder);
        xsw.writeEndElement();
    }

    private static void writePolygon(XMLStreamWriter xsw, Polygon geometry, boolean xyOrder)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_POLYGON);
        writeSRID(xsw, geometry.getSRID());
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, "exterior");
        writeLinearRing(xsw, (LinearRing) geometry.getExteriorRing(), xyOrder);
        xsw.writeEndElement();
        for (int i = 0; i < geometry.getNumInteriorRing(); i++) {
            xsw.writeStartElement(GMLConstants.GML_NAMESPACE, "interior");
            writeLinearRing(xsw, (LinearRing) geometry.getInteriorRingN(i), xyOrder);
            xsw.writeEndElement();
        }
        xsw.writeEndElement();
    }

    private static void writeMultiPoint(XMLStreamWriter xsw, MultiPoint geometry, boolean xyOrder)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_MULTI_POINT);
        writeSRID(xsw, geometry.getSRID());
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_POINT_MEMBER);
            writePoint(xsw, (Point) geometry.getGeometryN(i), xyOrder);
            xsw.writeEndElement();
        }
        xsw.writeEndElement();
    }

    private static void writeMultiLineString(XMLStreamWriter xsw, MultiLineString geometry, boolean xyOrder)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_MULTI_LINESTRING);
        writeSRID(xsw, geometry.getSRID());
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_LINESTRING_MEMBER);
            writeLineString(xsw, (LineString) geometry.getGeometryN(i), xyOrder);
            xsw.writeEndElement();
        }
        xsw.writeEndElement();
    }

    private static void writeMultiPolygon(XMLStreamWriter xsw, MultiPolygon geometry, boolean xyOrder)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_MULTI_POLYGON);
        writeSRID(xsw, geometry.getSRID());
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_POLYGON_MEMBER);
            writePolygon(xsw, (Polygon) geometry.getGeometryN(i), xyOrder);
            xsw.writeEndElement();
        }
        xsw.writeEndElement();
    }

    private static void writeMultiGeometry(XMLStreamWriter xsw, GeometryCollection geometry, boolean xyOrder)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_MULTI_GEOMETRY);
        writeSRID(xsw, geometry.getSRID());
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_GEOMETRY_MEMBER);
            writeGeometry(xsw, geometry.getGeometryN(i), xyOrder);
            xsw.writeEndElement();
        }
        xsw.writeEndElement();
    }

    private static void writePos(XMLStreamWriter xsw, Coordinate coordinate, boolean xyOrder)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, "pos");
        xsw.writeAttribute("srsDimension", "2");
        String pos = xyOrder ? coordinate.x + " " + coordinate.y : coordinate.y + " " + coordinate.x;
        xsw.writeCharacters(pos);
        xsw.writeEndElement();
    }

    private static void writePosList(XMLStreamWriter xsw, Coordinate[] coordinates, boolean xyOrder)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, "posList");
        xsw.writeAttribute("srsDimension", "2");
        StringBuilder sb = new StringBuilder();
        for (Coordinate coordinate : coordinates) {
            if (xyOrder) {
                sb.append(' ').append(coordinate.x).append(' ').append(coordinate.y);
            } else {
                sb.append(' ').append(coordinate.y).append(' ').append(coordinate.x);
            }
        }
        xsw.writeCharacters(sb.substring(1));
        xsw.writeEndElement();
    }

    private static void writeLinearRing(XMLStreamWriter xsw, LinearRing linearRing, boolean xyOrder)
            throws XMLStreamException {
        xsw.writeStartElement(GMLConstants.GML_NAMESPACE, GMLConstants.GML_LINEARRING);
        writePosList(xsw, linearRing.getCoordinates(), xyOrder);
        xsw.writeEndElement();
    }

    private static void writeSRID(XMLStreamWriter xsw, int srid)
            throws XMLStreamException {
        if (srid != 0) {
            xsw.writeAttribute("srsName", getSrsName(srid));
        }
    }

    public static String getSrsName(int srid) {
        return "http://www.opengis.net/def/crs/EPSG/0/" + srid;
    }

}
