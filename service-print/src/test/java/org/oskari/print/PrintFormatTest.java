package org.oskari.print;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oskari.print.request.PrintFormat;

public class PrintFormatTest {

    @Test
    public void nullContentTypeReturnsNull() {
        PrintFormat format = PrintFormat.getByContentType(null);
        Assertions.assertNull(format);
    }

    @Test
    public void unknownContentTypeReturnsNull() {
        PrintFormat format = PrintFormat.getByContentType("foo");
        Assertions.assertNull(format);
    }

    @Test
    public void knownFormatsReturnThemselves() {
        PrintFormat expected = PrintFormat.PDF;
        PrintFormat actual = PrintFormat.getByContentType(expected.contentType);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void casingOfContentTypeDoesNotMatter() {
        PrintFormat expected = PrintFormat.PDF;
        String type = expected.contentType.toUpperCase();
        PrintFormat actual = PrintFormat.getByContentType(type);
        Assertions.assertEquals(expected, actual);

        type = type.toLowerCase();
        actual = PrintFormat.getByContentType(type);
        Assertions.assertEquals(expected, actual);
    }

}