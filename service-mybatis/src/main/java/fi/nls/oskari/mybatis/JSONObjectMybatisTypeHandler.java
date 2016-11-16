package fi.nls.oskari.mybatis;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes({JSONObject.class})
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.NULL})
public class JSONObjectMybatisTypeHandler extends BaseTypeHandler<JSONObject> {
	private final static Logger log = LogFactory.getLogger(JSONObjectMybatisTypeHandler.class);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JSONObject parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.toString());
    }

    @Override
    public JSONObject getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return valueOf(rs.getString(columnName));
    }

    @Override
    public JSONObject getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return valueOf(rs.getString(columnIndex));
    }

    @Override
    public JSONObject getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return valueOf(cs.getString(columnIndex));
    }

    public JSONObject valueOf(String s) {
        try {
            return new JSONObject(s);
        } catch (JSONException e) {
            log.error("Couldn't parse DB string to JSONObject:", s, e);
            return new JSONObject();
        }
    }
}
