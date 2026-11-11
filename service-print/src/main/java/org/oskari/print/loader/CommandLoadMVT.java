package org.oskari.print.loader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.oskari.print.mvt.MVTTile;
import org.oskari.print.mvt.MVTTileGrid;
import org.oskari.print.request.PrintLayer;
import org.oskari.service.mvt.TileCoord;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;
import no.ecc.vectortile.VectorTileDecoder;

/**
 * Loads the Mapbox vector tiles covering the printed area straight from the
 * layer's url.
 */
public class CommandLoadMVT {

    private static final Logger LOG = LogFactory.getLogger(CommandLoadMVT.class);

    private CommandLoadMVT() {}

    /**
     * Starts loading the tiles covering the bbox. The returned future completes
     * once every tile has been loaded; tiles that fail are left out rather than
     * failing the layer.
     */
    public static CompletableFuture<List<MVTTile>> loadTiles(PrintLayer layer, MVTTileGrid tileGrid,
            double[] bbox, int zoom, PrintLoader loader) {
        List<TileCoord> tiles = tileGrid.getTiles(bbox, zoom);
        if (tiles.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        String commandKey = Integer.toString(layer.getId());
        String url = getLayerURL(layer.getOskariLayer());
        List<CompletableFuture<MVTTile>> futures = new ArrayList<>(tiles.size());
        for (TileCoord tile : tiles) {
            String uri = getTileURL(url, tile);
            double[] tileExtent = tileGrid.getTileExtent(tile);
            Supplier<MVTTile> supplier = () -> loadTile(uri, tileExtent,
                    layer.getUsername(), layer.getPassword());
            futures.add(loader.runSupplier(commandKey, supplier, () -> null));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(ignored -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }

    /**
     * The layer's url with its params on the query string, as the frontend
     * requests the tiles. An api key is commonly kept in the params rather
     * than in the url.
     */
    static String getLayerURL(OskariLayer layer) {
        JSONObject params = layer.getParams();
        if (params == null || params.isEmpty()) {
            return layer.getUrl();
        }
        Map<String, String> query = new HashMap<>();
        for (String key : params.keySet()) {
            query.put(key, params.optString(key));
        }
        return IOHelper.constructUrl(layer.getUrl(), query);
    }

    static String getTileURL(String url, TileCoord tile) {
        return url.replace("{z}", Integer.toString(tile.getZ()))
                .replace("{x}", Integer.toString(tile.getX()))
                .replace("{y}", Integer.toString(tile.getY()));
    }

    private static MVTTile loadTile(String uri, double[] tileExtent, String user, String pass) {
        LOG.debug("Loading vector tile from:", uri);
        try {
            HttpURLConnection conn = IOHelper.getConnection(uri, user, pass);
            int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_NO_CONTENT
                    || status == HttpURLConnection.HTTP_NOT_FOUND) {
                // Tile services commonly answer this for an area without data
                return null;
            }
            if (status != HttpURLConnection.HTTP_OK) {
                LOG.warn("Got status", status, "for vector tile:", uri);
                throw new ServiceRuntimeException("Unexpected status: " + status);
            }
            byte[] encoded = IOHelper.readBytes(conn);
            if (encoded.length == 0) {
                return null;
            }
            VectorTileDecoder decoder = new VectorTileDecoder();
            // Keep the tile local coordinates, they are transformed with the tile extent
            decoder.setAutoScale(false);
            return new MVTTile(tileExtent, decoder.decode(encoded).asList());
        } catch (IOException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }
}
