package fi.nls.oskari.domain.map.view;

import fi.nls.oskari.util.PropertyUtil;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

public class View implements Serializable {
    private long id = -1;
    private long oldId = -1;
    private String name = null;
    private String description = null;
    private String uuid = null;
    private boolean onlyForUuId = false;
    private List<Bundle> bundles = new ArrayList<Bundle>();

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

    /* Supplement bits, these should prolly have be in a member object */
    private long supplementId = -1;
    private String application = "full-map"; // app name
    private String page = "view"; // JSP
    private String developmentPath = "/applications";
    private long creator = -1;
    private boolean isPublic = false;
    private String type = null;
    private String pubDomain = "";
    private String lang = PropertyUtil.getDefaultLanguage();
    private int width = 0;
    private int height = 0;

    public long getSupplementId() { return this.supplementId; }
    public void setSupplementId(long supId) { this.supplementId = supId; }

    public String getApplication() { return this.application; }
    public void setApplication(String as) { this.application = as; }

    public String getPage() { return this.page; }
    public void setPage(String ba) { this.page = ba; }

    public long getCreator() { return this.creator; }
    public void setCreator(long creator) { this.creator = creator; }

    public boolean isPublic() { return this.isPublic; }
    public boolean getIsPublic() { return this.isPublic; }
    public void setIsPublic(boolean isPublic) { this.isPublic = isPublic; }

    public String getPubDomain() { return this.pubDomain; }
    public void setPubDomain(String pd) { this.pubDomain = pd; }

    public String getLang() { return this.lang; }
    public void setLang(String lang) { this.lang = lang; }

    public int getWidth() { return this.width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return this.height; }
    public void setHeight(int height) { this.height = height; }

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
        boolean first = true;
        /*
        for (Bundle s : this.states) {
            if (!first) sb.append(",");
            first = false;
            sb.append(s.toString());
        } */
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
            "  width: " + this.width + ",\n" +
            "  pubDomain: " + pubDomain + ",\n" +
            "  height: " + this.height + ",\n" +
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
     * Skips id, oldId and uuid but clones the rest of the info. Bundles retain ids.
     * @return cloned object with bundles
     */
    public View cloneBasicInfo() {
        View view = new View();
        // skip id, oldId, uuid, supplementId
        view.setName(getName());
        view.setDescription(getDescription());
        view.setType(getType());
        view.setDevelopmentPath(getDevelopmentPath());
        view.setApplication(getApplication());
        view.setIsPublic(getIsPublic());
        view.setLang(getLang());
        view.setPage(getPage());
        view.setPubDomain(getPubDomain());
        for(Bundle bundle : getBundles()) {
            view.addBundle(bundle.clone());
        }

        return view;
    }
}
