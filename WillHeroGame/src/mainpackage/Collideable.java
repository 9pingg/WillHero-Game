package mainpackage;

import main_elements.Hero;
import javafx.geometry.Bounds;

public interface Collideable {
    Bounds getBounds();
    int hasCollided(Hero b);
}
