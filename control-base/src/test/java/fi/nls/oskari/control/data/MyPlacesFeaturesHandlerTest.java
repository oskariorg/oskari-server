
package fi.nls.oskari.control.data;

import fi.nls.oskari.control.ActionParameters;
import org.junit.Test;

public class MyPlacesFeaturesHandlerTest {

    @Test
    public void MyPlacesFeaturesHandler() throws Exception {
        MyPlacesFeaturesHandler handler = new MyPlacesFeaturesHandler();
        ActionParameters parameters = new ActionParameters();
        handler.handleAction(parameters);
    }

}