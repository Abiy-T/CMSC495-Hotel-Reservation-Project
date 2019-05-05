package app;

import app.db.Database;
import app.db.Utilities;
import app.entities.Guest;
import app.entities.Reservation;
import app.entities.Room;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static app.db.Utilities.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTests {

    @BeforeAll
    static void init() {
        createSchema();
    }

    @Test
    @Order(1)
    void insertRoomTypes() throws SQLException {
        insert(ROOM_TYPES,
                List.of("description", "max_occupants", "price_per_day"),
                List.of(
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
    @Order(2)
    void generateRoomsTest() throws SQLException {
        int floors = 3;
        int rooms = 3;
        generateRooms(floors, rooms);
        var rs = Database.getConnection().createStatement().executeQuery("select room_id from Rooms");

        ArrayList<Integer> roomNumbers = new ArrayList<>();
        while (rs.next()) {
            roomNumbers.add(rs.getInt(1));
        }

        Assertions.assertIterableEquals(roomNumbers, List.of(
                101, 102, 103, 201, 202, 203, 301, 302, 303
        ));
    }
    @Test
    @Order(3)
    void insertRooms() throws SQLException {
        init();
        insertRoomTypes();
        insert(ROOMS,
                List.of("room_id", "type_id"),
                List.of(
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
    void insertEmployees() throws SQLException {
        insert(EMPLOYEES,
                List.of("password"),
                List.of("emp1pass", "emp2pass", "emp3pass")
        );
        assertCount(3, EMPLOYEES);
    }

    @Test
    @Order(5)
    void insertGuests() throws SQLException {
        insert(GUESTS,
                List.of("first_name", "last_name", "email"),
                List.of(
                        "Tom", "Hanks", "asdf",
                        "Ron", "Brown", "asdf",
                        "Tim", "Robertson", "asdf"
                )
        );
        assertCount(3, GUESTS);
    }

    @Test
    @Order(6)
    void insertReservations() throws SQLException {
        insert(RESERVATIONS,
                List.of(
                        "guest_id", "room_id", "check_in_date", "check_out_date", "occupants"
                ),
                List.of(
                        1, 101, daysFromToday(-3), daysFromToday(4), 2,
                        2, 102, daysFromToday(0), daysFromToday(7), 3,
                        3, 103, daysFromToday(11), daysFromToday(15), 1
                )
        );
        assertCount(3, RESERVATIONS);
    }

    @Test
    @Order(7)
    void insertAmenities() throws SQLException {
        Utilities.insert(AMENITIES,
                List.of(
                        "description", "price_per_day"
                ),
                List.of(
                        "amen1", 5.0,
                        "amen2", 10
                )
        );
        assertCount(2, AMENITIES);
    }

    @Test
    @Order(8)
    void insertResAmenities() throws SQLException {
        Utilities.insert(RESERVATION_AMENITIES,
                List.of(
                        "reservation_id", "amenity_id"
                ),
                List.of(
                        2,1,
                        2,2,
                        3,2
                )
        );
        assertCount(3, RESERVATION_AMENITIES);
    }

    @Test
    @Order(9)
    void availableRoomsQuery() {
        var numRooms = 6;
        var today = LocalDate.now();

        // Rooms 101 and 201 unavailable
        var rooms = Functions.findAvailableRooms(
                today, today.plusDays(4)
        );
        Assertions.assertEquals(numRooms - 2, rooms.size());

        // Room 101 unavailable
        rooms = Functions.findAvailableRooms(
                today.minusDays(7), today.minusDays(3)
        );
        Assertions.assertEquals(numRooms - 1, rooms.size());

        // All rooms available
        rooms = Functions.findAvailableRooms(
                today.minusDays(7), today.minusDays(4)
        );
        Assertions.assertEquals(numRooms, rooms.size());

        // Room 201 unavailable
        rooms = Functions.findAvailableRooms(
                today.plusDays(7), today.plusDays(10)
        );
        Assertions.assertEquals(numRooms - 1, rooms.size());

        // All rooms available
        rooms = Functions.findAvailableRooms(
                today.plusDays(8), today.plusDays(10)
        );
        Assertions.assertEquals(numRooms, rooms.size());
    }

    @Test
    @Order(10)
    void currentCapacityTest() {
        Assertions.assertEquals(5, Functions.getCurrentCapacity());
    }

    @Test
    @Order(11)
    void dailyReportTest() {
        Assertions.assertEquals(2, Functions.getDailyReport().size());
    }

    @Test
    @Order(12)
    void validateLoginTests() {
        Assertions.assertTrue(Functions.validateLogin("1", "").isPresent());
        Assertions.assertTrue(Functions.validateLogin("1", "asdfsdaf").isPresent());
        Assertions.assertTrue(Functions.validateLogin("12342344234412341234321", "asdfsdaf").isPresent());
        Assertions.assertTrue(Functions.validateLogin("1", "emp1pass").isEmpty());
    }

    @Test
    @Order(13)
    void validateGuestTests() {
        Guest valid = new Guest("alex", "d", "alexd@a.com");
        Guest blank = new Guest("asdfd", "asdadf", "   ");
        Guest tooLong = new Guest("asfadsf", "asdfadsf",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        Assertions.assertTrue(Functions.validateGuest(valid).isEmpty());
        Assertions.assertTrue(Functions.validateGuest(blank).isPresent());
        Assertions.assertTrue(Functions.validateGuest(tooLong).isPresent());
    }

    @Test
    @Order(14)
    void calculateTotalTest() {
        var room = new Room(1, "", 1, BigDecimal.valueOf(100));
        var res = new Reservation(room, LocalDate.now(), LocalDate.now().plusDays(2), 1, Functions.getAmenities());
        Assertions.assertEquals(0, Functions.calculateTotal(res).compareTo(BigDecimal.valueOf((100 + 5 + 10) * 2)));
    }

    @Test
    @Order(15)
    void registerTests() throws SQLException {
        int guests = getCount(GUESTS);
        int reservations = getCount(RESERVATIONS);
        int resAmenities = getCount(RESERVATION_AMENITIES);

        var amenities = Functions.getAmenities();
        var room = new Room(105, "", 1, BigDecimal.valueOf(100));
        var res = new Reservation(room, LocalDate.now(), LocalDate.now().plusDays(2), 1, amenities);
        var guest = new Guest("jim", "o", "jimmy@aol.com");

        Functions.register(guest, res);

        assertCount(guests + 1, GUESTS);
        assertCount(reservations + 1, RESERVATIONS);
        assertCount(resAmenities + amenities.size(), RESERVATION_AMENITIES);

    }
    static void assertCount(int expected, String table) throws SQLException {
        Assertions.assertEquals(expected, getCount(table));
    }

    static int getCount(String table) throws SQLException {
        var stmt = Database.getConnection().createStatement();
        var rs = stmt.executeQuery("select count(*) from " + table);
        rs.next();
        int count = rs.getInt(1);
        stmt.close();
        return count;
    }

}