package fi.nls.oskari.domain.utils;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

public class UuIdTypeHandler implements TypeHandlerCallback {

    private final static Logger log = LogFactory.getLogger(JSONTypeHandler.class);
	
	@Override
	public void setParameter(ParameterSetter setter, Object parameter)
			throws SQLException {
		// TODO Auto-generated method stub
		log.debug("setParameter");
		String uuId = ((String)parameter);
		if (uuId == null) {
			setter.setNull(Types.VARCHAR);
		} else {
			setter.setObject(uuId);
		}

	}

	@Override
	public Object getResult(ResultGetter getter) throws SQLException {
		try{
			log.debug("geting Result");
			return (String)getter.getObject();
		} catch(Exception e) {
			throw new SQLException ("Something went wrong when making uuid conversoin in UuidTypeHandlder." + e.getMessage());
		}
	}

	@Override
	public Object valueOf(String s) {
		try{
			log.debug("value of");
			return UUID.fromString(s);
		} catch(Exception e) {
            log.error("Couldn't parse s to UUID:", s, e);
            return null;
		}
	}

}
