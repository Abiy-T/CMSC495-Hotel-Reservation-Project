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
import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Utilities {
    private static final Map<String, SQLType> typeMap  = new HashMap<>();

    public static final String GUESTS = initTable("Guests", List.of(
            new Pair<>("guest_id", JDBCType.INTEGER),
            new Pair<>("first_name", JDBCType.VARCHAR),
            new Pair<>("last_name", JDBCType.VARCHAR),
            new Pair<>("email", JDBCType.VARCHAR)
    ));
    public static final String EMPLOYEES = initTable("Employees", List.of(
            new Pair<>("employee_id", JDBCType.INTEGER),
            new Pair<>("password", JDBCType.VARCHAR)
    ));
    public static final String ROOM_TYPES = initTable("RoomTypes", List.of(
            new Pair<>("type_id", JDBCType.INTEGER),
            new Pair<>("description", JDBCType.VARCHAR),
            new Pair<>("max_occupants", JDBCType.INTEGER),
            new Pair<>("price_per_day", JDBCType.DECIMAL)
    ));
    public static final String ROOMS = initTable("Rooms", List.of(
            new Pair<>("room_id", JDBCType.INTEGER),
            new Pair<>("type_id", JDBCType.INTEGER)
    ));
    public static final String RESERVATIONS = initTable("Reservations", List.of(
            new Pair<>("reservation_id", JDBCType.INTEGER),
            new Pair<>("guest_id", JDBCType.INTEGER),
            new Pair<>("room_id", JDBCType.INTEGER),
            new Pair<>("check_in_date", JDBCType.DATE),
            new Pair<>("check_out_date", JDBCType.DATE),
            new Pair<>("total", JDBCType.DECIMAL),
            new Pair<>("occupants", JDBCType.INTEGER)
    ));
    public static final String AMENITIES = initTable("Amenities", List.of(
            new Pair<>("amenity_id", JDBCType.INTEGER),
            new Pair<>("description", JDBCType.VARCHAR),
            new Pair<>("price_per_day", JDBCType.DECIMAL)
    ));
    public static final String RESERVATION_AMENITIES = initTable("ReservationAmenities", List.of(
            new Pair<>("reservation_id", JDBCType.INTEGER),
            new Pair<>("amenity_id", JDBCType.INTEGER)
    ));

    public static boolean runScript(File script, OutputStream os) {
        try (FileInputStream fs = new FileInputStream(script)) {
            return ij.runScript(Database.getConnection(), fs, "UTF-8", os, "UTF-8") == 0;
        } catch (IOException ex) {
            throw new DatabaseException("IO error running " + script.getPath(), ex);
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
                .prepareStatement(createPreparedInsert(table, columns), Statement.RETURN_GENERATED_KEYS);
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

    public static String createPreparedInsert(String table, List<String> columns) {
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
           var stmt = Database.getConnection().createStatement();
           var rs = stmt.executeQuery("select count(*) from RoomTypes");
           rs.next();
           int numTypes = rs.getInt(1);
            stmt.close();
           var arr = new ArrayList<Integer>(floors * rooms * 2);
           Random rng = new Random();
           for (int i = 1; i <= floors; ++i) {
               for (int first = i * 100 + 1, last = first + rooms; first < last; ++first) {
                   arr.add(first);
                   arr.add(rng.nextInt(numTypes) + 1);
               }
           }
           insert(ROOMS, List.of("room_id", "type_id"), arr);
       } catch (SQLException ex) {
            throw new DatabaseException("Error occurred generating rooms", ex);
       }
    }

    public static Date daysFromToday(int i) {
        return java.sql.Date.valueOf(LocalDate.now().plusDays(i));
    }

    public static void createSchema() {
        runScript(new File("scripts/create.sql"), OutputStream.nullOutputStream());
    }

    public static void populateSchema() {
        try {
            insert(EMPLOYEES,
                    List.of("password"),
                    List.of("password", "emp2pass", "emp3pass")
            );
            insert(GUESTS,
                    List.of("first_name", "last_name", "email"),
                    List.of(
                            "Tom", "Hanks", "asdf",
                            "Ron", "Brown", "asdf",
                            "Julia", "White", "asdf",
                            "Tim", "Robertson", "asdf",
                            "Mary", "Anne", "asdf",
                            "Bradly", "Rymer", "asdf"
                    )
            );
            insert(ROOM_TYPES,
                    List.of("description", "max_occupants", "price_per_day"),
                    List.of(
                            "Single", 1, 140,
                            "Queen", 2, 180,
                            "Double Single", 2, 180,
                            "Double Queen", 4, 210,
                            "Double King", 4, 240,
                            "Studio", 6, 300
                    )
            );

            generateRooms(2, 10);

            insert(AMENITIES,
                    List.of("description", "price_per_day"),
                    List.of(
                            "Breakfast", 10,
                            "Dinner", 15,
                            "High-speed WiFi", 5,
                            "Dry Cleaning", 5,
                            "Parking", 5
                    )
            );

            insert(RESERVATIONS,
                    List.of(
                            "guest_id", "room_id",
                            "check_in_date", "check_out_date", "occupants"
                    ),
                    List.of(
                            1, 101, daysFromToday(-3), daysFromToday(2), 2,
                            2, 110, daysFromToday(-1), daysFromToday(2), 3,
                            3, 201, daysFromToday(-5), daysFromToday(0), 1,
                            4, 208, daysFromToday(0), daysFromToday(2), 4,
                            5, 204, daysFromToday(-2), daysFromToday(1), 3,
                            6, 106, daysFromToday(-2), daysFromToday(3), 1
                    )
            );

            insert(RESERVATION_AMENITIES,
                    List.of("reservation_id", "amenity_id"),
                    List.of(
                            2, 1,
                            2, 2,
                            3, 2,
                            5, 4,
                            5, 5,
                            5, 2,
                            6, 3,
                            6, 1
                    )
            );
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to populate database", ex);
        }
    }

    private static String initTable(String name, List<Pair<String, SQLType>> columns) {
        for (var p : columns) {
            typeMap.put(p.getKey(), p.getValue());
        }
        return name;
    }

}
