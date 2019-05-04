/*
 * Alex Dwivedi
 * 4/20/2019
 * CMSC 495
 */

package app.db;

import javafx.util.Pair;
import org.apache.derby.tools.ij;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;


public class Utilities {
    private static final Map<String, SQLType> typeMap  = new HashMap<>();

    public static final String GUESTS = initTable("Guests", Arrays.asList(
            new Pair<>("guest_id", JDBCType.INTEGER),
            new Pair<>("first_name", JDBCType.VARCHAR),
            new Pair<>("last_name", JDBCType.VARCHAR),
            new Pair<>("email", JDBCType.VARCHAR)
    ));
    public static final String EMPLOYEES = initTable("Employees", Arrays.asList(
            new Pair<>("employee_id", JDBCType.INTEGER),
            new Pair<>("password", JDBCType.VARCHAR)
    ));
    public static final String ROOM_TYPES = initTable("RoomTypes", Arrays.asList(
            new Pair<>("type_id", JDBCType.INTEGER),
            new Pair<>("description", JDBCType.VARCHAR),
            new Pair<>("max_occupants", JDBCType.INTEGER),
            new Pair<>("price_per_day", JDBCType.DECIMAL)
    ));
    public static final String ROOMS = initTable("Rooms", Arrays.asList(
            new Pair<>("room_id", JDBCType.INTEGER),
            new Pair<>("type_id", JDBCType.INTEGER)
    ));
    public static final String RESERVATIONS = initTable("Reservations", Arrays.asList(
            new Pair<>("reservation_id", JDBCType.INTEGER),
            new Pair<>("guest_id", JDBCType.INTEGER),
            new Pair<>("room_id", JDBCType.INTEGER),
            new Pair<>("check_in_date", JDBCType.DATE),
            new Pair<>("check_out_date", JDBCType.DATE),
            new Pair<>("total", JDBCType.DECIMAL),
            new Pair<>("occupants", JDBCType.INTEGER)
    ));
    public static final String AMENITIES = initTable("Amenities", Arrays.asList(
            new Pair<>("amenity_id", JDBCType.INTEGER),
            new Pair<>("description", JDBCType.VARCHAR),
            new Pair<>("price_per_day", JDBCType.DECIMAL)
    ));
    public static final String RESERVATION_AMENITIES = initTable("ReservationAmenities", Arrays.asList(
            new Pair<>("reservation_id", JDBCType.INTEGER),
            new Pair<>("amenity_id", JDBCType.INTEGER)
    ));

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
        return insert(table, columns, getTypes(columns), values);
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

    public static List<SQLType> getTypes(List<String> columns) {
        return columns.stream().map(col -> {
            var type = typeMap.get(col);
            if (type != null) {
                return type;
            }
            throw new IllegalArgumentException("Invalid column: " + col);
        }).collect(Collectors.toList());
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

    private static String initTable(String name, List<Pair<String, SQLType>> columns) {
        for (var p : columns) {
            typeMap.put(p.getKey(), p.getValue());
        }
        return name;
    }

}
