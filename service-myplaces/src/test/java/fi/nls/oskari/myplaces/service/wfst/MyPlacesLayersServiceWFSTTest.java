package fi.nls.oskari.myplaces.service.wfst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;

public class MyPlacesLayersServiceWFSTTest {

    @Ignore("Requires Geoserver")
    @Test
    public void testInsertUpdateDelete() throws ServiceException, DuplicateException {
        MyPlacesLayersServiceWFST service = new MyPlacesLayersServiceWFST();
        PropertyUtil.addProperty("myplaces.ows.url", "http://localhost:6082/geoserver/ows", true);
        String uuid = "fad28601-4b15-4211-896f-7b284a295c1e";
        List<MyPlaceCategory> myCategories = service.getByUserId(uuid);
        if (myCategories.size() > 0) {
            long[] ids = myCategories.stream().mapToLong(MyPlaceCategory::getId).toArray();
            assertEquals(myCategories.size(), service.delete(ids));
            myCategories = service.getByUserId(uuid);
            assertEquals(0, myCategories.size());
        }

        MyPlaceCategory myCategory = createSomeCategory(uuid);
        myCategories.add(myCategory);
        assertEquals("id should be 0 before inserting", 0, myCategory.getId());
        assertEquals("Expect 1 inserted features", 1, service.insert(myCategories));
        assertNotEquals("id should have been set to something other than 0", 0, myCategory.getId());

        myCategory.setCategory_name("barbaz");
        assertEquals("Expected 1 updated feature", 1, service.update(myCategories));
        myCategories = service.getByUserId(uuid);
        assertEquals(1, myCategories.size());
        MyPlaceCategory myUpdatedCategory = myCategories.get(0);
        assertTrue("Objects are different", myCategory != myUpdatedCategory);
        assertEquals("Yet they have the same id", myCategory.getId(), myUpdatedCategory.getId());
        assertEquals("category_name was updated in the service", "barbaz", myUpdatedCategory.getCategory_name());

        assertEquals(1, service.delete(new long[] { myCategory.getId() }));
        myCategories = service.getByUserId(uuid);
        assertEquals(0, myCategories.size());
    }

    @Ignore("Requires Geoserver")
    @Test
    public void testGETAfterInsert() throws ServiceException, DuplicateException {
        MyPlacesLayersServiceWFST service = new MyPlacesLayersServiceWFST();
        PropertyUtil.addProperty("myplaces.ows.url", "http://localhost:6082/geoserver/ows", true);
        String uuid = "fad28601-4b15-4211-896f-7b284a295c1e";
        List<MyPlaceCategory> myCategories = service.getByUserId(uuid);
        if (myCategories.size() > 0) {
            long[] ids = myCategories.stream().mapToLong(MyPlaceCategory::getId).toArray();
            assertEquals(myCategories.size(), service.delete(ids));
            myCategories = service.getByUserId(uuid);
            assertEquals(0, myCategories.size());
        }

        MyPlaceCategory myCategory = createSomeCategory(uuid);
        assertEquals("id should be 0 before inserting", 0, myCategory.getId());
        assertEquals("Expect 1 inserted feature", 1, service.insert(Arrays.asList(myCategory)));
        assertNotEquals("id should have been set to something other than 0", 0, myCategory.getId());

        assertEquals("Expect 1 updated feature", 1, service.update(Arrays.asList(myCategory)));

        myCategories = service.getByUserId(uuid);
        MyPlaceCategory afterGet = myCategories.get(0);
        assertEquals("", afterGet.getPublisher_name());
        assertEquals("", afterGet.getBorder_dasharray());
        assertEquals("", afterGet.getStroke_dasharray());

        myCategories = service.getByUserId(uuid);
        MyPlaceCategory GETafterPUT = myCategories.get(0);
        assertEquals("", GETafterPUT.getPublisher_name());
        assertEquals("", GETafterPUT.getBorder_dasharray());
        assertEquals("", GETafterPUT.getStroke_dasharray());
    }

    private MyPlaceCategory createSomeCategory(String uuid) {
        MyPlaceCategory myCategory = new MyPlaceCategory();
        myCategory.setUuid(uuid);
        myCategory.setDefault(true);
        myCategory.setCategory_name("foobar");
        myCategory.setStroke_width(10);
        myCategory.setDot_shape("3");
        myCategory.setFill_color("#0000FF");
        myCategory.setPublisher_name("");
        myCategory.setBorder_dasharray("");
        myCategory.setStroke_dasharray("");
        return myCategory;
    }

}
