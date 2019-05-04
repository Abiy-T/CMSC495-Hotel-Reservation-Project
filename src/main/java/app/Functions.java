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
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

public class Functions {
    // List of all amenities in database
    private static final List<Amenity> AMENITIES = initAmenities();

    // Returns a list of current reservations sorted by check out date
    public static ArrayList<ReservationReport> getDailyReport() {
        try {
            ArrayList<ReservationReport> report = new ArrayList<>();
            ArrayList<String> resAmenities = new ArrayList<>(AMENITIES.size());
            var formatter = new SimpleDateFormat("M/dd");
            var rs = Database.getReport.executeQuery();
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
            rs.close();
            return report;
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to retrieve current reservations", ex);
        }
    }

    // Returns number of current occupants
    public static int getCurrentCapacity() {
        try {
            var rs = Database.getCapacity.executeQuery();
            rs.next();
            int occupants = rs.getInt(1);
            rs.close();
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
            var stmt = Database.findAvailableRooms;
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
        return sum.multiply(BigDecimal.valueOf(days))
                .setScale(2, RoundingMode.UP);
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
            var stmt = Database.insertGuest;
            stmt.setString(1, guest.firstName);
            stmt.setString(2, guest.lastName);
            stmt.setString(3, guest.email);
            stmt.execute();
            return getKey(stmt);
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to register guest", ex);
        }
    }

    private static int registerReservation(int guestId, Reservation res) {
        try {
            var stmt = Database.insertReservation;
            stmt.setInt(1, guestId);
            stmt.setInt(2, res.room.number);
            stmt.setDate(3, Date.valueOf(res.checkInDate));
            stmt.setDate(4, Date.valueOf(res.checkOutDate));
            stmt.setInt(5, res.occupants);
            stmt.execute();
            int resId = getKey(stmt);
            registerReservationAmenities(resId, res.amenities);
            return resId;
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to register reservation", ex);
        }
    }

    private static void registerReservationAmenities(int resId, List<Amenity> amenities) {
        try {
            var stmt = Database.insertReservationAmenities;
            for (var amen : amenities) {
                stmt.setInt(1, resId);
                stmt.setInt(2, amen.id);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to register amenities for reservation", ex);
        }
    }

    private static int getKey(Statement stmt) {
        try {
            var rs = stmt.getGeneratedKeys();
            rs.next();
            int key = rs.getInt(1);
            rs.close();
            return key;
        } catch (SQLException ex) {
            throw new DatabaseException("Error occurred retrieving key", ex);
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
                amenities.add(new Amenity(rs.getInt(1), rs.getString(2), rs.getBigDecimal(3)));
            }
            amenities.trimToSize();
            stmt.close();
            return amenities;
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to get list of hotel amenities", ex);
        }
    }

}
