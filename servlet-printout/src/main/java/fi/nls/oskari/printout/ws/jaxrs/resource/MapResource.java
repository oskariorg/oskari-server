package fi.nls.oskari.printout.ws.jaxrs.resource;

import fi.nls.oskari.printout.config.ConfigValue;
import fi.nls.oskari.printout.ws.jaxrs.format.StreamingJSONImpl;
import fi.nls.oskari.printout.ws.jaxrs.format.StreamingPDFImpl;
import fi.nls.oskari.printout.ws.jaxrs.map.SharedMapProducerResource;
import fi.nls.oskari.printout.ws.jaxrs.map.WebServiceMapProducerResource;
import org.geowebcache.GeoWebCacheException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * JAX-RS API for printout Methods of this class provide access either via GET
 * URL parameter based requests or POST JSON requests.
 * 
 * API paths are combined based on @Path annotations from below.
 * 
 * see. https://jersey.java.net/documentation/latest/user-guide.html
 * https://jersey.java.net/documentation/1.7/user-guide.html
 * 
 * 
 * @todo fix parameter handling
 * 
 */
@Path("/imaging")
public class MapResource {
    public static final String[] MAPLINKARGS = new String[] { "zoomLevel",
            "coord", "mapLayers", "width", "height", "scaledWidth",
            "scaledHeight", "bbox", "pageSize", "pageTitle", "pageLogo",
            "pageDate", "pageScale", "pageLegend", "pageCopyleft",
            "pageTemplate", "pageMapRect" };

    @HeaderParam("X-Forwarded-For")
    private String xForwardedFor;

    @HeaderParam("Forwarded-For")
    private String forwardedFor;

    @HeaderParam("Cookie")
    private String Cookie;

    protected Map<String, String> getParameterMap(
            final MultivaluedMap<String, String> queryParams) {
        Map<String, String> values = new HashMap<String, String>();

        for (String mapLinkArg : MAPLINKARGS) {
            String upper = new String(mapLinkArg).toUpperCase();
            if (queryParams.get(mapLinkArg) == null) {
                continue;
            }
            values.put(upper, queryParams.get(mapLinkArg).get(0));
        }

        return values;
    }

    /**
     * Input: URL parameters as de-facto maplink with extra parameters for
     * printing: pageSize=A4|A4_Landscape|A3|A3_Landscape
     * 
     * Output: This outputs GeoJSON which describes printout extent. Extent is
     * calculized based on parameters.
     * 
     */
    @GET
    @Path("service/thumbnail/extent.jsonp")
    @Produces("application/json")
    public StreamingJSONImpl getSnapshotExtentJson(@Context UriInfo ui)
            throws IOException {
        StreamingJSONImpl result = null;
        try {
            final MultivaluedMap<String, String> queryParams = ui
                    .getQueryParameters();
            final Map<String, String> values = getParameterMap(queryParams);

            final WebServiceMapProducerResource getmap = SharedMapProducerResource
                    .acquire();

            result = getmap.getMapExtentJSON(values,
                    getXClientInfo(getmap.getProps()));
        } catch (Exception e) {
            throw new IOException(e);
        }
        return result;

    }

    /**
     * Input: URL parameters as de-facto maplink with extra parameters for
     * printing: pageSize=A4|A4_Landscape|A3|A3_Landscape
     * 
     * Output: This outputs PDF document with map layer images embedded as PDF
     * optional content layers.
     */
    @GET
    @Path("service/thumbnail/maplink.pdf")
    @Produces("application/pdf")
    public StreamingOutput getSnapshotPDF(@Context UriInfo ui)
            throws IOException {

        StreamingPDFImpl result = null;
        try {
            MultivaluedMap<String, String> queryParams = ui
                    .getQueryParameters();
            Map<String, String> values = getParameterMap(queryParams);

            final WebServiceMapProducerResource getmap = SharedMapProducerResource
                    .acquire();

            result = getmap
                    .getMapPDF(values, getXClientInfo(getmap.getProps()));
        } catch (Exception e) {
            throw new IOException(e);
        } finally {

        }

        return result;

    }

    /**
     * 
     * Input: Input is a JSON document describing contents of the map document
     * to be generated
     * 
     * Output: This outputs PDF document wiht embedded map images as PDF
     * optional content layers. Input
     */
    @POST
    @Path("service/thumbnail/maplinkgeojson.pdf")
    @Consumes("application/json")
    @Produces("application/pdf")
    public StreamingOutput getSnapshotPDFByActionRouteGeoJson(InputStream inp)
            throws IOException {

        StreamingOutput result = null;
        try {
            final WebServiceMapProducerResource getmap = SharedMapProducerResource
                    .acquire();
            result = getmap.getGeoJsonMapPDF(inp,
                    getXClientInfo(getmap.getProps()));
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            inp.close();
        }

        return result;

    }

