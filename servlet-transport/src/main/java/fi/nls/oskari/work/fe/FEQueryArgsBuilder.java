package fi.nls.oskari.work.fe;

import java.util.List;

import fi.nls.oskari.work.JobType;
import org.apache.http.client.utils.URIBuilder;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;

public interface FEQueryArgsBuilder {

    public void buildParams(URIBuilder builder, JobType type,
            WFSLayerStore layer, SessionStore session, List<Double> bounds,
            MathTransform transform, CoordinateReferenceSystem crs);
}
