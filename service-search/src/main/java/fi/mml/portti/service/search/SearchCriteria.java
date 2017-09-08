package fi.mml.portti.service.search;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.PropertyUtil;

import java.io.Serializable;
import java.util.*;

/**
 * Search criteria.
 */
public class SearchCriteria implements Serializable {
    private static final long serialVersionUID = -3217931790577562692L;
    private final Map<String, Object> parameters = new HashMap<String, Object>();
    private String locale = PropertyUtil.getDefaultLanguage();
    private User user;
    /**
     * our search string
     */
    private String searchString;

    /**
     * input/output location SRS
     */
    private String srs;

    private double lon = -1;
    private double lat = -1;

    private Date fromDate;
    private Date toDate;
    private int maxResults = -1;

    // queried channels
    private List<String> channels;

    public SearchCriteria() {
        this(null);
    }

    public SearchCriteria(User user) {
        channels = new ArrayList<>();
        this.user = user;
        if(user == null) {
            try {
                this.user = UserService.getInstance().getGuestUser();
            } catch (ServiceException ignored) {}
        }
    }

    public User getUser() {
        return user;
    }

    public void addParam(final String key, final Object value) {
        parameters.put(key, value);
    }

    public Object getParam(final String key) {
        return parameters.get(key);
    }

    public String getParamAsString(final String key) {
        Object value = parameters.get(key);
        if(value != null) {
            return value.toString();
        }
        return null;
    }

    public Map<String, Object> getParams() {
        return Collections.unmodifiableMap(parameters);
    }

    public boolean isReverseGeocode() {
        return getLat() != -1 && getLon() != -1;
    }

    public void setReverseGeocode(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
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

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getSearchString1stUp() {
        return searchString.substring(0, 1).toUpperCase() + searchString.substring(1);
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
