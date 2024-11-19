package UI.Controllers;

import Cliente.ClienteFacade;
import Cliente.Comunicacao;
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

public class MenuGroupController implements PropertyChangeListener {
    private ClienteFacade facade;
    private Stage stage;
    private Comunicacao resposta;
    private String comando;

    @FXML
    Button goBackBtn;
    @FXML
    Label output;
    @FXML
    TextField groupName;
    @FXML
    Button joinGroupBtn;

    public MenuGroupController() {
    }

    public MenuGroupController(ClienteFacade facade) {
        this.facade = facade;
        this.facade.addPropertyChangeListener(this);
    }

    public void setFacade(ClienteFacade facade) {
        this.facade = facade;
        this.facade.addPropertyChangeListener(this);
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

    public void handleChooseGroup() {
        System.out.println("CALLED: handleChooseGroup");

        Platform.runLater(() -> {
            groupName.setVisible(true);
            groupName.setManaged(true);
            joinGroupBtn.setVisible(true);
            joinGroupBtn.setManaged(true);
        });

        comando = "Escolher grupo";
    }

    public void handleCreateGroup() {
        System.out.println("CALLED: handleCreateGroup");

        Platform.runLater(() -> {
            groupName.setVisible(true);
            groupName.setManaged(true);
            joinGroupBtn.setVisible(true);
            joinGroupBtn.setManaged(true);
        });

        comando = "Criar grupo";
    }

    public void handleSeeGroups() {
        System.out.println("CALLED: handleSeeGroups");

        facade.viewGroups();
    }

    public void handleConfirm() {
        System.out.println("CALLED: handleJoinGroup");

        if (groupName.getText().isEmpty()) {
            output.setText("Por favor insira um nome de grupo.");
            return;
        }

        if(comando.equals("Criar grupo")) {
            facade.createGroup(groupName.getText());
        } else if(comando.equals("Escolher grupo")) {
            facade.chooseGroup(groupName.getText());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("mensagemRecebida".equals(evt.getPropertyName())) {
            resposta = (Comunicacao) evt.getNewValue();
            System.out.println("[Resposta do Servidor - GroupController]: " + resposta.getMensagem());
            if (resposta.getMensagem().equalsIgnoreCase("Ver grupos bem sucedido")) {
                List<Grupo> grupos = resposta.getUtilizador().getGrupos();
                StringBuilder sb = new StringBuilder();
                for (Grupo grupo : grupos) {
                    sb.append(grupo.getNome()).append("\n");
                }
                Platform.runLater(() -> output.setText("Grupos pertencentes: \n" + sb));
            } else if (resposta.getMensagem().equalsIgnoreCase("Grupo escolhido")) {
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/currentGroupMenu.fxml"));
                        Parent root = loader.load();
                        MenuCurrentGroupController controller = loader.getController();
                        controller.setFacade(this.facade);
                        facade.removePropertyChangeListener(this);

                        stage = (Stage) goBackBtn.getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("PDProject");
                        stage.show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else if (resposta.getMensagem().equalsIgnoreCase("Grupo criado")) {
                Platform.runLater(() -> {
                    groupName.setVisible(false);
                    groupName.setManaged(false);
                    joinGroupBtn.setVisible(false);
                    joinGroupBtn.setManaged(false);
                    output.setText("Grupo criado com sucesso.");
                });
            }
        }
    }
}
