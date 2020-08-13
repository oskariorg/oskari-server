package fi.mml.map.mapwindow.service.wms;

public class WebMapServiceFactoryHelper {

    public static WebMapService createFromXML(final String layerName, final String xml)
            throws WebMapServiceParseException, LayerNotFoundInCapabilitiesException {
        if (isVersion1_3_0(xml)) {
            return new WebMapServiceV1_3_0_Impl("from DataBase", xml, layerName);
        } else if (isVersion1_1_1(xml)) {
            return new WebMapServiceV1_1_1_Impl("from DataBase", xml, layerName);
        } else {
            throw new WebMapServiceParseException("Could not detect version to be 1.3.0 or 1.1.1");
        }
    }

    /**
     * Returns true if data represents a WMS 1.1.1 version
     */
    public static boolean isVersion1_1_1(String data) {
        return data != null &&
                data.contains("WMT_MS_Capabilities") &&
                data.contains("version=\"1.1.1\"");
    }

    /**
     * Returns true if data represents a WMS 1.3.0 version
     */
    public static boolean isVersion1_3_0(String data) {
        return data != null &&
                data.contains("WMS_Capabilities") &&
                data.contains("version=\"1.3.0\"");
    }

}
