package fi.nls.oskari.domain.map.view;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals("Bundle name should match", bundleName, bundle.getName());
    }

    @Test
    public void testGetBundles() throws Exception {
        View view = getView();
        Assert.assertEquals(view.getBundles().size(), 20);
    }

    @Test
    public void testRemoveBundle() throws Exception {
        View view = getView();
        Assert.assertEquals("Should have 20 bundles",view.getBundles().size(), 20);
        final String bundleName = "bundle_3";
        view.removeBundle(bundleName);
        Assert.assertEquals("Should have 19 bundles", view.getBundles().size(), 19);
        Bundle bundle = view.getBundleByName(bundleName);
        Assert.assertNull("Bundle should be null after removal", bundle);
    }
}
