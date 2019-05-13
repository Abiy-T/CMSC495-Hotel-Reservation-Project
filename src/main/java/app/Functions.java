/*
 * Alex Dwivedi
 * 4/27/2019
 * CMSC 495
 */

package app;

import app.db.Database;
import app.db.DatabaseException;
import app.entities.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

import static app.db.Utilities.*;

public class Functions {
    // List of all amenities in database
    private static final List<Amenity> AMENITIES = initAmenities();

    // Frequently used query which stays open for the lifetime of app
    private static final PreparedStatement findAvailableRooms = initFindAvailableRooms();

    // Returns a list of current reservations sorted by check out date
    public static ArrayList<ReservationReport> getDailyReport() {
        try {
            var q = "select room_id, occupants, first_name, last_name, check_out_date, description\n" +
                    "from Reservations\n"+
                    "natural join Guests\n"+
                    "natural join Rooms\n"+
                    "natural left join ReservationAmenities\n"+
                    "natural left join Amenities\n"+
                    "where check_out_date >= current_date\n"+
                    "and check_in_date <= current_date\n"+
                    "order by check_out_date, room_id, amenity_id";
            var stmt = Database.getConnection()
                    .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            var rs = stmt.executeQuery(q);

            ArrayList<ReservationReport> report = new ArrayList<>();
            ArrayList<String> resAmenities = new ArrayList<>(AMENITIES.size());
            var formatter = new SimpleDateFormat("M/dd");
            while (rs.next()) {
                String room = rs.getString(1);
                String numOccupants = rs.getString(2);
                String fname = rs.getString(3);
                String lname = rs.getString(4);
                String checkOut = formatter.format(rs.getDate(5));
                String amenity = rs.getString(6);
                if (!rs.wasNull()) {
                    resAmenities.add(amenity);
                    while(rs.next()) {
                        if (room.equals(rs.getString(1))) {
                            resAmenities.add(rs.getString(6));
                            continue;
                        }
                        rs.previous();
                        break;
                    }
                }
                report.add(new ReservationReport(
                        room, numOccupants, fname, lname, checkOut, String.join(", ", resAmenities)
                ));
                resAmenities.clear();
            }
            stmt.close();
            return report;
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to retrieve current reservations", ex);
        }
    }

    public static int getCurrentCapacity() {
        try {
            var q = "select sum(occupants) from Reservations\n"+
                    "where check_out_date > current_date\n"+
                    "and check_in_date <= current_date";
            var stmt = Database.getConnection().createStatement();
            var rs = stmt.executeQuery(q);
            rs.next();
            int occupants = rs.getInt(1);
            stmt.close();
            return occupants;
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to retrieve current capacity", ex);
        }
    }

    // Inserts the new guest and reservation in the database
    public static int register(Guest guest, Reservation res) {
        int guestId = registerGuest(guest);
        return registerReservation(guestId, res);
    }

    // Returns a list of available rooms of unique type
    public static List<Room> findAvailableRooms(LocalDate in, LocalDate out) {
        try {
            var availableRooms = new ArrayList<Room>();
            var stmt = findAvailableRooms;
            stmt.setDate(1, Date.valueOf(in));
            stmt.setDate(2, Date.valueOf(out));
            var rs = stmt.executeQuery();
            while (rs.next()) {
                availableRooms.add(new Room(
                        rs.getInt(1), rs.getString(2),
                        rs.getInt(3), rs.getBigDecimal(4)
                ));
            }
            rs.close();
            return availableRooms;
        } catch (SQLException ex) {
            throw new DatabaseException("Error processing room query", ex);
        }
    }

    // Returns error message if login was invalid
    public static Optional<String> validateLogin(String employeeId, String password)  {
        if (employeeId.isBlank() || password.isEmpty()) {
            return Optional.of("Both fields required");
        }
        try {
            int id = Integer.valueOf(employeeId);
            String q = "select count(*) from Employees where employee_id = ? and password = ?";
            var stmt = Database.getConnection().prepareStatement(q);
            stmt.setInt(1, id);
            stmt.setString(2, password);
            var rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            stmt.close();
            if (count > 0) {
                return Optional.empty();
            }
            return Optional.of("Invalid ID or password");
        } catch (NumberFormatException ex) {
            return Optional.of("Invalid ID");
        } catch(SQLException ex) {
            throw new DatabaseException("Error processing login validation", ex);
        }
    }

