package fi.nls.oskari;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.spring.extension.OskariParam;
import org.oskari.status.AppStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Controller
public class StatusController {

    private Collection<AppStatus> getChecks() {
        Map<String, AppStatus> statuses = OskariComponentManager.getComponentsOfType(AppStatus.class);
        return statuses.values();
    }

    @RequestMapping("/health")
    public ResponseEntity<String> health() {
        boolean highSeverityChecksOk = getChecks().stream()
                .filter(s -> s.isEnabled())
                .filter(s -> s.getSeverity() == AppStatus.Severity.HIGH)
                .allMatch(s -> s.isOk());
        if (!highSeverityChecksOk) {
            return new ResponseEntity<String>("DISABLED", HttpStatus.SERVICE_UNAVAILABLE);
        }
        return new ResponseEntity("OK", HttpStatus.OK);
    }

    @RequestMapping("/status")
    public Collection<AppStatus> status(@OskariParam ActionParameters params) {
        if (!params.getUser().isAdmin()) {
            return null;
        }
        return getChecks();
    }

}
