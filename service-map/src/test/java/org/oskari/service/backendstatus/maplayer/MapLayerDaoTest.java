package org.oskari.service.backendstatus.maplayer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

public class MapLayerDaoTest {

    @Test
    @Ignore("Requires a database connection, assumes DB state")
    public void sanityTest() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(System.getProperty("oskari.test.db.url"));
        MapLayerDao dao = new MapLayerDao(ds);
        List<MapLayer> wmsLayers = dao.findWMSMapLayers();
        assertNotNull(wmsLayers);
        assertTrue("Finds atleast one WMS Layer", wmsLayers.size() > 0);

        List<MapLayer> wfsLayers = dao.findWMSMapLayers();
        assertNotNull(wfsLayers);
        assertTrue("Finds atleast one WFS Layer", wfsLayers.size() > 0);
    }

}
