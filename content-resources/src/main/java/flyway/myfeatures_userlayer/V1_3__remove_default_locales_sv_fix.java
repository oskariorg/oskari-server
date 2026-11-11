package flyway.myfeatures_userlayer;

import java.util.Set;
import org.json.JSONObject;

public class V1_3__remove_default_locales_sv_fix extends V1_2__remove_default_locales {

    // this came in handy as the swedish default name was typoed in the original migration v1.2
    public Set<String> getDefaultValuesForLang(String lang) {
        return switch (lang) {
            case "fi" -> Set.of("Oma karttataso", "Omat aineistot");
            case "sv" -> Set.of("Mitt kartlager", "Användarens kartlager");
            case "en" -> Set.of("My map layer", "User layers");
            case "is" -> Set.of("Kortalagið mitt");
            default -> Set.of();
        };
    }
    // we don't need to do these since they have been done in migration 1.2
    public boolean removeUnrecognizedFields(JSONObject locale) {
        return false;
    }
    // we don't need to do these since they have been done in migration 1.2
    public boolean addMissingDefaults(JSONObject locale) {
        return false;
    }

}
