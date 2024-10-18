package BaseDeDados;

import Entidades.Convite;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
public class ConviteDB {
    private final Connection connection;
    public ConviteDB(Connection connection) {
        this.connection = connection;
    }
    public ArrayList<Convite> listarConvitesPendentes(String utilizadorEmail) {
    ArrayList<Convite> convites = new ArrayList<>();
    try {
        String query = "SELECT nome_grupo, remetente_email, destinatario_email, estado FROM Convites_Grupo WHERE destinatario_email = ? AND estado = 'pendente'";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, utilizadorEmail);
        ResultSet resultSet = preparedStatement.executeQuery();

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
        System.out.println("AQUI2");
    }
    return convites;
}
    public boolean updateNomeConvites(String nomeAtual,String nomeNovo) {
        try {
            String query= "UPDATE Convites_Grupo SET nome_grupo=? WHERE nome_grupo = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1,nomeNovo);
            preparedStatement.setString(2,nomeAtual);
            int rowsUpdated = preparedStatement.executeUpdate();

            if(rowsUpdated>0)
                return true;

        } catch (SQLException e) {
            System.out.println("Erro ao listar convites pendentes: " + e.getMessage());
            System.out.println("AQUI3");
        }
        return false;
    }
    public boolean removeConvite(String utilizadorEmail, String grupoNome) {
        try {
            String queryDelete = "DELETE FROM Convites_Grupo WHERE destinatario_email = ? AND nome_grupo = ?";
            PreparedStatement preparedStatementDelete = connection.prepareStatement(queryDelete);
            preparedStatementDelete.setString(1, utilizadorEmail);
            preparedStatementDelete.setString(2, grupoNome);
            int rowsDeleted = preparedStatementDelete.executeUpdate();

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
            String query = "SELECT COUNT(*) FROM Convites_Grupo WHERE nome_grupo = ? AND  destinatario_email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, convite.getNomeGrupo());
            preparedStatement.setString(2, convite.getDestinatario());
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
    public boolean removeTodosConvitesPorGrupo(String nomeGrupo) {
        try {
            String queryDelete = "DELETE FROM Convites_Grupo WHERE nome_grupo = ?";
            PreparedStatement preparedStatementDelete = connection.prepareStatement(queryDelete);
            preparedStatementDelete.setString(1, nomeGrupo);
            int rowsDeleted = preparedStatementDelete.executeUpdate(); // Executa a deleção

            if (rowsDeleted > 0) {
                System.out.println("Todos os convites para o grupo '" + nomeGrupo + "' foram removidos com sucesso.");
                return true;
            } else {
                System.out.println("Nenhum convite encontrado para o grupo: " + nomeGrupo);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao remover convites do grupo: " + e.getMessage());
        }
        return false;
    }

    public Convite selectConvitePorEstado(String destinatarioEmail, String estado) {
        Convite convite = null;
        try {
            String query = "SELECT  remetente_email, destinatario_email, estado " +
                    "FROM Convites_Grupo " +
                    "WHERE destinatario_email = ? AND estado = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, destinatarioEmail);
            preparedStatement.setString(2, estado);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                convite = new Convite(
                        resultSet.getString("nome_grupo"),
                        resultSet.getString("remetente_email"),
                        resultSet.getString("destinatario_email"),
                        resultSet.getString("estado")
                );
            }
        } catch (SQLException e) {
            System.out.println("Erro ao selecionar convite por estado: " + e.getMessage());
        }
        return convite;
    }


}
