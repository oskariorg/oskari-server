package fi.nls.oskari.mybatis;

import java.sql.*;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.json.JSONArray;
import org.json.JSONException;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

@MappedTypes({JSONArray.class})
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.NULL})
public class JSONArrayMybatisTypeHandler extends BaseTypeHandler<JSONArray> {
	private final static Logger log = LogFactory.getLogger(JSONArrayMybatisTypeHandler.class);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JSONArray parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.toString());
    }

    @Override
    public JSONArray getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return valueOf(rs.getString(columnName));
    }

    @Override
    public JSONArray getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return valueOf(rs.getString(columnIndex));
    }

    @Override
    public JSONArray getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return valueOf(cs.getString(columnIndex));
    }

    public JSONArray valueOf(String s) {
        if (s == null) {
            // Mimic JSONObjectMybatisTypeHandler
            return new JSONArray();
        }
        try {
            return new JSONArray(s);
        } catch (JSONException e) {
            if (s.startsWith("\"[") && s.endsWith("]\"")) {
                // H2 DB wraps a stringified json to quotes and escapes the content
                String unwrapped = s.substring(1, s.length()  - 1);
                return valueOf(StringEscapeUtils.unescapeJava(unwrapped));
            }
            log.error("Couldn't parse DB string to JSONArray:", s, e);
            return new JSONArray();
        }
    }
}
