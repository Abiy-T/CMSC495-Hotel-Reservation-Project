/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hotel.reservation;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;




/**
 *
 * @author ABIY
 */
public class FXMLDocumentController implements Initializable {

    //@FXML
    //private Label label;
    @FXML
    private DatePicker checkindate;

    @FXML
    private DatePicker checkoutdate;

    @FXML
    private ComboBox Roomtype;

    @FXML
    private void handleButtonAction(ActionEvent event) {

    }

    public void initialize(URL url, ResourceBundle rb) {

    }

}
