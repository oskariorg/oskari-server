package fi.nls.oskari.control.data;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.print.PrintService;
import org.oskari.print.request.PrintFormat;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;
import org.oskari.print.request.PrintTile;
import org.oskari.service.util.ServiceFactory;

import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.layer.PermissionHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("GetPrint")
public class GetPrintHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(GetPrintHandler.class);

    private static final String PARM_COORD = "coord";
    private static final String PARM_RESOLUTION = "resolution";
    private static final String PARM_PAGE_SIZE = "pageSize";
    private static final String PARM_MAPLAYERS = "mapLayers";
    private static final String PARM_FORMAT = "format";
    private static final String PARM_SRSNAME = "srs";
    private static final String PARM_TILES = "tiles";
    private static final String PARM_TITLE = "pageTitle";
    private static final String PARM_SCALE = "pageScale";
    private static final String PARM_LOGO = "pageLogo";
    private static final String PARM_DATE = "pageDate";

    private static final String ALLOWED_FORMATS = Arrays.toString(new String[] {
            PrintFormat.PDF.contentType, PrintFormat.PNG.contentType
    });

    private static final String ALLOWED_PAGE_SIZES = Arrays.toString(new String[] {
            "A4", "A4_Landscape", "A3", "A3_Landscape"
    });

    private static final double MM_PER_INCH = 25.4;
    private static final double OGC_DPI = MM_PER_INCH / 0.28;

    private static final int A4W = 210;
    private static final int A4H = 297;
    private static final int A3W = 297;
    private static final int A3H = 420;

    // 10mm left + 10mm right
    private static final int MARGIN_WIDTH = 10 * 2;
    private static final int MARGIN_HEIGHT = 15 * 2;

    private final PermissionHelper permissionHelper;
    private final PrintService printService;

    public static int mmToPx(int mm) {
        return (int) Math.round((OGC_DPI * mm) / MM_PER_INCH);
    }

    public GetPrintHandler() {
        this(ServiceFactory.getMapLayerService(),
                ServiceFactory.getPermissionsService(),
                new PrintService());
    }

    public GetPrintHandler(OskariLayerService layerService,
            PermissionsService permissionService, PrintService printService) {
        this.permissionHelper = new PermissionHelper(layerService, permissionService);
        this.printService = printService;
    }

    public void handleAction(ActionParameters params) throws ActionException {
        PrintRequest pr = createPrintRequest(params);
        switch (pr.getFormat()) {
        case PDF:
            handlePDF(pr, params);
            break;
        case PNG:
            handlePNG(pr, params);
            break;
        default:
            throw new ActionParamsException(String.format(
                    "Invalid value for key '%s'. Allowed values are: %s",
                    PARM_FORMAT, ALLOWED_FORMATS));
        }
    }

    private PrintRequest createPrintRequest(ActionParameters params)
            throws ActionException {
        PrintRequest request = new PrintRequest();

        request.setSrsName(params.getRequiredParam(PARM_SRSNAME));
        request.setResolution(params.getRequiredParamDouble(PARM_RESOLUTION));
        request.setTitle(params.getHttpParam(PARM_TITLE));
        request.setShowLogo(params.getHttpParam(PARM_LOGO, false));
        request.setShowScale(params.getHttpParam(PARM_SCALE, false));
        request.setShowDate(params.getHttpParam(PARM_DATE, false));

        setPagesize(params, request);
        setCoordinates(params.getRequiredParam(PARM_COORD), request);
        setFormat(params.getRequiredParam(PARM_FORMAT), request);

        List<PrintLayer> layers = getLayers(params.getRequiredParam(PARM_MAPLAYERS),
                params.getUser());
        request.setLayers(layers);
        setTiles(layers, params.getHttpParam(PARM_TILES));

        return request;
    }

    private void setPagesize(ActionParameters params, PrintRequest request)
            throws ActionParamsException {
        String pageSizeStr = params.getRequiredParam(PARM_PAGE_SIZE);

        int width;
        int height;

        switch (pageSizeStr) {
        case "A4":
            width = A4W;
            height = A4H;
            break;
        case "A4_Landscape":
            width = A4H;
            height = A4W;
            break;
        case "A3":
            width = A3W;
            height = A3H;
            break;
        case "A3_Landscape":
            width = A3H;
            height = A3W;
            break;
        default:
            throw new ActionParamsException(String.format(
                    "Invalid value for key '%s'. Allowed values are: %s",
                    PARM_PAGE_SIZE, ALLOWED_PAGE_SIZES));
        }

        width -= MARGIN_WIDTH;
        height -= MARGIN_HEIGHT;

        width = mmToPx(width);
        height = mmToPx(height);

        request.setWidth(width);
        request.setHeight(height);
    }

    private void setCoordinates(String coord, PrintRequest req)
            throws ActionParamsException {
        int i = coord.indexOf('_');
        if (i < 0) {
            i = coord.indexOf(' ');
            if (i < 0) {
                throw new ActionParamsException(String.format(
                        "Invalid value for key '%s'. Could not parse coordinates from '%s'",
                        PARM_COORD, coord));
            }
        }

        double e = ConversionHelper.getDouble(coord.substring(0, i), Double.NaN);
        double n = ConversionHelper.getDouble(coord.substring(i + 1), Double.NaN);
        if (e == Double.NaN || n == Double.NaN) {
            throw new ActionParamsException(String.format(
                    "Invalid value for key '%s'. Could not parse coordinates from '%s'",
                    PARM_COORD, coord));
        }
        req.setEast(e);
        req.setNorth(n);
    }

    private void setFormat(String formatStr, PrintRequest req)
            throws ActionParamsException {
        PrintFormat format = PrintFormat.getByContentType(formatStr);
        if (format == null) {
            throw new ActionParamsException(String.format(
                    "Invalid value for key '%s'. Allowed values are: %s", 
                    PARM_FORMAT, ALLOWED_FORMATS));
        }
        req.setFormat(format);
    }

    private List<PrintLayer> getLayers(String mapLayers, User user)
            throws ActionException {
        LayerProperties[] requestedLayers = parseLayersProperties(mapLayers);

        List<PrintLayer> printLayers = new ArrayList<>(requestedLayers.length);
        for (LayerProperties requestedLayer : requestedLayers) {
            OskariLayer oskariLayer = permissionHelper.getLayer(requestedLayer.getId(), user);
            PrintLayer printLayer = createPrintLayer(oskariLayer, requestedLayer);
            if (printLayer != null) {
                printLayers.add(printLayer);
            }
        }

        return printLayers;
    }

    private LayerProperties[] parseLayersProperties(String layerParams)
            throws ActionParamsException {
        final String[] layers = layerParams.split(",");
        final int n = layers.length;
        final LayerProperties[] layerProperties = new LayerProperties[n];
        for (int i = 0; i < n; i++) {
            layerProperties[i] = parseLayerProperties(layers[i]);
        }
        return layerProperties;
    }

    private LayerProperties parseLayerProperties(String layerParam)
            throws ActionParamsException {
        if (layerParam == null || layerParam.isEmpty()) {
            throw new ActionParamsException(String.format(
                    "Invalid value for key '%s'", PARM_MAPLAYERS));
        }
        final String[] parts = layerParam.split(" ");
        final String id = parts[0];
        final Integer opacity = parts.length > 1 ? Integer.valueOf(parts[1]) : null;
        final String style = parts.length > 2 ? parts[2] : null;
        return new LayerProperties(id, opacity, style);
    }

    private PrintLayer createPrintLayer(OskariLayer oskariLayer, LayerProperties requestedLayer) {
        int opacity = getOpacity(requestedLayer.getOpacity(), oskariLayer.getOpacity());
        if (opacity <= 0) {
            // Ignore fully transparent layers
            return null;
        }

        PrintLayer layer = new PrintLayer();
        layer.setId(oskariLayer.getId());
        layer.setType(oskariLayer.getType());
        layer.setUrl(oskariLayer.getUrl());
        layer.setVersion(oskariLayer.getVersion());
        layer.setName(oskariLayer.getName());
        layer.setUsername(oskariLayer.getUsername());
        layer.setPassword(oskariLayer.getPassword());
        layer.setOpacity(opacity);
        if (requestedLayer.getStyle() != null && !requestedLayer.getStyle().isEmpty()) {
            layer.setStyle(requestedLayer.getStyle());
        }
        return layer;
    }

    private int getOpacity(Integer requestedOpacity, Integer layersDefaultOpacity) {
        int opacity;
        if (requestedOpacity != null) {
            // If the opacity is set in the request use that
            opacity = requestedOpacity.intValue();
        } else if (layersDefaultOpacity != null) {
            // Otherwise use layers default opacity
            opacity = layersDefaultOpacity.intValue();
        } else {
            // If that's missing just use 100 (full opacity)
            opacity = 100;
        }
        return Math.min(opacity, 100);
    }

    private void setTiles(List<PrintLayer> layers, String tilesJson)
            throws ActionException {
        if (tilesJson == null || tilesJson.isEmpty()) {
            return;
        }
        try {
            JSONArray tiles = new JSONArray(tilesJson);
            for (int i = 0; i < tiles.length(); i++) {
                JSONObject layersTiles = tiles.getJSONObject(i);
                setTiles(layers, layersTiles);
            }
        } catch (JSONException e) {
            throw new ActionParamsException(String.format(
                    "Invalid value for key '%s'", PARM_TILES), e);
        }
    }

    private void setTiles(List<PrintLayer> layers, JSONObject layersTiles)
            throws JSONException, ActionParamsException {
        String key = (String) layersTiles.keys().next();
        int layerId = ConversionHelper.getInt(key, -1);
        if (layerId == -1) {
            LOG.info("Could not parse integer layerId from:", key);
            return;
        }

        final PrintLayer layer = findById(layers, layerId);
        if (layer == null) {
            LOG.info("Could not find layerId:", layerId);
            return;
        }

        final JSONArray tilesArray = layersTiles.getJSONArray(key);
        layer.setTiles(parseTiles(tilesArray));
    }

    private PrintTile[] parseTiles(JSONArray tilesArray) throws JSONException, ActionParamsException {
        final int n = tilesArray.length();
        final PrintTile[] tiles = new PrintTile[n];
        for (int i = 0; i < n; i++) {
            JSONObject tile = tilesArray.getJSONObject(i);
            JSONArray bboxArray = tile.getJSONArray("bbox");
            double[] bbox = toDoubleArray(bboxArray);
            String url = tile.getString("url");
            tiles[i] = new PrintTile(bbox, url);
        }
        return tiles;
    }

    private PrintLayer findById(List<PrintLayer> layers, int id) {
        for (PrintLayer l : layers) {
            if (l.getId() == id) {
                return l;
            }
        }
        return null;
    }

    private double[] toDoubleArray(JSONArray array) throws JSONException {
        final int n = array.length();
        final double[] arr = new double[n];
        for (int i = 0; i < n; i++) {
            arr[i] = array.getDouble(i);
        }
        return arr;
    }

    private void handlePNG(PrintRequest pr, ActionParameters params) throws ActionException {
        try {
            BufferedImage bi = printService.getPNG(pr);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, PrintFormat.PNG.fileExtension, baos);
            ResponseHelper.writeResponse(params, 200, PrintFormat.PNG.contentType, baos);
        } catch (IOException | ServiceException e) {
            throw new ActionException("Failed to create PNG", e);
        }
    }

    private void handlePDF(PrintRequest pr, ActionParameters params) throws ActionException {
        try (PDDocument doc = new PDDocument()) {
            printService.getPDF(pr, doc);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            ResponseHelper.writeResponse(params, 200, PrintFormat.PDF.contentType, baos);
        } catch (IOException | ServiceException e) {
            throw new ActionException("Failed to create PDF", e);
        }
    }

    public static class LayerProperties {

        private final String id;
        private final Integer opacity;
        private final String style;

        public LayerProperties(String id, Integer opacity, String style) {
            this.id = id;
            this.opacity = opacity;
            this.style = style;
        }

        public String getId() {
            return id;
        }

        public Integer getOpacity() {
            return opacity;
        }

        public String getStyle() {
            return style;
        }

    }

}
