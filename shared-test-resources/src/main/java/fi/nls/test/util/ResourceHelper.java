package fi.nls.test.util;

import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * @author SMAKINEN
 */
public class ResourceHelper {

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
            final String resource = IOHelper.readString(loader.getResourceAsStream(resourceName));
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
            final String resource = IOHelper.readString(loader.getResourceAsStream(resourceName));
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
            final String resource = IOHelper.readString(loader.getResourceAsStream(resourceName));
            return JSONHelper.createJSONObject(resource);
        }
        catch (IOException ex) {
            //fail("Couldn't read resource " + resourceName);
        }
        return null;
    }
}
