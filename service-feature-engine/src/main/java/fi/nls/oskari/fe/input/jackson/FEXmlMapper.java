package fi.nls.oskari.fe.input.jackson;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import fi.nls.oskari.eu.inspire.util.GeometryProperty;

public class FEXmlMapper extends XmlMapper {
    private static final long serialVersionUID = 8171154327483725418L;

    public FEXmlMapper() {
        super(new FEXmlFactory());

        JacksonXmlModule module = new JacksonXmlModule();
        module.addDeserializer(GeometryProperty.class,
                new FEGeometryDeserializer());
        registerModule(module);

    }

}
