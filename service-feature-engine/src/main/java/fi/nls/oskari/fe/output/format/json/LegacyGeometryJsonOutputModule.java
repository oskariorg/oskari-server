package fi.nls.oskari.fe.output.format.json;


import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fi.nls.oskari.fe.output.jackson.LegacyGeometryPropertySerializer;

public class LegacyGeometryJsonOutputModule extends SimpleModule {

    LegacyGeometryJsonOutputModule() {
        super("SimpleModule", new Version(1, 0, 0, null));
    }

    @Override
    public void setupModule(SetupContext context) {
        addSerializer(new LegacyGeometryPropertySerializer());

        super.setupModule(context);
    }
};
