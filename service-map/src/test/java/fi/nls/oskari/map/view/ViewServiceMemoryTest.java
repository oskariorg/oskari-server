package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.map.view.View;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ViewServiceMemoryTest {

    private ViewService viewService;

    @Before
    public void init() {
        viewService = new ViewServiceMemory();
    }

    @Test
    public void testAddView() throws ViewException {
        // Should be empty at start
        List<Object> all = viewService.findAll();
        assertNotNull(all);
        assertEquals(0, all.size());

        View view = new View();
        view.setName("bazqux");
        view.setType("RANDOM");

        // id and uuid are unset
        assertEquals(-1L, view.getId());
        assertNull(view.getUuid());

        viewService.addView(view);

        // addView should set the id and the UUID of the View
        assertEquals(0L, view.getId());
        assertNotNull(view.getUuid());
        assertEquals(36, view.getUuid().length());

        // After adding one view size should be one
        all = viewService.findAll();
        assertNotNull(all);
        assertEquals(1, all.size());

        Object obj = all.get(0);
        if (!(obj instanceof View)) {
            fail("Expected obj to be instanceof View");
            return;
        }

        // Expect same reference
        assertTrue(view == obj);
    }

}
