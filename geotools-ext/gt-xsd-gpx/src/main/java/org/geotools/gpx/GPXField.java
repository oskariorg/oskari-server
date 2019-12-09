package org.geotools.gpx;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.xml.Node;

public enum GPXField implements GPXParseableField {

    Name("name", "name", String.class),
    Cmt("cmt", "cmt", String.class),
    Desc("desc", "desc", String.class),
    Src("src", "src", String.class),
    Type("type", "type", String.class),
    LinkHref("linkHref", "", String.class) {
        @Override
        public void parse(SimpleFeatureBuilder fb, Node node) {
            String href = null;
            Node linkNode = node.getChild("link");
            if (linkNode != null) {
                href = linkNode.getAttributeValue("href").toString();
            } else {
                // Try GPX 1.0
                Object url = node.getChildValue("url");
                if (url != null) {
                    href = url.toString();
                }
            }
            fb.set(getAttribute(), href);
        }
    },
    LinkText("linkText", "", String.class) {
        @Override
        public void parse(SimpleFeatureBuilder fb, Node node) {
            String text = null;
            Node linkNode = node.getChild("link");
            if (linkNode != null) {
                text = (String) linkNode.getChildValue("text");
            } else {
                // Try GPX 1.0
                Object url = node.getChildValue("urlname");
                if (url != null) {
                    text = url.toString();
                }
            }
            fb.set(getAttribute(), text);
        }
    }
    ;

    private static final GPXField[] CACHED_VALUES = GPXField.values();

    public static GPXField[] getCachedValues() {
        return CACHED_VALUES;
    }

    private String attribute;
    private String element;
    private Class<?> clazz;

    private GPXField(String attribute, String element, Class<?> clazz) {
        this.attribute = attribute;
        this.element = element;
        this.clazz = clazz;
    }

    @Override
    public String getAttribute() {
        return attribute;
    }

    @Override
    public String getElement() {
        return element;
    }

    @Override
    public Class<?> getBinding() {
        return clazz;
    }

    public void addBinding(SimpleFeatureTypeBuilder sftb) {
        sftb.add(getAttribute(), getBinding());
    }

}
