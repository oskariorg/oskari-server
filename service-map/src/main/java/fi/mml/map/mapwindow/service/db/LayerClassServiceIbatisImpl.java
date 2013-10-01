package fi.mml.map.mapwindow.service.db;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.InspireTheme;
import fi.nls.oskari.domain.map.Layer;
import fi.nls.oskari.domain.map.wms.LayerClass;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.util.ArrayList;
import java.util.List;

/**
 * LayerClass implementation for Ibatis
 * 
 *
 */
public class LayerClassServiceIbatisImpl extends BaseIbatisService<LayerClass> implements LayerClassService {

	private MapLayerService mapLayerService = new MapLayerServiceIbatisImpl();
	private InspireThemeService inspireThemeService = new InspireThemeServiceIbatisImpl();
	
	
	public InspireTheme getInspireThemeByLayerId(Integer layerId) {
		return inspireThemeService.find(layerId);
	}
	
	@Override
	protected String getNameSpace() {
		return "LayerClass";
	}
	
	 public boolean hasPermissionToUpdate(final User user, final int layerclId) {

	        // TODO: check against permissions
	        if(!user.isAdmin()) {
	            return false;
	        }
	        if (layerclId <= -1){
	            // TODO: maybe check if we have a layer with given id in DB 
	            return false;
	        }
	        return true;
	    }
	
	public List<LayerClass> findWithParent(int parentId) {
		return queryForList(getNameSpace() + ".findWithParent", parentId);
	}

	public List<LayerClass> findWhereParentNotNull() {
		return queryForList(getNameSpace() + ".findWhereParentNotNull");
	}

	public List<LayerClass> findInspireThemeStructure() {
		List<InspireTheme> allThemes = inspireThemeService.findAll();
		
		// Create layerclasses representing inspire themes
		List<LayerClass> layerClasses = new ArrayList<LayerClass>();
		for(InspireTheme t: allThemes) {
			LayerClass lc = new LayerClass();
            lc.setNames(t.getNames());
			lc.setId(t.getId());
			layerClasses.add(lc);
		}
		fillMapLayersForInspireThemes(layerClasses);
		
		// Find basemaps. They are the ones that have parent layerclass set.
		// Attach basemap to basemap parent
        // FIXME no hardcoded locales
		LayerClass backgroundMap = new LayerClass();
		backgroundMap.setName("fi", "Taustakartat");
		backgroundMap.setName("sv", "Bakgrundskartor");
		backgroundMap.setName("en", "Background Maps");
		layerClasses.add(backgroundMap);
		
		List<LayerClass> baseMapLayers = findWhereParentNotNull();
		fillMapLayersForClasses(baseMapLayers);
		for(LayerClass baseMapLayer: baseMapLayers) {
			backgroundMap.addChild(baseMapLayer);
		}
				
		return layerClasses;
	}
	

	public List<LayerClass> findOrganizationalStructure() {
		return findOrganizationalStructure(true);
	}

    public List<LayerClass> findOrganizationalStructure(boolean showWithoutSublayers) {
        List<LayerClass> allLayers = findAll();
        fillMapLayersForClasses(allLayers);

        for (LayerClass layer: allLayers) {

            List<LayerClass> subLayers = findWithParent(layer.getId());
            if (showWithoutSublayers && subLayers.size() == 0) {
                // do not process empty sublayers
                continue;
            }

            fillMapLayersForClasses(subLayers);
            layer.addChildren(subLayers);
        }

        return allLayers;
    }

	public LayerClass findOrganizationalStructureByClassId(int classId) {
		LayerClass layerClass = this.find(classId);
		//fillMapLayersForClasses(allLayers);
		
		List<Layer> mapLayers = mapLayerService.findWithLayerClass(classId);
		layerClass.addMapLayers(mapLayers);
		
		List<LayerClass> subLayers = findWithParent(classId);//findWithParent(classId);
		
		fillMapLayersForClasses(subLayers);
		layerClass.addChildren(subLayers);
				
		return layerClass;
	}
	
	
	
	private List<LayerClass> fillMapLayersForClasses(List<LayerClass> layerClasses) {
		for (LayerClass lc: layerClasses) {
			List<Layer> mapLayers = mapLayerService.findWithLayerClass(lc.getId());
			lc.addMapLayers(mapLayers);
		}
		return layerClasses;
	}
	
	private List<LayerClass> fillMapLayersForInspireThemes(List<LayerClass> layerClasses) {
		for (LayerClass lc: layerClasses) {
			List<Layer> mapLayers = mapLayerService.findWithInspireTheme(lc.getId());
			lc.addMapLayers(mapLayers);
		}
		return layerClasses;
	}
	

}
