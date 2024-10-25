package BaseDeDados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VersaoDB {
    private final Connection connection;
    public VersaoDB(Connection connection) {
        this.connection = connection;
    }

    public void incrementarVersao() {
        try {
            String queryAtualizar = "UPDATE Versao SET versao_numero = versao_numero + 1";
            PreparedStatement preparedStatement = connection.prepareStatement(queryAtualizar);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao incrementar a versão: " + e.getMessage());
        }
    }
}
