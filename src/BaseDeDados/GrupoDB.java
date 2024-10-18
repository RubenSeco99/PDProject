package BaseDeDados;

import Entidades.Grupo;
import java.sql.*;

public class GrupoDB {
    private final Connection connection;
    public GrupoDB(Connection connection) {
        this.connection = connection;
    }
    private boolean verificaNomeGrupo(String nome) throws SQLException {
        String query = "SELECT * FROM Grupo WHERE nome = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, nome);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next(); // Retorna true se já existir grupo com este nome
    }
    public boolean insertGrupo(Grupo grupo, String criadorEmail) {
        try {
            if (verificaNomeGrupo(grupo.getNome())) {
                System.out.println("Erro: Já existe um grupo com este nome.");
                return false;
            }
            String query = "INSERT INTO Grupo (nome, criador_email) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, grupo.getNome());
            preparedStatement.setString(2, criadorEmail);

            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int grupoId = generatedKeys.getInt(1);
                System.out.println("Grupo inserido com sucesso. ID: " + grupoId);
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir grupo: " + e.getMessage());
            return false;
        }
    }

    public Grupo selectGrupo(String nome) {
        try {
            String query = "SELECT * FROM Grupo WHERE nome = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, nome);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Grupo grupo = new Grupo();
                grupo.setId(resultSet.getInt("id"));
                grupo.setNome(resultSet.getString("nome"));
                return grupo;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao selecionar grupo: " + e.getMessage());
        }
        return null;
    }
    public boolean updateNomeGrupo(String nomeAtual, String nomeNovo) {
        try {
            if (verificaNomeGrupo(nomeNovo) && !nomeAtual.equals(nomeNovo)) {
                System.out.println("Erro: Já existe um grupo com este nome.");
                return false;
            }

            String query = "UPDATE Grupo SET nome = ? WHERE nome = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, nomeNovo);
            preparedStatement.setString(2, nomeAtual);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar o nome do grupo: " + e.getMessage());
            return false;
        }
    }
    public boolean deleteGrupo(String nomeGrupo) {
        try {
            String deleteGrupo = "DELETE FROM Grupo WHERE nome = ?";
            PreparedStatement preparedStatementGrupo = connection.prepareStatement(deleteGrupo);
            preparedStatementGrupo.setString(1, nomeGrupo);
            int rowsAffected = preparedStatementGrupo.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Grupo '" + nomeGrupo + "' removido com sucesso.");
                return true;
            } else {
                System.out.println("Erro: Grupo não encontrado.");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Erro ao remover grupo: " + e.getMessage());
            return false;
        }
    }

}
