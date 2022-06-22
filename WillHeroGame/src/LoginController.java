
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import mainpackage.DuplicateUsernameException;
import mainpackage.InvalidCredentialsException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML private TextField usernameTxt;
    @FXML private PasswordField passwordTxt;
    @FXML private Button loginBtn;
    @FXML private Button createAccBtn;
    @FXML private Text login_status_label;
    @FXML private Pane login_plus_1, login_plus_2;
    @FXML private ImageView login_ring_1, login_ring_2;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


    }



    public static void serialize (Player player) throws IOException {
        String filename = player.getFileName();
        System.out.println(filename);
        String filepath = "./data/player_data/" + filename;

        new File("data/player_data/").mkdirs();

        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(filepath));
            out.writeObject(player);
        }
        finally {
            out.close();
        }
    }

    public static Player deserialize(Player player) throws IOException, ClassNotFoundException {
        String filename = player.getFileName();
        System.out.println(filename);
        String filepath = "./data/player_data/" + filename;

        new File("data/player_data/").mkdirs();

        Player p = null;
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream (new FileInputStream(filepath));
            p = (Player) in.readObject();
        }
        finally {
            in.close();
        }
        return p;
    }

    private static boolean checkPlayerName(Player p1){
        String filepath = "./data/player_data/" + p1.getFileName();
        File temp = new File(filepath);
        System.out.println(p1.getFileName() + ": " + temp.exists());
        return temp.exists();
    }
    @FXML
    public void loginValidate(MouseEvent me) {
        try {
            if (usernameTxt.getText() == null || passwordTxt.getText() == null ||
                    usernameTxt.getText().trim().isEmpty() || passwordTxt.getText().trim().isEmpty()) {
                throw new NullPointerException();
            }
            String user_name = usernameTxt.getText();
            String password = passwordTxt.getText();
            Player new_player = new Player(user_name, password);

            Player validatedPlayer = valid(new_player);

            // Validate player credentials
            if (validatedPlayer != null){
                login_status_label.setText("Logged in successfully!");
                login_status_label.setFill(Color.GREEN);
                Main.getInstance().setCurrentPlayer(validatedPlayer);
                Main.getInstance().loadHome();
            }
            else {
                throw new InvalidCredentialsException("Wrong credentials entered!");
            }
        }
        catch(NullPointerException | NumberFormatException e){
            login_status_label.setFill(Color.RED);
            login_status_label.setText("Invalid details. Try again!");
        }
        catch(InvalidCredentialsException e){
            login_status_label.setFill(Color.RED);
            login_status_label.setText("Wrong credentials entered!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void createNewAccount(MouseEvent me) {
        try{
            if (usernameTxt.getText() == null || passwordTxt.getText() == null ||
                    usernameTxt.getText().trim().isEmpty() || passwordTxt.getText().trim().isEmpty()) {
                throw new NullPointerException();
            }
            String user_name = usernameTxt.getText();
            String password = passwordTxt.getText();

            // Checking for duplicate username
            Player new_player = new Player(user_name, password);
            if (checkDuplicateUsername(new_player)){
                throw new DuplicateUsernameException("Username already exists!");
            }
            else{
                login_status_label.setText("Account created successfully!");
                login_status_label.setFill(Color.GREEN);
                Main.getInstance().setCurrentPlayer(new_player);

                serialize(new_player);


                Main.getInstance().getCurrentPlayer().print_player();

                Main.getInstance().loadHome();
            }
        }
        catch (NullPointerException | NumberFormatException e){
            login_status_label.setFill(Color.RED);
            login_status_label.setText("Invalid details. Try again!");
        }
        catch (DuplicateUsernameException e){
            login_status_label.setFill(Color.RED);
            login_status_label.setText("Username already exists!");
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean checkDuplicateUsername(Player p1) {
        return checkPlayerName(p1);
    }

    private static Player valid(Player fiouydsIUFH) throws IOException, ClassNotFoundException {
        if (checkPlayerName(fiouydsIUFH)){
            Player check_player = deserialize(fiouydsIUFH);
            if (fiouydsIUFH.getPassword().equals(check_player.getPassword())){


                check_player.print_player();

                return check_player;
            }
        }
        return null;
    }
}
