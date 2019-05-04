package app;

import app.db.Database;
import app.db.Utilities;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static app.db.Utilities.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseTests {

    @BeforeAll
    static void createTables() {
        var f = new File("scripts/create.sql");
        Utilities.runScript(f, OutputStream.nullOutputStream());
    }

    @Test
    @Order(1)
    void insertEmployees() throws SQLException {
        Utilities.insert(EMPLOYEES,
                Arrays.asList("password"),
                Arrays.asList("emp1pass", "emp2pass", "emp3pass")
        );
        assertCount(3, EMPLOYEES);
    }

    @Test
    @Order(2)
    void insertRoomTypes() throws SQLException {
        Utilities.insert(ROOM_TYPES,
                Arrays.asList("description", "max_occupants", "price_per_day"),
                Arrays.asList(
                        "Single", 1, 140,
                        "Queen", 2, 180,
                        "Double Single", 2, 180,
                        "Double Queen", 4, 210,
                        "Double King", 4, 240,
                        "Studio", 4, 280
                )
        );
        assertCount(6, ROOM_TYPES);
    }

    @Test
    @Order(3)
    void insertRooms() throws SQLException {
        insert(ROOMS,
                Arrays.asList("room_id", "type_id"),
                Arrays.asList(
                        101, 1,
                        102, 2,
                        103, 3,
                        104, 4,
                        105, 5,
                        106, 6
                )
        );
        assertCount(6, ROOMS);
    }

    @Test
    @Order(4)
    void insertGuests() throws SQLException {
        Utilities.insert(GUESTS,
                Arrays.asList("first_name", "last_name", "email"),
                Arrays.asList(
                        "Tom", "Hanks", "asdf",
                        "Ron", "Brown", "asdf",
                        "Tim", "Robertson", "asdf"
                )
        );
        assertCount(3, GUESTS);
    }

    @Test
    @Order(5)
    void availableRoomsQueryTests() throws SQLException {
        Utilities.insert(RESERVATIONS,
                Arrays.asList(
                        "guest_id", "room_id", "check_in_date", "check_out_date", "occupants"
                ),
                Arrays.asList(
                        1, 101, Date.valueOf("2019-4-20"), Date.valueOf("2019-4-27"), 2,
                        2, 102, Date.valueOf("2019-4-23"), Date.valueOf("2019-4-30"), 3,
                        3, 103, Date.valueOf("2019-10-20"), Date.valueOf("2019-11-20"), 1
                )
        );
        assertCount(3, RESERVATIONS);

        Utilities.insert(AMENITIES,
                Arrays.asList(
                        "description", "price_per_day"
                ),
                Arrays.asList(
                        "amen1", 2.2,
                        "amen2", 10
                )
        );

        Utilities.insert(RESERVATION_AMENITIES,
                Arrays.asList(
                        "reservation_id", "amenity_id"
                ),
                Arrays.asList(
                        2,1,
                        2,2,
                        3,2
                )
        );

        var stmt = Database.findAvailableRooms;
        var numRooms = 6;
        // Rooms 101 and 201 unavailable
        stmt.setDate(1, Date.valueOf("2019-4-23"));
        stmt.setDate(2, Date.valueOf("2019-4-27"));
        assertCount(numRooms - 2, stmt.executeQuery());

        // Room 101 unavailable
        stmt.setDate(1, Date.valueOf("2019-4-15"));
        stmt.setDate(2, Date.valueOf("2019-4-20"));
        assertCount(numRooms - 1, stmt.executeQuery());

        // All rooms available
        stmt.setDate(1, Date.valueOf("2019-3-27"));
        stmt.setDate(2, Date.valueOf("2019-4-19"));
        assertCount(numRooms, stmt.executeQuery());

        // Room 201 unavailable
        stmt.setDate(1, Date.valueOf("2019-4-30"));
        stmt.setDate(2, Date.valueOf("2019-5-5"));
        assertCount(numRooms - 1, stmt.executeQuery());

        // All rooms available
        stmt.setDate(1, Date.valueOf("2019-5-1"));
        stmt.setDate(2, Date.valueOf("2019-5-5"));
        assertCount(numRooms, stmt.executeQuery());
    }

    static void assertCount(int expected, String table) throws SQLException {
        var stmt = Database.getConnection().createStatement();
        stmt.closeOnCompletion();
        var rs = stmt.executeQuery("select count(*) from " + table);
        rs.next();
        Assertions.assertEquals(expected, rs.getInt(1));
    }

    static void assertCount(int expected, ResultSet rs) throws SQLException {
        int count = 0;
        while (rs.next()) ++count;
        Assertions.assertEquals(expected, count);
    }

}