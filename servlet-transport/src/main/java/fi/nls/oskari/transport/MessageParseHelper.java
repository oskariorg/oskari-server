package fi.nls.oskari.transport;

import fi.nls.oskari.pojo.Grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper for parsing websocket messages
 */
public class MessageParseHelper {

    public static final String PARAM_GRID = "grid";
    public static final String PARAM_ROWS = "rows";
    public static final String PARAM_COLUMNS = "columns";
    public static final String PARAM_BOUNDS = "bounds";

    /**
     * Helper for creating Grid from
     *
     * @param params
     */
    @SuppressWarnings("unchecked")
    public static Grid parseGrid(Map<String, Object> params) {
        Grid grid = new Grid();

        Map<String, Object> tmpgrid = (Map<String, Object>) params.get(PARAM_GRID);
        List<List<Double>> bounds = parseBounds(tmpgrid.get(PARAM_BOUNDS));

        grid.setRows(((Number)tmpgrid.get(PARAM_ROWS)).intValue());
        grid.setColumns(((Number)tmpgrid.get(PARAM_COLUMNS)).intValue());
        grid.setBounds(bounds);

        return grid;
    }

    public static List<Double> parseBbox(Object tmpbbox_fld) {
        List<Double> bbox = new ArrayList<Double>();
        Object[] tmpbbox = getArray(tmpbbox_fld);
        for(Object obj : tmpbbox) {
            if(obj instanceof Double) {
                bbox.add((Double) obj);
            } else {
                bbox.add(((Number) obj).doubleValue());
            }
        }
        return bbox;
    }

    public static Object[] getArray(Object obj) {
        if(obj instanceof List) {
            return ((List)obj).toArray();
        }
        return (Object[]) obj;
    }

    public static List<List<Double>> parseBounds(Object param) {
        if(param == null) {
            return null;
        }
        Object[] params = getArray(param);

        List<List<Double>> bounds = new ArrayList<List<Double>>();
        List<Double> tile = null;

        for(Object obj : params) {

            if(obj instanceof Object[] || obj instanceof List) {
                tile = new ArrayList<Double>();

                Object[] objs = obj instanceof List ? ((List) obj).toArray():(Object[])obj;
                for(Object bound : objs ) {
                    if(bound instanceof Double) {
                        tile.add((Double)bound);
                    } else {
                        tile.add(((Number)bound).doubleValue());
                    }
                }
                bounds.add(tile);
            }
        }

        return bounds;
    }
}
