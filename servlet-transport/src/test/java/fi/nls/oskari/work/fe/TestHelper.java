package fi.nls.oskari.work.fe;

import fi.nls.oskari.pojo.Layer;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.work.JobType;
import fi.nls.oskari.work.JobValidator;
import fi.nls.oskari.work.ResultProcessor;

import java.io.IOException;
import java.util.Map;

/**
 * Created by SMAKINEN on 9.4.2015.
 */
public class TestHelper {

    public static FEMapLayerJob createJob(String sessionJSON, ResultProcessor processor, String layerSpec)  throws IOException {

        SessionStore session = SessionStore.setJSON(sessionJSON);

        Map<String, Layer> layers = session.getLayers();
        for (Layer layer : layers.values()) {
            // init bounds to tiles (render all)
            layer.setTiles(session.getGrid().getBounds());
        }
        FEMapLayerJob job = new TestRunFEMapLayerJob(processor, session, layerSpec);
        JobValidator validator = new JobValidator(job);
        validator.validateJob();
        return job;
    }

    /* Test Helper to setup session and permission */
    static class TestRunFEMapLayerJob extends FEMapLayerJob {

        TestRunFEMapLayerJob(ResultProcessor resultProcessor,
                             SessionStore session, String layerjson) {
            super(resultProcessor, JobType.NORMAL, session, getLayer(layerjson), true, true, true);
        }

        private static WFSLayerStore getLayer(String layerjson) {
            try {
                return WFSLayerStore.setJSON(layerjson);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
