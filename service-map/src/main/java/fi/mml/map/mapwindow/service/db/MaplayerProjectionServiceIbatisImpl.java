package fi.mml.map.mapwindow.service.db;

import com.ibatis.sqlmap.client.SqlMapClient;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.sql.SQLException;
import java.util.*;

/**
 * Maplayer projections implementation for Ibatis
 * 
 *
 */
public class MaplayerProjectionServiceIbatisImpl extends BaseIbatisService implements MaplayerProjectionService {

    private static final Logger log = LogFactory.getLogger(MaplayerProjectionServiceIbatisImpl.class);


	@Override
	protected String getNameSpace() {
		return "MaplayerProjections";
	}


    public void delete(int id) {
        super.delete(id);
    }


    public synchronized void insertList(final int maplayerId, final Set<String> CRSs) {

        if(CRSs == null || CRSs.isEmpty()){
            return;
        }

        SqlMapClient client = null;
        try {
            client = getSqlMapClient();
            client.startTransaction();
            // remove old links
            client.delete(getNameSpace() + ".removeLayerProjections", maplayerId);

            final Map<String, Object> map = new HashMap<String, Object>();
            List<Crs> list =  new ArrayList<Crs>() ;
            for (String s :CRSs){
               list.add(new Crs(maplayerId, s));
            }
            map.put("list", list);
            client.insert(getNameSpace() + ".insertList", map);

            client.commitTransaction();

        } catch (Exception e) {
            throw new RuntimeException("Failed to set map projections", e);
        } finally {
            if (client != null) {
                try {
                    client.endTransaction();
                } catch (SQLException ignored) { }
            }
        }
    }
    static class Crs{
        int maplayerid;
        String name;

        Crs(int id, String name){
            this.maplayerid = id;
            this.name = name;
        }
    }

}
