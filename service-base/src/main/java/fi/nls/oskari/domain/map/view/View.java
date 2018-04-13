package fi.nls.oskari.domain.map.view;

import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.commons.lang.text.StrSubstitutor;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class View implements Serializable {
    private long id = -1;
    private long oldId = -1;
    private String name = null;
    private String description = null;
    private String uuid = null;
    private boolean onlyForUuId = false;
    private JSONObject metadata = null;
    private List<Bundle> bundles = new ArrayList<Bundle>();

    public String getUrl() {
        final Map<String, String> valuesMap = new HashMap();
        valuesMap.put("lang", getLang());
        valuesMap.put("uuid", getUuid());
        final StrSubstitutor sub = new StrSubstitutor(valuesMap);

        String baseUrl = getBaseUrlForView(getType().toLowerCase(), getLang());
        return sub.replace(baseUrl);
    }

    public JSONObject getMetadata() {
        if(metadata == null) {
            metadata = new JSONObject();
        }
        return metadata;
    }

    public String getMetadataAsString() {
        return getMetadata().toString();
    }

    public void setMetadata(JSONObject metadata) {
        this.metadata = metadata;
    }

    private String getBaseUrlForView(final String type, final String lang) {
        String value = null;
        // view.published.url = http://foo.bar/${lang}/${uuid}
        // view.user.url.fi = http://foo.bar/kayttaja/${uuid}
        // view.user.url.en = http://foo.bar/user/${uuid}
        final String basePropKey = "view." + type + ".url";
        List<String> urls = PropertyUtil.getPropertyNamesStartingWith(basePropKey);
        if(urls.size() == 1) {
            // normal override of defaults
            value =  PropertyUtil.get(basePropKey);
        }
        else if(urls.size() > 1) {
            // locale-specific urls
            value = PropertyUtil.getOptional(basePropKey + "." + lang);
        }
        if(value == null) {
            // not defined, use reasonable default
            // oskari.domain=http://foo.bar
            // oskari.map.url=/oskari-map
            value = PropertyUtil.get("oskari.domain") + PropertyUtil.get("oskari.map.url");
            // uuid param name should match ActionConstants.PARAM_UUID
            value = value + "?lang=${lang}&uuid=${uuid}";
        }
        return value;
    }

    public long getId() { return this.id; }
    public void setId(long id) { this.id = id; }

    public long getOldId() { return this.oldId; }
    public void setOldId(long oldId) { this.oldId = oldId; }

    public String getUuid() { return this.uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public boolean isOnlyForUuId() { return this.onlyForUuId; }
    public void setOnlyForUuId(boolean onlyForUuId) { this.onlyForUuId = onlyForUuId; }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() {
    	if(this.description == null) return "";
    	return this.description;
    }
    public void setDescription(String description) { this.description = description; }

    private String application = "full-map"; // app name
    private String page = "view"; // JSP
    private String developmentPath = "/applications";
    private long creator = -1;
    private boolean isPublic = false;
    private boolean isDefault = false;
    private String type = null;
    private String pubDomain = "";
    private String lang = PropertyUtil.getDefaultLanguage();

    public String getApplication() { return this.application; }
    public void setApplication(String as) { this.application = as; }

    public String getPage() { return this.page; }
    public void setPage(String ba) { this.page = ba; }

    public long getCreator() { return this.creator; }
    public void setCreator(long creator) { this.creator = creator; }

    public boolean isPublic() { return this.isPublic; }
    public void setIsPublic(boolean isPublic) { this.isPublic = isPublic; }
    public boolean isDefault() { return this.isDefault; }
    public void setIsDefault(boolean isDefault) { this.isDefault = isDefault; }

    public String getPubDomain() { return this.pubDomain; }
    public void setPubDomain(String pd) { this.pubDomain = pd; }

    public String getLang() { return this.lang; }
    public void setLang(String lang) { this.lang = lang; }

    public String getDevelopmentPath() {
        return developmentPath;
    }

    public void setDevelopmentPath(String developmentPath) {
        this.developmentPath = developmentPath;
    }

    public String getType() { return this.type; }
    public void setType(String type) { this.type = type; }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        String name = this.name == null ? null :
            "'" + this.name.replace("\n", "").replace("\r", "") + "'";
        String description = this.description == null ? null :
            "'" + this.description.replace("\n", "").replace("\r", "") + "'";
        String uuid = this.uuid == null ? null :
            "'" + this.uuid.replace("\n", "").replace("\r", "") + "'";
        String lang = this.lang == null ? null :
            "'" + this.lang.replace("\n", "").replace("\r", "") + "'";
        String pubDomain = this.pubDomain == null ? null :
            "'" + this.pubDomain.replace("\n", "").replace("\r", "") + "'";

        sb.append("]");
        
        String ret =
            "{\n" +
            "  id: " + this.id + ",\n" +
            "  oldId: " + this.oldId + ",\n" +
            "  name: " + name + ",\n" +
            "  description: " + description + ",\n" +
            "  uuid: " + uuid + ",\n" +
            "  lang: " + lang + ",\n" +
            "  pubDomain: " + pubDomain + ",\n" +
            "  url: '" + getUrl() + "',\n" +
            "  states: " + sb.toString() + "\n" +
            "  }\n";
        
        return ret;
    }

    public Bundle getBundleByName(String bundleName) {
        for (Bundle bundle : this.bundles) {
            if (bundle.getName().equals(bundleName)) {
                return bundle;
            }
        }
        return null;
    }

    public List<Bundle> getBundles() {
        return this.bundles;
    }

    public void setBundles(List<Bundle> bundles) {
        if (!checkSeqNumbers(bundles)) {
            resetSeqNumbers(bundles);
        }
        this.bundles = bundles;
    }

    private boolean checkSeqNumbers(List<Bundle> bundles) {
        if (bundles != null) {
            int expected = 1;
            for (Bundle bundle : bundles) {
                if (expected++ != bundle.getSeqNo()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void resetSeqNumbers(List<Bundle> bundles) {
        if (bundles != null) {
            int seqNo = 1;
            for (Bundle bundle : bundles) {
                bundle.setSeqNo(seqNo++);
            }
        }
    }

    public void addBundle(final Bundle bundle) {
        if(bundle.getSeqNo() == -1) {
            // fix sequence number if not set
            if(this.bundles.size() == 0) {
                bundle.setSeqNo(1);
            }
            else {
                final int lastIndex = this.bundles.get(bundles.size() -1).getSeqNo();
                bundle.setSeqNo(lastIndex + 1);
            }
        }
        this.bundles.add(bundle);
    }

    public void removeBundle(final String bundleName) {
        final Bundle bundle = getBundleByName(bundleName);
        if(bundle == null) {
            return;
        }
        this.bundles.remove(bundle);
        int seqNo = 0;
        for (Bundle b : this.bundles) {
            b.setSeqNo(seqNo);
            seqNo++;
        }
    }

    /**
     * Reset bundle's segment number to be highest values (last bundle in loading)
      * @param bundleName  bundle, which segment number must be highest
     */
    public void pushBundleLast(String bundleName) {
        final int lastIndex = this.bundles.get(bundles.size() -1).getSeqNo();
        for (Bundle bundle : this.bundles) {
            if (bundle.getName().equals(bundleName)) {
                bundle.setSeqNo(lastIndex + 1);
            }
        }
    }

    /**
     * Skips id, oldId and uuid but clones the rest of the info. Bundles retain ids.
     * @return cloned object with bundles
     */
    public View cloneBasicInfo() {
        View view = new View();
        // skip id, oldId, uuid
        view.setName(getName());
        view.setDescription(getDescription());
        view.setType(getType());
        view.setDevelopmentPath(getDevelopmentPath());
        view.setApplication(getApplication());
        view.setIsPublic(isPublic());
        view.setLang(getLang());
        view.setPage(getPage());
        view.setPubDomain(getPubDomain());
        view.setIsDefault(isDefault());
        for(Bundle bundle : getBundles()) {
            view.addBundle(bundle.clone());
        }

        return view;
    }

    public JSONObject getMapOptions() {
        Bundle mapfull = getBundleByName("mapfull");
        if (mapfull == null) {
            return null;
        }
        JSONObject config = mapfull.getConfigJSON();
        return JSONHelper.getJSONObject(config, "mapOptions");
    }

    public String getSrsName() {
        JSONObject mapOptions = getMapOptions();
        if (mapOptions == null) {
            return null;
        }
        return JSONHelper.getStringFromJSON(mapOptions, "srsName", null);
    }

}
