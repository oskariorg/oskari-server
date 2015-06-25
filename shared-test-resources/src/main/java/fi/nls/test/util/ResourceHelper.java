package fi.nls.test.util;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

/**
 * @author SMAKINEN
 */
public class ResourceHelper {

    private static final String DEFAULT_CHARSET = "UTF-8";
    /**
     * Uses ResourceHelper.class to load resource. Usable for resources in the shared-test-resources package.
     * @param resourceName
     * @return
     */
    public static String readStringResource(final String resourceName) {
        return readStringResource(resourceName, ResourceHelper.class);
    }
    /**
     * Uses the Class of given object to load resource. Usable for resources outside shared-test-resources package.
     * @param resourceName
     * @return
     */
    public static String readStringResource(final String resourceName, final Object loaderObject) {
        return readStringResource(resourceName, loaderObject.getClass());
    }

    /**
     * Uses the given Class to load resource. Usable for resources outside shared-test-resources package.
     * @param resourceName
     * @return
     */
    public static String readStringResource(final String resourceName, final Class loader) {
        try {
            final String resource = readString(loader.getResourceAsStream(resourceName));
            return resource;
        }
        catch (IOException ex) {
            //fail("Couldn't read resource " + resourceName);
        }
        return null;
    }

    /**
     * Removes white space including tabs and new lines. The problem with comparing JSON as strings with this
     * is that white spaces are removed inside the JSON as well.
     * @param resource
     * @return
     */
    public static String removeWhiteSpace(final String resource) {
        return resource.replaceAll("\\s", "");
    }

    /**
     * Reads the resource and returns as
     * @param resourceName
     * @param loaderObject
     * @return
     */
    public static JSONArray readJSONArrayResource(final String resourceName, final Object loaderObject) {
        return readJSONArrayResource(resourceName, loaderObject.getClass());
    }

    public static JSONArray readJSONArrayResource(final String resourceName, final Class loader) {
        try {
            final String resource = readString(loader.getResourceAsStream(resourceName));
            return JSONHelper.createJSONArray(resource);
        }
        catch (IOException ex) {
            //fail("Couldn't read resource " + resourceName);
        }
        return null;
    }

    /**
     * Reads the resource and returns as
     * @param resourceName
     * @param loaderObject
     * @return
     */
    public static JSONObject readJSONResource(final String resourceName, final Object loaderObject) {
        return readJSONResource(resourceName, loaderObject.getClass());
    }

    public static JSONObject readJSONResource(final String resourceName, final Class loader) {
        try {
            final String resource = readString(loader.getResourceAsStream(resourceName));
            return JSONHelper.createJSONObject(resource);
        }
        catch (IOException ex) {
            //fail("Couldn't read resource " + resourceName);
        }
        return null;
    }

    /**
     * Reads the given input stream and converts its contents to a string using #DEFAULT_CHARSET
     * @param is
     * @return
     * @throws IOException
     */
    private static String readString(InputStream is) throws IOException {
        return readString(is, DEFAULT_CHARSET);
    }

    /**
     * Reads the given input stream and converts its contents to a string using given charset
     * @param is
     * @param charset
     * @return
     * @throws IOException
     */
    private static String readString(InputStream is, final String charset)
            throws IOException {
        /*
         * To convert the InputStream to String we use the Reader.read(char[]
         * buffer) method. We iterate until the Reader return -1 which means
         * there's no more data to read. We use the StringWriter class to
         * produce the string.
         */
        if (is == null) {
            return "";
        }
        final Writer writer = new StringWriter();
        final char[] buffer = new char[1024];
        try {
            final Reader reader = new BufferedReader(new InputStreamReader(is,
                    charset));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        return writer.toString();
    }
}
