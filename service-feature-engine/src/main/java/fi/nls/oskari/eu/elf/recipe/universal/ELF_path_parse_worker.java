package fi.nls.oskari.eu.elf.recipe.universal;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

public class ELF_path_parse_worker {

    protected JSONObject parseConfig = null;

    public ELF_path_parse_worker(JSONObject conf) {
        this.parseConfig = conf;
    }

    public void setupMaps(Map<String, String> attrmap, Map<String, String> elemmap, Map<String, String> typemap,
                          Map<String, String> nilmap) {

        JSONArray paths = JSONHelper.getJSONArray(this.parseConfig, "paths");
        if (paths == null) return;

        for (int i = 0; i < paths.length(); i++) {
            JSONObject item = paths.optJSONObject(i);
            if (JSONHelper.getStringFromJSON(item, "path", "").indexOf("@") > -1) {
                attrmap.put(JSONHelper.getStringFromJSON(item, "path", "unknown").replace("@", ""),
                        JSONHelper.getStringFromJSON(item, "label", "unknown"));
            } else {
                elemmap.put(JSONHelper.getStringFromJSON(item, "path", "unknown"),
                        JSONHelper.getStringFromJSON(item, "label", ""));
            }

            //Type mapping
            typemap.put(JSONHelper.getStringFromJSON(item, "path", "unknown"), JSONHelper.getStringFromJSON(item, "type", "String"));
            //Not id
            if (JSONHelper.getStringFromJSON(item, "label", "unknown").equals("id")) continue;


        }

        // Additional features  - local href links point to these features

        for (int i = 0; i < paths.length(); i++) {
            JSONObject item = paths.optJSONObject(i);
            if (JSONHelper.getStringFromJSON(item, "type", "String").equals("Href")) {
                JSONArray jshref = JSONHelper.getJSONArray(item, "hrefPath");
                if (jshref != null) {
                    for (int j = 0; j < jshref.length(); j++) {
                        JSONObject itemh = jshref.optJSONObject(j);
                        if (JSONHelper.getStringFromJSON(itemh, "path", "").indexOf("@") > -1) {
                            attrmap.put(JSONHelper.getStringFromJSON(itemh, "path", "unknown").replace("@", ""),
                                    JSONHelper.getStringFromJSON(itemh, "label", "unknown"));
                        } else {
                            elemmap.put(JSONHelper.getStringFromJSON(itemh, "path", "unknown"),
                                    JSONHelper.getStringFromJSON(itemh, "label", ""));
                        }

                        //Type mapping
                        typemap.put(JSONHelper.getStringFromJSON(itemh, "path", "unknown"), JSONHelper.getStringFromJSON(itemh, "type", "String"));

                    }

                }
            }


        }
    }


    public QName getRootQN() {

        if (this.parseConfig != null) {
            JSONObject root = JSONHelper.getJSONObject(this.parseConfig, "root");
            if (root == null) return null;
            final String rootNS = JSONHelper.getStringFromJSON(root, "rootNS", null);
            final String name = JSONHelper.getStringFromJSON(root, "name", null);
            if (name == null || rootNS == null) return null;
            final QName rootQN = new QName(rootNS, name);

            return rootQN;
        }
        return null;
    }

    public QName getScanQN() {

        if (this.parseConfig != null) {
            JSONObject scan = JSONHelper.getJSONObject(this.parseConfig, "scan");
            if (scan == null) return null;
            final String scanNS = JSONHelper.getStringFromJSON(scan, "scanNS", null);
            final String name = JSONHelper.getStringFromJSON(scan, "name", null);
            if (name == null || scanNS == null) return null;
            final QName rootQN = new QName(scanNS, name);

            return rootQN;
        }
        return null;

    }

    public void setParseConfig(JSONObject parseConfig) {
        this.parseConfig = parseConfig;
    }

    public static String getPathString(List tags) {
        int j = 0;
        String out = "";
        while (j < tags.size()) {
            out = out + tags.get(j);
            j++;
        }
        return out;
    }
    public static String getGenericName(List tags) {

        String lastTag = tags.get(tags.size()-1).toString().replace("/","");;
        String sndlastTag = "";
        lastTag = lastTag.split(":")[lastTag.split(":").length-1];
        if(tags.size() > 2) {
            sndlastTag = tags.get(tags.size()-2).toString().replace("/","");;
            sndlastTag = sndlastTag.split(":")[sndlastTag.split(":").length-1] + "_";
        }

        return sndlastTag + lastTag;
    }

    /**
     * Fix open tag path tail
     *
     * @param tags open tags (element local names)
     * @param ns   current end element name
     */
    public static void unStepPathTag(List tags, QName ns) {
        // Fix end element path tail, because of pull parser
        // Don't use remove(o), there could be same name elements in sub elements
        while (tags.size() > 0 && !tags.get(tags.size() - 1).equals("/" + ns.getPrefix() + ":" + ns.getLocalPart())) {
            tags.remove(tags.size() - 1);
        }
        //Remove current end element
        if (tags.size() > 0) tags.remove(tags.size() - 1);

    }

    /**
     * Add child element to element path
     * Clean element path if current element is root element
     *
     * @param tags   open tags (element local names)
     * @param rootNS current start element name
     * @param curNS  current start element name
     */
    public static void addPathTag(List tags, QName rootNS, QName curNS) {
        if (rootNS.getPrefix().equals(curNS.getPrefix()) && rootNS.getLocalPart().equals(curNS.getLocalPart()))
            tags.clear();
        tags.add("/" + curNS.getPrefix() + ":" + curNS.getLocalPart());
    }


}
