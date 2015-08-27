package fi.nls.oskari.utils;


import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.Module;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * This uses the Jackson 1.x version since it's used by the current version of CometD.
 * Don't upgrade if not upgrading CometD.
 */
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
