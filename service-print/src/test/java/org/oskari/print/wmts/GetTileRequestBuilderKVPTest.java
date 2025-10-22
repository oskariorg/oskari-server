package org.oskari.print.wmts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class GetTileRequestBuilderKVPTest {

    @Test
    public void buildingWithAnyMandatoryParameterMissingThrowsException()
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {
        final String[] strFields = new String[] {
                "endPoint",
                "layer",
                "format",
                "tileMatrixSet",
                "tileMatrix"
        };

        final String[] intFields = new String[] {
                "tileRow",
                "tileCol"
        };

        final Map<String, Object> additionalParams = new LinkedHashMap<>();
        additionalParams.put("api-key", "0e10838f-de3c-429d-afc8-808c3bcb32a5");
        additionalParams.put("client-id", 678);

        final String exceptionPattern = "Required parameter '%s' missing";

        GetTileRequestBuilderKVP builder = new GetTileRequestBuilderKVP();

        for (int i = 0; i < strFields.length; i++) {
            String f = strFields[i];
            Method m = GetTileRequestBuilderKVP.class.getMethod(f, String.class);
            Assertions.assertNotNull(m);
            String expected = String.format(exceptionPattern, f);
            String actual = null;
            try {
                builder.build();
            } catch (IllegalArgumentException e) {
                actual = e.getMessage();
            }
            Assertions.assertEquals(expected, actual);
            m.invoke(builder, "foo");
        }

        for (int i = 0; i < intFields.length; i++) {
            String f = intFields[i];
            Method m = GetTileRequestBuilderKVP.class.getMethod(f, int.class);
            Assertions.assertNotNull(m);
            String expected = String.format(exceptionPattern, f);
            String actual = null;
            try {
                builder.build();
            } catch (IllegalArgumentException e) {
                actual = e.getMessage();
            }
            Assertions.assertEquals(expected, actual);
            m.invoke(builder, 1);
        }

        builder.additionalParams(additionalParams);

        String expected = "foo"
                + "?api-key=0e10838f-de3c-429d-afc8-808c3bcb32a5"
                + "&client-id=678"
                + "&SERVICE=WMTS"
                + "&VERSION=1.0.0"
                + "&REQUEST=GetTile"
                + "&LAYER=foo"
                + "&STYLE="
                + "&FORMAT=foo"
                + "&TILEMATRIXSET=foo"
                + "&TILEMATRIX=foo"
                + "&TILEROW=1"
                + "&TILECOL=1";
        String actual = builder.build();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testParametersAreURLEncoded() {
        String actual = new GetTileRequestBuilderKVP().endPoint("foo")
                .layer("foo bar")
                .format("image/png")
                .tileMatrixSet("ETRS-TM35FIN")
                .tileMatrix("ETRS_TM35-FIN:8")
                .tileRow(114)
                .tileCol(208)
                .additionalParams(Map.of("api-key", "0e10838f-de3c-429d-afc8-808c3bcb32a5"))
                .build();
        String expected = "foo"
                + "?api-key=0e10838f-de3c-429d-afc8-808c3bcb32a5"
                + "&SERVICE=WMTS"
                + "&VERSION=1.0.0"
                + "&REQUEST=GetTile"
                + "&LAYER=foo%20bar"
                + "&STYLE="
                + "&FORMAT=image%2Fpng"
                + "&TILEMATRIXSET=ETRS-TM35FIN"
                + "&TILEMATRIX=ETRS_TM35-FIN%3A8"
                + "&TILEROW=114"
                + "&TILECOL=208";
        Assertions.assertEquals(expected, actual);
    }


}
