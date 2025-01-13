package org.geotools.gpx;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.xsd.Node;

public interface GPXParseableField {

    public String getAttribute();
    public String getElement();
    public Class<?> getBinding();

    public default void parse(SimpleFeatureBuilder fb, Node node) {
        Object value = node.getChildValue(getElement());
        fb.set(getAttribute(), value);
    }

}
