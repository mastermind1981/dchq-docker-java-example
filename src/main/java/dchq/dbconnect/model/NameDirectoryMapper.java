package dchq.dbconnect.model;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @since 11/22/2015
 */
public class NameDirectoryMapper implements RowMapper<NameDirectory> {
    @Override
    public NameDirectory mapRow(ResultSet rs, int line) throws SQLException {
        NameDirectoryResultSetExtractor extractor = new NameDirectoryResultSetExtractor();
        return extractor.extractData(rs);
    }

    class NameDirectoryResultSetExtractor implements ResultSetExtractor<NameDirectory> {
        @Override
        public NameDirectory extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            NameDirectory nd = new NameDirectory();
            nd.setId(resultSet.getLong("id"));
            nd.setFirstName(resultSet.getString("firstName"));
            nd.setLastName(resultSet.getString("lastName"));
            nd.setCreatedTimestamp(resultSet.getTimestamp("createdTimestamp"));
            return nd;
        }
    }
}
