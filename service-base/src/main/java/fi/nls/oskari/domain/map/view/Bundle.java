package fi.nls.oskari.domain.map.view;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

import java.io.Serializable;

public class Bundle implements Comparable, Serializable {
    private long bundleId = -1;
    private long viewId = -1;
    private int seqNo = -1;

    private String state;
    private String config;
    private String startup;
    private String name;
    private String bundleinstance;

    private JSONObject configJSON;
    private JSONObject stateJSON;

    public String toString() {
        return
                "bundleId = '" + bundleId +"'\n" +
                "viewId   = '" + viewId +"'\n" +
                "seqNo    = '" + seqNo +"'\n" +
                "state    = '" + state +"'\n" +
                "config   = '" + config +"'\n" +
                "startup  = '" + startup +"'\n" +
                "bundleinstance  = '" + bundleinstance +"'\n" +
                "name     = '" + name +"'\n";

    }

    public int compareTo(Object o) throws ClassCastException {
        if (o instanceof Bundle) {
            return (this.seqNo - ((Bundle) o).seqNo);
        } else {
            throw new ClassCastException("Can't compare Bundle with " +
                         o.getClass().getName());
        }
    }

    public long getViewId() {
        return this.viewId;
    }
    public void setViewId(long viewId) {
        this.viewId = viewId;
    }

    /**
     * Returns the id for database.
     * @see #getName() for frontend "bundleid"
     * @return
     */
    public long getBundleId() {
        return this.bundleId;
    }
    public void setBundleId(long bundleId) {
        this.bundleId = bundleId;
    }
    
    public int getSeqNo() { return this.seqNo; }
    public void setSeqNo(int seqNo) { this.seqNo = seqNo; }

    public String getState() {
        if(stateJSON != null) {
            // sync to get possible modifications from JSON
            state = stateJSON.toString();
        }
        return state;
    }

    public JSONObject getStateJSON() {
        if(stateJSON == null) {
            stateJSON = JSONHelper.createJSONObject(getState());
        }
        return stateJSON;
    }

    public void setState(String state) {
        this.state = state;
        // reset to keep in sync!
        this.stateJSON = null;
    }

    public String getConfig() {
        if(configJSON != null) {
            // sync to get possible modifications from JSON
            config = configJSON.toString();
        }
        return config;
    }
    public JSONObject getConfigJSON() {
        if(configJSON == null) {
            configJSON = JSONHelper.createJSONObject(getConfig());
        }
        return configJSON;
    }

    public void setConfig(String config) {
        this.config = config;
        // reset to keep in sync!
        this.configJSON = null;
    }

    public String getStartup() {
        return startup;
    }

    public void setStartup(String startup) {
        this.startup = startup;
    }

    /**
     * Returns the "bundleid" as known by frontend
     * @return
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBundleinstance() {
        if(bundleinstance == null || bundleinstance.isEmpty()) {
            return getName();
        }
        return bundleinstance;
    }

    public void setBundleinstance(String bundleinstance) {
        this.bundleinstance = bundleinstance;
    }

    public Bundle clone() {
        Bundle b = new Bundle();
        b.setBundleId(getBundleId()); // db id
        b.setName(getName()); // bundleid as known by client
        b.setBundleinstance(getBundleinstance());
        b.setStartup(getStartup());
        b.setConfig(getConfig());
        b.setState(getState());
        b.setSeqNo(getSeqNo());
        return b;
    }
}
