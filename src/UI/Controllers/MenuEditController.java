package UI.Controllers;

import Cliente.ClienteFacade;
import Comunicacao.Comunicacao;
import Entidades.Grupo;
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
import java.util.List;

public class MenuEditController implements PropertyChangeListener {
    private ClienteFacade facade;
    private Comunicacao resposta;
    private Stage stage;
    private String comando;

    @FXML
    Button goBackBtn;
    @FXML
    TextField input;
    @FXML
    Button confirmBtn;
    @FXML
    Label output;

    public MenuEditController() {}
    public MenuEditController(ClienteFacade facade) {
        this.facade = facade;
        this.facade.addPropertyChangeListener(this);
    }

    public void setFacade(ClienteFacade facade) {
        this.facade = facade;
        this.facade.addPropertyChangeListener(this);
    }

    public void handleEditName() {
        System.out.println("CALLED: handleEditName");

        Platform.runLater(() -> {
            input.setVisible(true);
            input.setManaged(true);
            input.setPromptText("Novo nome");
            confirmBtn.setVisible(true);
            confirmBtn.setManaged(true);
        });

        comando = "Editar nome";
    }

    public void handleEditPassword() {
        System.out.println("CALLED: handleEditPassword");

        Platform.runLater(() -> {
            input.setVisible(false);
            input.setManaged(false);
            input.setPromptText("Nova password");
            confirmBtn.setVisible(false);
            confirmBtn.setManaged(false);
        });

        comando = "Editar password";
    }

    public void handleEditPhone() {
        System.out.println("CALLED: handleEditPhone");

        Platform.runLater(() -> {
            input.setVisible(false);
            input.setManaged(false);
            input.setPromptText("Novo numero de telefone");
            confirmBtn.setVisible(false);
            confirmBtn.setManaged(false);
        });

        comando = "Novo numero";
    }

    public void handleConfirm() {
        System.out.println("CALLED: handleConfirm");

        if (input.getText().isEmpty()) {
            output.setText("Por favor insira um nome de grupo.");
            return;
        }

        if (comando.equals("Editar nome")) {
            facade.updateUserName(input.getText());
        } else if (comando.equals("Editar password")) {
            facade.updateUserPassword(input.getText());
        } else if (comando.equals("Novo numero")) {
            facade.updateUserPhone(input.getText());
        }

        Platform.runLater(() -> {
            input.setVisible(false);
            confirmBtn.setVisible(false);
        });
    }

    public void handleGoBack() {
            System.out.println("CALLED: handleGoBack");

            Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/mainMenu.fxml"));
                Parent root = loader.load();
                MenuMainController controller = loader.getController();
                controller.setFacade(this.facade);

                stage = (Stage) goBackBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("PDProject");
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("mensagemRecebida".equals(evt.getPropertyName())) {
            resposta = (Comunicacao) evt.getNewValue();
            System.out.println("[Resposta do Servidor - MenuEditController]: " + resposta.getMensagem());
            if (resposta.getMensagem().equalsIgnoreCase("Edicao utilizador bem sucedida")) {
                Platform.runLater(() -> output.setText("Dados de utilizador atualizados com sucesso"));
            } else {
                Platform.runLater(() -> output.setText(resposta.getMensagem()));
            }
        }
    }
}
