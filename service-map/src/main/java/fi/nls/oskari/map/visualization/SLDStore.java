package fi.nls.oskari.map.visualization;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Loads SLD files based on wmsname from resources (refactored from hard coded MapLayerServiceNoDbImpl)
 * @author SMAKINEN
 */
public class SLDStore {

    private static final Logger log = LogFactory.getLogger(SLDStore.class);
    private final static ConcurrentMap<String, String> sldCache = new ConcurrentHashMap<String, String>();
    private static String defaultSLD = null;
    static {
        try {
            defaultSLD = IOHelper.readString(SLDStore.class.getResourceAsStream("default.sld"));
        }
        catch(Exception ex) {
            log.warn(ex, "Error loading default SLD");
        }
    }

    public static String getSLD(final String wmsName) {
        if(wmsName == null) {
            return defaultSLD;
        }
        // return from cache if loaded already
        if(sldCache.containsKey(wmsName)) {
            return sldCache.get(wmsName);
        }
        try {
            final String resource = IOHelper.readString(SLDStore.class.getResourceAsStream(wmsName + ".sld"));
            if(resource == null || resource.isEmpty()) {
                // default to default.sld;
                return defaultSLD;
            }
            // populate cache
            sldCache.put(wmsName, resource);
            return resource;
        }
        catch(Exception ex) {
            log.warn(ex, "Error loading SLD for wmsName:", wmsName);
        }
        return "";
    }

/*
Hardcoded SLDs from MapLayerServiceNoDbImpl, these weren't mapped to any wmsname so never used. Saved them anyway for later check on functionality
 */
    private static String sld_RakennusSijaintitiedot =
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
                    "<StyledLayerDescriptor version=\"1.0.0\" xmlns=\"http://www.opengis.net/sld\" xmlns:ogc=\"http://www.opengis.net/ogc\"\n"+
                    " xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"+
                    " xsi:schemaLocation=\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\">\n"+
                    "    <NamedLayer>\n"+
                    "    <Name>RalemmisSijaintitiedot</Name>\n"+
                    "    <UserStyle>\n"+
                    "      <Title>Default RalemmisSijaintitiedot style</Title>\n"+
                    "      <Abstract></Abstract>\n"+
                    "      <FeatureTypeStyle>\n"+
                    "        <Rule>\n"+
                    " 			<Title>Point</Title>\n"+
                    "           <PointSymbolizer>\n"+
                    "         		<Graphic>\n"+
                    "           <Mark>\n"+
                    "	             <WellKnownName>square</WellKnownName>\n"+
                    "	             <Fill>\n"+
                    "	               <CssParameter name=\"fill\">#0000FF</CssParameter>\n"+
                    "	             </Fill>\n"+
                    "	           </Mark>\n"+
                    "	           <Size>6</Size>\n"+
                    "         	</Graphic>\n"+
                    "       	</PointSymbolizer>        \n"+
                    "        </Rule>\n"+
                    "      </FeatureTypeStyle>\n"+
                    "    </UserStyle>\n"+
                    "  </NamedLayer>\n"+
                    "</StyledLayerDescriptor> \n";



