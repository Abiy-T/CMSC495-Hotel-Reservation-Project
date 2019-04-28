/*
 * Alex Dwivedi
 * 4/20/2019
 * CMSC 495
 */

package app.db;

import org.apache.derby.tools.ij;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static app.db.Tables.ROOMS;

public class Utilities {

   public static void printResults(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        System.out.println("Results");
        int columnsNumber = rsmd.getColumnCount();
        while (rs.next()) {
            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1) System.out.print(",  ");
                String columnValue = rs.getString(i);
                System.out.print(columnValue + " " + rsmd.getColumnName(i));
            }
            System.out.println();
        }
   }

    public static boolean runScript(File script, OutputStream os) {
        try (FileInputStream fs = new FileInputStream(script)) {
            return ij.runScript(Database.getConnection(), fs, "UTF-8", os, "UTF-8") == 0;
        } catch (IOException ex) {
            throw new DatabaseException("IO error running script", ex);
        }
    }

    // Returns the set of keys generated from the final insertion only.
    // Documented here: https://db.apache.org/derby/docs/10.13/ref/crefjavstateautogen.html
    public static <T> List<Integer> insert(String table, List<String> columns, List<T> values) throws SQLException {
        return insert(table, columns, Tables.getTypes(columns), values);
    }

    public static <T> List<Integer> insert(String table, List<String> columns,
                                           List<SQLType> types, List<T> values) throws SQLException {
        var prepStmt = Database.getConnection()
                .prepareStatement(Utilities.createInsertString(table, columns), Statement.RETURN_GENERATED_KEYS);
        for (var valIt = values.listIterator(); valIt.hasNext(); ) {
            for (var typeIt = types.listIterator(); typeIt.hasNext(); ) {
                prepStmt.setObject(typeIt.nextIndex() + 1, valIt.next(), typeIt.next());
            }
            prepStmt.addBatch();
        }
        prepStmt.executeBatch();
        var rs = prepStmt.getGeneratedKeys();
        var numkeys = rs.getMetaData().getColumnCount();
        var keys = new ArrayList<Integer>(numkeys);
        while(rs.next()) {
            for (int i = 1; i <= numkeys; ++i) {
                keys.add(rs.getInt(i));
            }
        }
        prepStmt.close();
        return keys;
    }

    public static String createInsertString(String table, List<String> columns) {
        return "insert into " + table + " (" + String.join(", ", columns)
                + ") values (" + "?, ".repeat(columns.size() - 1) + "?)";
    }

    public static void generateRooms(int floors, int rooms) {
       try {
           var rs = Database.getConnection().createStatement().executeQuery("select count(*) from RoomTypes");
           rs.next();
           int numTypes = rs.getInt(1);

           var arr = new ArrayList<Integer>(floors * rooms * 2);
           Random rng = new Random();
           for (int i = 1; i <= floors; ++i) {
               int first = i * 100 + 1;
               for (int j = first; j < first + rooms; ++j) {
                   arr.add(j);
                   arr.add(rng.nextInt(numTypes) + 1);
               }
           }
           insert(ROOMS, Arrays.asList("room_id", "type_id"),
                   Arrays.asList(JDBCType.INTEGER, JDBCType.INTEGER), arr);
       } catch (SQLException ex) {
            throw new DatabaseException("Error occurred generating rooms", ex);
       }
    }
}
