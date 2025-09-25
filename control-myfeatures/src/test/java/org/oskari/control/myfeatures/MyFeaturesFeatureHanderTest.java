package org.oskari.control.myfeatures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.oskari.control.myfeatures.dto.CreateMyFeaturesFeature;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.control.ActionParameters;

public class MyFeaturesFeatureHanderTest {

    private static MyFeaturesFeatureHandler handler;

    @BeforeAll
    public static void init() {
        handler = new MyFeaturesFeatureHandler();
        handler.initObjectMapper();
    }

    @Test
    public void parsePayloadWorksWithGeoJSONGeometry() throws Exception {
        CreateMyFeaturesFeature expected = new CreateMyFeaturesFeature();
        expected.setLayerId(UUID.fromString("6a58d4ee-d91f-45ea-bc56-0870b8866793"));
        expected.setFid("abc123");
        expected.setGeometry(new GeometryFactory().createPoint(new Coordinate(125.6, 10.1)));
        expected.setProperties(Collections.singletonMap("name", "Dinagat Islands"));

        String input = "{'layerId': '6a58d4ee-d91f-45ea-bc56-0870b8866793', 'fid': 'abc123', 'geometry': {'type': 'Point','coordinates': [125.6, 10.1]}, 'properties': {'name': 'Dinagat Islands'}}"
                .replace('\'', '"');
        ActionParameters params = mock(ActionParameters.class);
        when(params.getPayLoad()).thenReturn(input);

        CreateMyFeaturesFeature actual = handler.parsePayload(params, CreateMyFeaturesFeature.class);
        assertEquals(expected.getLayerId(), actual.getLayerId());
        assertEquals(expected.getFid(), actual.getFid());
        assertEquals(expected.getGeometry(), actual.getGeometry());
        assertEquals(expected.getProperties(), actual.getProperties());

        String response = handler.toJSONString(actual.toDomain(new ObjectMapper()));
        String expectedResponse = "{'id':0,'created':null,'updated':null,'fid':'abc123','geometry':{'type':'Point','coordinates':[125.6,10.1]},'properties':{'name':'Dinagat Islands'}}"
                .replace('\'', '"');
        assertEquals(expectedResponse, response);
    }

}
