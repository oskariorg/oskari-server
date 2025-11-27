package org.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.*;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.domain.map.LayerExtendedOutput;
import org.oskari.service.maplayer.DescribeLayerQuery;
import org.oskari.service.maplayer.LayerProvider;
import org.oskari.user.User;

import java.util.*;

import static fi.nls.oskari.control.ActionConstants.PARAM_ID;
import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;

/**
 * An action route that returns metadata for layers
 */
@OskariActionRoute("DescribeLayer")
public class DescribeLayerHandler extends RestActionHandler {

    private static final String ERR_INVALID_ID = "Invalid id";
    private static final String PARAM_STYLES = "styleId";

    private Collection<LayerProvider> layerProviders;

    @Override
    public void init() {
        Map<String, LayerProvider> components = OskariComponentManager.getComponentsOfType(LayerProvider.class);
        this.layerProviders = components.values();
    }

    public void setLayerProviders(Collection<LayerProvider> layerProviders) {
        this.layerProviders = layerProviders;
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final String layerId = params.getRequiredParam(PARAM_ID);
        final User user = params.getUser();
        final String lang = params.getLocale().getLanguage();
        final CoordinateReferenceSystem crs = params.getHttpParam(PARAM_SRS) == null
            ? null
            : WKTHelper.getCRS(params.getHttpParam(PARAM_SRS));
        // link params or published map could have selected style which is created by another user
        final String styles = params.getHttpParam(PARAM_STYLES);
        final List<String> styleIds = styles != null ? Arrays.asList(styles.split(",")) : null;

        final DescribeLayerQuery query = new DescribeLayerQuery(layerId, user, lang, crs, styleIds);

        LayerExtendedOutput output = describeLayer(query);

        ResponseHelper.writeJsonResponse(params, output);
    }

    private LayerExtendedOutput describeLayer(DescribeLayerQuery query) throws ActionException {
        LayerProvider provider = layerProviders.stream()
            .filter(p -> p.maybeProvides(query.getLayerId()))
            .findAny()
            .orElseThrow(() -> new ActionParamsException(ERR_INVALID_ID));

        try {
            return provider.describeLayer(query);
        } catch (SecurityException e) {
            throw new ActionDeniedException(e.getMessage());
        }
    }

}
