package org.oskari.print;

import org.junit.Assert;
import org.junit.Test;
import org.oskari.print.request.PrintFormat;

public class PrintFormatTest {

    @Test
    public void nullContentTypeReturnsNull() {
        PrintFormat format = PrintFormat.getByContentType(null);
        Assert.assertNull(format);
    }

    @Test
    public void unknownContentTypeReturnsNull() {
        PrintFormat format = PrintFormat.getByContentType("foo");
        Assert.assertNull(format);
    }

    @Test
    public void knownFormatsReturnThemselves() {
        PrintFormat expected = PrintFormat.PDF;
        PrintFormat actual = PrintFormat.getByContentType(expected.contentType);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void casingOfContentTypeDoesNotMatter() {
        PrintFormat expected = PrintFormat.PDF;
        String type = expected.contentType.toUpperCase();
        PrintFormat actual = PrintFormat.getByContentType(type);
        Assert.assertEquals(expected, actual);

        type = type.toLowerCase();
        actual = PrintFormat.getByContentType(type);
        Assert.assertEquals(expected, actual);
    }

}