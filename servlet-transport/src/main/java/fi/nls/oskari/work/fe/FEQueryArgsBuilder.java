package fi.nls.oskari.work.fe;

import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.pojo.WFSLayerStore;
import fi.nls.oskari.work.OWSMapLayerJob;

public interface FEQueryArgsBuilder {

    public void buildParams(URIBuilder builder, OWSMapLayerJob.Type type,
            WFSLayerStore layer, SessionStore session, List<Double> bounds,
            MathTransform transform, CoordinateReferenceSystem crs);
}
