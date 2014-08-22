package fi.nls.oskari.control.statistics;

import java.util.List;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Returns statistical datasources registered to Oskari.
 */
@OskariActionRoute("StatisticalDatasources")
public class StatisticalDatasourcesHandler extends ActionHandler {
    private final static Logger log = LogFactory.getLogger(StatisticalDatasourcesHandler.class);
	
	@Override
	public void handleAction(ActionParameters params) throws ActionException {
    	ResponseHelper.writeResponse(params, getDataSources());
    }
    
    
    private JSONObject getDataSources(){
    	
		List dataSources = getDataSourcesFromDB();
    	 if(dataSources != null){
    		 return null;
    	 }else{
    	     JSONArray datasources = new JSONArray();
    	     JSONObject response = JSONHelper.createJSONObject("dataSources", datasources);
    	     datasources.put(createDummyDS());
    	     datasources.put(createDummyDS2());
    	     return response;
    	 }
    }
    
    
    private List getDataSourcesFromDB(){
    	// TODO: load datasources from DB
    	return null;
    }
    
    private JSONObject createDummyDS() {
        JSONObject ds = new JSONObject();
        JSONHelper.putValue(ds, "id", 1);
        JSONHelper.putValue(ds, "locale", JSONHelper.createJSONObject("fi", "SotkaNet"));
        JSONArray options = new JSONArray();
        options.put(JSONHelper.createJSONObject("name", "year"));
        options.put(JSONHelper.createJSONObject("name", "sex"));
        JSONHelper.putValue(ds, "indicatorParams", options);
        return ds;
    }

    private JSONObject createDummyDS2() {
        JSONObject ds = new JSONObject();
        JSONHelper.putValue(ds, "id", 11);
        JSONHelper.putValue(ds, "locale", JSONHelper.createJSONObject("fi", "TestNet"));
        JSONArray options = new JSONArray();
        options.put(JSONHelper.createJSONObject("name", "year"));
        options.put(JSONHelper.createJSONObject("name", "sex"));
        JSONHelper.putValue(ds, "indicatorParams", options);
        return ds;
    }

}
