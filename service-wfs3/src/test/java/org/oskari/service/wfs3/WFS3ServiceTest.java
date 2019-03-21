package org.oskari.service.wfs3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Ignore;
import org.junit.Test;
import org.oskari.service.wfs3.model.WFS3CollectionInfo;
import org.oskari.service.wfs3.model.WFS3Exception;

public class WFS3ServiceTest {

    @Test
    @Ignore("Depends on outside service")
    public void testJSONWorks() throws WFS3Exception, IOException {
        String url = "https://beta-paikkatieto.maanmittauslaitos.fi/geographic-names/wfs3/v1/";
        WFS3Service expected = WFS3Service.fromURL(url);
        byte[] jsonExpected = WFS3Service.toJSON(expected);
        WFS3Service actual = WFS3Service.fromJSON(jsonExpected);
        assertEquals(expected, actual);
    }

    @Test
    @Ignore("Depends on outside service")
    public void testBasicParsingWorks() throws WFS3Exception, IOException {
        String url = "https://beta-paikkatieto.maanmittauslaitos.fi/geographic-names/wfs3/v1/";
        WFS3Service service = WFS3Service.fromURL(url);

        List<String> expected = Stream.of("placenames", "places", "mapnames", "placenames_simple")
                .sorted()
                .collect(Collectors.toList());

        List<String> actual = service.getCollections().stream()
                .map(WFS3CollectionInfo::getId)
                .sorted()
                .collect(Collectors.toList());

        assertIterablesEquals(expected, actual);
    }

    private <T> void assertIterablesEquals(Iterable<T> expected, Iterable<T> actual) {
        Iterator<T> expectedIter = expected.iterator();
        Iterator<T> actualIter = actual.iterator();
        while (expectedIter.hasNext()) {
            assertTrue(actualIter.hasNext());
            assertTrue(Objects.equals(expectedIter.next(), actualIter.next()));
        }
        assertFalse(actualIter.hasNext());
    }

}
