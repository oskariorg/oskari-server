package fi.nls.oskari.wfs;

import fi.nls.oskari.util.JSONHelper;
import net.opengis.wfs20.WFSCapabilitiesType;
import org.eclipse.emf.ecore.EObject;
import org.json.JSONArray;
import org.json.JSONObject;
import net.opengis.ows11.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.OUTPUT_FORMAT;
import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.GET_FEATURE;
import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.COUNT;
import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.KEY_MAX_FEATURES;
import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.KEY_FEATURE_OUTPUT_FORMATS;

public class WFSCapabilitiesParser200 extends WFSCapabilitiesService {
    public static JSONObject parse (WFSCapabilitiesType capa) {
        JSONObject json = new JSONObject();
        List<OperationType> operations = capa.getOperationsMetadata().getOperation();
        Optional<OperationType> featureOp = operations
                .stream()
                .filter(ot -> ot.getName() != null && !ot.getName().isEmpty())
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
                Optional<AllowedValuesType> avt = format.get().eContents()
                        .stream()
                        .filter(e -> e instanceof AllowedValuesType)
                        .map(e -> (AllowedValuesType) e)
                        .findFirst();
                if (avt.isPresent()) {
                    Set<String> formats  =  (Set) avt.get().getValue()
                            .stream()
                            .filter(e -> e instanceof ValueType)
                            .map(e -> ((ValueType) e).getValue())
                            .collect(Collectors.toSet());
                    JSONHelper.put(json, KEY_FEATURE_OUTPUT_FORMATS, new JSONArray(WFSCapabilitiesService.getFormatsToStore(formats)));
                }
            }
            List<DomainType> constraints = featureOp.get().getConstraint();
            Optional<DomainType> count = constraints
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(c -> c.getName().toLowerCase().equals(COUNT))
                    .findFirst();
            if (count.isPresent()) {
                for (EObject e : count.get().eContents()) {
                    if (e instanceof ValueType) {
                        JSONHelper.putValue(json, KEY_MAX_FEATURES, ((ValueType) e).getValue());
                    }
                }
            }
        }
        return json;
    }
}
