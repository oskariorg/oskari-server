package fi.nls.oskari.util;


import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 12.6.2014
 * Time: 17:08
 * To change this template use File | Settings | File Templates.
 */
public class IOHelperTest {

    @Test
    public void testConstructUrl() throws Exception {
        String baseUrl = "/testing";
        assertEquals(baseUrl, IOHelper.constructUrl(baseUrl, null), "Null params should return given base URL");

        // NOTE! using LinkedHashMap to ensure params order in result
        Map<String, String> params = new LinkedHashMap<String, String>();
        assertEquals( baseUrl, IOHelper.constructUrl(baseUrl, params), "Empty params should return given base URL");

        params.put("test", "testing");
        assertEquals( baseUrl + "?test=testing", IOHelper.constructUrl(baseUrl, params), "Simple params should return expected URL");

        baseUrl = "/testing?";
        assertEquals( baseUrl + "test=testing", IOHelper.constructUrl(baseUrl, params), "BaseUrl with ending ? should return expected URL");

        baseUrl = "/testing?t=1";
        assertEquals( baseUrl + "&test=testing", IOHelper.constructUrl(baseUrl, params), "BaseUrl having existing queryString should return expected URL");

        baseUrl = "/testing?t=1&";
        assertEquals(baseUrl + "test=testing", IOHelper.constructUrl(baseUrl, params), "BaseUrl with ending & should return expected URL");

        params.put("t2", "3");
        assertEquals( baseUrl + "test=testing&t2=3", IOHelper.constructUrl(baseUrl, params), "Multiple params should return expected URL");

        params.put("t3", "&&&");
        assertEquals( baseUrl + "test=testing&t2=3&t3=%26%26%26", IOHelper.constructUrl(baseUrl, params), "Problematic params should return expected encoded URL");
    }

    @Test
    public void testFixPath() {
        assertEquals( "http://testing/if/path/OK", IOHelper.fixPath("http://testing//if/path//OK"), "Duplicate slashes should be removed");
        assertEquals("https://testing//if/path/OK", IOHelper.fixPath("https://testing///if/path//OK"), "Triple slashes should be duplicate slashes");
    }

    @Test
    public void testGetParams() {
        assertEquals("", IOHelper.getParams(null), "null map should return an empty string");

        Map<String, String> params = new LinkedHashMap<String, String>();
        assertEquals( "", IOHelper.getParams(params), "Empty map should return an empty string");

        params.put("key1", "foo");
        assertEquals("key1=foo", IOHelper.getParams(params), "Key and value should be separated with a '='");

        params.put("key2", "bar");
        assertEquals( "key1=foo&key2=bar", IOHelper.getParams(params), "Values should be separated with a '&'");
        params.remove("key2");

        params.put("key1", "baz+qux");
        assertEquals( "key1=baz%2Bqux", IOHelper.getParams(params), "Values should be URL encoded");
        params.remove("key1");

        params.put("foo+bar", "baz+qux");
        assertEquals( "foo%2Bbar=baz%2Bqux", IOHelper.getParams(params), "Keys should be URL encoded");
        params.remove("foo+bar");

        params.put("key1", "baz qux");
        assertEquals( "key1=baz%20qux", IOHelper.getParams(params), "Space characters are replaced by '%20' in encoding");
    }

    @Test
    public void testGetParamsMultiValue() {
        assertEquals( "", IOHelper.getParamsMultiValue(null), "null map should return an empty string");

        Map<String, String[]> params = new LinkedHashMap<String, String[]>();
        assertEquals("", IOHelper.getParamsMultiValue(params), "Empty map should return an empty string");

        params.put("key1", new String[]{ "foo" });
        assertEquals("key1=foo", IOHelper.getParamsMultiValue(params), "If there's only one key-value pair no '&' follows");

        params.put("key2", new String[]{ "bar" });
        assertEquals( "key1=foo&key2=bar", IOHelper.getParamsMultiValue(params), "Values are be separated with a '&'");

        params.put("key3", new String[]{ "foobar", "baz+qux" });
        assertEquals("key1=foo&key2=bar&key3=foobar&key3=baz%2Bqux", IOHelper.getParamsMultiValue(params), "Values are URL encoded, keys with multiple values appear as multiple ${key}=${value1} entries");
    }

    @Test
    public void testReadString() throws IOException {
        String expected = "foobar;baz;qux;whatIsThat\r\n\nErrorFoo    öäöäöäöäöä";
        byte[] in = expected.getBytes(StandardCharsets.UTF_8);
        String actual = IOHelper.readString(new ByteArrayInputStream(in));
        assertEquals(expected, actual);

        String longString = Collections.nCopies(1000, expected).stream()
                .collect(Collectors.joining("::"));
        in = longString.getBytes(StandardCharsets.UTF_8);
        String actualLongString = IOHelper.readString(new ByteArrayInputStream(in));
        assertEquals(longString, actualLongString);
    }

    @Test
    public void testHumanReadableBytes() {
        long hundredMegsInBytes = 1024*1024*100;
        assertEquals("100,0 MiB", ignoreNumberFormatting(IOHelper.humanReadableByteCount(hundredMegsInBytes)));
    }

    private String ignoreNumberFormatting(String str) {
        return str.replace('.', ',');
    }

    @Test
    public void testCopy() throws IOException {
        byte[] input = new byte[5000];
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOHelper.copy(in, out);
        assertTrue(Arrays.equals(input, out.toByteArray()));
    }

    @Test()
    public void testCopySizeLimit() throws IOException {
        assertThrows(IOException.class, () -> {
            try {
                byte[] input = new byte[5000];
                ByteArrayInputStream in = new ByteArrayInputStream(input);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                IOHelper.copy(in, out, 400);
            }
            catch(IOException e) {
                assertEquals("Size limit reached: 400 B", e.getMessage());
                throw e;
            }
        });
    }
}
