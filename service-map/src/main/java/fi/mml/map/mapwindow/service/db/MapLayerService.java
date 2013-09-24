package fi.mml.map.mapwindow.service.db;

import java.util.List;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.CapabilitiesCache;
import fi.nls.oskari.domain.map.Layer;
import fi.nls.oskari.domain.map.stats.StatsVisualization;
import fi.nls.oskari.domain.map.wfs.WFSLayer;
import fi.nls.oskari.service.db.BaseService;

/**
 * Interface for Layerclass service
 * 
 *
 */
public interface MapLayerService extends BaseService<Layer> {

	/**
	 * Return maplayers for given layerclass
	 * 
	 * @param layerClassId
	 * @return
	 */
	public List<Layer> findWithLayerClass(int layerClassId);
	
	/**
	 * Return maplayers for given inspire theme
	 * 
	 * @param inspireThemeId
	 * @return
	 */
	public List<Layer> findWithInspireTheme(int inspireThemeId);
	
	/**
	 * Insert WFS layer.
	 * 
	 * @param wfsLayer
	 */
	public int insertWFSLayer(WFSLayer wfsLayer);
	
	/**
	 * Modify WFS layer.
	 * 
	 * @param wfsLayer
	 */
	public void modifyWFSLayer(WFSLayer wfsLayer);
	
	/**
	 * Return WFS layer with given id.
	 * 
	 * @param wfsLayerId
	 * @return
	 */
	public WFSLayer findWFSLayer(int wfsLayerId);
	
	public List<Layer> findAllWMS();
	
	public int insertCapabilities(CapabilitiesCache cc);
	public void updateCapabilities(CapabilitiesCache cc);
	
	public CapabilitiesCache getCapabilitiesCache(int id);
	public boolean hasPermissionToUpdate(final User user, final int layerId);
	public List<StatsVisualization> findStatsLayerVisualizations(final int layerId);
}
