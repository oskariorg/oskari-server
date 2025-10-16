package org.oskari.control.myfeatures;

import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.oskari.util.GeometryDeserializer;
import org.oskari.util.GeometrySerializer;
import org.oskari.util.JSONObjectSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ObjectMapperProvider {

    public static final ObjectMapper OM;
    static {
        OM = new ObjectMapper();
        GeometryFactory gf = new GeometryFactory(PackedCoordinateSequenceFactory.DOUBLE_FACTORY);
        SimpleModule jtsModule = new SimpleModule();
        jtsModule.addSerializer(Geometry.class, new GeometrySerializer());
        jtsModule.addDeserializer(Geometry.class, new GeometryDeserializer(gf));
        OM.registerModule(jtsModule);
        SimpleModule jsonModule = new SimpleModule();
        jsonModule.addSerializer(JSONObject.class, new JSONObjectSerializer());
        OM.registerModule(jsonModule);
        OM.registerModule(new JavaTimeModule());
    }

}
