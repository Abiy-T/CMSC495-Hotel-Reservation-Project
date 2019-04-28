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
import app.entities.ReservationView;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Facts {
    // List of all amenities in database
    public static final List<Amenity> amenities = initAmenities();

    // Returns the reservation number of next reservation
    public static int getNextReservation() {
        try {
            var stmt = Database.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("select count(*) from Reservations");
            rs.next();
            int nextRes = rs.getInt(1) + 1;
            stmt.close();
            return nextRes;
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to retrieve next reservation number", ex);
        }
    }

    // Returns number of current occupants
    public static int getCurrentCapacity() {
        try {
            var rs = Statements.getCapacity.executeQuery();
            rs.next();
            int occupants = rs.getInt(1);
            rs.close();
            return occupants;
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to retrieve current capacity", ex);
        }
    }

    // Returns a list of current reservations sorted by check out date
    public static ArrayList<ReservationView> getDailyReport() {
        try {
            ArrayList<ReservationView> views = new ArrayList<>();
            ArrayList<String> resAmenities = new ArrayList<>(amenities.size());
            var formatter = new SimpleDateFormat("M/dd");
            var rs = Statements.getReport.executeQuery();
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
                views.add(new ReservationView(
                        room, numOccupants, fname, lname, checkOut, String.join(", ", resAmenities)
                ));
                resAmenities.clear();
            }
            rs.close();
            return views;
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to retrieve current reservations", ex);
        }
    }

    private static List<Amenity> initAmenities() {
        try {
            var stmt = Database.getConnection().createStatement();
            var rs = stmt.executeQuery(
                    "select amenity_id, description, price_per_day\n"+
                            "from Amenities order by amenity_id"
            );
            ArrayList<Amenity> arr = new ArrayList<>();
            while (rs.next()) {
                arr.add(new Amenity(rs.getInt(1), rs.getString(2), rs.getBigDecimal(3)));
            }
            arr.trimToSize();
            stmt.close();
            return Collections.unmodifiableList(arr);
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to get list of hotel amenities", ex);
        }
    }

}
