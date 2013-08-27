package fi.nls.oskari.service.db;

import java.util.List;
import java.util.Map;

/**
 * Base service interface for parameterized type
 *
 * @param <E>
 */
public interface BaseService<E> {

	/**
	 * Returns one object
	 * 
	 * @param id
	 * @return
	 */
	public E find(int id);
	
	/**
	 * Returns one object
	 * 
	 * @param id
	 * @return
	 */
	public E find(String id);
	
	/**
	 * Returns all objects
	 * 
	 * @return
	 */
	public List<E> findAll();

	/**
	 * Updates an object
	 * 
	 * @param layerClass
	 */
	public void update(E layerClass);
	
	/**
	 * Inserts an object
	 * 
	 * @param layerClass
	 * @return
	 */
	public int insert(E layerClass);
	
	/**
	 * Deletes an object
	 * 
	 * @param id
	 */
	public void delete(int id);
	
	/**
	 * Deletes an object
	 * 
	 * @param parameterMap
	 */
	public void delete(Map<String, String> parameterMap);
}
