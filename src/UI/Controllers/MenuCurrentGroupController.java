package UI.Controllers;

import Cliente.ClienteFacade;
import Cliente.Comunicacao;
import Entidades.Despesas;
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
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MenuCurrentGroupController implements PropertyChangeListener {
    private ClienteFacade facade;
    private Stage stage;
    private Comunicacao resposta;
    private String comando;

    @FXML
    TextField prompt;
    @FXML
    Button confirmBtn;
    @FXML
    Label output;
    @FXML
    Button goBackBtn;

    public MenuCurrentGroupController() {
    }

    public MenuCurrentGroupController(ClienteFacade facade) {
        this.facade = facade;
        this.facade.addPropertyChangeListener(this);
    }

    public void setFacade(ClienteFacade facade) {
        this.facade = facade;
        this.facade.addPropertyChangeListener(this);
    }


    public void handleNewExpense() {
        System.out.println("CALLED: handleNewExpense");

        Platform.runLater(() -> {
            prompt.setPromptText("Descricao Valor Data");
            prompt.setVisible(true);
            prompt.setManaged(true);
            confirmBtn.setVisible(true);
            confirmBtn.setManaged(true);
        });

        Platform.runLater(() -> {
            output.setText("Crie uma nova despesa com o seguinte formato: 'Descricao Valor Data'");
        });

        comando = "Nova despesa";
    }

    public void handlePayDebt() {
        System.out.println("CALLED: handlePayDebt");

        Platform.runLater(() -> {
            prompt.setPromptText("Valor Id");
            prompt.setVisible(true);
            prompt.setManaged(true);
            confirmBtn.setVisible(true);
            confirmBtn.setManaged(true);
        });

        Platform.runLater(() -> {
            output.setText("Pague uma despesa com o seguinte formato: 'Valor Id'");
        });

        comando = "Pagar despesa";
    }

    public void handleDeleteExpense() {
        System.out.println("CALLED: handleDeleteExpense");

        Platform.runLater(() -> {
            prompt.setPromptText("Id da despesa");
            prompt.setVisible(true);
            prompt.setManaged(true);
            confirmBtn.setVisible(true);
            confirmBtn.setManaged(true);
        });

        comando = "Apagar despesa";
    }

    public void handleEditExpense() {
        System.out.println("CALLED: handleEditExpense");

        Platform.runLater(() -> {
            prompt.setPromptText("Descricao Valor Data Id");
            prompt.setVisible(true);
            prompt.setManaged(true);
            confirmBtn.setVisible(true);
            confirmBtn.setManaged(true);
        });

        Platform.runLater(() -> {
            output.setText("Edite uma despesa com o seguinte formato: 'Descricao Valor Data Id'");
        });

        comando = "Editar despesa";
    }

    public void handleExportCSV() {
        System.out.println("CALLED: handleExportCSV");

        facade.exportExpensesToCSV();
    }

    public void handleViewExpenses() {
        System.out.println("CALLED: handleViewExpenses");

        facade.viewExpenses();
    }

    public void handleViewTotalExpenses() {
        System.out.println("CALLED: handleViewTotalExpenses");

        facade.viewTotalExpenses();
    }

    public void handleDeleteGroup() {
        System.out.println("CALLED: handleDeleteGroup");

        facade.deleteGroup();
    }

    public void handleChangeGroupName() {
        System.out.println("CALLED: handleChangeGroupName");


        Platform.runLater(() -> {
            prompt.setPromptText("Nome do grupo");
            prompt.setVisible(true);
            prompt.setManaged(true);
            confirmBtn.setVisible(true);
            confirmBtn.setManaged(true);
        });

        comando = "Alterar nome do grupo";
    }

    public void handleSendInvite() {
        System.out.println("CALLED: handleSendInvite");

        Platform.runLater(() -> {
            prompt.setPromptText("Email do utilizador a convidar");
            prompt.setVisible(true);
            prompt.setManaged(true);
            confirmBtn.setVisible(true);
            confirmBtn.setManaged(true);
        });

        comando = "Convite";
    }

    public void handleLeaveGroup() {
        System.out.println("CALLED: handleLeaveGroup");

        facade.exitGroup();
    }

    public void handleConfirm() {
        System.out.println("CALLED: handleConfirm");

        if (comando.equalsIgnoreCase("Convite")) {
            if (prompt.getText().isEmpty()) {
                output.setText("Por favor insira um email");
                return;
            }
            facade.sendGroupInvite(prompt.getText());

        } else if (comando.equalsIgnoreCase("Alterar nome do grupo")) {
            facade.updateGroupName(prompt.getText());
        } else if (comando.equalsIgnoreCase("Pagar despesa")) {
            String[] partes = prompt.getText().split(" ");
            facade.payDebt(Double.parseDouble(partes[0]), Integer.parseInt(partes[1]));
        } else if (comando.equalsIgnoreCase("Apagar despesa")) {
            facade.deleteExpense(Integer.parseInt(prompt.getText()));
        } else if (comando.equalsIgnoreCase("Nova despesa")) {
            try {
                String[] partes = prompt.getText().split(" ");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate localDate = LocalDate.parse(partes[2], formatter);
                java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);
                facade.addExpense(partes[0], Double.parseDouble(partes[1]), sqlDate);
            } catch (DateTimeParseException e) {
                System.out.println("Erro ao interpretar a data. Formato esperado: dd/MM/yyyy.");
                e.printStackTrace();
            }
        } else if (comando.equalsIgnoreCase("Editar despesa")) {
            try {
                String[] partes = prompt.getText().split(" ");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate localDate = LocalDate.parse(partes[2], formatter);
                java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);
                facade.updateExpense(partes[0], Double.parseDouble(partes[1]), sqlDate, Integer.parseInt(partes[3]));
            } catch (DateTimeParseException e) {
                System.out.println("Erro ao interpretar a data. Formato esperado: dd/MM/yyyy.");
                e.printStackTrace();
            }
        }

        Platform.runLater(() -> {
            prompt.setVisible(false);
            prompt.setManaged(false);
            confirmBtn.setVisible(false);
            confirmBtn.setManaged(false);
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
            System.out.println("[Resposta do Servidor - CurrentGroupController]: " + resposta.getMensagem());
            if (resposta.getMensagem().equalsIgnoreCase("Convite repetido")) {
                Platform.runLater(() -> output.setText("Convite repetido, tente novamente com outro email!"));
            } else if (resposta.getMensagem().equalsIgnoreCase("Historio de despesas")) {
                StringBuilder sb = new StringBuilder();
                List<Despesas> despesas = resposta.getDespesa();
                for (Despesas despesa : despesas) {
                    sb.append(despesa.getIdDespesa()).append(" - ").append(despesa.getDescricao()).append(" - ").append(despesa.getValor()).append(" - ").append(despesa.getData()).append("\n");
                }
                Platform.runLater(() -> output.setText("Despesas: \n" + sb));
            } else {
                Platform.runLater(() -> output.setText(resposta.getMensagem()));
            }
        }
    }


}
