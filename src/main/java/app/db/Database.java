/*
 * Alex Dwivedi
 * 4/20/2019
 * CMSC 495
 */

package app.db;

import org.apache.derby.jdbc.EmbeddedDriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final Connection CONN = initDerby();
    public static final String DRIVER = "jdbc:derby:";
    public static final String NAME = "database";

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
                        System.err.println("Derby did not shut down normally");
                        System.err.println(ex.getMessage());
                    }
                }
            }));
            return conn;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to establish derby connection");
        }
    }
}
