package fi.nls.oskari.control.metadata;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupServiceIbatisImpl;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.search.channel.MetadataCatalogueChannelSearchService;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oskari.service.util.ServiceFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * Problems mocking the responses -> IGNORE for now
 */
@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {IOHelper.class, PropertyUtil.class, ServiceFactory.class})
public class GetMetadataSearchOptionsHandlerTest extends JSONActionRouteTest {

    final private GetMetadataSearchOptionsHandler handler = new GetMetadataSearchOptionsHandler();
    @Before
    public void setup() throws Exception{
        mockInternalServices();
    }

    @Test
    public void testHandleAction() throws Exception {

        final ActionParameters params = createActionParams();
        handler.handleAction(params);

        final JSONObject response = ResourceHelper.readJSONResource("GetMetadataSearchOptionsHandler-response.json", this);

        verifyResponseContent(response);
    }

    private void mockInternalServices() throws Exception {
        final OskariMapLayerGroupService service = mock(OskariMapLayerGroupServiceIbatisImpl.class);
        doReturn(Collections.emptyList()).when(service).findAll();


        // return mocked service if a new one is created
        // classes doing this must be listed in PrepareForTest annotation
        PowerMockito.whenNew(OskariMapLayerGroupServiceIbatisImpl.class).withNoArguments().
                thenAnswer(new Answer<Object>() {
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return service;
                    }
                });

        PowerMockito.mockStatic(IOHelper.class);


        for(MetadataField field : MetadataCatalogueChannelSearchService.getFields()) {
            MetadataFieldHandler handler = field.getHandler();

            final HttpURLConnection connection = mock(HttpURLConnection.class);
            when(IOHelper.getConnection(handler.getSearchURL() + handler.getPropertyName())).
                    thenReturn(connection);
            doReturn(getInputstreamForProperty(handler.getPropertyName())).when(connection).getInputStream();
        }

        /*
            when(IOHelper.getURL(handler.getSearchURL() + handler.getPropertyName())).
                    thenReturn(ResourceHelper.readStringResource(handler.getPropertyName() + "-response.json",this));
         */
    }
    private InputStream getInputstreamForProperty(final String property) {
        final String resource = ResourceHelper.readStringResource(property + "-response.json",this);
        System.out.println(property + "-response.json: " + resource);
        return new ByteArrayInputStream(resource.getBytes());
    }

}
