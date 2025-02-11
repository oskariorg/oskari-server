package fi.nls.oskari.mybatis;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.apache.commons.text.StringEscapeUtils;
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
import java.util.Collections;
import java.util.Map;

@MappedTypes({Map.class})
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.NULL})
public class JsonMapMybatisTypeHandler extends BaseTypeHandler<Map> {
	private final static Logger log = LogFactory.getLogger(JsonMapMybatisTypeHandler.class);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map parameter, JdbcType jdbcType) throws SQLException {
        JSONObject json = new JSONObject(parameter);
        ps.setString(i, json.toString());
    }

    @Override
    public Map getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return valueOf(rs.getString(columnName));
    }

    @Override
    public Map getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return valueOf(rs.getString(columnIndex));
    }

    @Override
    public Map getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return valueOf(cs.getString(columnIndex));
    }

    public Map valueOf(String s) {
        if (s == null) {
            return Collections.emptyMap();
        }
        try {
            return JSONHelper.getObjectAsMap(new JSONObject(s));
        } catch (JSONException e) {
            if (s.startsWith("\"{") && s.endsWith("}\"") && s.length() > 5) {
                // H2 DB wraps a stringified json to quotes and escapes the content
                String unwrapped = s.substring(1, s.length()  - 1);
                return valueOf(StringEscapeUtils.unescapeJava(unwrapped));
            }
            log.info("Couldn't parse DB string to JSONObject:", s, e);
            return Collections.emptyMap();
        }
    }
}
