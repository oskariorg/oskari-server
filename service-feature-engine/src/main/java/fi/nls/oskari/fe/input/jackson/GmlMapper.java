package fi.nls.oskari.fe.input.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.input.format.gml.FEPullParser;
import fi.nls.oskari.fe.output.jackson.GeometryPropertySerializer;
import org.geotools.xml.Configuration;

import java.util.Calendar;

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

        // Registering serializers to XmlModule doesn't seem to do the trick anymore on Jackson 2.x
        final SimpleModule oskariGMLModule = new SimpleModule("OskariGMLModule", new Version(1, 0, 0, null))
                .addDeserializer(GeometryProperty.class, geometryDeserializer)
                .addDeserializer(Calendar.class, calendarDeserializer);
        registerModule(oskariGMLModule);

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
