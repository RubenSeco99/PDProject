package UI.Controllers;

import Cliente.ClienteFacade;
import Cliente.Comunicacao;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class MenuRegisterController {
    private String email;
    private String username;
    private String password;
    private ClienteFacade facade;
    private Comunicacao resposta;
    private Stage stage;

    @FXML
    TextField emailField;
    @FXML
    TextField passwordField;
    @FXML
    TextField usernameField;
    @FXML
    Button registerBtn;

    public MenuRegisterController() {}
    public MenuRegisterController(ClienteFacade facade) { this.facade = facade; }

    public void setFacade(ClienteFacade facade) {
        this.facade = facade;
    }

    public void handleRegister() {
        System.out.println("CALLED: handleRegisterButton");

        email = emailField.getText();
        username = usernameField.getText();
        password = passwordField.getText();

        facade.register(username, email, password);

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/loginMenu.fxml"));
                Parent root = loader.load();
                MenuLoginController controller = loader.getController();
                controller.setFacade(facade);

                stage = (Stage) registerBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("PDProject");
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void handleGoToLogin() {
        System.out.println("CALLED: handleGoToLogin");
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/loginMenu.fxml"));
                Parent root = loader.load();
                MenuLoginController controller = loader.getController();
                controller.setFacade(this.facade);

                stage = (Stage) registerBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("PDProject");
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void handleQuit() {
        System.out.println("CALLED: handleQuit");
    }
}
