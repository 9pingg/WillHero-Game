package main_elements.controllers;

import mainpackage.SuperController;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class WillHeroLogoController extends SuperController {
    @FXML
    ImageView ring1, ring2;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addRotation(ring1, defaultRotatingDuration/2, true);
        addRotation(ring2, defaultRotatingDuration/2, true);
    }
}
