package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import static fi.nls.oskari.control.statistics.util.Constants.*;

/**
 * Returns indicators for statistical datasource registered to Oskari.
 */
@OskariActionRoute("StatisticalIndicatorValues")
public class StatisticalIndicatorValuesHandler extends ActionHandler {
    private final static Logger log = LogFactory.getLogger(StatisticalIndicatorValuesHandler.class);

    public void handleAction(ActionParameters params) throws ActionException {

        final int datasourceId = params.getRequiredParamInt(PARAM_DATASOURCE);
        final int indicatorId = params.getRequiredParamInt(PARAM_ID);

        throw new ActionDeniedException("Not implemented yet");
    }

}
