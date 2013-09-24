package fi.nls.oskari.domain.map;

import java.util.Date;

public class BackendStatus {
    
    private long id;
    private Date ts;
    private String maplayer_id;
    private String status;
    private String statusmessage;
    private String infourl;
    private String statusjson;
    
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public Date getTs() {
        return ts;
    }
    public void setTs(Date ts) {
        this.ts = ts;
    }
    public String getMaplayer_id() {
        return maplayer_id;
    }
    public void setMaplayer_id(String maplayer_id) {
        this.maplayer_id = maplayer_id;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatusmessage() {
        return statusmessage;
    }
    public void setStatusmessage(String statusmessage) {
        this.statusmessage = statusmessage;
    }
    public String getInfourl() {
        return infourl;
    }
    public void setInfourl(String infourl) {
        this.infourl = infourl;
    }
    public String getStatusjson() {
        return statusjson;
    }
    public void setStatusjson(String statusjson) {
        this.statusjson = statusjson;
    }
}
