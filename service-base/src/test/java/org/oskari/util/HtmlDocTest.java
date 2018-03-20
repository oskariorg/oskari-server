package org.oskari.util;

import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.jsoup.Jsoup;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class HtmlDocTest {

    final String html = "<html><head><title>Document title</title><style></style><script>alert('illegal!')</script></head>" +
            "<body><h4 class=\"moi\"><a href=\"hello.html\">Allowed</a></h4><img src=\"/image.png\" onError=\"alert('asdf');\"/><a href=\"https://my.domain.com\">My site</a></body></html>";


    @BeforeClass
    public static void setUp() {

        try {
            PropertyUtil.addProperty("html.whitelist.attr.a","target");
            PropertyUtil.addProperty("gfi.html.whitelist.attr.img.dataurl","true");
        } catch (DuplicateException e) {
            //this method is called once for every test, duplicates don't matter.
        }
    }

    @AfterClass
    public static void teardown() {
        PropertyUtil.clearProperties();
    }


    @Test
    public void getFiltered() {
        assertEquals("<h4><a>Allowed</a></h4>\n" +
                "<img>\n" +
                "<a href=\"https://my.domain.com\">My site</a>", new HtmlDoc(html).getFiltered());
    }

    @Test
    public void getFilteredNull() {
        assertNull(new HtmlDoc(null).getFiltered());
    }

    @Test
    public void getFilteredPlainText() {
        assertEquals("Hello world", new HtmlDoc("Hello world").getFiltered());
    }

    @Test
    public void getFilteredLinkFix() {
        assertEquals("<h4><a href=\"https://oskari.org/testing/hello.html\" target=\"_blank\">Allowed</a></h4>\n" +
                "<img src=\"https://oskari.org/image.png\">\n" +
                "<a href=\"https://my.domain.com\" target=\"_blank\">My site</a>", new HtmlDoc(html)
                .modifyLinks("https://oskari.org/testing/?test=true&param=4")
                .getFiltered());
    }

    @Test
    @Ignore
    public void getFilteredDataURL() throws IOException {
        String html = IOHelper.readString(getClass().getResourceAsStream("gfi-data-response.html"));
        String expected = IOHelper.readString(getClass().getResourceAsStream("gfi-data-response-expected.html"));
        assertEquals(expected, new HtmlDoc(html)
                .modifyLinks("https://oskari.org/testing/?test=true&param=4")
                .getFiltered("gfi"));
    }
}