    /**
     * 
     * Input: Input is a JSON document describing contents of the map document
     * to be generated
     * 
     * Output: This outputs PDF document with embedded map images as PDF
     * optional content layers. Input
     * 
     */
    @POST
    @Path("service/thumbnail/maplinkjson.pdf")
    @Consumes("application/json")
    @Produces("application/pdf")
    public StreamingOutput getSnapshotPDFByActionRouteJson(InputStream inp)
            throws IOException {

        StreamingOutput result = null;
        try {
            final WebServiceMapProducerResource getmap = SharedMapProducerResource
                    .acquire();
            result = getmap.getMapPDF(inp, getXClientInfo(getmap.getProps()));
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            inp.close();
        }

        return result;

    }

    /**
     * 
     * Input: Input is a JSON document describing contents of the map document
     * to be generated
     * 
     * Output: This outputs PNG image with anchored down map image layers
     * combined with opacities to resulting PNG image.
     * 
     */
    @GET
    @Path("service/thumbnail/maplink.png")
    @Produces("image/png")
    public StreamingOutput getSnapshotPNG(@Context UriInfo ui)
            throws IOException {

        StreamingOutput result = null;
        try {
            final MultivaluedMap<String, String> queryParams = ui
                    .getQueryParameters();
            final Map<String, String> values = new HashMap<String, String>();

            for (String mapLinkArg : MAPLINKARGS) {
                String upper = new String(mapLinkArg).toUpperCase();
                if (queryParams.get(mapLinkArg) == null) {
                    continue;
                }
                values.put(upper, queryParams.get(mapLinkArg).get(0));
            }

            final WebServiceMapProducerResource getmap = SharedMapProducerResource
                    .acquire();

            result = getmap
                    .getMapPNG(values, getXClientInfo(getmap.getProps()));
        } catch (Exception e) {
            throw new IOException(e);
        }
        return result;

    }

    /**
     * 
     * Input: Input is a JSON document describing contents of the map document
     * to be generated This endpoint rasterises embedded GeoJSON a
     * 
     * Output: This outputs PNG image with anchored down map image layerss
     * combined with opacities to resulting PNG image.
     */
    @POST
    @Path("service/thumbnail/maplinkgeojson.png")
    @Consumes("application/json")
    @Produces("image/png")
    public StreamingOutput getSnapshotPNGByActionRouteGeoJson(InputStream inp)
            throws IOException {

        StreamingOutput result = null;
        try {
            final WebServiceMapProducerResource getmap = SharedMapProducerResource
                    .acquire();
            result = getmap.getGeoJsonMapPNG(inp,
                    getXClientInfo(getmap.getProps()));
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            inp.close();
        }

        return result;
    }

    /**
     * 
     * Input: Input is a JSON document describing contents of the map document
     * to be generated
     * 
     * Output: This outputs PNG image with anchored down map image layers
     * combined with opacities to resulting PNG image.
     * 
     */
    @POST
    @Path("service/thumbnail/maplinkjson.png")
    @Consumes("application/json")
    @Produces("image/png")
    public StreamingOutput getSnapshotPNGByActionRouteJson(InputStream inp)
            throws IOException {

        StreamingOutput result = null;
        try {
            final WebServiceMapProducerResource getmap = SharedMapProducerResource
                    .acquire();

            result = getmap.getMapPNG(inp, getXClientInfo(getmap.getProps()));
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            inp.close();
        }

        return result;
    };
    
    protected Map<String, String> getXClientInfo(final Properties props) {

        final Map<String, String> xClientInfo = new HashMap<String, String>();

        final String userAgent = ConfigValue.MAPPRODUCER_USERAGENT
                .getConfigProperty(props,
                        "Mozilla/5.0 (Windows NT 6.1) oskari.org/printout");
        final String referer = ConfigValue.MAPPRODUCER_REFERER
                .getConfigProperty(props);

        xClientInfo.put("User-Agent", userAgent);
        if (referer != null) {
            xClientInfo.put("Referer", referer);
        }
        xClientInfo.put("Cookie", Cookie);

        if (forwardedFor != null) {
            xClientInfo.put("X-MapTile-Forwarded-For", forwardedFor);
        }
        if (xForwardedFor != null) {
            xClientInfo.put("X-MapTile-X-Forwarded-For", forwardedFor);
        }
        return xClientInfo;
    }

    /**
     * endpoint to support reloading global layers list used when processing GET
     * requests
     */
    @GET
    @Path("service/layers/reload")
    @Produces("application/json")
    public String reloadLayers() throws NoSuchAuthorityCodeException,
            IOException, GeoWebCacheException, FactoryException,
            com.vividsolutions.jts.io.ParseException {

        WebServiceMapProducerResource shared = SharedMapProducerResource
                .acquire();

        if (shared != null) {
            shared.setLayersDirty(true);
        }

        shared = SharedMapProducerResource.acquire();

        if (shared != null && shared.getLayerJson() != null) {
            return "{ \"result\" : true, \"url\" : \""
                    + shared.getLayerJSONurl().toString() + "\" }";
        } else {
            return "{ \"result\" : false }";
        }
    }

}
