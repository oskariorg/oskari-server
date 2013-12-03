package fi.mml.map.mapwindow.service.db;

import fi.nls.oskari.domain.map.wfs.WFSService;

import java.util.List;

/**
 * @deprecated Use WFSLayerConfigurationService instead
 */
@Deprecated
public interface WFSDbService {
	public int insertWFSService(WFSService wfsService);
	public void updateWFSService(WFSService wfsService);
	public WFSService findWFSService(int wfsServiceId);
	public List<WFSService> findAll();
	public void delete(int id);
}
