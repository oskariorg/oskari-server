package fi.mml.map.mapwindow.service.db;

import com.ibatis.sqlmap.client.SqlMapClient;
import fi.nls.oskari.domain.map.wfs.*;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WFSDbServiceIbatisImpl extends BaseIbatisService<WFSService> implements WFSDbService {
	
	SqlMapClient sqlMapClient;
	
	public WFSDbServiceIbatisImpl() {		
	}
	
	public WFSDbServiceIbatisImpl(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}
	
	@Override
	protected String getNameSpace() {
		return "WFSService";
	}
	
	@Override
	public WFSService findWFSService(int wfsServiceId) {
		
		// search database
		WFSService wfsService = find(wfsServiceId);
		
		List<FeatureType> featureTypes =
			queryForList(getNameSpace() + ".findFeatureTypesOfWFSService", wfsServiceId);
		List<FeatureParameter> featureParameters = 
			queryForList(getNameSpace() + ".findFeatureParametersOfWFSService", wfsServiceId);
		List<OwsOperation> owsOperations = 
			queryForList(getNameSpace() + ".findOwsOperationsOfWFSService", wfsServiceId);
		
		// process results
		// KEY: featureTypeId, VALUE: feature type
		Map<Integer, FeatureType> featureTypeMap = new HashMap<Integer, FeatureType>();
		
		for (FeatureType featureType : featureTypes) {
			featureTypeMap.put(featureType.getId(), featureType);
		}
		
		for (FeatureParameter featureParameter : featureParameters) {
			FeatureType featureType = featureTypeMap.get(featureParameter.getFeatureType().getId());
			featureType.getFeatureParameters().add(featureParameter);
		}
		
		// set associations
		wfsService.setFeatureTypes(featureTypes);
		wfsService.setOwsOperations(owsOperations);
		
		return wfsService;
	}

    public List<WFSSLDStyle> findWFSLayerStyles(int wfsId) {
        List<WFSSLDStyle> styleList = queryForList(getNameSpace() + ".findLayerStyles", wfsId);
        return styleList;
    }

	@Override
	public int insertWFSService(WFSService wfsService) {
		Integer wfsServiceId = null;
		SqlMapClient client = null;
		try {
			client = getSqlMapClient();
			client.startTransaction();
			client.insert(getNameSpace() + ".insert", wfsService);
			wfsServiceId = (Integer) client.queryForObject(getNameSpace() + ".maxId");
			wfsService.setId(wfsServiceId);
			insertWfsServiceAssociations(client, wfsService);
			client.commitTransaction();
		} catch (Exception e) {
			throw new RuntimeException("Failed to insert", e);
		} finally {
			if (client != null) {
				try {
					client.endTransaction();
				} catch (SQLException e) {
					// forget
				}
			}
		}
		return wfsServiceId;
	}
	
	@Override
	public void updateWFSService(WFSService wfsService) {
		SqlMapClient client = null;
		try {
			client = getSqlMapClient();
			client.startTransaction();
			client.update(getNameSpace() + ".update", wfsService);
			client.delete(getNameSpace() + ".deleteFeatureParameters", wfsService.getId());
			client.delete(getNameSpace() + ".deleteFeatureTypes", wfsService.getId());
			client.delete(getNameSpace() + ".deleteOwsOperations", wfsService.getId());
			insertWfsServiceAssociations(client, wfsService);
			client.commitTransaction();
		} catch (Exception e) {
			throw new RuntimeException("Failed to update", e);
		} finally {
			if (client != null) {
				try {
					client.endTransaction();
				} catch (SQLException e) {
					// forget
				}
			}
		}
	}
	
	private void insertWfsServiceAssociations(SqlMapClient client, WFSService wfsService) throws SQLException {		
		for (FeatureType featureType : wfsService.getFeatureTypes()) {
			featureType.setWfsService(wfsService);
			client.insert(getNameSpace() + ".insertFeatureTypes", featureType);
			int featureTypeId = (Integer) client.queryForObject(getNameSpace() + ".maxIdFeatureType");
			featureType.setId(featureTypeId);
			
			for (FeatureParameter featureParameter : featureType.getFeatureParameters()) {
				featureParameter.setFeatureType(featureType);
				
				if (featureParameter.getParentId() > 0) {
					client.insert(getNameSpace() + ".insertFeatureParametersWithParentId", featureParameter);
				} else {
					client.insert(getNameSpace() + ".insertFeatureParametersNoParentId", featureParameter);
				}
			}
		}
		
		for (OwsOperation owsOperation : wfsService.getOwsOperations()) {
			owsOperation.setWfsService(wfsService);
			client.insert(getNameSpace() + ".insertOwsOperations", owsOperation);
		}
	}
}
