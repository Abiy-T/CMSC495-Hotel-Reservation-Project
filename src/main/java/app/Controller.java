/*
 * Alex Dwivedi
 * 5/2/2019
 * CMSC 495
 */

package app;

import app.entities.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

@FXML
GridPane main;
@FXML
TabPane tabs;
@FXML
Tab reserveTab;
    @FXML
    GridPane reservePane;
        @FXML
        DatePicker checkIn;
        @FXML
        DatePicker checkOut;
        @FXML
        ComboBox<Room> roomsCombo;
        @FXML
        ComboBox<Integer> occupantsCombo;
        @FXML
        HBox amenitiesPane;
        @FXML
        Button reserveSubmit;
        @FXML
        Button reserveCancel;
    @FXML
    GridPane paymentPane;
        @FXML
        ToggleGroup ccTypeGroup;
        @FXML
        TextField cardName;
        @FXML
        TextField cardNumber;
        @FXML
        DatePicker cardExpiration;
        @FXML
        TextField cvvNumber;
        @FXML
        TextArea billingAddress;
        @FXML
        TextField guestFirst;
        @FXML
        TextField guestLast;
        @FXML
        TextField guestEmail;
        @FXML
        Button paymentSubmit;
        @FXML
        Button paymentCancel;
        @FXML
        Label total;
        @FXML
        Label paymentErrorLabel;
    @FXML
    VBox confirmPane;
        @FXML
        Button confirmSubmit;
        @FXML
        Label reservationNumber;
        @FXML
        Label roomNumber;
