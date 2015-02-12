package fi.nls.oskari.utils;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;

public class GeometryJSONOutputModule extends SimpleModule {

    public GeometryJSONOutputModule() {
        super("SimpleModule", new Version(1, 0, 0, null));
    }

    @Override
    public void setupModule(SetupContext context) {
        addSerializer(new GeometrySerializer());

        super.setupModule(context);
    }
};
