package UI.Controllers;

import Cliente.ClienteFacade;
import Comunicacao.Comunicacao;
import Entidades.Convite;
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

public class MenuInviteController implements PropertyChangeListener {
    private ClienteFacade facade;
    private Stage stage;
    private Comunicacao resposta;
    private String comando;

    @FXML
    Button goBackBtn;
    @FXML
    Label output;
    @FXML
    TextField input;
    @FXML
    Button confirmBtn;

    public MenuInviteController() {}
    public MenuInviteController(ClienteFacade facade) {
        this.facade = facade;
        this.facade.addPropertyChangeListener(this);
    }

    public void setFacade(ClienteFacade facade) {
        this.facade = facade;
        this.facade.addPropertyChangeListener(this);
    }

    public void handleSeeInvites() {
        System.out.println("CALLED: handleSeeInvites");
        facade.seeInvites();
    }

    public void handleAcceptInvite() {
        System.out.println("CALLED: handleAcceptInvite");

        Platform.runLater(() -> {
            input.setVisible(true);
            input.setManaged(true);
            confirmBtn.setVisible(true);
            confirmBtn.setManaged(true);
        });

        comando = "Aceitar convite";
    }

    public void handleRejectInvite() {
        System.out.println("CALLED: handleRejectInvite");

        comando = "Rejeitar convite";
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

    public void handleConfirm() {
        System.out.println("CALLED: handleConfirm");

        if(input.getText().isEmpty()) {
            output.setText("Introduza um nome de grupo.");
            return;
        }

        if (comando.equalsIgnoreCase("Aceitar convite")) {
            facade.handleChangeInviteState("Aceite", input.getText());
        } else if (comando.equalsIgnoreCase("Rejeitar convite")) {
            facade.handleChangeInviteState("Rejeitado", input.getText());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("mensagemRecebida".equals(evt.getPropertyName())) {
            resposta = (Comunicacao) evt.getNewValue();
            System.out.println("[Resposta do Servidor - InvitesController]: " + resposta.getMensagem());
            if (resposta.getMensagem().equalsIgnoreCase("Lista de convites vazia")) {
                Platform.runLater(() -> output.setText("Não tem convites pendentes."));
            } else if (resposta.getMensagem().equalsIgnoreCase("Lista de convites")) {
                List<Convite> convites = resposta.getConvites();
                StringBuilder sb = new StringBuilder();
                for (Convite c : convites) {
                    sb.append("Convite para o grupo: ").append(c.getNomeGrupo()).append("\n");
                }
                Platform.runLater(() -> output.setText("Convites pendentes: \n" + sb));
            } else {
                Platform.runLater(() -> output.setText(resposta.getMensagem()));
            }
        }
    }
}
