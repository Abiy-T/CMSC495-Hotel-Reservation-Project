/*
 * Alex Dwivedi
 * 4/20/2019
 * CMSC 495
 */

package app.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

// Statements which stay open for the lifetime of the application.
public class Statements {

    public static final PreparedStatement findAllAvailableRooms = initFindAllAvailableRooms();
    public static final PreparedStatement findAvailableRooms = initFindAvailableRooms();
    public static final PreparedStatement insertGuest = initInsertGuest();
    public static final PreparedStatement insertReservation = initInsertReservation();
    public static final PreparedStatement insertReservationAmenities = initInsertReservationAmenities();
    public static final PreparedStatement getReport = initGetReport();
    public static final PreparedStatement getCapacity = initGetCapacity();

    // Only works under the assumption that check_out_date > check_in_date
    private static PreparedStatement initFindAllAvailableRooms() {
        String s = "select * from Rooms except\n" +
                "select room_id, type_id from Reservations natural join Rooms\n" +
                "where Reservations.check_out_date >= ?\n"+
                "and Reservations.check_in_date <= ?";
        return createStatement(s);
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
        String s = Utilities.createInsertString(Tables.GUESTS, Arrays.asList(
                "first_name", "last_name", "address", "email"
        ));
        return createStatement(s);
    }

    private static PreparedStatement initInsertReservation() {
        String s = Utilities.createInsertString(Tables.RESERVATIONS, Arrays.asList(
                "guest_id", "room_id", "check_in_date",
                "check_out_date", "total", "occupants"
        ));
        return createStatement(s);
    }

    private static PreparedStatement initInsertReservationAmenities() {
        String s = Utilities.createInsertString(Tables.RESERVATION_AMENITIES, Arrays.asList(
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
            return Database.getConnection()
                    .prepareStatement(s, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
            return Database.getConnection().prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to initialize statement: " + stmt, ex);
        }
    }
}
