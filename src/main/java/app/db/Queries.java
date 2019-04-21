/*
 * Alex Dwivedi
 * 4/20/2019
 * CMSC 495
 */

package app.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Queries {

    // Only works under the assumption that check_out_date > check_in_date
    public static PreparedStatement availableRooms() throws SQLException {
        String q = "select * from Rooms except\n" +
                "select room_id, type_id from Reservations natural join Rooms\n" +
                "where Reservations.check_out_date >= ?\n"+
                "and Reservations.check_in_date <= ?";
        return Database.getConnection().prepareStatement(q);
    }
}
