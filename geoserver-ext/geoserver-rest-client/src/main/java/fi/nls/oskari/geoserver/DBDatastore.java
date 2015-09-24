package fi.nls.oskari.geoserver;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SMAKINEN on 1.9.2015.
 */
@JsonRootName("dataStore")
public class DBDatastore {
    public String name;
    public boolean enabled = true;

    public ConnectionParameters connectionParameters = new ConnectionParameters();
    /*
    String xml = "" +
            "<connectionParameters><host>" + host
            + "</host><port>" + port + "</port><database>" + db + "</database><user>" + user + "</user><passwd>"
            + pwd + "</passwd><dbtype>" + dbType + "</dbtype><namespace>" + dsNamespace + "</namespace>"
            + exposePKsParamter + "</connectionParameters></dataStore>";

    String exposePKsParamter = "<entry key=\"Expose primary keys\">" + exposePKs + "</entry>";
*/
    public void addEntry(String key, String value) {
        Entry entry = new Entry();
        entry.key = key;
        entry.value = value;
        connectionParameters.entry.add(entry);
    }
    class ConnectionParameters {
        public String host = "localhost";
        public int port = 5432;
        public String database;
        public String schema = "public";
        public String user;
        public String passwd;
        public String dbtype = "postgis";
        public String namespace;
        public List<Entry> entry = new ArrayList<Entry>();

    }
    class Entry {
        @JsonProperty("@key")
        String key;

        @JsonProperty("$")
        String value;
    }
}
