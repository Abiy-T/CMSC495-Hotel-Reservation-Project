/*
 * Alex Dwivedi
 * 4/20/2019
 * CMSC 495
 */

package app.db;

import org.apache.derby.jdbc.EmbeddedDriver;

import java.sql.*;
import java.util.Arrays;

import static app.db.Utilities.*;

public class Database {
    private static final Connection CONN = initDerby();
    public static final String DRIVER = "jdbc:derby:";
    public static final String NAME = "database";

    // Statements which stay open for the lifetime of the application.
    public static final PreparedStatement findAvailableRooms = initFindAvailableRooms();
    public static final PreparedStatement insertGuest = initInsertGuest();
    public static final PreparedStatement insertReservation = initInsertReservation();
    public static final PreparedStatement insertReservationAmenities = initInsertReservationAmenities();
    public static final PreparedStatement getReport = initGetReport();
    public static final PreparedStatement getCapacity = initGetCapacity();

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


    private static PreparedStatement initFindAvailableRooms() {
        String s = "select room, description, max_occupants, price_per_day from (\n" +
                "select type_id, min(room_id) as room from (\n"+
                "select * from Rooms except\n"+
                "select room_id, type_id from Reservations natural join Rooms\n"+
                "where Reservations.check_out_date >= ?\n"+
                "and Reservations.check_in_date <= ?\n"+
                ") a\n"+
                "group by type_id\n"+
                ") b\n"+
                "natural join RoomTypes rt\n"+
                "order by max_occupants, price_per_day";
        return createStatement(s);
    }

    private static PreparedStatement initInsertGuest() {
        String s = Utilities.createInsertString(GUESTS, Arrays.asList(
                "first_name", "last_name", "email"
        ));
        return createStatement(s);
    }

    private static PreparedStatement initInsertReservation() {
        String s = Utilities.createInsertString(RESERVATIONS, Arrays.asList(
                "guest_id", "room_id", "check_in_date",
                "check_out_date", "occupants"
        ));
        return createStatement(s);
    }

    private static PreparedStatement initInsertReservationAmenities() {
        String s = Utilities.createInsertString(RESERVATION_AMENITIES, Arrays.asList(
                "reservation_id", "amenity_id"
        ));
        return createStatement(s);
    }

    private static PreparedStatement initGetReport() {
        String s = "select room_id, occupants, first_name, last_name, check_out_date, description\n" +
                "from Reservations\n"+
                "natural join Guests\n"+
                "natural join Rooms\n"+
                "natural left join ReservationAmenities\n"+
                "natural left join Amenities\n"+
                "where check_out_date >= current_date\n"+
                "and check_in_date <= current_date\n"+
                "order by check_out_date, room_id, amenity_id";
        try {
            return getConnection().prepareStatement(s, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        } catch (SQLException ex) {
            throw new DatabaseException("Error initializing daily report statement", ex);
        }
    }

    private static PreparedStatement initGetCapacity() {
        String s = "select sum(occupants) from Reservations\n"+
                "where check_out_date > current_date\n"+
                "and check_in_date <= current_date";
        return createStatement(s);
    }

    private static PreparedStatement createStatement(String stmt) {
        try {
            return getConnection().prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to initialize statement: " + stmt, ex);
        }
    }
}
