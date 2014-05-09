package fi.mml.portti.service.search;

import fi.nls.oskari.util.PropertyUtil;

import java.io.Serializable;
import java.util.*;

/**
 * Search criteria.
 */
public class SearchCriteria implements Serializable {
    private static final long serialVersionUID = -3217931790577562692L;

    private String locale = PropertyUtil.getDefaultLanguage();

    private final Map<String, Object> parameters = new HashMap<String, Object>();

    /**
     * our search string
     */
    private String searchString;

    /**
     * target coordinate ref system
     */
    private String srs;

    /**
     * region name for to limit location search to certain area
     */
    private String region = "";

    /**
     * fuzzy search mode
     */
    private Boolean fuzzy = false;

    /**
     * alternative language results
     */
    private Boolean exonym = false;

    /**
     * from
     */
    private Date fromDate;

    /**
     * from
     */
    private Date toDate;

    /**
     * How many results max
     */
    private int maxResults;

    /**
     * queried channels
     */
    private List<String> channels;

    public SearchCriteria() {
        channels = new ArrayList<String>();
    }

    public void addParam(final String key, final Object value) {
        parameters.put(key, value);
    }

    public Object getParam(final String key) {
        return parameters.get(key);
    }

    public Map<String, Object> getParams() {
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Returns true if search should be done using given channel
     *
     * @param tested
     * @return
     */
    public boolean containsChannel(String tested) {
        for (String c : channels) {
            if (c.equals(tested)) {
                return true;
            }
        }

        return false;
    }

    public void addChannel(String channelId) {
        channels.add(channelId);
    }

    public String toString() {
        return "SearchCriteria [searchString=" + searchString + ", fromDate=" + fromDate
                + ", toDate=" + toDate + ", maxResults=" + maxResults + "]";
    }

    public String getSRS() {
        return srs;
    }

    public void setSRS(String srs) {
        this.srs = srs;
    }

    public Boolean getFuzzy() {
        return fuzzy;
    }

    public void setFuzzy(String fuzzy) {
        if (fuzzy.equals("true")) this.fuzzy = true;
        else this.fuzzy = false;
    }

    public Boolean getExonym() {
        return exonym;
    }

    public void setExonym(String exonym) {
        if (exonym.equals("true")) this.exonym = true;
        else this.exonym = false;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    public String getSearchString() {
        return searchString;
    }

    public String getSearchString1stUp() {
        return searchString.substring(0, 1).toUpperCase() + searchString.substring(1);
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getLocale() {
        if (locale == null) {
            return PropertyUtil.getDefaultLanguage();
        }
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

}
