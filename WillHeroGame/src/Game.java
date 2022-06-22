import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import main_elements.Hand;
import main_elements.Hero;
import main_elements.HeroLogo;
import mainpackage.Collideable;
import mainpackage.GameObjects;
import mainpackage.SuperController;
import obstacles.Obstacle;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Game implements Serializable {
    private transient final Hero hero;
    private final long id;
    private final Player player;
    private String date_time;
    private transient final List<GameObjects> gameObjects;
    private static final double margin = 200, shift = 100, width = 768, height = 1024;
    int score;
    private transient final List<DoubleProperty> objectsPosProperty;
    private final int resurrection_coins = 10;
    private volatile boolean gameOver;
    private List<Pair<Class, Double>> objectsPosition;
    public Game(Player player, Scene scene) {
        this.player = player;
        id = assignID();

        gameObjects = new ArrayList<>();
        objectsPosProperty = new ArrayList<>();
        score = 0;
        initialiseDateTime();
        objectsPosition = new ArrayList<>();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/game.fxml"));
            root = loader.load();
            scene.setRoot(root);
            Main.getInstance().scale(root);
            gameController = loader.getController();
            gameController.setGame(this);
            obstaclesBox = gameController.obstaclesBox;
        } catch (IOException e) {
            e.printStackTrace();
        }
        hero = new Hero();
        gameOver = false;
        initializeGame();

        if (Main.getInstance().isAutoSave()) player.saveGame(this);
    }

