package fi.nls.oskari.helpers;

import java.util.ArrayList;
import java.util.List;

import fi.nls.oskari.service.OskariComponentManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.wfs.WFSSearchChannelsConfiguration;
import fi.nls.oskari.wfs.WFSSearchChannelsService;
import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class SearchWFSChannelHelper {
	
	private static final Logger log = LogFactory.getLogger(SearchWFSChannelHelper.class);
	
	/**
	 * Get WFS channels
	 * @param lang
	 * @param user 
	 * @return
	 */
	public static JSONObject getChannels(User user, String lang) throws JSONException{
		WFSSearchChannelsService channelService = OskariComponentManager.getComponentOfType(WFSSearchChannelsService.class);
		JSONObject job = new JSONObject();
		List<WFSSearchChannelsConfiguration> channels = channelService.findChannels();
		JSONArray channelsJSONArray = new JSONArray();
		
		for(int i=0;i<channels.size();i++){
			WFSSearchChannelsConfiguration channel = channels.get(i);
			JSONObject channelJSON = channel.getAsJSONObject();
			List<String> layerIds = new ArrayList<String>();
			layerIds.add(String.valueOf(channel.getWFSLayerId())); 
			JSONObject userLayers = OskariLayerWorker.getListOfMapLayersById(layerIds, user, lang);
			JSONArray layers = userLayers.getJSONArray(OskariLayerWorker.KEY_LAYERS);

			if(layers.length() > 0){
				channelsJSONArray.put(channelJSON);
			}
		}
	   	
	   	job.put("channels", channelsJSONArray);
	   	 
	   	return job;
	}
	
	/**
	 * Get WFS channels id's
	 * @return JSONArray
	 */
	public static JSONArray getDefaultChannelsIds() throws JSONException{
		WFSSearchChannelsService channelService = OskariComponentManager.getComponentOfType(WFSSearchChannelsService.class);
		JSONArray job = new JSONArray();
		List<WFSSearchChannelsConfiguration> channels = channelService.findChannels();
		
		for(int i=0;i<channels.size();i++){
			WFSSearchChannelsConfiguration channel = channels.get(i);
			if(channel.getIsDefault()){
				job.put(channel.getId());
			}
		}
	   	 
	   	return job;
	}
	
	/**
	 * Get channel by id
	 * @param channelIds
	 * @return
	 * @throws JSONException 
	 */
	public static List<WFSSearchChannelsConfiguration> getChannelById(JSONArray channelIds) throws JSONException{

		WFSSearchChannelsService channelService = OskariComponentManager.getComponentOfType(WFSSearchChannelsService.class);
		List<WFSSearchChannelsConfiguration> channels = new ArrayList<WFSSearchChannelsConfiguration>();
		for (int i = 0; i < channelIds.length(); i++) {
			channels.add(channelService.findChannelById(channelIds.getInt(i)));
		}
	   	 
	   	return channels;
	}
	
	/**
	 * Delete selected channel
	 * @param channelId
	 */
	public static JSONObject delete(final int channelId) {
		WFSSearchChannelsService channelService = OskariComponentManager.getComponentOfType(WFSSearchChannelsService.class);
		JSONObject job = new JSONObject();
		try{
			channelService.delete(channelId);
			job.put("success", true);
		} catch (Exception e) {
			try{
				job.put("success", false);
			} catch (Exception ex) {}
		}
		return job;
	}
	
	/**
	 * Add WFS channel
	 * @param channel
	 */
	public static JSONObject insert(final WFSSearchChannelsConfiguration channel) {
		WFSSearchChannelsService channelService = OskariComponentManager.getComponentOfType(WFSSearchChannelsService.class);
		JSONObject job = new JSONObject();
		try{
			long newId = channelService.insert(channel);
			job.put("success", newId > 0);
		} catch (Exception e) {
			log.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			try{
				job.put("success", false);
			} catch (Exception ex) {}
		}
		return job;
	}
	
	/**
	 * Update WFS channel
	 * @param channel
	 */
	public static JSONObject update(final WFSSearchChannelsConfiguration channel) {
		WFSSearchChannelsService channelService = OskariComponentManager.getComponentOfType(WFSSearchChannelsService.class);
		JSONObject job = new JSONObject();
		try{
			channelService.update(channel);
			job.put("success", true);
		} catch (Exception e) {
			log.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			try{
				job.put("success", false);
			} catch (Exception ex) {}
		}
		return job;
	}
}
