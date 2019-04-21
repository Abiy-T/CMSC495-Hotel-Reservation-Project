/*
 * Alex Dwivedi
 * 4/20/2019
 * CMSC 495
 */

package app.db;

import javafx.util.Pair;

import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.*;

public class Tables {

    private static final Map<String, SQLType> typeMap  = new HashMap<>();
    
    public static final String GUESTS = initTable("Guests", Arrays.asList(
            new Pair<>("guest_id", JDBCType.INTEGER),
            new Pair<>("first_name", JDBCType.VARCHAR),
            new Pair<>("last_name", JDBCType.VARCHAR),
            new Pair<>("address", JDBCType.VARCHAR),
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
            new Pair<>("employee_id", JDBCType.INTEGER),
            new Pair<>("check_in_date", JDBCType.DATE),
            new Pair<>("check_out_date", JDBCType.DATE),
            new Pair<>("total", JDBCType.DECIMAL)
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

    private static String initTable(String name, List<Pair<String, SQLType>> columns) {
        for (var p : columns) {
            typeMap.put(p.getKey(), p.getValue());
        }
        return name;
    }

    public static Map<String, SQLType> getTypes() {
        return Collections.unmodifiableMap(typeMap);
    }

}
