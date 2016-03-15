package fi.nls.oskari;

import fi.nls.common.grt.projections.ProjectionTransform;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.PointTransformer;
import fi.nls.oskari.map.geometry.ProjectionHelper;

import java.awt.geom.Point2D;

/**
 * Oskari wrapper for custom transform library implemented by NLSFI.
 * Defaults to fi.nls.oskari.map.geometry.ProjectionHelper one or both of the projections are unsupported by
 * the custom lib.
 */
public class NLSFIPointTransformer implements PointTransformer {

    private static final Logger LOG = LogFactory.getLogger(NLSFIPointTransformer.class);

    @Override
    public Point reproject(Point point, String sourceSRS, String targetSRS) {

        NLSFIProjections source = NLSFIProjections.forCode(sourceSRS);
        NLSFIProjections target = NLSFIProjections.forCode(targetSRS);
        if(source.equals(NLSFIProjections.UNSUPPORTED) ||
                target.equals(NLSFIProjections.UNSUPPORTED)) {
            LOG.info("Projections not supported", sourceSRS, targetSRS, "by NLSFI library. Using ProjectionHelper instead.");
            return ProjectionHelper.transformPoint(point, sourceSRS, targetSRS);
        }

        ProjectionTransform transform = new ProjectionTransform(source.code, target.code);

        Point2D.Double input = getPoint(point, source.northFirst);
        Point2D.Double output = new Point2D.Double();

        transform.transform(input, output);
        if(target.northFirst) {
            return new Point(output.getY(), output.getX());
        }
        return new Point(output.getX(), output.getY());
    }

    private Point2D.Double getPoint(Point point, boolean isNorthAxisFirst) {
        if(isNorthAxisFirst) {
            return new Point2D.Double(point.getLat(), point.getLon());
        }
        return new Point2D.Double(point.getLon(), point.getLat());
    }
}
