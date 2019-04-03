package org.oskari.print.wmts;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

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

        final String exceptionPattern = "Required parameter '%s' missing";

        GetTileRequestBuilderKVP builder = new GetTileRequestBuilderKVP();

        for (int i = 0; i < strFields.length; i++) {
            String f = strFields[i];
            Method m = GetTileRequestBuilderKVP.class.getMethod(f, String.class);
            assertNotNull(m);
            String expected = String.format(exceptionPattern, f);
            String actual = null;
            try {
                builder.build();
            } catch (IllegalArgumentException e) {
                actual = e.getMessage();
            }
            assertEquals(expected, actual);
            m.invoke(builder, "foo");
        }

        for (int i = 0; i < intFields.length; i++) {
            String f = intFields[i];
            Method m = GetTileRequestBuilderKVP.class.getMethod(f, int.class);
            assertNotNull(m);
            String expected = String.format(exceptionPattern, f);
            String actual = null;
            try {
                builder.build();
            } catch (IllegalArgumentException e) {
                actual = e.getMessage();
            }
            assertEquals(expected, actual);
            m.invoke(builder, 1);
        }

        String expected = "foo?SERVICE=WMTS"
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
        assertEquals(expected, actual);
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
                .build();
        String expected = "foo?SERVICE=WMTS"
                + "&VERSION=1.0.0"
                + "&REQUEST=GetTile"
                + "&LAYER=foo%20bar"
                + "&STYLE="
                + "&FORMAT=image%2Fpng"
                + "&TILEMATRIXSET=ETRS-TM35FIN"
                + "&TILEMATRIX=ETRS_TM35-FIN%3A8"
                + "&TILEROW=114"
                + "&TILECOL=208";
        assertEquals(expected, actual);
    }


}
