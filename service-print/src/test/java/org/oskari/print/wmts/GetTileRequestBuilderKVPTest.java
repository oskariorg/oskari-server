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
                "style",
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
                + "&REQUEST=GetTile"
                + "&VERSION=1.0.0"
                + "&LAYER=foo"
                + "&STYLE=foo"
                + "&FORMAT=foo"
                + "&TILEMATRIXSET=foo"
                + "&TILEMATRIX=foo"
                + "&TILEROW=1"
                + "&TILECOL=1";
        String actual = builder.build();
        assertEquals(expected, actual);
    }

}