package fi.nls.oskari.utils;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class GeometryJSONOutputModule extends SimpleModule {

    public GeometryJSONOutputModule() {
        super("SimpleModule", new Version(1, 0, 0, null));
    }

    @Override
    public void setupModule(Module.SetupContext context) {
        addSerializer(new GeometrySerializer());

        super.setupModule(context);
    }
};
