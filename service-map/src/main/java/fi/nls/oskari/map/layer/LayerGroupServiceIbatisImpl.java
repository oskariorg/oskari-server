package fi.nls.oskari.map.layer;

import fi.nls.oskari.domain.map.LayerGroup;
import fi.nls.oskari.service.db.BaseIbatisService;


/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 16.12.2013
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
public class LayerGroupServiceIbatisImpl extends BaseIbatisService<LayerGroup> implements LayerGroupService {

    @Override
    protected String getNameSpace() {
        return "LayerGroup";
    }
}
