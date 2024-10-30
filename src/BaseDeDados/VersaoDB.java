package BaseDeDados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VersaoDB {
    private final Connection connection;
    private int versao;

    public VersaoDB(Connection connection) {
        this.connection = connection;
        this.versao = obterVersaoAtual();
    }

    private int obterVersaoAtual() {
        try {
            String querySelect = "SELECT versao_numero FROM Versao";
            PreparedStatement preparedStatement = connection.prepareStatement(querySelect);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("versao_numero");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter a versão atual: " + e.getMessage());
        }
        return 0;
    }

    public void incrementarVersao() {
        try {
            String queryAtualizar = "UPDATE Versao SET versao_numero = versao_numero + 1";
            PreparedStatement preparedStatement = connection.prepareStatement(queryAtualizar);
            preparedStatement.executeUpdate();

            this.versao = obterVersaoAtual();
        } catch (SQLException e) {
            System.out.println("Erro ao incrementar a versão: " + e.getMessage());
        }
    }

    public int getVersao() {
        return obterVersaoAtual();
    }
}
