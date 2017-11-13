package fi.nls.oskari.util;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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
        assertEquals("Space characters are replaced by '+' in form encoding", "key1=baz+qux", IOHelper.getParams(params));
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

}
