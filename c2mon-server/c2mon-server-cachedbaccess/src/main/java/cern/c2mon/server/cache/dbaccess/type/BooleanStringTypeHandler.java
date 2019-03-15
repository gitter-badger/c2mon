package cern.c2mon.server.cache.dbaccess.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(Boolean.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class BooleanStringTypeHandler extends BaseTypeHandler<Boolean> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Boolean aBoolean, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(i, aBoolean ? "Y" : "N");
    }
    @Override
    public Boolean getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return getBoolean(resultSet.getString(s));
    }
    @Override
    public Boolean getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return getBoolean(resultSet.getString(i));
    }
    @Override
    public Boolean getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return getBoolean(callableStatement.getString(i));
    }
    private Boolean getBoolean(String s) {
        return "Y".equalsIgnoreCase(s)|| "ACTIVE".equalsIgnoreCase(s);
    }

}
