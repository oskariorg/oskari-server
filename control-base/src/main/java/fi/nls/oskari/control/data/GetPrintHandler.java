package fi.nls.oskari.control.data;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.oskari.print.PrintService;
import org.oskari.print.request.PrintFormat;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;
import org.oskari.print.request.PrintTile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.util.ServiceFactory;

@OskariActionRoute("GetPrint")
public class GetPrintHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(GetPrintHandler.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String PARM_COORD = "coord";
    private static final String PARM_RESOLUTION = "resolution";
    private static final String PARM_ZOOMLEVEL = "zoomLevel";
    private static final String PARM_PAGE_SIZE = "pageSize";
    private static final String PARM_MAPLAYERS = "mapLayers";
    private static final String PARM_FORMAT = "format";
    private static final String PARM_SRSNAME = "srs";
    private static final String PARM_TILES = "tiles";

    private static final double MM_PER_INCH = 25.4;
    private static final double OGC_DPI = MM_PER_INCH / 0.28;

    private static final int A4W = 210;
    private static final int A4H = 297;
    private static final int A3W = 297;
    private static final int A3H = 420;

    // 10mm left + 10mm right
    private static final int MARGIN_WIDTH = 10 * 2;
    private static final int MARGIN_HEIGHT = 15 * 2;

    private PermissionHelper permissionHelper;

    public static int mmToPx(int mm) {
        return (int) Math.round((OGC_DPI * mm) / MM_PER_INCH);
    }

    public GetPrintHandler() {
        this(ServiceFactory.getMapLayerService(), ServiceFactory.getPermissionsService());
    }

    public GetPrintHandler(OskariLayerService layerService, PermissionsService permissionService) {
        permissionHelper = new PermissionHelper(
                ServiceFactory.getMapLayerService(),
                ServiceFactory.getPermissionsService());
    }

    public void handleAction(ActionParameters params) throws ActionException {
        PrintRequest pr = createPrintRequest(params);
        PrintService.validate(pr);
        switch (pr.getFormat()) {
        case PDF:
            handlePDF(pr, params);
            break;
        case PNG:
            handlePNG(pr, params);
            break;
        default:
            ResponseHelper.writeError(params, "Invalid format", 400);
        }
    }

    private PrintRequest createPrintRequest(ActionParameters params)
            throws ActionException {
        PrintRequest request = new PrintRequest();

        request.setSrsName(params.getRequiredParam(PARM_SRSNAME));
        request.setZoomLevel(params.getRequiredParamInt(PARM_ZOOMLEVEL));
        request.setResolution(params.getRequiredParamDouble(PARM_RESOLUTION));

        setPagesize(params, request);
        setCoordinates(params.getRequiredParam(PARM_COORD), request);
        setFormat(params.getRequiredParam(PARM_FORMAT), request);

        List<PrintLayer> layers = getLayers(params.getRequiredParam(PARM_MAPLAYERS),
                params.getUser());
        request.setLayers(layers);
        setTiles(params, layers);

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
            throw new ActionParamsException("Unknown pageSize");
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
                throw new ActionParamsException("Could not parse coordinates from " + coord);
            }
        }

        double e = ConversionHelper.getDouble(coord.substring(0, i), Double.NaN);
        double n = ConversionHelper.getDouble(coord.substring(i + 1), Double.NaN);
        if (e == Double.NaN || n == Double.NaN) {
            throw new ActionParamsException("Could not parse coordinates from " + coord);
        }
        req.setEast(e);
        req.setNorth(n);
    }

    private void setFormat(String formatStr, PrintRequest req)
            throws ActionParamsException {
        PrintFormat format = PrintFormat.getByContentType(formatStr);
        if (format == null) {
            throw new ActionParamsException("Invalid value for key '" + PARM_FORMAT + "'");
        }
        req.setFormat(format);
    }

    private List<PrintLayer> getLayers(String mapLayers, User user)
            throws ActionException {
        LayerProperties[] requestedLayers = LayerProperties.parse(mapLayers);

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

    private PrintLayer createPrintLayer(OskariLayer oskariLayer, LayerProperties requestedLayer) {
        int opacity = getOpacity(requestedLayer.getOpacity(), oskariLayer.getOpacity());
        if (opacity <= 0) {
            // Ignore fully transparent layers
            return null;
        }

        PrintLayer layer = new PrintLayer();
        layer.setId(oskariLayer.getExternalId());
        layer.setType(oskariLayer.getType());
        layer.setUrl(oskariLayer.getUrl());
        layer.setVersion(oskariLayer.getVersion());
        layer.setName(oskariLayer.getName());
        layer.setUsername(oskariLayer.getUsername());
        layer.setPassword(oskariLayer.getPassword());
        layer.setOpacity(opacity);
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
        return opacity > 100 ? 100 : opacity;
    }

    private void setTiles(ActionParameters params, List<PrintLayer> layers)
            throws ActionException {
        String tilesJson = params.getHttpParam(PARM_TILES);
        if (tilesJson == null || tilesJson.isEmpty()) {
            return;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(tilesJson);
            if (!root.isArray()) {
                throw new ActionParamsException("Tiles not array!");
            }

            Iterator<String> it = root.fieldNames();
            while (it.hasNext()) {
                String layerId = it.next();
                PrintLayer printLayer = find(layers, layerId);
                if (printLayer == null) {
                    continue;
                }

                JsonNode layerNode = root.get(layerId);
                int n = layerNode.size();
                if (n != 1) {
                    throw new ActionParamsException("Bad tiles! Size not 1");
                }

                JsonNode tilesNode = layerNode.get(0);
                if (!tilesNode.isArray()) {
                    throw new ActionParamsException("Bad tiles! Not array");
                }

                int m = tilesNode.size();
                PrintTile[] tiles = new PrintTile[m];

                for (int j = 0; j < m; j++) {
                    JsonNode tileNode = tilesNode.get(j);

                    JsonNode bboxNode = tileNode.get("bbox");
                    if (bboxNode == null
                            || !bboxNode.isArray()
                            || bboxNode.size() != 4) {
                        throw new ActionParamsException("Bad tiles! "
                                + "Missing 'bbox' or not array or not size 4");
                    }

                    JsonNode urlNode = tileNode.get("url");
                    if (urlNode == null || !urlNode.isTextual()) {
                        throw new ActionParamsException("Bad tiles! "
                                + "Missing 'url' or not text");
                    }
                    double[] bbox = new double[4];
                    for (int k = 0; k < 4; k++) {
                        bbox[k] = bboxNode.get(k).asDouble();
                    }
                    String url = urlNode.asText();

                    tiles[j] = new PrintTile(bbox, url);
                }

                printLayer.setTiles(tiles);
            }
        } catch (IOException e) {
            LOG.warn(e);
            throw new ActionException("Failed to parse tiles");
        }
    }

    private PrintLayer find(final List<PrintLayer> layers, final String layerId) {
        for (PrintLayer layer : layers) {
            if (layerId.equals(layer.getId())) {
                return layer;
            }
        }
        return null;
    }

    private void handlePNG(PrintRequest pr, ActionParameters params) {
        try {
            BufferedImage bi = PrintService.getPNG(pr);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, PrintFormat.PNG.fileExtension, baos);
            ResponseHelper.writeResponse(params, 200, PrintFormat.PNG.contentType, baos.toByteArray());
        } catch (IOException e) {
            LOG.warn(e);
            ResponseHelper.writeError(params);
        }
    }

    private void handlePDF(PrintRequest pr, ActionParameters params) {
        try (PDDocument doc = new PDDocument()) {
            PrintService.getPDF(pr, doc);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            ResponseHelper.writeResponse(params, 200, PrintFormat.PDF.contentType, baos.toByteArray());
        } catch (IOException e) {
            LOG.warn(e);
            ResponseHelper.writeError(params);
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

        public static LayerProperties parse(String[] layerParam)
                throws IllegalArgumentException {
            if (layerParam == null || layerParam.length == 0) {
                throw new IllegalArgumentException(
                        "layerParam must be non-null with positive length!");
            }

            String id = layerParam[0];
            Integer opacity = layerParam.length > 1 ? Integer.valueOf(layerParam[1]) : null;
            String style = layerParam.length > 2 ? layerParam[2] : null;
            return new LayerProperties(id, opacity, style);
        }

        public static LayerProperties[] parse(String mapLayers)
                throws IllegalArgumentException {
            final String[] layers = mapLayers.split(",");
            final int n = layers.length;

            final LayerProperties[] layerProperties = new LayerProperties[n];
            for (int i = 0; i < n; i++) {
                String[] layerParam = layers[i].split(" ");
                layerProperties[i] = parse(layerParam);
            }

            return layerProperties;
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
