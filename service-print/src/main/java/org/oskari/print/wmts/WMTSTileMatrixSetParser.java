package org.oskari.print.wmts;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.oskari.util.DOMHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.wmts.domain.TileMatrix;
import fi.nls.oskari.wmts.domain.TileMatrixSet;

/**
 * Parses TileMatrixSet information from WMTSCapabilities.xml
 * @see http://schemas.opengis.net/wmts/1.0/wmtsGetCapabilities_response.xsd
 */
public class WMTSTileMatrixSetParser {

    private static final Logger LOG = LogFactory.getLogger(WMTSTileMatrixSetParser.class);

    public static List<TileMatrixSet> parse(String uri) {
        try (InputStream in = new URL(uri).openStream()) {
            return parse(in);
        } catch (Exception e) {
            LOG.warn(e, "Failed to parse TileMatrixSet information from {}", uri);
            return null;
        }
    }

    public static List<TileMatrixSet> parse(InputStream in) 
            throws ParserConfigurationException, SAXException, IOException {
        // The result of this function should be cached
        // therefore it's not worth the hassle to cache
        // the DocumentBuilderFactory or the DocumentBuilders
        // (they aren't threadsafe) 
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(in);

        List<TileMatrixSet> tileMatrixSets = new ArrayList<>();
        Element contents = DOMHelper.getFirst(doc.getDocumentElement(), "Contents");
        for (Element tileMatrixSet : DOMHelper.getAll(contents, "TileMatrixSet")) {
            tileMatrixSets.add(parseTileMatrixSet(tileMatrixSet));
        }
        return tileMatrixSets;
    }

    private static TileMatrixSet parseTileMatrixSet(Element tileMatrixSet) {
        TileMatrixSet set = new TileMatrixSet();
        String id = DOMHelper.getFirst(tileMatrixSet, "Identifier").getTextContent();
        String supportedCRS = DOMHelper.getFirst(tileMatrixSet, "SupportedCRS").getTextContent();
        set.setId(id);
        set.setCrs(supportedCRS);
        for (Element tileMatrix : DOMHelper.getAll(tileMatrixSet, "TileMatrix")) {
            set.addTileMatrix(parseTileMatrix(tileMatrix));
        }
        return set;
    }

    private static TileMatrix parseTileMatrix(Element eTileMatrix) {
        String id = DOMHelper.getFirst(eTileMatrix, "Identifier").getTextContent();
        double scaleDenominator = DOMHelper.getDouble(
                DOMHelper.getFirst(eTileMatrix, "ScaleDenominator"), 0.0);
        String topLeftCorner = DOMHelper.getFirst(eTileMatrix, "TopLeftCorner").getTextContent();
        int tileWidth = DOMHelper.getInt(DOMHelper.getFirst(eTileMatrix, "TileWidth"), 256);
        int tileHeight = DOMHelper.getInt(DOMHelper.getFirst(eTileMatrix, "TileHeight"), 256);
        int matrixWidth = DOMHelper.getInt(DOMHelper.getFirst(eTileMatrix, "MatrixWidth"), 0);
        int matrixHeight = DOMHelper.getInt(DOMHelper.getFirst(eTileMatrix, "MatrixHeight"), 0);

        int i = topLeftCorner.indexOf(' ');
        if (i < 0) {
            return null;
        }
        double topLeftCorner1 = ConversionHelper.getDouble(topLeftCorner.substring(0, i), -1);
        double topLeftCorner2 = ConversionHelper.getDouble(topLeftCorner.substring(i + 1), -1);

        TileMatrix tileMatrix = new TileMatrix();
        tileMatrix.setId(id);
        tileMatrix.setScaleDenominator(scaleDenominator);
        tileMatrix.setTopLeftCorner(topLeftCorner1, topLeftCorner2);
        tileMatrix.setTileWidth(tileWidth);
        tileMatrix.setTileHeight(tileHeight);
        tileMatrix.setMatrixWidth(matrixWidth);
        tileMatrix.setMatrixHeight(matrixHeight);
        return tileMatrix;
    }

}
