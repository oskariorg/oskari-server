package fi.mml.map.mapwindow.service.db;

import java.util.List;

import fi.nls.oskari.domain.map.wfs.WFSService;

public interface WFSDbService {
	public int insertWFSService(WFSService wfsService);
	public void updateWFSService(WFSService wfsService);
	public WFSService findWFSService(int wfsServiceId);
	public List<WFSService> findAll();
	public void delete(int id);
	
}
