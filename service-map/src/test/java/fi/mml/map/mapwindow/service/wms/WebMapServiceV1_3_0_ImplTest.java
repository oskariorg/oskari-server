package fi.mml.map.mapwindow.service.wms;

import fi.nls.oskari.util.IOHelper;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;

/**
 * Created by SMAKINEN on 12.2.2018.
 */
public class WebMapServiceV1_3_0_ImplTest {

    @Test
    public void sameNameParentChildMatchesParent() throws Exception {
        String data = IOHelper.readString(getClass().getResourceAsStream("/capabilities-with-duplicated-layername.xml"));
        WebMapServiceV1_3_0_Impl caps = new WebMapServiceV1_3_0_Impl("test", data, "muinaismuistot", new HashSet<>());
        // if NPE is not thrown we are good
        assertEquals("Duplicate layer matches the parent without keywords", 0, caps.getKeywords().length);
    }

}