/*
 * Alex Dwivedi
 * 5/2/2019
 * CMSC 495
 */
package app;

import app.db.DatabaseException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;

import static app.db.Utilities.*;

public class HotelReservation extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        populate();
        Parent root = FXMLLoader.load(getClass().getResource("view.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Hotel Reservation");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void populate() {
        runScript(new File("scripts/create.sql"), OutputStream.nullOutputStream());
        try {
            insert(EMPLOYEES,
                    Arrays.asList("password"),
                    Arrays.asList("password", "emp2pass", "emp3pass")
            );
            insert(GUESTS,
                    Arrays.asList("first_name", "last_name", "email"),
                    Arrays.asList(
                            "Tom", "Hanks", "asdf",
                            "Ron", "Brown", "asdf",
                            "Julia", "White", "asdf",
                            "Tim", "Robertson", "asdf",
                            "Mary", "Anne", "asdf",
                            "Bradly", "Rymer", "asdf"
                    )
            );
            insert(ROOM_TYPES,
                    Arrays.asList("description", "max_occupants", "price_per_day"),
                    Arrays.asList(
                            "Single", 1, 140,
                            "Queen", 2, 180,
                            "Double Single", 2, 180,
                            "Double Queen", 4, 210,
                            "Double King", 4, 240,
                            "Studio", 6, 300
                    )
            );

            int floors = 4;
            int rooms = 30;
            generateRooms(floors, rooms);

            insert(AMENITIES,
                    Arrays.asList(
                            "description", "price_per_day"
                    ),
                    Arrays.asList(
                            "Breakfast", 10,
                            "Dinner", 15,
                            "High-speed WiFi", 5,
                            "Dry Cleaning", 5,
                            "Parking", 5
                    )
            );

            insert(RESERVATIONS,
                    Arrays.asList(
                            "guest_id", "room_id",
                            "check_in_date", "check_out_date", "occupants"
                    ),
                    Arrays.asList(
                            1, 101, daysFromToday(-3), daysFromToday(2), 2,
                            2, 122, daysFromToday(-1), daysFromToday(2), 3,
                            3, 301, daysFromToday(-5), daysFromToday(0), 1,
                            4, 412, daysFromToday(0), daysFromToday(2), 4,
                            5, 204, daysFromToday(-2), daysFromToday(1), 3,
                            6, 402, daysFromToday(-2), daysFromToday(3), 1
                    )
            );

            insert(RESERVATION_AMENITIES,
                    Arrays.asList(
                            "reservation_id", "amenity_id"
                    ),
                    Arrays.asList(
                            2, 1,
                            2, 2,
                            3, 2,
                            5, 4,
                            5, 5,
                            5, 2,
                            6, 3,
                            6, 1
                    )
            );

        } catch (SQLException ex) {
            throw new DatabaseException("Failed to populate demo database", ex);
        }
    }

    public static Date daysFromToday(int i) {
        return java.sql.Date.valueOf(LocalDate.now().plusDays(i));
    }
}
