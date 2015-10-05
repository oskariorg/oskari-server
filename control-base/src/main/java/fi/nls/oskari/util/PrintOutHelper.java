package fi.nls.oskari.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONArray;

/**
 * Printout utilities for e.g. GetPreviewHandler
 */

public class PrintOutHelper {
    
    private static Logger log = LogFactory.getLogger(PrintOutHelper.class);



    /**
     * Compares 2 JSONArrays for equality.
     * @param jsonArray1
     * @param jsonArray2
     * @return
     */
    public static boolean isEqual(JSONArray jsonArray1, JSONArray jsonArray2) {
        if(jsonArray1.length() != jsonArray2.length()) {
            return false;
        }
        if(jsonArray1.length() == 0) {
            return true;
        }
        for(int i = 0; i < jsonArray1.length(); ++i) {
            try {
                final Object value1 = jsonArray1.get(i);
                final Object value2 = jsonArray2.get(i);

            } catch (Exception ex) {
                log.warn(ex, "Error comparing JSONArrays");
                return false;
            }
        }
        return true;
    }


}
