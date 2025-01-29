package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.map.view.Bundle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BundleServiceMemoryTest {

    private BundleServiceMemory bundleService;

    @BeforeEach
    public void init() {
        bundleService = new BundleServiceMemory();
    }

    @Test
    public void testAddView() throws ViewException {
        // Should be empty at start
        List<Bundle> all = bundleService.findAll();
        Assertions.assertNotNull(all);
        Assertions.assertEquals(0, all.size());

        Bundle bundle = new Bundle();
        bundle.setName("foobar");

        // bundleId is unset
        Assertions.assertEquals(-1L, bundle.getBundleId());

        long bundleId = bundleService.addBundleTemplate(bundle);
        // bundleId should be set after registering bundle
        Assertions.assertNotEquals(-1L, bundle.getBundleId());
        // the id returned by #addBundleTemplate should be same value 
        // as now known by the Bundle itself
        Assertions.assertEquals(bundle.getBundleId(), bundleId);
        // First registered bundle in a bundle service should be given id 0
        Assertions.assertEquals(0L, bundleId);

        // After registering one Bundle there should be a total of one Bundles
        all = bundleService.findAll();
        Assertions.assertNotNull(all);
        Assertions.assertEquals(1, all.size());
        // Expect same reference
        Assertions.assertTrue(bundle == all.get(0));
    }

}
