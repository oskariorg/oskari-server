package fi.nls.oskari.wfs;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import net.opengis.wfs.WFSCapabilitiesType;
import net.opengis.ows10.*;

import java.util.*;

import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.*;

public class WFSCapabilitiesParser110 extends WFSCapabilitiesService {
    public static JSONObject parse (WFSCapabilitiesType capa) {
        JSONObject json = new JSONObject();
        OperationsMetadataType meta = capa.getOperationsMetadata();
        List<OperationType> operations = meta.getOperation();
        Optional<OperationType> featureOp = operations
                .stream()
                .filter(Objects::nonNull)
                .filter(ot -> ot.getName().toLowerCase().equals(GET_FEATURE))
                .findFirst();
        if (featureOp.isPresent()) {
            List<DomainType> params = featureOp.get().getParameter();
            Optional<DomainType> format = params
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(p -> p.getName().toLowerCase().equals(OUTPUT_FORMAT))
                    .findFirst();
            if (format.isPresent()) {
                Set<String> formats = new HashSet<>(format.get().getValue());
                JSONHelper.putValue(json, KEY_FEATURE_OUTPUT_FORMATS, new JSONArray(WFSCapabilitiesService.getFormatsToStore(formats)));
            }
        }
        List <DomainType> constraints = meta.getConstraint();
        Optional<DomainType> maxFeatures = constraints
                .stream()
                .filter(Objects::nonNull)
                .filter(dt ->  dt.getName().toLowerCase().equals(MAX_FEATURES))
                .findFirst();
        if (maxFeatures.isPresent()){
            List<Object> values = maxFeatures.get().getValue();
            if (!values.isEmpty()) {
                int max = Integer.parseInt(values.get(0).toString());
                JSONHelper.putValue(json, KEY_MAX_FEATURES, max);
            }
        }
        return json;
    }
}
