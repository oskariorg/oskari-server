package fi.nls.oskari.fe.input.jackson;

import java.util.Calendar;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.tuple.Pair;
import org.geotools.xml.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;

import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.input.format.gml.FEPullParser;
import fi.nls.oskari.fe.input.format.gml.FEPullParser.PullParserHandler;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.jackson.GeometryPropertySerializer;
import fi.nls.oskari.fe.xml.util.NillableType;

public class GmlMapper extends XmlMapper {

    /**
     * 
     */
    private static final long serialVersionUID = -4030547605256325814L;
    private FEPullParser parserAny;
    private GeometryPropertyDeserializer geometryDeserializer;
    private GeometryPropertySerializer geometrySerializer;
    private CalendarDeserializer calendarDeserializer;

    static <T> Class getClazz(T... param) {
        return param.getClass().getComponentType();
    }
    
    public GmlMapper(Configuration gml, boolean lenient) {

        super(new XmlFactory());

        JacksonXmlModule module = new JacksonXmlModule();
        
        // ?
        module.setDefaultUseWrapper(false);

        parserAny = new FEPullParser(gml, null);

        geometryDeserializer = new GeometryPropertyDeserializer(gml, parserAny);
        calendarDeserializer = new CalendarDeserializer();

        module.addDeserializer(GeometryProperty.class, geometryDeserializer);
        module.addDeserializer(Calendar.class,calendarDeserializer);
        
        enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        if (lenient) {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }

        registerModule(module);

    }

    public GeometryPropertyDeserializer getGeometryDeserializer() {
        return geometryDeserializer;
    }

    public void setLenient(boolean l) {
        if (!l) {
            enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        } else {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }

    }

}
