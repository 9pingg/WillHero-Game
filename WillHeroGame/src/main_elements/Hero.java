package main_elements;

import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import main_elements.controllers.HeroController;
import mainpackage.GameObjects;

public class Hero extends GameObjects {

    private HeroController heroController;
    private int colour;
    private double speed;

    public Hero() {
        loadFXMLtoPane("/main_elements/fxml/hero.fxml");
        heroController = (HeroController) controller;
        setColour();
    }

    public Bounds getBounds() {
        if(heroController == null || heroController.circle_ball == null)
            return null;
        return heroController.circle_ball.getBoundsInLocal();
    }

    public int getColour(){
        return colour;
    }

    public void setColour(){
        heroController.circle_ball.getStyleClass().add("fill-yellow");
    }

    public HeroController getBallController() {
        return heroController;
    }

    public double getSpeed(){
        return speed;
    }

    public void setSpeed(int speed_level){
        speed *= speed_level;
    }

    @Override
    public void attachToPane(Pane node, double i, double j) {
        super.attachToPane(node, i, j);
        heroController.welcome();
    }
}