//     Controller With this Class
    private transient GameController gameController;
    @FXML
    private transient Pane obstaclesBox, root;

    private synchronized void toggleGameOver() {
        gameOver = !gameOver;
    }

    @Override
    public boolean equals(Object o1){
        if (o1 != null && getClass() == o1.getClass()){
            Game g = (Game) o1;
            return (this.id == g.id);
        }
        else{
            return false;
        }
    }

    private void attachGameObject(GameObjects ob) {
        double pos = height;
        if(gameObjects.size() != 0) {
            pos = gameObjects.get(gameObjects.size() - 1).getPosY().getValue();
        }
        pos -= margin + ob.getHeight();
        if(gameObjects.isEmpty()) {
            pos += margin;
            pos -= 30;
        }
        else if(gameObjects.size() <= 2) {
            pos += margin/2;
        }

        ob.attachToPane(obstaclesBox, (width-ob.getWidth())/2, pos);
        gameObjects.add(ob);
        objectsPosProperty.add(ob.getPosY());
    }



    private void initializeGame() {
        attachGameObject(new Hand());
        attachGameObject(new HeroLogo());

        hero.attachToPane((Pane)obstaclesBox.getParent(), (width/2- hero.getWidth()/2), gameObjects.get(0).getPosY().getValue() - hero.getHeight() - 10);

        Thread collisionThread = new Thread(new Collision(), "Collision Thread");
        collisionThread.start();
    }

    public void resumeGame(Scene scene) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/game.fxml"));
            root = loader.load();
            scene.setRoot(root);
            Main.getInstance().scale(root);
            gameController = loader.getController();
            gameController.setGame(this);
            gameController.setScore(score);
            obstaclesBox = gameController.obstaclesBox;
        } catch (IOException e) {
            e.printStackTrace();
        }

        gameObjects.clear();
        objectsPosProperty.clear();

        for(Pair<Class, Double> pair : objectsPosition) {
            try {
                GameObjects go = (GameObjects) (pair.getKey().getDeclaredConstructor().newInstance());
                if(go instanceof Hero) {
                    go.attachToPane((Pane)obstaclesBox.getParent(), (width/2- hero.getWidth()/2), pair.getValue());
                    ((Hero)go).getBallController().pause();
                }
                else {
                    go.attachToPane(obstaclesBox, (width - go.getWidth())/2, pair.getValue());
                    gameObjects.add(go);
                    objectsPosProperty.add(go.getPane().layoutYProperty());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(new Collision(), "Collision Thread").start();
    }

    public void shiftObstacles() {
        double shiftExcess = Math.min(shift, hero.getBallController().moveUp());
        Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        timeline.play();

    }

    private long assignID() {
        String strFilePath = "./data/gameId";
        long input = 1;
        try {
            FileInputStream fin = new FileInputStream(strFilePath);
            DataInputStream din = new DataInputStream(fin);
            input = din.readLong();
            din.close();
        } catch(IOException fe) {
            System.out.println(fe.toString());
        } finally {
            FileOutputStream fout;
            try {
                fout = new FileOutputStream(strFilePath, false);
                DataOutputStream dout = new DataOutputStream(fout);
                dout.writeLong(input+1);
                dout.close();
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }
        return input;
    }

    // Getters and setters
    public long getId() {
        return id;
    }



    public String getDateTime() {
        return this.date_time;
    }

    public void setDateTime(String date_time) {
        this.date_time = date_time;
    }

    private void initialiseDateTime(){
        DateTimeFormatter dt_format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String dt = dt_format.format(now);
        setDateTime(dt);
    }

    private int level = 0;

    private synchronized void updateGameScore(){
        this.score ++;
        if(score % 5 == 0) {
            level = Math.max(level, 5);
            SuperController.defaultRotatingDuration -= 100 * level;
        }
        gameController.setScore(this.score);
    }

    public void saveObjectPositions(){
        for (GameObjects gameObject: gameObjects){
            objectsPosition.add(new Pair<>(gameObject.getClass(), gameObject.getPosY().getValue()));
        }
        objectsPosition.add(new Pair<>(hero.getClass(), hero.getPosY().getValue()));
    }

    private boolean setGameOver(Obstacle obstacle) throws Exception {
        int player_highscore = player.getHighScore();
        if (score > player_highscore) {
            player.setHighScore(score);
        }
        player.setCoinsEarned(player.getCoinsEarned() + score);

        saveObjectPositions();

        Thread thread = new Thread(() -> {
            try {
                player.deleteSavedGame(this);

                Main.getInstance().loadGameOver(score, player.getHighScore());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        if (player.getCoinsEarned() > resurrection_coins){
            System.out.println("Resurrection possible!");
            int difference = player.getCoinsEarned() - resurrection_coins;
            // give option of ressurection

            boolean success = gameController.displayResurrect();

            // resurrection to be implemented
            if(!success) {
                thread.start();
                return false;
            }
            resurrectPlayer(obstacle);
            player.setCoinsEarned(difference);
            return true;
        }
        thread.start();

        return false;
    }

    private void resurrectPlayer(Obstacle obstacle){
        obstacle.getPane().setVisible(false);
        gameObjects.remove(obstacle);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    class Collision implements Runnable {
        @Override
        public void run() {
            long counter = 0;
            for (int i=0; ;++i) {
                if(i == -1) {
                    System.out.println("Invalid State! i is -1!");
                    return;
                }
                if(gameOver) {
                    System.out.println("Game is now over, exiting collision thread");
                    return;
                }
                try {
                    GameObjects object = gameObjects.get(i);
                    if(object instanceof Collideable) {
                        int ret = ((Collideable) object).hasCollided(hero);
                        if(ret != 0) {
                            System.out.println("Collision detected, ret: " + ret + "; it: " + counter);
                            if (ret == -1){
                                // Collision with star
                                Main.getInstance().playStarCollisionSound();
                                updateGameScore();
                            }

                            if (ret == 2) {

                            }

                            if (ret == 1){
                                // Collision with obstacle

                                hero.getBallController().pause();
                                boolean resurrect = setGameOver((Obstacle) object);
                                if(!resurrect) {
                                    toggleGameOver();
                                }
                                else
                                    hero.getBallController().resume();

                            }
                        }
                    }
                    // To increase efficiency
                    counter++;
                    if(i > 3)
                        throw new Exception("reached too far!");
                }
                catch(Exception e) {
                    i = -1;
                }

                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}