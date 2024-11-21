package UI;

import Cliente.ClienteFacade;
import UI.Controllers.MenuLoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainJFX  extends Application {
    private ClienteFacade facade;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        facade = new ClienteFacade("localhost", 5000);
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/loginMenu.fxml"));
            loader.setControllerFactory(c -> {
                if(c == MenuLoginController.class){
                    return new MenuLoginController(facade);
                } else {
                    try {
                        return c.getDeclaredConstructor().newInstance();
                    } catch (Exception exc) {
                        throw new RuntimeException(exc);
                    }
                }
            });
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setTitle("PDProject");
            stage.setScene(scene);
            stage.show();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
