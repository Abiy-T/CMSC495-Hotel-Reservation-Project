/*
 * Alex Dwivedi
 * 5/2/2019
 * CMSC 495
 */
package app;

import app.db.Utilities;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HotelReservation extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Utilities.createSchema();
        Utilities.populateSchema();

        Parent root = FXMLLoader.load(getClass().getResource("view.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Hotel Reservation");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
