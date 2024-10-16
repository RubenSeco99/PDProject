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

//    public ArrayList<Convite> listarConvitesPendentes(int utilizadorId) {
//        ArrayList<Convite> convites = new ArrayList<>();
//        try {
//            String query = "SELECT grupo_id FROM Convites_Grupo WHERE utilizador_id = ? AND estado = 'pendente'";
//            PreparedStatement preparedStatement = connection.prepareStatement(query);
//            preparedStatement.setInt(1, utilizadorId);
//            ResultSet resultSet = preparedStatement.executeQuery();
//            convites.add(new Convite("Teste", "pendente"));
//            while (resultSet.next()) {
//                convites.add(new Convite(resultSet.getString("nomeGrupo")));
//            }
//        } catch (SQLException e) {
//            System.out.println("Erro ao listar convites pendentes: " + e.getMessage());
//        }
//        return convites;
//    }

    public ArrayList<Convite> listarConvitesPendentes(String utilizadorEmail) {
        ArrayList<Convite> convites = new ArrayList<>();
        try {
            String query = "SELECT nome_grupo FROM Convites_Grupo WHERE destinatario_email = ? AND estado = 'pendente'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, utilizadorEmail);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                convites.add(new Convite(resultSet.getString("nome_grupo")));
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