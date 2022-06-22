package obstacles;

import javafx.geometry.Bounds;
import mainpackage.Collideable;
import mainpackage.GameObjects;

import java.util.ArrayList;

public abstract class Obstacle extends GameObjects implements Collideable {

    private ArrayList<Integer> a;

    public Obstacle() {
        a = new ArrayList<>();
    }

    // Getters and Setters

    // To be verified, which type of bound is required
    @Override
    public Bounds getBounds() {
        if(getPane() == null)
            return null;
        return getPane().getBoundsInLocal();
    }

     }



