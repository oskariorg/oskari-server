package fi.nls.oskari.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class GeometryMybatisTypeHandler extends BaseTypeHandler<Geometry> {

    private static final Logger LOG = LogFactory.getLogger(GeometryMybatisTypeHandler.class);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Geometry parameter, JdbcType jdbcType) throws SQLException {
        ps.setBytes(i, new WKBWriter().write(parameter));
    }

    @Override
    public Geometry getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return valueOf(rs.getBytes(columnName));
    }

    @Override
    public Geometry getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return valueOf(rs.getBytes(columnIndex));
    }

    @Override
    public Geometry getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return valueOf(cs.getBytes(columnIndex));
    }

    private static Geometry valueOf(byte[] ewkb) {
        if (ewkb == null) {
            return null;
        }
        try {
            return new WKBReader().read(ewkb);
        } catch (Exception e) {
            LOG.error("Couldn't parse DB geometry to Geometry", e);
            return null;
        }
    }

}
