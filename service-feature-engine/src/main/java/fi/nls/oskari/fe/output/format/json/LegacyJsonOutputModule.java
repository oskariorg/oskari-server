package fi.nls.oskari.fe.output.format.json;


import fi.nls.oskari.fe.iri.Resource;
import org.apache.commons.lang3.tuple.Pair;
public class LegacyJsonOutputModule extends LegacyGeometryJsonOutputModule {
    static <T> Class getClazz(T... param) {
        return param.getClass().getComponentType();
    }

    @Override
    public void setupModule(SetupContext context) {
        addSerializer(
                LegacyJsonOutputModule
                        .<Pair<Resource, Object>> getClazz(),
                new LegacyPairSerializer());

        super.setupModule(context);
    }
}
