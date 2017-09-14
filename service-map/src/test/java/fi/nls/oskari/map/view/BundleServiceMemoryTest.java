package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.map.view.Bundle;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class BundleServiceMemoryTest {

    private BundleService bundleService;

    @Before
    public void init() {
        bundleService = new BundleServiceMemory();
    }

    @Test
    public void testAddView() throws ViewException {
        // Should be empty at start
        List<Bundle> all = bundleService.findAll();
        assertNotNull(all);
        assertEquals(0, all.size());

        Bundle bundle = new Bundle();
        bundle.setName("foobar");

        // bundleId is unset
        assertEquals(-1L, bundle.getBundleId());

        long bundleId = bundleService.addBundleTemplate(bundle);
        // bundleId should be set after registering bundle
        assertNotEquals(-1L, bundle.getBundleId());
        // the id returned by #addBundleTemplate should be same value 
        // as now known by the Bundle itself
        assertEquals(bundle.getBundleId(), bundleId);
        // First registered bundle in a bundle service should be given id 0
        assertEquals(0L, bundleId);

        // After registering one Bundle there should be a total of one Bundles
        all = bundleService.findAll();
        assertNotNull(all);
        assertEquals(1, all.size());
        // Expect same reference
        assertTrue(bundle == all.get(0));
    }

}
