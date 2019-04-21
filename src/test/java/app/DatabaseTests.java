package app;

import app.db.Database;
import app.db.Queries;
import app.db.Updates;
import app.db.Utilities;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static app.db.Tables.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseTests {
    private static final int numRoomTypes = 6;
    private static final int numRoomsPerFloor = 50;
    private static final int numFloors = 3;
    private static final int totalRooms = numFloors * numRoomsPerFloor;

    @BeforeAll
    static void createTables() {
        var f = new File("scripts/create.sql");
        Utilities.runScript(f, OutputStream.nullOutputStream());
    }

    @Test
    @Order(1)
    void insertEmployees() throws SQLException {
        Updates.insert(EMPLOYEES,
                Arrays.asList("password"),
                Arrays.asList("emp1pass", "emp2pass", "emp3pass")
        );
        assertCount(3, EMPLOYEES);
    }

    @Test
    @Order(2)
    void insertRoomTypes() throws SQLException {
        Updates.insert(ROOM_TYPES,
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
        assertCount(numRoomTypes, ROOM_TYPES);
    }

    @Test
    @Order(3)
    void insertRooms() throws SQLException {
        Updates.insert(ROOMS, Arrays.asList("room_id", "type_id"),
                genRooms(numFloors, numRoomsPerFloor, numRoomTypes)
        );

        assertCount(totalRooms, ROOMS);
    }

    @Test
    @Order(4)
    void insertGuests() throws SQLException {
        Updates.insert(GUESTS,
                Arrays.asList("first_name", "last_name", "address", "email"),
                Arrays.asList(
                        "Tom", "Hanks", "asdf", "asdf",
                        "Ron", "Brown", "asdf", "asdf"
                )
        );
        assertCount(2, GUESTS);
    }

    @Test
    @Order(5)
    void availableRoomsQueryTests() throws SQLException {
        Updates.insert(RESERVATIONS,
                Arrays.asList(
                        "guest_id", "employee_id", "room_id",
                        "check_in_date", "check_out_date", "total"
                ),
                Arrays.asList(
                        1, 1, 101, Date.valueOf("2019-4-20"), Date.valueOf("2019-4-27"), 543.20,
                        2, 2, 201, Date.valueOf("2019-4-23"), Date.valueOf("2019-4-30"), 1030.5
                )
        );
        assertCount(2, RESERVATIONS);

        var stmt = Queries.availableRooms();

        // Rooms 101 and 201 unavailable
        stmt.setDate(1, Date.valueOf("2019-4-23"));
        stmt.setDate(2, Date.valueOf("2019-4-27"));
        assertCount(totalRooms - 2, stmt.executeQuery());

        // Room 101 unavailable
        stmt.setDate(1, Date.valueOf("2019-4-15"));
        stmt.setDate(2, Date.valueOf("2019-4-20"));
        assertCount(totalRooms - 1, stmt.executeQuery());

        // All rooms available
        stmt.setDate(1, Date.valueOf("2019-3-27"));
        stmt.setDate(2, Date.valueOf("2019-4-19"));
        assertCount(totalRooms, stmt.executeQuery());

        // Room 201 unavailable
        stmt.setDate(1, Date.valueOf("2019-4-30"));
        stmt.setDate(2, Date.valueOf("2019-5-5"));
        assertCount(totalRooms - 1, stmt.executeQuery());

        // All rooms available
        stmt.setDate(1, Date.valueOf("2019-5-1"));
        stmt.setDate(2, Date.valueOf("2019-5-5"));
        assertCount(totalRooms, stmt.executeQuery());
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

    static ArrayList<Integer> genRooms(int floors, int rooms, int numTypes) {
        var arr = new ArrayList<Integer>(floors * rooms * 2);
        Random rng = new Random();

        for (int i = 1; i <= floors; ++i) {
            int first = i * 100 + 1;
            for (int j = first; j < first + rooms; ++j) {
                arr.add(j);
                arr.add(rng.nextInt(numTypes) + 1);
            }
        }
        return arr;
    }

}