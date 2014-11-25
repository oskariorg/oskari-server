package fi.nls.oskari.fe.input.jackson;

import java.util.Map;

import javax.xml.namespace.QName;

import org.geotools.xml.Configuration;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;

import fi.nls.oskari.fe.input.format.gml.FEPullParser;
import fi.nls.oskari.fe.input.format.gml.FEPullParser.PullParserHandler;
import fi.nls.oskari.fe.output.jackson.GeometryPropertySerializer;

public class GmlMapper extends XmlMapper {

    /**
     * 
     */
    private static final long serialVersionUID = -4030547605256325814L;
    private FEPullParser parserAny;
    private GeometryPropertyDeserializer geometryDeserializer;
    private GeometryPropertySerializer   geometrySerializer;

    public GmlMapper(Configuration gml) {

        super(new XmlFactory());

        JacksonXmlModule module = new JacksonXmlModule();

        parserAny = new FEPullParser(gml, null);

        geometryDeserializer = new GeometryPropertyDeserializer(gml, parserAny);

        module.addDeserializer(GeometryProperty.class, geometryDeserializer);

/*        geometrySerializer = new GeometryPropertySerializer();
        module.addSerializer(GeometryProperty.class, geometrySerializer);*/
        
        
        registerModule(module);

    }

    public GeometryPropertyDeserializer getGeometryDeserializer() {
        return geometryDeserializer;
    }

}
