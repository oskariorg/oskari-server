package fi.nls.oskari.csw.worker;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.worker.ScheduledJob;

import java.util.Map;

/**
 * Dummy skeleton for scheduled job
 */
@Oskari("CSWCoverageImport")
public class CSWCoverageUpdateService extends ScheduledJob {
    private static final Logger log = LogFactory.getLogger(CSWCoverageUpdateService.class);

    @Override
    public void execute(Map<String, Object> params) {
        log.debug("CSWCoverageUpdateService running: ", PropertyUtil.get("jdbc.default.jndi.name"), PropertyUtil.get("db.jndi.name"));
        System.out.println("sysout: CSWCoverageUpdateService running: "
                + PropertyUtil.get("jdbc.default.jndi.name")
                + " " + PropertyUtil.get("db.jndi.name"));
    }
}
