package fi.nls.oskari.wmts;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.XmlHelper;
import fi.nls.oskari.wmts.domain.TileMatrix;
import fi.nls.oskari.wmts.domain.TileMatrixSet;
import org.apache.axiom.om.OMElement;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * NOTE!!! Initial scripblings for WMTS capabilities parsing. Not used anywhere and might change without notice!!
 */
public class TileMatrixSetParser {

    private Logger log = LogFactory.getLogger(TileMatrixSetParser.class);

    public static TileMatrixSet parse(final OMElement tileMatrixSet) throws Exception {
        if(tileMatrixSet == null) {
            return null;
        }
        TileMatrixSet result = new TileMatrixSet();
        result.setId(XmlHelper.getChildValue(tileMatrixSet, "Identifier"));
        result.setCrs(XmlHelper.getChildValue(tileMatrixSet, "SupportedCRS"));
        Iterator<OMElement> matrixIterator = tileMatrixSet.getChildrenWithLocalName("TileMatrix");
        while(matrixIterator.hasNext()) {
            OMElement matrixElem = matrixIterator.next();
            TileMatrix matrix = new TileMatrix();
            matrix.setId(XmlHelper.getChildValue(matrixElem, "Identifier"));
            matrix.setScaleDenominator(ConversionHelper.getDouble(
                    XmlHelper.getChildValue(matrixElem, "ScaleDenominator"), matrix.getScaleDenominator()));
            matrix.setTopLeftCorner(XmlHelper.getChildValue(matrixElem, "TopLeftCorner"));
            matrix.setTileWidth(ConversionHelper.getInt(
                    XmlHelper.getChildValue(matrixElem, "TileWidth"), matrix.getTileWidth()));
            matrix.setTileHeight(ConversionHelper.getInt(
                    XmlHelper.getChildValue(matrixElem, "TileHeight"), matrix.getTileHeight()));
            matrix.setMatrixWidth(ConversionHelper.getInt(
                    XmlHelper.getChildValue(matrixElem, "MatrixWidth"), matrix.getMatrixWidth()));
            matrix.setMatrixHeight(ConversionHelper.getInt(
                    XmlHelper.getChildValue(matrixElem, "MatrixHeight"), matrix.getMatrixHeight()));
            result.addTileMatrix(matrix);
        }

        return result;
    }
}
