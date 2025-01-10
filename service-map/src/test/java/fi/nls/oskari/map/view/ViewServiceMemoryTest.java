package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.map.view.View;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ViewServiceMemoryTest {

    private ViewServiceMemory viewService;

    @BeforeEach
    public void init() {
        viewService = new ViewServiceMemory();
    }

    @Test
    public void testAddView() throws ViewException {
        // Should be empty at start
        List<Object> all = viewService.findAll();
        Assertions.assertNotNull(all);
        Assertions.assertEquals(0, all.size());

        View view = new View();
        view.setName("bazqux");
        view.setType("RANDOM");

        // id and uuid are unset
        Assertions.assertEquals(-1L, view.getId());
        Assertions.assertNull(view.getUuid());

        viewService.addView(view);

        // addView should set the id and the UUID of the View
        Assertions.assertEquals(0L, view.getId());
        Assertions.assertNotNull(view.getUuid());
        Assertions.assertEquals(36, view.getUuid().length());

        // After adding one view size should be one
        all = viewService.findAll();
        Assertions.assertNotNull(all);
        Assertions.assertEquals(1, all.size());

        Object obj = all.get(0);
        if (!(obj instanceof View)) {
            Assertions.fail("Expected obj to be instanceof View");
            return;
        }

        // Expect same reference
        Assertions.assertTrue(view == obj);
    }

}