@FXML
Tab statusTab;
    @FXML
    GridPane loginPane;
        @FXML
        TextField empIdText;
        @FXML
        PasswordField passwordText;
        @FXML
        Button loginSubmit;
        @FXML
        Label loginErrorLabel;
    @FXML
    GridPane statusPane;
        @FXML
        Button logoutSubmit;
        @FXML
        Button refreshSubmit;
        @FXML
        Label capacity;
        @FXML
        TableView<ReservationReport> reportTable;

    private final SimpleBooleanProperty loggedIn = new SimpleBooleanProperty(true);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.tabMinWidthProperty().bind(main.widthProperty().multiply(0.3));
        initReservationTab();
        initStatusTab();
    }

    private void initReservationTab() {
        reservePane.setGridLinesVisible(true);
        reserveTab.disableProperty().bind(loggedIn.not());
        reserveTab.getContent().visibleProperty().bind(reserveTab.disableProperty().not());

        final SimpleObjectProperty<Reservation> reservation = new SimpleObjectProperty<>();
        final SimpleObjectProperty<Guest> guest = new SimpleObjectProperty<>();

        reservation.addListener((obs, old, newVal) -> {
            if (newVal == null) {
                cleanReservePane();
                return;
            }
            var t = Functions.calculateTotal(reservation.getValue());
            total.setText(NumberFormat.getCurrencyInstance().format(t));
        });
        guest.addListener((obs, old, newVal) -> {
            if (newVal == null) {
                cleanPaymentPane();
                return;
            }
            var res = reservation.getValue();
            roomNumber.setText(String.valueOf(res.room.number));
            int resNum = Functions.register(guest.getValue(), res);
            reservationNumber.setText(String.valueOf(resNum));
            if (res.checkInDate.isEqual(LocalDate.now())) {
                refreshHotelInfo();
            }
        });

        reservePane.visibleProperty().bind(reservation.isNull().and(guest.isNull()));
        paymentPane.visibleProperty().bind(reservation.isNotNull().and(guest.isNull()));
        confirmPane.visibleProperty().bind(reservation.isNotNull().and(guest.isNotNull()));

        paymentSubmit.setOnAction(e -> {
            if (ccTypeGroup.getSelectedToggle() == null) {
                paymentErrorLabel.setText("Card type required");
                return;
            }
            var cardType = ((RadioButton)ccTypeGroup.getSelectedToggle()).getText();
            Optional<String> error = Functions.validateBilling(
                    cardName.getText(), billingAddress.getText(), cardType,
                    cardNumber.getText(), cvvNumber.getText(), cardExpiration.getValue()
            );
            if (error.isPresent()) {
                paymentErrorLabel.setText(error.get());
                return;
            }
            var g = new Guest(guestFirst.getText(), guestLast.getText(), guestEmail.getText());
            error = Functions.validateGuest(g);
            error.ifPresentOrElse(err -> paymentErrorLabel.setText(err), () -> guest.set(g));
        });

        reserveCancel.setOnAction(e -> cleanReservePane());
        paymentCancel.setOnAction(e -> {
            cleanPaymentPane();
            reservation.set(null);
        });
        confirmSubmit.setOnAction(e -> {
            roomNumber.setText("");
            reservationNumber.setText("");
            reservation.set(null);
            guest.set(null);
        });

        reserveSubmit.defaultButtonProperty().bind(reservePane.visibleProperty());
        reserveCancel.cancelButtonProperty().bind(reservePane.visibleProperty());
        paymentSubmit.defaultButtonProperty().bind(paymentPane.visibleProperty());
        paymentCancel.cancelButtonProperty().bind(paymentPane.visibleProperty());
        confirmSubmit.defaultButtonProperty().bind(confirmPane.visibleProperty());
        confirmSubmit.cancelButtonProperty().bind(confirmPane.visibleProperty());

        final BooleanBinding dateRangeValid = Bindings.createBooleanBinding(() -> {
            var in = checkIn.getValue();
            var out = checkOut.getValue();
            return in != null && out != null && in.compareTo(out) < 0;
        }, checkIn.valueProperty(), checkOut.valueProperty());

        occupantsCombo.disableProperty().bind(dateRangeValid.not());
        roomsCombo.disableProperty().bind(dateRangeValid.not());

        roomsCombo.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            if (dateRangeValid.get()) {
                return FXCollections.observableList(
                        Functions.findAvailableRooms(
                                checkIn.getValue(), checkOut.getValue()));
            }
            return FXCollections.emptyObservableList();
        }, dateRangeValid));

        occupantsCombo.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            if (roomsCombo.getItems().isEmpty()) {
                return FXCollections.emptyObservableList();
            }
            var rooms = roomsCombo.getItems();
            var max = rooms.get(rooms.size() - 1).maxOccupants;
            return IntStream.rangeClosed(1, max)
                    .boxed().collect(Collectors.toCollection(
                            FXCollections::observableArrayList));
        }, roomsCombo.itemsProperty()));

        checkIn.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(LocalDate.now()) < 0 );
            }
        });

        checkOut.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(LocalDate.now()) <= 0 );
            }
        });

        roomsCombo.setCellFactory(lv -> new ComboBoxListCell<>() {
            @Override
            public void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                var occupants = occupantsCombo.getValue();
                setDisable(empty || (occupants != null && room.maxOccupants < occupants));
            }
        });

        occupantsCombo.setCellFactory(lv -> new ComboBoxListCell<>() {
            @Override
            public void updateItem(Integer occupants, boolean empty) {
                super.updateItem(occupants, empty);
                var room = roomsCombo.getValue();
                setDisable(empty || (room != null && room.maxOccupants < occupants));
            }
        });

        // ComboBox exposes no method to apply updateItem to its list,
        // but the underlying ListView does.
        occupantsCombo.valueProperty().addListener((obs, old, newVal) -> {
            var skin = (ComboBoxListViewSkin<?>) roomsCombo.getSkin();
            @SuppressWarnings("unchecked")
            var view = (ListView<Room>) skin.getPopupContent();
            view.refresh();
        });
        roomsCombo.valueProperty().addListener((obs, old, newVal) -> {
            var skin = (ComboBoxListViewSkin<?>) occupantsCombo.getSkin();
            @SuppressWarnings("unchecked")
            var view = (ListView<Integer>) skin.getPopupContent();
            view.refresh();
        });

        amenitiesPane.getChildren().addAll(
                Functions.getAmenities().stream()
                        .map(a -> new CheckBox(a.description) {{
                            setUserData(a);
                        }})
                        .collect(Collectors.toList()));

        reserveSubmit.disableProperty().bind(
                roomsCombo.valueProperty().isNull().or(
                        occupantsCombo.valueProperty().isNull()));

        reserveSubmit.setOnAction(e -> {
            System.out.println(roomsCombo.getValue());
            System.out.println(occupantsCombo.getValue());
            var amenities = amenitiesPane.getChildren().stream()
                    .filter(cb -> ((CheckBox)cb).isSelected())
                    .map(cb -> (Amenity)cb.getUserData())
                    .collect(Collectors.toList());
            reservation.set(new Reservation(
                    roomsCombo.getValue(), checkIn.getValue(), checkOut.getValue(),
                    occupantsCombo.getValue(), amenities
            ));
        });

    }

    private void initStatusTab() {
        statusTab.textProperty().bind(Bindings
                .when(loggedIn)
                .then("Hotel Status")
                .otherwise("Login")
        );
        statusPane.visibleProperty().bind(loggedIn);
        loginPane.visibleProperty().bind(loggedIn.not());
        logoutSubmit.setOnAction(e -> loggedIn.set(false));
        loginSubmit.defaultButtonProperty().bind(loginPane.visibleProperty());
        loginSubmit.setOnAction(e -> {
            Optional<String> error = Functions.validateLogin(
                    empIdText.getText(), passwordText.getText());
            if (error.isEmpty()) {
                loginErrorLabel.setText("");
                loggedIn.set(true);
            }
            else {
                loginErrorLabel.setText(error.get());
                empIdText.requestFocus();
            }
            empIdText.clear();
            passwordText.clear();
        });
        refreshSubmit.setOnAction(e -> refreshHotelInfo());
        refreshHotelInfo();
    }

    private void refreshHotelInfo() {
        capacity.setText(String.valueOf(Functions.getCurrentCapacity()));
        reportTable.setItems(FXCollections.observableList(Functions.getDailyReport()));
    }

    private void cleanReservePane() {
        checkIn.setValue(null);
        checkOut.setValue(null);
        occupantsCombo.setValue(null);
        roomsCombo.setValue(null);
        amenitiesPane.getChildren().forEach(cb -> ((CheckBox)cb).setSelected(false));
    }

    private void cleanPaymentPane() {
        ccTypeGroup.selectToggle(null);
        cardName.clear();
        cardNumber.clear();
        cardExpiration.setValue(null);
        cvvNumber.clear();
        billingAddress.clear();
        guestFirst.clear();
        guestLast.clear();
        guestEmail.clear();
        paymentErrorLabel.setText("");
    }

}
