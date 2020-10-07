package org.geotools.mif;

import static org.geotools.mif.util.MIFUtil.startsWithIgnoreCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.mif.util.QueueBufferedReader;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;

/**
 * Read MapInfo MIF Data section (graphical objects)
 */
public class MIFDataReader implements Iterator<Geometry>, AutoCloseable {

    private static final String[] OPTIONAL_PLINE = { "PEN", "SMOOTH" };
    private static final String[] OPTIONAL_REGION = { "PEN", "BRUSH", "CENTER" };
    private static final String[] OPTIONAL_RECT = { "PEN", "BRUSH" };

    private QueueBufferedReader mif;
    private GeometryFactory gf;

    private Geometry next;
    private boolean end;

    public MIFDataReader(File mif) throws IOException {
        BufferedReader r = Files.newBufferedReader(mif.toPath(), StandardCharsets.US_ASCII);
        skipToDataSection(r);
        this.mif = new QueueBufferedReader(r);
        this.gf = JTSFactoryFinder.getGeometryFactory();
        this.end = false;
    }

    private void skipToDataSection(BufferedReader r) throws IOException {
        try {
            String line;
            do {
                line = r.readLine();
                if (line == null) {
                    throw new IOException("Could not find DATA section");
                }
            } while (!startsWithIgnoreCase(line, "DATA"));
        } catch (IOException e) {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException e1) {
                    // Ignore the close exception throw the original exception
                }
            }
            throw e;
        }
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
        mif = null;
        gf = null;
        next = null;
    }

    private Geometry readGeometry() {
        try {
            String line;
            do {
                line = mif.poll();
                if (line == null) {
                    end = true;
                    return null;
                }
                line = line.trim();
            } while (line.isEmpty());

            if (startsWithIgnoreCase(line, "NONE")) {
                return gf.createPoint((CoordinateSequence) null);
            } else if (startsWithIgnoreCase(line, "POINT")) {
                return parsePoint(line);
            } else if (startsWithIgnoreCase(line, "LINE")) {
                return parseLine(line);
            } else if (startsWithIgnoreCase(line, "PLINE")) {
                return parsePLine(line);
            } else if (startsWithIgnoreCase(line, "REGION")) {
                return parseRegion(line);
            } else if (startsWithIgnoreCase(line, "ARC")) {
                return parseArc(line);
            } else if (startsWithIgnoreCase(line, "TEXT")) {
                return parseText(line);
            } else if (startsWithIgnoreCase(line, "RECT")
                    || startsWithIgnoreCase(line, "ROUNDRECT")
                    || startsWithIgnoreCase(line, "ELLIPSE")) {
                return parseRect(line);
            } else if (startsWithIgnoreCase(line, "MULTIPOINT")) {
                return parseMultiPoint(line);
            } else if (startsWithIgnoreCase(line, "COLLECTION")) {
                return parseGeometryCollection(line);
            } else {
                // Ignore geometry
                return null;
            }
        } catch (IOException e) {
            close();
            throw new RuntimeException(e);
        }
    }

    private Point parsePoint(String line) throws IOException {
        String[] a = line.split("\\s+");
        double x = Double.parseDouble(a[1]);
        double y = Double.parseDouble(a[2]);

        skipOptional("SYMBOL");
        return gf.createPoint(toCoordinateSequence(x, y));
    }

    private LineString parseLine(String line) throws IOException {
        String[] a = line.split("\\s+");
        double x1 = Double.parseDouble(a[1]);
        double y1 = Double.parseDouble(a[2]);
        double x2 = Double.parseDouble(a[3]);
        double y2 = Double.parseDouble(a[4]);

        skipOptional("PEN");
        return gf.createLineString(toCoordinateSequence(x1, y1, x2, y2));
    }

    private Geometry parsePLine(String line) throws IOException {
        String[] a = line.split("\\s+");

        if (a.length == 3) {
            // PLINE MULTIPLE numlines
            if (!startsWithIgnoreCase(a[1], "MULTIPLE")) {
                throw new IllegalArgumentException("Invalid PLINE statement, expected MULTIPLE for 2 arguments");
            }
            int numLines = Integer.parseInt(a[2]);
            LineString[] lineStrings = new LineString[numLines];
            for (int i = 0; i < numLines; i++) {
                int numpts = Integer.parseInt(mif.poll().trim());
                lineStrings[i] = gf.createLineString(readCoordinatesN(numpts));
            }
            skipOptional(OPTIONAL_PLINE);
            return numLines == 1 ? lineStrings[0] : gf.createMultiLineString(lineStrings);
        } else {
            // PLINE numpts OR
            // PLINE
            // numpts
            String _numpts = a.length == 2 ? a[1] : mif.poll().trim();
            int numpts = Integer.parseInt(_numpts);
            LineString ls = gf.createLineString(readCoordinatesN(numpts));
            skipOptional(OPTIONAL_PLINE);
            return ls;
        }
    }

    private Geometry parseRegion(String line) throws NumberFormatException, IOException {
        String[] a = line.split("\\s+");

        int numRings = Integer.parseInt(a[1]);
        LinearRing[] rings = new LinearRing[numRings];
        for (int i = 0; i < numRings; i++) {
            int numpts = Integer.parseInt(mif.poll().trim());
            CoordinateSequence csq = readCoordinatesN(numpts);
            if (!isClosed(csq)) {
                csq = close(csq);
            }
            rings[i] = gf.createLinearRing(csq);
        }

        skipOptional(OPTIONAL_REGION);

        if (numRings == 1) {
            return gf.createPolygon(rings[0]);
        }

        List<Polygon> polygons = new ArrayList<>();

        LinearRing exterior = rings[0];
        List<LinearRing> interiorRings = new ArrayList<>();
        for (int i = 1; i < rings.length; i++) {
            if (exterior.contains(rings[i])) {
                interiorRings.add(rings[i]);
            } else {
                polygons.add(gf.createPolygon(exterior, interiorRings.toArray(new LinearRing[0])));
                interiorRings.clear();
                exterior = rings[i];
            }
        }
        polygons.add(gf.createPolygon(exterior, interiorRings.toArray(new LinearRing[0])));

        return polygons.size() == 1 ? polygons.get(0) : gf.createMultiPolygon(polygons.toArray(new Polygon[0]));
    }

    private LineString parseArc(String line) throws IOException {
        String[] arr = line.split("\\s+");
        double x1 = Double.parseDouble(arr[1]);
        double y1 = Double.parseDouble(arr[2]);
        double x2 = Double.parseDouble(arr[3]);
        double y2 = Double.parseDouble(arr[4]);
        Envelope e = new Envelope(x1, x2, y1, y2);

        arr = mif.poll().split("\\s+");
        double a = Double.parseDouble(arr[0]);
        double b = Double.parseDouble(arr[1]);

        skipOptional("PEN");

        GeometricShapeFactory gsf = new GeometricShapeFactory(gf);
        gsf.setEnvelope(e);
        return gsf.createArc(a, b);
    }

    private Point parseText(String line) throws IOException {
        // Skip TEXT line
        line = mif.poll();

        String[] a = line.split("\\s+");
        double x1 = Double.parseDouble(a[1]);
        double y1 = Double.parseDouble(a[2]);
        // double x2 = Double.parseDouble(a[3]);
        // double y2 = Double.parseDouble(a[4]);

        skipOptional(new String[] { "FONT", "SPACING", "JUSTIFY", "ANGLE", "LABEL" });

        return gf.createPoint(toCoordinateSequence(x1, y1));
    }

    private Polygon parseRect(String line) throws IOException {
        String[] a = line.split("\\s+");
        double x1 = Double.parseDouble(a[1]);
        double y1 = Double.parseDouble(a[2]);
        double x2 = Double.parseDouble(a[3]);
        double y2 = Double.parseDouble(a[4]);

        if (startsWithIgnoreCase(a[0], "ROUNDRECT")) {
            mif.poll(); // Skip the degree of rounding (a)
        }

        skipOptional(OPTIONAL_RECT);
        return JTS.toGeometry(new Envelope(x1, x2, y1, y2));
    }

    private Geometry parseMultiPoint(String line) throws IOException {
        String[] a = line.split("\\s+");
        int numPoints = Integer.parseInt(a[1]);

        Point[] points = new Point[numPoints];
        for (int i = 0; i < numPoints; i++) {
            a = mif.poll().split("\\s+");
            points[i] = gf.createPoint(readCoordinatesN(1));
        }

        skipOptional("SYMBOL");

        return numPoints == 1 ? points[0] : gf.createMultiPoint(points);
    }

    private GeometryCollection parseGeometryCollection(String line) throws IOException {
        int n = Integer.parseInt(line.substring("COLLECTION".length()));
        Geometry[] geometries = new Geometry[n];
        for (int i = 0; i < n; i++) {
            geometries[i] = readGeometry();
        }
        return gf.createGeometryCollection(geometries);
    }

    private CoordinateSequence readCoordinatesN(int n) throws IOException {
        CoordinateSequence csq = gf.getCoordinateSequenceFactory().create(n, 2);
        for (int i = 0; i < n; i++) {
            String[] a = mif.poll().split("\\s+");
            double x = Double.parseDouble(a[0]);
            double y = Double.parseDouble(a[1]);
            csq.setOrdinate(i, 0, x);
            csq.setOrdinate(i, 1, y);
        }
        return csq;
    }

    private void skipOptional(String s) throws IOException {
        String line = mif.peek();
        if (!startsWithIgnoreCase(line, s)) {
            mif.poll();
        }
    }

    private void skipOptional(String[] a) throws IOException {
        String line = mif.peek();
        if (line == null) {
            return;
        }
        line = line.trim();
        for (String s : a) {
            if (startsWithIgnoreCase(line, s)) {
                mif.poll();
                line = mif.peek();
                if (line == null) {
                    return;
                }
                line = line.trim();
            }
        }
    }

    private CoordinateSequence toCoordinateSequence(double x, double y) {
        CoordinateSequence csq = gf.getCoordinateSequenceFactory().create(1, 2);
        csq.setOrdinate(0, 0, x);
        csq.setOrdinate(0, 1, y);
        return csq;
    }

    private CoordinateSequence toCoordinateSequence(double x1, double y1, double x2, double y2) {
        CoordinateSequence csq = gf.getCoordinateSequenceFactory().create(2, 2);
        csq.setOrdinate(0, 0, x1);
        csq.setOrdinate(0, 1, y1);
        csq.setOrdinate(1, 0, x2);
        csq.setOrdinate(1, 1, y2);
        return csq;
    }

    private boolean isClosed(CoordinateSequence csq) {
        double x1 = csq.getX(0);
        double y1 = csq.getY(0);
        double xN = csq.getX(csq.size() - 1);
        double yN = csq.getY(csq.size() - 1);
        return x1 == xN && y1 == yN;
    }

    private CoordinateSequence close(CoordinateSequence csq) {
        int n = csq.size();
        CoordinateSequence closed = gf.getCoordinateSequenceFactory().create(n + 1, 2);
        for (int i = 0; i < n; i++) {
            closed.setOrdinate(i, CoordinateSequence.X, csq.getX(i));
            closed.setOrdinate(i, CoordinateSequence.Y, csq.getY(i));
        }
        closed.setOrdinate(n, CoordinateSequence.X, csq.getX(0));
        closed.setOrdinate(n, CoordinateSequence.Y, csq.getY(0));
        return closed;
    }

}
