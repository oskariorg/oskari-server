package fi.nls.oskari.map.publish.domain;

import java.sql.Timestamp;
import java.util.Date;

public class TermsOfUse {
    
    private long userid;
    private boolean agreed;
    private Timestamp time;
    
    public long getUserid() {
        return userid;
    }
    public void setUserid(long userid) {
        this.userid = userid;
    }
    public boolean isAgreed() {
        return agreed;
    }
    public void setAgreed(boolean agreed) {
        this.agreed = agreed;
    }
    public Date getTime() {
        return time;
    }
    public void setTime(Timestamp time) {
        this.time = time;
    }
}
