package fi.nls.oskari.domain.utils;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

import java.sql.SQLException;
import java.sql.Types;

/**
 * Created with IntelliJ IDEA.
 * User: TMIKKOLAINEN
 * Date: 10.9.2013
 * Time: 9:47
 * To change this template use File | Settings | File Templates.
 */
public class JSONTypeHandler implements TypeHandlerCallback {

    public void setParameter(ParameterSetter parameterSetter, Object parameter) throws SQLException {
        if (parameter == null) {
            parameterSetter.setNull(Types.VARCHAR);
        } else {
            parameterSetter.setString(((JSONObject)parameter).toString());
        }
    }

    public Object getResult(ResultGetter resultGetter) throws SQLException {
        String value = resultGetter.getString();
        if (resultGetter.wasNull()) {
            return null;
        }
        return valueOf(value);
    }

    public Object valueOf(String s) {
        JSONObject jsonObject = JSONHelper.createJSONObject(s);
        return jsonObject;
    }
}
