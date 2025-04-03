package com.sanvalero.imagefilters;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;

public class MainController implements Initializable {

    @FXML
    private ChoiceBox<String> mainFilter1;
    @FXML
    private ChoiceBox<String> mainFilter2;
    @FXML
    private ChoiceBox<String> mainFilter3;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mainFilter1.getItems().addAll("Grayscale", "Invert Colors", "Brightness");
        mainFilter2.getItems().addAll("Grayscale", "Invert Colors", "Brightness");
        mainFilter3.getItems().addAll("Grayscale", "Invert Colors", "Brightness");
    }
}
