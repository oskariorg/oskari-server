package fi.nls.oskari.mybatis;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.locationtech.jts.geom.Envelope;

public class EnvelopeMybatisTypeHandler extends BaseTypeHandler<Envelope> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Envelope parameter, JdbcType jdbcType)
            throws SQLException {
        Double[] elements = {
            parameter.getMinX(),
            parameter.getMinY(),
            parameter.getMaxX(),
            parameter.getMaxY()
        };
        Array array = ps.getConnection().createArrayOf("double", elements);
        try {
            ps.setArray(i, array);
        } finally {
            array.free();
        }
    }

    @Override
    public Envelope getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return extractArray(rs.getArray(columnName));
    }

    @Override
    public Envelope getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return extractArray(rs.getArray(columnIndex));
    }

    @Override
    public Envelope getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return extractArray(cs.getArray(columnIndex));
    }
    
    private static Envelope extractArray(Array array) throws SQLException {
        if (array == null) {
            return null;
        }
        Object javaArray = array.getArray();
        array.free();

        // Envelope constructor takes parameters in order minX, maxX, minY, maxY
        // whereas we stored them in more humane order of minX, minY, maxX, maxY
        if (javaArray instanceof Object[] oa) {
            return new Envelope(
                ((Number)oa[0]).doubleValue(),
                ((Number)oa[2]).doubleValue(),
                ((Number)oa[1]).doubleValue(),
                ((Number)oa[3]).doubleValue()
            );
        } else if (javaArray instanceof Double[] da) {
            return new Envelope(da[0], da[2], da[1], da[3]);
        } else {
            throw new IllegalArgumentException();
        }
        
    }
    
}