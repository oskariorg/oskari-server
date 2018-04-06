package org.oskari.print.wmts;

import java.util.ArrayList;
import java.util.List;

/**
 * WMTS GetTile Request Builder (REST)
 */
public class GetTileRequestBuilderREST implements GetTileRequestBuilder {

    private static final String TEMPLATE_LAYER = "layer";
    private static final String TEMPLATE_STYLE = "style";
    private static final String TEMPLATE_MATRIX_SET = "TileMatrixSet";
    private static final String TEMPLATE_MATRIX = "TileMatrix";
    private static final String TEMPLATE_ROW = "TileRow";
    private static final String TEMPLATE_COL = "TileCol";

    private static final int INDEX_LAYER = 0;
    private static final int INDEX_STYLE = 1;
    private static final int INDEX_MATRIX_SET = 2;
    private static final int INDEX_MATRIX = 3;
    private static final int INDEX_ROW = 4;
    private static final int INDEX_COL = 5;

    private final List<String> parts;
    private final int[] templateIndexes;

    private String layer;
    private String style;
    private String tileMatrixSet;
    private String tileMatrix;
    private int tileRow;
    private int tileCol;

    public GetTileRequestBuilderREST(String template) throws IllegalArgumentException {
        if (template == null || template.length() == 0) {
            throw new IllegalArgumentException("Template must be non-empty!");
        }

        parts = new ArrayList<String>();
        templateIndexes = new int[6];
        int templateCounter = 0;

        int i = 0;
        int j = 0;
        int k = 0;

        while (true) {
            i = template.indexOf('{', k);
            if (i < 0) {
                break;
            }
            if (i > k) {
                parts.add(template.substring(k, i));
            }
            k = i + 1;
            j = template.indexOf('}', k);
            if (j < 0) {
                parts.add(template.substring(i));
                break;
            }

            String key = template.substring(k, j);
            k = j + 1;

            int templateIndex = getTemplateIndex(key);
            if (templateIndex < 0) {
                parts.add(template.substring(i, k));
            } else {
                parts.add(null);
                templateIndexes[templateCounter++] = templateIndex;
            }
        }

        String suffix = template.substring(k);
        if (suffix.length() > 0) {
            parts.add(suffix);
        }
    }

    public GetTileRequestBuilderREST layer(String layer) {
        this.layer = layer;
        return this;
    }

    public GetTileRequestBuilderREST style(String style) {
        this.style = style;
        return this;
    }

    public GetTileRequestBuilderREST tileMatrixSet(String tileMatrixSet) {
        this.tileMatrixSet = tileMatrixSet;
        return this;
    }

    public GetTileRequestBuilderREST tileMatrix(String tileMatrix) {
        this.tileMatrix = tileMatrix;
        return this;
    }

    public GetTileRequestBuilderREST tileRow(int tileRow) {
        this.tileRow = tileRow;
        return this;
    }

    public GetTileRequestBuilderREST tileCol(int tileCol) {
        this.tileCol = tileCol;
        return this;
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        int templateCounter = 0;
        for (String part : parts) {
            if (part == null) {
                int index = templateIndexes[templateCounter++];
                part = getString(index);
            }
            // If part is still null don't append "null"
            if (part != null) {
                sb.append(part);
            }
        }
        return sb.toString();
    }

    private static int getTemplateIndex(String key) {
        switch (key) {
        case TEMPLATE_LAYER: 
            return INDEX_LAYER;
        case TEMPLATE_STYLE: 
            return INDEX_STYLE;
        case TEMPLATE_MATRIX_SET:
            return INDEX_MATRIX_SET;
        case TEMPLATE_MATRIX:
            return INDEX_MATRIX;
        case TEMPLATE_ROW:
            return INDEX_ROW;
        case TEMPLATE_COL:
            return INDEX_COL;
        default:
            return -1;
        }
    }

    private String getString(final int index) {
        switch (index) {
        case INDEX_LAYER:
            return layer;
        case INDEX_STYLE:
            return style;
        case INDEX_MATRIX_SET:
            return tileMatrixSet;
        case INDEX_MATRIX:
            return tileMatrix;
        case INDEX_ROW:
            return Integer.toString(tileRow);
        case INDEX_COL:
            return Integer.toString(tileCol);
        default:
            return null;
        }
    }

}
