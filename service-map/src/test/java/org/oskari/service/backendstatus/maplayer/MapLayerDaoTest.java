package org.oskari.service.backendstatus.maplayer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import java.util.List;

public class MapLayerDaoTest {

    @Test
    @Disabled("Requires a database connection, assumes DB state")
    public void sanityTest() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(System.getProperty("oskari.test.db.url"));
        MapLayerDao dao = new MapLayerDao(ds);
        List<MapLayer> wmsLayers = dao.findWMSMapLayers();
        Assertions.assertNotNull(wmsLayers);
        Assertions.assertTrue(wmsLayers.size() > 0, "Finds atleast one WMS Layer");

        List<MapLayer> wfsLayers = dao.findWMSMapLayers();
        Assertions.assertNotNull(wfsLayers);
        Assertions.assertTrue(wfsLayers.size() > 0, "Finds atleast one WFS Layer");
    }

}
