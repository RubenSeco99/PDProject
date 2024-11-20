package UI.Controllers;

import Cliente.ClienteFacade;
import Cliente.Comunicacao;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public class MenuLoginController implements PropertyChangeListener {
    private String email;
    private String password;
    private ClienteFacade facade;
    private Comunicacao resposta;
    private Stage stage;

    @FXML
    TextField emailField;
    @FXML
    TextField passwordField;
    @FXML
    Button loginBtn;
    @FXML
    Button registerBtn;
    @FXML
    Label loginError;

    public MenuLoginController() {}

    public MenuLoginController(ClienteFacade facade) {
        this.facade = facade;
        this.facade.addPropertyChangeListener(this);
    }

    public void handleLoginButton() {
        System.out.println("CALLED: handleLoginButton");

        email = emailField.getText();
        password = passwordField.getText();

        facade.login(email, password);
    }

    public void handleGoToRegister() {
        System.out.println("CALLED: handleGoToRegister");
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/registerMenu.fxml"));
                Parent root = loader.load();
                MenuRegisterController controller = loader.getController();
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

    public void handleQuit() {
        System.out.println("CALLED: handleQuit");
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("mensagemRecebida".equals(evt.getPropertyName())) {
            resposta = (Comunicacao) evt.getNewValue();
            System.out.println("[Resposta do Servidor - LoginRegisterController]: " + resposta.getMensagem());
            if (resposta.getMensagem().equalsIgnoreCase("Login aceite")) {
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/mainMenu.fxml"));
                        Parent root = loader.load();
                        MenuMainController controller = loader.getController();
                        controller.setFacade(facade);
                        facade.removePropertyChangeListener(this);

                        stage = (Stage) loginBtn.getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("PDProject");
                        stage.show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else if (resposta.getMensagem().equalsIgnoreCase("Credencias incorretas")) {
                loginError.visibleProperty().setValue(true);
            }
        }
    }

    public void setFacade(ClienteFacade facade) {
        this.facade = facade;
        this.facade.addPropertyChangeListener(this);
    }
}
