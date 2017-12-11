package fi.nls.oskari.myplaces.handler;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import fi.nls.oskari.myplaces.MyPlaceWithGeometry;
import fi.nls.oskari.util.IOHelper;

public class MyPlacesFeaturesHandlerTest {

    private static final String INSERT = "insert_payload.json";

    @Test
    public void testParseMyPlaces() throws Exception {
        MyPlacesFeaturesHandler handler = new MyPlacesFeaturesHandler();
        byte[] payload = getInput(INSERT);
        MyPlaceWithGeometry[] myPlaces = handler.parseMyPlaces(payload, false);
        assertEquals(2, myPlaces.length);

        MyPlaceWithGeometry myPlace1 = myPlaces[0];
        assertEquals(1, myPlace1.getCategoryId());
        assertEquals("name", myPlace1.getName());
        assertEquals("place description", myPlace1.getDesc());
        assertEquals("url", myPlace1.getLink());
        assertEquals("attention text", myPlace1.getAttentionText());
        assertEquals("image url", myPlace1.getImageUrl());

        MyPlaceWithGeometry myPlace2 = myPlaces[1];
        assertEquals(1, myPlace2.getCategoryId());
        assertEquals("name2", myPlace2.getName());
        assertEquals("place description2", myPlace2.getDesc());
        assertEquals("url2", myPlace2.getLink());
        assertEquals("attention text2", myPlace2.getAttentionText());
        assertEquals("image url2", myPlace2.getImageUrl());
    }

    private byte[] getInput(String resource) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            return IOHelper.readBytes(in);
        }
    }

}