package BaseDeDados;

import Entidades.Convite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConviteDB {

    private final Connection connection;
    public ConviteDB(Connection connection) {
        this.connection = connection;
    }
//public Convite(String nomeGrupo, String destinatario, String remetente, String estado)
public ArrayList<Convite> listarConvitesPendentes(String utilizadorEmail) {
    ArrayList<Convite> convites = new ArrayList<>();
    try {
        // Seleciona todas as colunas necessárias para a criação do objeto Convite
        String query = "SELECT nome_grupo, remetente_email, destinatario_email, estado FROM Convites_Grupo WHERE destinatario_email = ? AND estado = 'pendente'";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, utilizadorEmail);
        ResultSet resultSet = preparedStatement.executeQuery();

        // Itera sobre os resultados e adiciona cada convite à lista
        while (resultSet.next()) {
            Convite convite = new Convite(
                    resultSet.getString("nome_grupo"),
                    resultSet.getString("remetente_email"),
                    resultSet.getString("destinatario_email"),
                    resultSet.getString("estado")
            );
            convites.add(convite);
        }
    } catch (SQLException e) {
        System.out.println("Erro ao listar convites pendentes: " + e.getMessage());
    }
    return convites;
}



    public boolean acceptConvite(int utilizadorId, int grupoId) {
        try {
            // Atualizar o estado do convite para 'aceito'
            String queryUpdate = "UPDATE Convites_Grupo SET estado = 'aceito' WHERE utilizador_id = ? AND grupo_id = ?";
            PreparedStatement preparedStatementUpdate = connection.prepareStatement(queryUpdate);
            preparedStatementUpdate.setInt(1, utilizadorId);
            preparedStatementUpdate.setInt(2, grupoId);
            int rowsUpdated = preparedStatementUpdate.executeUpdate();

            // Se o convite foi aceito, adiciona o utilizador ao grupo
            if (rowsUpdated > 0) {
                UtilizadorGrupoDB utilizadorGrupoDB = new UtilizadorGrupoDB(connection);
                return utilizadorGrupoDB.insertUtilizadorGrupo(utilizadorId, grupoId);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao aceitar convite: " + e.getMessage());
        }
        return false;
    }
    public boolean declineConvite(int utilizadorId, int grupoId) {
        try {
            // Deletar o convite da tabela Convites_Grupo
            String queryDelete = "DELETE FROM Convites_Grupo WHERE utilizador_id = ? AND grupo_id = ?";
            PreparedStatement preparedStatementDelete = connection.prepareStatement(queryDelete);
            preparedStatementDelete.setInt(1, utilizadorId);
            preparedStatementDelete.setInt(2, grupoId);
            int rowsDeleted = preparedStatementDelete.executeUpdate();

            // Verifica se o convite foi removido com sucesso
            if (rowsDeleted > 0) {
                System.out.println("Convite removido com sucesso.");
                return true;
            } else {
                System.out.println("Nenhum convite encontrado para ser removido.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao remover convite: " + e.getMessage());
        }
        return false;
    }
    public boolean checkConviteExistance(Convite convite) {
        try {
            String query = "SELECT COUNT(*) FROM Convites_Grupo WHERE nome_grupo = ? AND remetente_email = ? AND destinatario_email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, convite.getNomeGrupo());
            preparedStatement.setString(2, convite.getRemetente());
            preparedStatement.setString(3, convite.getDestinatario());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // Retorna true se o convite já existir
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar se o convite já existe: " + e.getMessage());
        }
        return false; // Retorna false se ocorrer um erro ou se o convite não existir
    }

    public void insertInvite(Convite convite) {
        try {
            String query = "INSERT INTO Convites_Grupo (nome_grupo, remetente_email, destinatario_email, estado) VALUES (?, ?, ?, 'pendente')";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, convite.getNomeGrupo());
            preparedStatement.setString(2, convite.getRemetente());
            preparedStatement.setString(3, convite.getDestinatario());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao inserir convite: " + e.getMessage());
        }
    }

}
