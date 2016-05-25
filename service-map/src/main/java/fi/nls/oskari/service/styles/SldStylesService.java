package fi.nls.oskari.service.styles;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SldStylesService extends OskariComponent {

    private static final Logger LOG = LogFactory.getLogger(SldStylesService.class);

    public abstract List<SldStyle> selectAll();
    public abstract int saveStyle(SldStyle style);




}