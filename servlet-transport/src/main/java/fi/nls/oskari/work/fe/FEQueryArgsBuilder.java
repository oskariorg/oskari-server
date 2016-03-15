package fi.nls.oskari.work.fe;

import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.work.JobType;
import org.apache.http.client.utils.URIBuilder;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.util.List;

public interface FEQueryArgsBuilder {

    public void buildParams(URIBuilder builder, JobType type,
            WFSLayerStore layer, SessionStore session, List<Double> bounds,
            MathTransform transform, CoordinateReferenceSystem crs);
}