    public static BigDecimal calculateTotal(Reservation res) {
        BigDecimal sum = res.room.pricePerDay;
        for (var amen : res.amenities) {
            sum = sum.add(amen.pricePerDay);
        }
        int days = Period.between(res.checkInDate, res.checkOutDate).getDays();
        return sum.multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.UP);
    }

    // Returns error message if card is invalid
    public static Optional<String> validateBilling (String name,
                                                    String address,
                                                    String ccType,
                                                    String ccNumber,
                                                    String cvv,
                                                    LocalDate exp) {
        if (name.isBlank()) {
            return Optional.of("Card name is required");
        }
        if (address.isBlank()) {
            return Optional.of("Billing address is required");
        }
        if (ccNumber.isBlank()) {
            return Optional.of("Card number is required");
        }
        if (cvv.isBlank()) {
            return Optional.of("CVV is required");
        }
        if (exp == null) {
            return Optional.of("Expiration date is required");
        }

        if (exp.compareTo(LocalDate.now()) < 0) {
            return Optional.of("Card is expired");
        }

        try {
            NumberFormat.getInstance().parse(cvv + ccNumber);
        } catch (ParseException ex) {
            System.err.println(ex.getMessage());
            return Optional.of("Invalid number format");
        }

        switch (ccType) {
            case "AMEX":
                if (cvv.length() == 4 && ccNumber.length() == 15 &&
                         (ccNumber.startsWith("34") || ccNumber.startsWith("37"))) {
                    break;
                }
                return Optional.of("Invalid American Express card");
            case "Visa":
                if  (cvv.length() == 3 && ccNumber.length() == 16 &&
                        ccNumber.startsWith("4")) {
                    break;
                }
                return Optional.of("Invalid Visa card");
            case "Mastercard":
                if  (cvv.length() == 3 && ccNumber.length() == 16) {
                    int prefix = Integer.parseInt(ccNumber.substring(0, 2));
                    if (prefix >= 50 && prefix <= 55) {
                        break;
                    }
                }
                return Optional.of("Invalid Mastercard card");
            default:
                return Optional.of("Invalid card type");
        }

        return Optional.empty();
    }

    public static Optional<String> validateGuest(Guest guest) {
        if (guest.firstName.isBlank()) {
            return Optional.of("First name is required");
        }
        if (guest.lastName.isBlank()) {
            return Optional.of("Last name is required");
        }
        if (guest.email.isBlank()) {
            return Optional.of("Email address is required");
        }
        final int databaseStringSize = 99;
        if (guest.firstName.length() > databaseStringSize) {
            return Optional.of("First name must be less than 100 characters");
        }
        if (guest.lastName.length() > databaseStringSize) {
            return Optional.of("Last name must be less than 100 characters");
        }
        if (guest.email.length() > databaseStringSize) {
            return Optional.of("Email must be less than 100 characters");
        }
        return Optional.empty();
    }

    public static List<Amenity> getAmenities() {
        return Collections.unmodifiableList(AMENITIES);
    }

    private static int registerGuest(Guest guest) {
        try {
            var keys = insert(GUESTS,
                    List.of("first_name", "last_name", "email"),
                    List.of(guest.firstName, guest.lastName, guest.email)
            );
            return keys.get(0);
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to register guest", ex);
        }
    }

    private static int registerReservation(int guestId, Reservation res) {
        try {
            var keys = insert(RESERVATIONS,
                    List.of(
                            "guest_id", "room_id", "check_in_date",
                            "check_out_date", "occupants"
                    ),
                    List.of(
                            guestId,
                            res.room.number,
                            Date.valueOf(res.checkInDate),
                            Date.valueOf(res.checkOutDate),
                            res.occupants
                    )
            );
            int resId = keys.get(0);
            insert(RESERVATION_AMENITIES,
                    List.of("reservation_id", "amenity_id"),
                    res.amenities.stream()
                            .map(amenity -> List.of(resId, amenity.id))
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList())
            );
            return resId;
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to register reservation", ex);
        }
    }

    private static PreparedStatement initFindAvailableRooms() {
        var q = "select room, description, max_occupants, price_per_day from (\n" +
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
        try {
            return Database.getConnection().prepareStatement(q);
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to initialize available rooms query", ex);
        }
    }

    private static List<Amenity> initAmenities() {
        try {
            var stmt = Database.getConnection().createStatement();
            var rs = stmt.executeQuery(
                    "select amenity_id, description, price_per_day\n"+
                            "from Amenities order by amenity_id"
            );
            ArrayList<Amenity> amenities = new ArrayList<>();
            while (rs.next()) {
                amenities.add(new Amenity(
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getBigDecimal(3)));
            }
            amenities.trimToSize();
            stmt.close();
            return amenities;
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to get list of hotel amenities", ex);
        }
    }

    private Functions(){}

}
