package org.oskari.capabilities.ogc;

/**
 * Class for Oskari capabilities layer style
 */
public class LayerStyle {

 /*   [{
        "title": "default",
                "legend": "http://kartat.lounaispaikka.fi/ms6/maakuntakaavat/lappi/rovaniemi_maakuntakaava/rovaniemi_maakuntakaava_ms6?version=1.3.0&service=WMS&request=GetLegendGraphic&sld_version=1.1.0&layer=symbolit&format=image/png; mode=24bit&STYLE=default",
                "name": "default" */

    private String title;
    private String legend;
    private String name;
    private boolean isDefault;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLegend() {
        return legend;
    }

    public void setLegend(String legend) {
        this.legend = legend;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}

