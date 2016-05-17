package fi.nls.oskari.domain.map.wfs;

/**
 * Created by Oskari team on 21.4.2016.
 */
public class WFS2FeatureType {

        private String name;
        private String title;
        private String defaultSrs;
        private String[] OtherSrs;
        private String nsUri;
        private String templateDescription;
        private String templateType;
        private String requestTemplate;
        private String responseTemplate;
        private String parseConfig;
        private String geomPropertyName;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDefaultSrs() {
            return defaultSrs;
        }

        public void setDefaultSrs(String defaultSrs) {
            this.defaultSrs = defaultSrs;
        }

        public String[] getOtherSrs() {
            return OtherSrs;
        }

        public void setOtherSrs(String[] otherSrs) {
            this.OtherSrs = otherSrs;
        }

        public String getNsUri() {
            return nsUri;
        }

        public void setNsUri(String nsUri) {
            this.nsUri = nsUri;
        }


        public String getTemplateDescription() {
            return templateDescription;
        }

        public void setTemplateDescription(String templateDescription) {
            this.templateDescription = templateDescription;
        }

        public String getTemplateType() {
            return templateType;
        }

        public void setTemplateType(String templateType) {
            this.templateType = templateType;
        }

        public String getRequestTemplate() {
            return requestTemplate;
        }

        public void setRequestTemplate(String requestTemplate) {
            this.requestTemplate = requestTemplate;
        }

        public String getResponseTemplate() {
            return responseTemplate;
        }

        public void setResponseTemplate(String responseTemplate) {
            this.responseTemplate = responseTemplate;
        }

        public String getParseConfig() {
            return parseConfig;
        }

        public void setParseConfig(String parseConfig) {
            this.parseConfig = parseConfig;
        }

        public String getGeomPropertyName() {
            return geomPropertyName;
        }

        public void setGeomPropertyName(String geomPropertyName) {
            this.geomPropertyName = geomPropertyName;
        }


}
