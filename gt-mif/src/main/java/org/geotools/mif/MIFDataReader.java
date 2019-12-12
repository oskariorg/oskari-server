package org.geotools.mif;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.mif.util.QueueBufferedReader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Read MapInfo MIF Data section (graphical objects)
 */
public class MIFDataReader implements Iterator<Geometry>, AutoCloseable {

    private static final String[] OPTIONAL_PLINE = { "PEN", "SMOOTH" };
    private static final String[] OPTIONAL_REGION = { "PEN", "BRUSH", "CENTER" };
    private static final String[] OPTIONAL_RECT = { "PEN", "BRUSH" };

    private final QueueBufferedReader mif;
    private final GeometryFactory gf;

    private Geometry next;
    private boolean end;

    public MIFDataReader(File mif, MIFHeader header) throws IOException {
        BufferedReader r = Files.newBufferedReader(mif.toPath(), StandardCharsets.US_ASCII);
        try {
            while (!"DATA".equals(r.readLine())); // SKIP
        } catch (IOException e) {
            if (r != null) {
                r.close();
            }
            throw e;
        }
        this.mif = new QueueBufferedReader(r);
        this.gf = JTSFactoryFinder.getGeometryFactory();
        this.end = false;
    }

    @Override
    public boolean hasNext() {
        if (next == null) {
            next = readGeometry();
        }
        return !end;
    }

    @Override
    public Geometry next() {
        Geometry geometry;
        if (next != null) {
            geometry = next;
            next = null;
        } else {
            geometry = readGeometry();
        }
        return geometry;
    }

    @Override
    public void close() {
        end = true;
        try {
            mif.close();
        } catch (IOException ignore) {
            // Do nothing
        }
    }

    private Geometry readGeometry() {
        try {
            String line = mif.poll();
            if (line == null) {
                end = true;
                return null;
            }
            line = line.trim();
            int i = line.indexOf(' ');
            String type = line.substring(0, i).toUpperCase();
            Geometry geom = null;
            switch (type) {
            case "NONE":
                return null;
            case "POINT":
                geom = parsePoint(line);
                skipOptional("SYMBOL");
                return geom;
            case "LINE":
                geom = parseLine(line);
                skipOptional("PEN");
                return geom;
            case "RECT":
                geom = parseRect(line);
                skipOptional(OPTIONAL_RECT);
                return geom;
            }
            int num = Integer.parseInt(type.substring(i + 1));
            // TODO: Support MultiLineStrings / MultiPolygons (e.g. PLINE MULTIPLE 2)
            switch (type) {
            case "PLINE":
                geom = parsePLine(num);
                skipOptional(OPTIONAL_PLINE);
                break;
            case "REGION":
                geom = parseRegion(num);
                skipOptional(OPTIONAL_REGION);
                break;
            case "MULTIPOINT":
                geom = parseMultiPoint(num);
                skipOptional("SYMBOL");
                break;
            case "COLLECTION":
                geom = parseGeometryCollection(num);
                break;
            default:
                throw new IllegalArgumentException("Invalid Geometry Tag");
            }
            return geom;
        } catch (IOException e) {
            close();
            throw new RuntimeException(e);
        }
    }

    private void skipOptional(String s) throws IOException {
        String line = mif.peek();
        if (!line.startsWith(s)) {
            mif.poll();
        }
    }

    private void skipOptional(String[] a) throws IOException {
        String line = mif.peek();
        String trimmed = line.trim();
        for (String s : a) {
            if (trimmed.startsWith(s)) {
                mif.poll();
                line = mif.peek();
                trimmed = line.trim();
            }
        }
    }

    private Point parsePoint(String line) {
        String[] a = line.split(" ");
        double x1 = Double.parseDouble(a[1]);
        double y1 = Double.parseDouble(a[2]);
        return gf.createPoint(new Coordinate(x1, y1));
    }

    private LineString parseLine(String line) {
        String[] a = line.split(" ");
        double x1 = Double.parseDouble(a[1]);
        double y1 = Double.parseDouble(a[2]);
        double x2 = Double.parseDouble(a[3]);
        double y2 = Double.parseDouble(a[4]);
        return gf.createLineString(new Coordinate[] {
                new Coordinate(x1, y1), new Coordinate(x2, y2)
        });
    }

    private LineString parsePLine(int n)
            throws IOException {
        int numPts = Integer.parseInt(mif.poll().trim());
        return gf.createLineString(readCoordinatesN(numPts));
    }

    private Polygon parseRegion(int n)
            throws NumberFormatException, IOException {
        int numPts = Integer.parseInt(mif.poll().trim());
        Coordinate[] shell = readCoordinatesN(numPts);
        if (n == 1) {
            return gf.createPolygon(shell);
        }
        LinearRing exterior = gf.createLinearRing(shell);
        LinearRing[] interiors = new LinearRing[n - 1];
        for (int i = 0; i < n - 1; i++) {
            numPts = Integer.parseInt(mif.poll().trim());
            Coordinate[] ring = readCoordinatesN(numPts);
            interiors[i] = gf.createLinearRing(ring);
        }
        return gf.createPolygon(exterior, interiors);
    }

    private Polygon parseRect(String line) {
        String[] a = line.split(" ");
        double x1 = Double.parseDouble(a[1]);
        double y1 = Double.parseDouble(a[2]);
        double x2 = Double.parseDouble(a[3]);
        double y2 = Double.parseDouble(a[4]);
        return gf.createPolygon(new Coordinate[] {
                new Coordinate(x1, y1),
                new Coordinate(x2, y1),
                new Coordinate(x2, y2),
                new Coordinate(x1, y2),
                new Coordinate(x1, y1)
        });
    }

    private MultiPoint parseMultiPoint(int n) throws IOException {
        return gf.createMultiPoint(readCoordinatesN(n));
    }

    private Coordinate[] readCoordinatesN(int n) throws IOException {
        Coordinate[] ca = new Coordinate[n];
        for (int i = 0; i < n; i++) {
            String line = mif.poll().trim();
            int j = line.indexOf(' ');
            String x = line.substring(0, j);
            String y = line.substring(j + 1);
            Coordinate c = new Coordinate(Double.parseDouble(x), Double.parseDouble(y));
            ca[i] = c;
        }
        return ca;
    }

    private GeometryCollection parseGeometryCollection(int n) throws IOException {
        Geometry[] geometries = new Geometry[n];
        for (int i = 0; i < n; i++) {
            geometries[i] = readGeometry();
        }
        return gf.createGeometryCollection(geometries);
    }

}
