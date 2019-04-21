/*
 * Alex Dwivedi
 * 4/20/2019
 * CMSC 495
 */

package app.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

public class Updates {

    public static <T> ResultSet insert(String table, List<String> columns, List<T> values) throws SQLException {
        var typeMap = Tables.getTypes();
        var types = columns.stream().map(typeMap::get).collect(Collectors.toList());
        return insert(table, columns, types, values);
    }

    // Returns the set of keys generated from the final insertion only.
    // Documented here: https://db.apache.org/derby/docs/10.13/ref/crefjavstateautogen.html
    public static <T> ResultSet insert(String table, List<String> columns,
                                       List<SQLType> types, List<T> values) throws SQLException {
        var conn = Database.getConnection();
        String stmt = "insert into " + table + " (" + String.join(", ", columns)
                + ") values (" + "?, ".repeat(columns.size() - 1) + "?)";
        var prepStmt = conn.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS);
        for (var valIt = values.listIterator(); valIt.hasNext(); ) {
            for (var typeIt = types.listIterator(); typeIt.hasNext(); ) {
                prepStmt.setObject(typeIt.nextIndex() + 1, valIt.next(), typeIt.next());
            }
            prepStmt.addBatch();
        }
        prepStmt.executeBatch();
        return prepStmt.getGeneratedKeys();
    }

}
