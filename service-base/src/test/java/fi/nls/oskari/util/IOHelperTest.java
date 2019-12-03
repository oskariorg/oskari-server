package fi.nls.oskari.util;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

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
        assertEquals("Null params should return given base URL", baseUrl, IOHelper.constructUrl(baseUrl, null));

        // NOTE! using LinkedHashMap to ensure params order in result
        Map<String, String> params = new LinkedHashMap<String, String>();
        assertEquals("Empty params should return given base URL", baseUrl, IOHelper.constructUrl(baseUrl, params));

        params.put("test", "testing");
        assertEquals("Simple params should return expected URL", baseUrl + "?test=testing", IOHelper.constructUrl(baseUrl, params));

        baseUrl = "/testing?";
        assertEquals("BaseUrl with ending ? should return expected URL", baseUrl + "test=testing", IOHelper.constructUrl(baseUrl, params));

        baseUrl = "/testing?t=1";
        assertEquals("BaseUrl having existing queryString should return expected URL", baseUrl + "&test=testing", IOHelper.constructUrl(baseUrl, params));

        baseUrl = "/testing?t=1&";
        assertEquals("BaseUrl with ending & should return expected URL", baseUrl + "test=testing", IOHelper.constructUrl(baseUrl, params));

        params.put("t2", "3");
        assertEquals("Multiple params should return expected URL", baseUrl + "test=testing&t2=3", IOHelper.constructUrl(baseUrl, params));

        params.put("t3", "&&&");
        assertEquals("Problematic params should return expected encoded URL", baseUrl + "test=testing&t2=3&t3=%26%26%26", IOHelper.constructUrl(baseUrl, params));
    }

    @Test
    public void testFixPath() {
        assertEquals("Duplicate slashes should be removed", "http://testing/if/path/OK", IOHelper.fixPath("http://testing//if/path//OK"));
        assertEquals("Triple slashes should be duplicate slashes", "https://testing//if/path/OK", IOHelper.fixPath("https://testing///if/path//OK"));
    }

    @Test
    public void testGetParams() {
        assertEquals("null map should return an empty string", "", IOHelper.getParams(null));

        Map<String, String> params = new LinkedHashMap<String, String>();
        assertEquals("Empty map should return an empty string", "", IOHelper.getParams(params));

        params.put("key1", "foo");
        assertEquals("Key and value should be separated with a '='", "key1=foo", IOHelper.getParams(params));

        params.put("key2", "bar");
        assertEquals("Values should be separated with a '&'", "key1=foo&key2=bar", IOHelper.getParams(params));
        params.remove("key2");

        params.put("key1", "baz+qux");
        assertEquals("Values should be URL encoded", "key1=baz%2Bqux", IOHelper.getParams(params));
        params.remove("key1");

        params.put("foo+bar", "baz+qux");
        assertEquals("Keys should be URL encoded", "foo%2Bbar=baz%2Bqux", IOHelper.getParams(params));
        params.remove("foo+bar");

        params.put("key1", "baz qux");
        assertEquals("Space characters are replaced by '%20' in encoding", "key1=baz%20qux", IOHelper.getParams(params));
    }

    @Test
    public void testGetParamsMultiValue() {
        assertEquals("null map should return an empty string", "", IOHelper.getParamsMultiValue(null));

        Map<String, String[]> params = new LinkedHashMap<String, String[]>();
        assertEquals("Empty map should return an empty string", "", IOHelper.getParamsMultiValue(params));

        params.put("key1", new String[]{ "foo" });
        assertEquals("If there's only one key-value pair no '&' follows", "key1=foo", IOHelper.getParamsMultiValue(params));

        params.put("key2", new String[]{ "bar" });
        assertEquals("Values are be separated with a '&'", "key1=foo&key2=bar", IOHelper.getParamsMultiValue(params));

        params.put("key3", new String[]{ "foobar", "baz+qux" });
        assertEquals("Values are URL encoded, keys with multiple values appear as multiple ${key}=${value1} entries",
                "key1=foo&key2=bar&key3=foobar&key3=baz%2Bqux", IOHelper.getParamsMultiValue(params));
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

    @Test(expected = IOException.class)
    public void testCopySizeLimit() throws IOException {
        try {
            byte[] input = new byte[5000];
            ByteArrayInputStream in = new ByteArrayInputStream(input);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOHelper.copy(in, out, 400);
            fail("Should have thrown IOException but did not!");
        }
        catch(IOException e) {
            assertEquals("Size limit reached: 400 B", e.getMessage());
            throw e;
        }
    }
}
