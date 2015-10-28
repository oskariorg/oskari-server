package fi.nls.oskari.map.layout;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Loads and manages printout templates for table data layout
 *
 */
public class OskariLayoutWorker {

    private static final Logger log = LogFactory.getLogger(OskariLayoutWorker.class);


    public static String getTableTemplate(final String tableLayout) {

        try {
            final String resource = IOHelper.readString(OskariLayoutWorker.class.getResourceAsStream(tableLayout + ".json"));

            return resource;
        }
        catch(Exception ex) {
            log.warn(ex, "Error loading table layout template for tableLayout:", tableLayout);
        }
        return null;
    }

    public static JSONObject fillTables(JSONObject tables, JSONObject rows) {

        try {

            // Get table id
            Iterator<?> keys = rows.keys();

            while( keys.hasNext() ){
                String key = (String)keys.next();  // should be table1, table2 ,...
                if(key.indexOf("table") > -1)
                {
                    // find key table rows in tables
                    if (tables.optJSONObject("content").has(key))
                    {
                        JSONArray tablerows = tables.optJSONObject("content").getJSONObject(key).optJSONArray("rows");
                        if(tablerows == null) return tables;
                        // Append rows
                        JSONArray datarows = rows.optJSONArray(key);
                        if(datarows == null) return tables;

                        for(int n = 0; n < datarows.length(); n++)
                        {
                            JSONObject row = datarows.getJSONObject(n);
                            tablerows.put(row);
                        }

                    }

                }

            }
            return tables;

        }
        catch(Exception ex) {
            log.warn(ex, "Error filling table layout template for printout");
        }
        return null;
    }

}