    private static String sld_PalstanTietoja =
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
                    "<StyledLayerDescriptor version=\"1.0.0\" xmlns=\"http://www.opengis.net/sld\" xmlns:ogc=\"http://www.opengis.net/ogc\"\n"+
                    "xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"+
                    " xsi:schemaLocation=\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\">\n"+
                    "  <NamedLayer>\n"+
                    "    <Name>PalstanTietoja</Name>\n"+
                    "    <UserStyle>\n"+
                    "      <Title>Default PalstanTietoja style</Title>\n"+
                    "      <Abstract></Abstract>\n"+
                    "      <FeatureTypeStyle>\n"+
                    "        <FeatureTypeName xmlns:ktjkiiwfs=\"http://xml.nls.fi/ktjkiiwfs/2010/02\">ktjkiiwfs:PalstanTietoja</FeatureTypeName>\n"+
                    "        <Rule>\n"+
                    "          <Title>Polygon</Title>\n"+
                    "          <PolygonSymbolizer>\n"+
                    "            <Fill>\n"+
                    "              <CssParameter name=\"fill\">#00C0C0</CssParameter>\n"+
                    "              <CssParameter name=\"fill-opacity\">\n"+
                    "                <ogc:Literal>0.1</ogc:Literal>\n"+
                    "              </CssParameter>\n"+
                    "            </Fill>\n"+
                    "            <Stroke>\n"+
                    "              <CssParameter name=\"stroke\">#0000c0</CssParameter>\n"+
                    "              <CssParameter name=\"stroke-width\">3</CssParameter>\n"+
                    "            </Stroke>\n"+
                    "          </PolygonSymbolizer>\n"+
                    "                <PointSymbolizer>\n"+
                    "         <Graphic>\n"+
                    "           <Mark>\n"+
                    "             <WellKnownName>circle</WellKnownName>\n"+
                    "             <Fill>\n"+
                    "               <CssParameter name=\"fill\">#0000FF</CssParameter>\n"+
                    "             </Fill>\n"+
                    "           </Mark>\n"+
                    "           <Size>6</Size>\n"+
                    "         </Graphic>\n"+
                    "       </PointSymbolizer>      \n"+
                    "       <TextSymbolizer>\n"+
                    "                <Label><ogc:PropertyName>tekstiKartalla</ogc:PropertyName></Label>\n"+
                    "                <Font>\n"+
                    "                                                        <CssParameter name=\"font-family\">Arial</CssParameter>\n"+
                    "                                                        <CssParameter name=\"font-style\">Normal</CssParameter>\n"+
                    "                                                        <CssParameter name=\"font-size\">9</CssParameter>\n"+
                    "                                                        <CssParameter name=\"font-weight\">bold</CssParameter>\n"+
                    "                                                </Font>            \n"+
                    "                <LabelPlacement>\n"+
                    "                    <PointPlacement>\n"+
                    "                        <AnchorPoint>\n"+
                    "                            <AnchorPointX>0</AnchorPointX>\n"+
                    "                            <AnchorPointY>0</AnchorPointY>\n"+
                    "                        </AnchorPoint>\n"+
                    "                    </PointPlacement>\n"+
                    "                </LabelPlacement>\n"+
                    "                <Fill>\n"+
                    "                    <CssParameter name=\"fill\">#000000</CssParameter>\n"+
                    "                </Fill>\n"+
                    "            </TextSymbolizer>    \n"+
                    "                        <LineSymbolizer>\n"+
                    "                                                <Stroke>\n"+
                    "                                                        <CssParameter name=\"stroke\">#319738</CssParameter>\n"+
                    "                                                        <CssParameter name=\"stroke-width\">2</CssParameter>\n"+
                    "                                                </Stroke>\n"+
                    "                                        </LineSymbolizer>    \n"+
                    "        </Rule>\n"+
                    "      </FeatureTypeStyle>\n"+
                    "    </UserStyle>\n"+
                    "  </NamedLayer>\n"+
                    "</StyledLayerDescriptor> \n";

    private static String sld_PalstanTunnuspisteenSijaintitiedot =
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
                    "<StyledLayerDescriptor version=\"1.0.0\" xmlns=\"http://www.opengis.net/sld\" xmlns:ogc=\"http://www.opengis.net/ogc\"\n"+
                    " xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"+
                    " xsi:schemaLocation=\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\">\n"+
                    "    <NamedLayer>\n"+
                    "    <Name>PalstanTunnuspisteenSijaintitiedot</Name>\n"+
                    "    <UserStyle>\n"+
                    "      <Title>Default PalstanTunnuspisteenSijaintitiedot style</Title>\n"+
                    "      <Abstract></Abstract>\n"+
                    "      <FeatureTypeStyle>\n"+
                    "        <!-- FeatureTypeName xmlns:ktjkiiwfs=\"http://xml.nls.fi/ktjkiiwfs/2010/02\">ktjkiiwfs:PalstanTunnuspisteenSijaintitiedot</FeatureTypeName-->\n"+
                    "        <Rule>\n"+
                    " <Title>Point</Title>\n"+
                    "                <PointSymbolizer>\n"+
                    "         <Graphic>\n"+
                    "           <Mark>\n"+
                    "             <WellKnownName>circle</WellKnownName>\n"+
                    "             <Fill>\n"+
                    "               <CssParameter name=\"fill\">#FF0000</CssParameter>\n"+
                    "             </Fill>\n"+
                    "           </Mark>\n"+
                    "           <Size>6</Size>\n"+
                    "         </Graphic>\n"+
                    "       </PointSymbolizer>        \n"+
                    "       <TextSymbolizer>\n"+
                    "                <Label><ogc:PropertyName>tekstiKartalla</ogc:PropertyName></Label>\n"+
                    "                <Font>\n"+
                    "                                                        <CssParameter name=\"font-family\">Arial</CssParameter>\n"+
                    "                                                        <CssParameter name=\"font-style\">Normal</CssParameter>\n"+
                    "                                                        <CssParameter name=\"font-size\">9</CssParameter>\n"+
                    "                                                        <CssParameter name=\"font-weight\">bold</CssParameter>\n"+
                    "                                                </Font>            \n"+
                    "                <LabelPlacement>\n"+
                    "                    <PointPlacement>\n"+
                    "                        <AnchorPoint>\n"+
                    "                            <AnchorPointX>10</AnchorPointX>\n"+
                    "                            <AnchorPointY>10</AnchorPointY>\n"+
                    "                        </AnchorPoint>\n"+
                    "                    </PointPlacement>\n"+
                    "                </LabelPlacement>\n"+
                    "                <Fill>\n"+
                    "                    <CssParameter name=\"fill\">#000000</CssParameter>\n"+
                    "                </Fill>\n"+
                    "                \n"+
                    "            </TextSymbolizer>    \n"+
                    "        </Rule>\n"+
                    "      </FeatureTypeStyle>\n"+
                    "    </UserStyle>\n"+
                    "  </NamedLayer>\n"+
                    "</StyledLayerDescriptor> \n";
}
