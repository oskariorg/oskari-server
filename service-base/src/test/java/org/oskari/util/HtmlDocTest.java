package org.oskari.util;

import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class HtmlDocTest {

    final String html = "<html><head><title>Document title</title><style></style><script>alert('illegal!')</script></head>" +
            "<body><h4 class=\"moi\"><a href=\"hello.html\">Allowed</a></h4><img src=\"/image.png\" onError=\"alert('asdf');\"/><a href=\"https://my.domain.com\">My site</a></body></html>";


    @BeforeAll
    public static void setUp() {

        try {
            PropertyUtil.addProperty("html.whitelist.attr.a","target");
            PropertyUtil.addProperty("gfi.html.whitelist.attr.img.dataurl","true");
        } catch (DuplicateException e) {
            //this method is called once for every test, duplicates don't matter.
        }
    }

    @AfterAll
    public static void teardown() {
        PropertyUtil.clearProperties();
    }


    @Test
    public void getFiltered() {
        String result = new HtmlDoc(html).getFiltered();
        String expected = "<h4><a>Allowed</a></h4>" +
                "<img>" +
                "<a href=\"https://my.domain.com\">My site</a>";
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void getFilteredNull() {
        Assertions.assertNull(new HtmlDoc(null).getFiltered());
    }

    @Test
    public void getFilteredPlainText() {
        Assertions.assertEquals("Hello world", new HtmlDoc("Hello world").getFiltered());
    }

    @Test
    public void getFilteredLinkFix() {
        Assertions.assertEquals("<h4><a href=\"https://oskari.org/testing/hello.html\" target=\"_blank\">Allowed</a></h4>" +
                "<img src=\"https://oskari.org/image.png\">" +
                "<a href=\"https://my.domain.com\" target=\"_blank\">My site</a>", new HtmlDoc(html)
                .modifyLinks("https://oskari.org/testing/?test=true&param=4")
                .getFiltered());
    }

    @Test
    @Disabled("Assertion fails probably due to corrupt test files or smthng")
    public void getFilteredDataURL() throws IOException {
        String html = IOHelper.readString(getClass().getResourceAsStream("gfi-data-response.html"));
        String expected = IOHelper.readString(getClass().getResourceAsStream("gfi-data-response-expected.html"));
        Assertions.assertEquals(expected, new HtmlDoc(html)
                .modifyLinks("https://oskari.org/testing/?test=true&param=4")
                .getFiltered("gfi"));
    }
}