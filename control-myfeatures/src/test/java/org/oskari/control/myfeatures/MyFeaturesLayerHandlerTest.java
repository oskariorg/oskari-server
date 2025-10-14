package org.oskari.control.myfeatures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.oskari.map.myfeatures.service.MyFeaturesService;
import org.oskari.user.User;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

public class MyFeaturesLayerHandlerTest {

    @Test
    public void getLayerByIdWorks() throws Exception {
        MyFeaturesLayer layer = new MyFeaturesLayer();
        layer.setId(UUID.fromString("d4ea8bb7-b323-4c8e-ab5d-a529ee9416f8"));
        layer.setName("en", "foo");
        layer.setDesc("fi", "bar");
        layer.setOwnerUuid("foo");
        layer.setExtent(new Envelope(0, 10, 0, 10));

        User user = new User();
        user.setUuid("foo");

        MyFeaturesLayerHandler handler = new MyFeaturesLayerHandler();
        handler.setService(
                when(mock(MyFeaturesService.class).getLayer(layer.getId()))
                        .thenReturn(layer).getMock());
        handler.init();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ServletOutputStream sos = spy(ServletOutputStream.class);
        doAnswer(invocation -> {
            baos.write(
                    invocation.getArgument(0, byte[].class),
                    invocation.getArgument(1, Integer.class),
                    invocation.getArgument(2, Integer.class));
            return null;

        }).when(sos).write(any(byte[].class), anyInt(), anyInt());

        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        when(servletResponse.getOutputStream()).thenReturn(sos);

        ActionParameters params = mock(ActionParameters.class);
        when(params.getHttpParam(MyFeaturesLayerHandler.PARAM_ID)).thenReturn(layer.getId().toString());
        when(params.getUser()).thenReturn(user);
        when(params.getResponse()).thenReturn(servletResponse);

        handler.handleGet(params);

        String expected = "[{'id':'d4ea8bb7-b323-4c8e-ab5d-a529ee9416f8','locale':{'fi':{'desc':'bar'},'en':{'name':'foo'}},'ownerUuid':'foo','created':null,'updated':null,'layerFields':[],'featureCount':0,'extent':[0.0,0.0,10.0,10.0],'layerOptions':{},'layerAttributes':{}}]"
                .replace('\'', '"');
        String actual = baos.toString();

        assertEquals(expected, actual);
    }

}
