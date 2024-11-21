package UI.Controllers;

import Cliente.ClienteFacade;
import Comunicacao.Comunicacao;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javafx.scene.control.Button;

public class MenuMainController implements PropertyChangeListener {
    private ClienteFacade facade;
    private Stage stage;
    private Comunicacao resposta;

    @FXML
    Button editDataBtn;

    @FXML
    Button groupMenuBtn;

    @FXML
    Button inviteMenuBtn;

    @FXML
    Button logoutBtn;

    public MenuMainController() {}
    public MenuMainController(ClienteFacade facade) {
        this.facade = facade;
    }

    public void setFacade(ClienteFacade facade) { this.facade = facade; }

    public void handleGoToEditData() {
        System.out.println("CALLED: handleGoToEditData");

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/editMenu.fxml"));
                Parent root = loader.load();
                MenuEditController controller = loader.getController();
                controller.setFacade(facade);

                stage = (Stage) editDataBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("PDProject");
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void handleGoToGroupMenu() {
        System.out.println("CALLED: handleGroupMenu");

        if(facade.getUtilizador().getGrupoAtual().getNome().equalsIgnoreCase("")) {
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/groupsMenu.fxml"));
                    Parent root = loader.load();
                    MenuGroupController controller = loader.getController();
                    controller.setFacade(facade);

                    stage = (Stage) editDataBtn.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("PDProject");
                    stage.show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/currentGroupMenu.fxml"));
                    Parent root = loader.load();
                    MenuCurrentGroupController controller = loader.getController();
                    controller.setFacade(facade);

                    stage = (Stage) editDataBtn.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("PDProject");
                    stage.show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

    }

    public void handleGoToInviteMenu() {
        System.out.println("CALLED: handleInviteMenu");

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/invitesMenu.fxml"));
                Parent root = loader.load();
                MenuInviteController controller = loader.getController();
                controller.setFacade(facade);

                stage = (Stage) editDataBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("PDProject");
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void handleGoToLogout() {
        System.out.println("CALLED: handleLogout");

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/loginMenu.fxml"));
                Parent root = loader.load();
                MenuLoginController controller = loader.getController();
                controller.setFacade(facade);

                stage = (Stage) editDataBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("PDProject");
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        facade.logout();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }
}
