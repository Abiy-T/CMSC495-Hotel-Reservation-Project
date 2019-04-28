/*
 * Alex Dwivedi
 * 4/27/2019
 * CMSC 495
 */

package app;

import app.db.Database;
import app.db.DatabaseException;
import app.db.Statements;
import app.entities.Amenity;
import app.entities.Guest;
import app.entities.Reservation;
import app.entities.Room;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Functions {

    // Inserts the new guest, reservation and amenities in the database
    public static void register(Guest guest, Reservation res, Amenity... amenities) {
        int guestId = registerGuest(guest);
        int resId = registerReservation(guestId, res);
        registerReservationAmenities(resId, amenities);
    }

    // Returns a list of available rooms of unique type
    public static List<Room> findAvailableRooms(LocalDate in, LocalDate out) {
        try {
            var availableRooms = new ArrayList<Room>();
            var stmt = Statements.findAvailableRooms;
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

    // Returns true if password is valid
    public static boolean validateLogin(int empid, String pass) {
        try {
            String q = "select count(*) from Employees where employee_id = ? and password = ?";
            var stmt = Database.getConnection().prepareStatement(q);
            stmt.setInt(1, empid);
            stmt.setString(2, pass);
            var rs = stmt.executeQuery();
            rs.next();
            boolean valid = rs.getInt(1) > 0;
            stmt.close();
            return valid;
        } catch (SQLException ex) {
            throw new DatabaseException("Error processing login validation", ex);
        }
    }

    // Returns true if card is valid
    public static boolean validateCard(String ccNumber, LocalDate exp, String cvv){
        if (exp.compareTo(LocalDate.now()) < 0) {
            return false;
        }

        try {
            Integer.parseInt(ccNumber + cvv);
        } catch (NumberFormatException ex) {
            return false;
        }

        // Luhn algorithm
        int sum = 0;
        boolean alternate = false;
        for (int i = ccNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(ccNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        if (sum % 10 != 0) {
            return false;
        }

        // Type/cvv check
        if (cvv.length() == 4 && ccNumber.length() == 15 &&
                (ccNumber.startsWith("34") || ccNumber.startsWith("37"))) {
            return true; // amex
        }
        else if (cvv.length() == 3 && ccNumber.length() == 16) {
            if (ccNumber.startsWith("4")) {
                return true; // visa
            }
            int valueOfPrefix = Integer.parseInt(ccNumber.substring(0, 2));
            return valueOfPrefix >= 50 && valueOfPrefix <= 55; // mastercard
        }

        return false;
    }

    private static int registerGuest(Guest guest) {
        try {
            var stmt = Statements.insertGuest;
            stmt.setString(1, guest.firstName);
            stmt.setString(2, guest.lastName);
            stmt.setString(3, guest.address);
            stmt.setString(4, guest.email);
            stmt.execute();
            return getKey(stmt);
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to register guest", ex);
        }
    }

    private static int registerReservation(int guestId, Reservation res) {
        try {
            var stmt = Statements.insertReservation;
            stmt.setInt(1, guestId);
            stmt.setInt(2, res.room.number);
            stmt.setDate(3, Date.valueOf(res.checkInDate));
            stmt.setDate(4, Date.valueOf(res.checkOutDate));
            stmt.setBigDecimal(5, res.total);
            stmt.setInt(6, res.occupants);
            stmt.execute();
            return getKey(stmt);
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to register reservation", ex);
        }
    }

    private static void registerReservationAmenities(int resId, Amenity... amenities) {
        try {
            var stmt = Statements.insertReservationAmenities;
            for (var amen : amenities) {
                stmt.setInt(1, resId);
                stmt.setInt(2, amen.id);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to register guest", ex);
        }
    }

    private static int getKey(PreparedStatement ps) {
        try {
            var rs = ps.getGeneratedKeys();
            rs.next();
            int key = rs.getInt(1);
            rs.close();
            return key;
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to retrieve key", ex);
        }

    }
}
