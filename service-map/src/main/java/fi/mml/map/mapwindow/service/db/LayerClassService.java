package fi.mml.map.mapwindow.service.db;

import java.util.List;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.InspireTheme;
import fi.nls.oskari.domain.map.wms.LayerClass;
import fi.nls.oskari.service.db.BaseService;

/**
 * Interface for Layerclass service
 * 
 *
 */
public interface LayerClassService extends BaseService<LayerClass> {

	/**
	 * Finds layerclasses with given parent
	 * 
	 * @param parentId
	 * @return List of LayerClasses
	 */
	public List<LayerClass> findWithParent(int parentId);
	
	/**
	 * Finds layerclasses where parent is not null
	 * 
	 * @return List of LayerClasses
	 */
	public List<LayerClass> findWhereParentNotNull();
	
	/**
	 * Returns nested layerclasse datastructure
	 * grouped in "organizational" form, so that every top level layerclass
	 * represents an organization
	 * 
	 * @return List of LayerClasses i.e. Organizations
	 */
	public List<LayerClass> findOrganizationalStructure();


    /**
     * Returns nested layerclasse datastructure
     * grouped in "organizational" form, so that every top level layerclass
     * represents an organization
     * @param showWithoutSublayers
     * @return List of LayerClasses i.e. Organizations
     */
    public List<LayerClass> findOrganizationalStructure(boolean showWithoutSublayers);
	
	
	public LayerClass findOrganizationalStructureByClassId(int classId);
	
	/**
	 * Returns nested layerclasse datastructure
	 * grouped in "inspire theme" form, so that every top level layerclass
	 * represents an inspire theme
	 * 
	 * @return List of LayerClasses i.e. Inspire themes
	 */
	public List<LayerClass> findInspireThemeStructure();

	public InspireTheme getInspireThemeByLayerId(Integer inspireThemeId);
	
	public boolean hasPermissionToUpdate(final User user, final int layerclId);
	
	
}
