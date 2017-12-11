package fi.nls.oskari.myplaces.handler;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import fi.nls.oskari.myplaces.MyPlaceWithGeometry;
import fi.nls.oskari.util.IOHelper;

public class MyPlacesFeaturesHandlerTest {

    private static final String PAYLOAD = "payload.json";

    @Test
    public void testParseMyPlaces() throws Exception {
        MyPlacesFeaturesHandler handler = new MyPlacesFeaturesHandler();
        byte[] payload = getInput(PAYLOAD);

        boolean shouldSetId = false;
        MyPlaceWithGeometry[] myPlaces = handler.parseMyPlaces(payload, shouldSetId);
        assertEquals(2, myPlaces.length);

        MyPlaceWithGeometry myPlace1 = myPlaces[0];
        assertEquals(1, myPlace1.getCategoryId());
        assertEquals("id should not be set because we set shouldSetId to false",
                0, myPlace1.getId());
        assertEquals("name", myPlace1.getName());
        assertEquals("place description", myPlace1.getDesc());
        assertEquals("url", myPlace1.getLink());
        assertEquals("attention text", myPlace1.getAttentionText());
        assertEquals("image url", myPlace1.getImageUrl());

        MyPlaceWithGeometry myPlace2 = myPlaces[1];
        assertEquals(1, myPlace2.getCategoryId());
        assertEquals("id should not be set because we set shouldSetId to false",
                0, myPlace1.getId());
        assertEquals("name2", myPlace2.getName());
        assertEquals("place description2", myPlace2.getDesc());
        assertEquals("url2", myPlace2.getLink());
        assertEquals("attention text2", myPlace2.getAttentionText());
        assertEquals("image url2", myPlace2.getImageUrl());
    }

    @Test
    public void testParseMyPlacesWithIds() throws Exception {
        MyPlacesFeaturesHandler handler = new MyPlacesFeaturesHandler();
        byte[] payload = getInput(PAYLOAD);
        boolean shouldSetId = true;
        MyPlaceWithGeometry[] myPlaces = handler.parseMyPlaces(payload, shouldSetId);
        assertEquals("ids should be set", 123L, myPlaces[0].getId());
        assertEquals("ids larger than Integer.MAX work", 9876543210123456L, myPlaces[1].getId());
    }

    private byte[] getInput(String resource) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            return IOHelper.readBytes(in);
        }
    }

}