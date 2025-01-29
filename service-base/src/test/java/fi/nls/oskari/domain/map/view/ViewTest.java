package fi.nls.oskari.domain.map.view;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Simple testcases for View/bundle removal
 */
public class ViewTest {

    /**
     * Create a dummy view
     * @return
     */
    private View getView() {
        View view = new View();
        for(int i = 0; i < 20; ++i) {
            Bundle b = new Bundle();
            b.setName("bundle_" + i);
            view.addBundle(b);
        }
        return view;
    }


    @Test
    public void testGetBundleByName() throws Exception {
        View view = getView();
        final String bundleName = "bundle_3";
        Bundle bundle = view.getBundleByName(bundleName);
        Assertions.assertEquals(bundleName, bundle.getName(), "Bundle name should match");
    }

    @Test
    public void testGetBundles() throws Exception {
        View view = getView();
        Assertions.assertEquals(view.getBundles().size(), 20);
    }

    @Test
    public void testRemoveBundle() throws Exception {
        View view = getView();
        Assertions.assertEquals(view.getBundles().size(), 20, "Should have 20 bundles");
        final String bundleName = "bundle_3";
        view.removeBundle(bundleName);
        Assertions.assertEquals(view.getBundles().size(), 19, "Should have 19 bundles");
        Bundle bundle = view.getBundleByName(bundleName);
        Assertions.assertNull(bundle, "Bundle should be null after removal");
    }
}
