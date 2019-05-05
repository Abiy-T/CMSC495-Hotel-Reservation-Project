/*
 * Alex Dwivedi
 * 4/20/2019
 * CMSC 495
 */

package app.db;

import org.apache.derby.jdbc.EmbeddedDriver;

import java.sql.*;

public class Database {
    private static final String DRIVER = "jdbc:derby:";
    private static final String NAME = "database";
    private static final Connection CONN = initDerby();

    public static Connection getConnection() {
        return CONN;
    }

    private static Connection initDerby() {
        try {
            DriverManager.registerDriver(new EmbeddedDriver());
            var conn = DriverManager.getConnection(DRIVER + NAME + ";create=true");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    DriverManager.getConnection(DRIVER + ";shutdown=true");
                } catch (SQLException ex) {
                    if (ex.getErrorCode() != 50000 || !"XJ015".equals(ex.getSQLState())) {
                        throw new DatabaseException("Derby shutdown abnormally", ex);
                    }
                }
            }));
            return conn;
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to establish derby connection", ex);
        }
    }

}
