package org.oskari.print.mvt;

import java.util.Arrays;

/**
 * A polyline measured by distance along it, for laying text out on.
 */
class LinePath {

    private final double[] xs;
    private final double[] ys;
    /** Distance from the start of the line to each point */
    private final double[] distances;

    /**
     * @param coordinates the line, as x, y pairs. A point repeating the one
     *        before it is dropped, it has no direction of its own.
     */
    public LinePath(double[] coordinates) {
        int n = coordinates.length / 2;
        double[] x = new double[n];
        double[] y = new double[n];
        double[] d = new double[n];
        int count = 0;
        for (int i = 0; i < n; i++) {
            double px = coordinates[i * 2];
            double py = coordinates[i * 2 + 1];
            if (count > 0 && px == x[count - 1] && py == y[count - 1]) {
                continue;
            }
            x[count] = px;
            y[count] = py;
            d[count] = count == 0 ? 0 : d[count - 1] + Math.hypot(px - x[count - 1], py - y[count - 1]);
            count++;
        }
        this.xs = Arrays.copyOf(x, count);
        this.ys = Arrays.copyOf(y, count);
        this.distances = Arrays.copyOf(d, count);
    }

    public double getLength() {
        return distances.length == 0 ? 0 : distances[distances.length - 1];
    }

    /**
     * @return the same line running the other way
     */
    public LinePath reverse() {
        double[] coordinates = new double[xs.length * 2];
        for (int i = 0; i < xs.length; i++) {
            int j = xs.length - 1 - i;
            coordinates[i * 2] = xs[j];
            coordinates[i * 2 + 1] = ys[j];
        }
        return new LinePath(coordinates);
    }

    /**
     * @return index of the segment the distance falls on, the first or last
     *         one for a distance off either end
     */
    public int getSegment(double distance) {
        int last = distances.length - 2;
        for (int i = 0; i < last; i++) {
            if (distance <= distances[i + 1]) {
                return i;
            }
        }
        return last;
    }

    public double getX(double distance) {
        int i = getSegment(distance);
        return xs[i] + (xs[i + 1] - xs[i]) * along(i, distance);
    }

    public double getY(double distance) {
        int i = getSegment(distance);
        return ys[i] + (ys[i + 1] - ys[i]) * along(i, distance);
    }

    /**
     * @return direction of the segment, in radians
     */
    public double getAngle(int segment) {
        return Math.atan2(ys[segment + 1] - ys[segment], xs[segment + 1] - xs[segment]);
    }

    /**
     * @return how much the line turns from the segment before into this one,
     *         in radians from -PI to PI
     */
    public double getTurn(int segment) {
        double turn = getAngle(segment) - getAngle(segment - 1);
        if (turn > Math.PI) {
            return turn - 2 * Math.PI;
        }
        if (turn < -Math.PI) {
            return turn + 2 * Math.PI;
        }
        return turn;
    }

    /**
     * @return how far along the segment the distance is, 0 at its start and 1
     *         at its end
     */
    private double along(int segment, double distance) {
        return (distance - distances[segment]) / (distances[segment + 1] - distances[segment]);
    }
}
