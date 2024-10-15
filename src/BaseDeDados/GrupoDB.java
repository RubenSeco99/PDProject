package BaseDeDados;

import Entidades.Grupo;
import Entidades.Utilizador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    public boolean insertGrupo(Grupo grupo) {
        try {
            if (verificaNomeGrupo(grupo.getNome())) {
                System.out.println("Erro: Já existe um grupo com este nome.");
                return false;
            }
            String query = "INSERT INTO Grupo (nome) VALUES (?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, grupo.getNome());
            preparedStatement.executeUpdate();

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
    public Grupo selectGrupoById(int id) {
        try {
            String query = "SELECT * FROM Grupo WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Grupo grupo = new Grupo();
                grupo.setId(resultSet.getInt("id"));
                grupo.setNome(resultSet.getString("nome"));
                return grupo;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao selecionar grupo por id: " + e.getMessage());
        }
        return null;
    }

    public boolean updateNomeGrupo(String nomeAtual, String nomeNovo) {
        try {
            // Verifica se o novo nome já existe, mas garante que o nome não seja o mesmo que o atual
            if (verificaNomeGrupo(nomeNovo) && !nomeAtual.equals(nomeNovo)) {
                System.out.println("Erro: Já existe um grupo com este nome.");
                return false;
            }

            // Atualiza o nome do grupo com base no nome antigo
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

    public int selectGrupoId(String nomeGrupo) {
        try {
            String query = "SELECT id FROM Grupo WHERE nome = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, nomeGrupo);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar o id do grupo: " + e.getMessage());
        }
        return -1;//nao encontrado
    }
    public boolean deleteGrupo(String nomeGrupo) {
        try {
            Grupo grupo = selectGrupo(nomeGrupo);
            if (grupo == null) {
                System.out.println("Erro: Grupo não encontrado");
                return false;
            }

            String deleteAssociacoes = "DELETE FROM Utilizador_Grupo WHERE grupo_id = ?";
            PreparedStatement preparedStatementAssociacoes = connection.prepareStatement(deleteAssociacoes);
            preparedStatementAssociacoes.setInt(1, grupo.getId());
            preparedStatementAssociacoes.executeUpdate();

            String deleteGrupo = "DELETE FROM Grupo WHERE id = ?";
            PreparedStatement preparedStatementGrupo = connection.prepareStatement(deleteGrupo);
            preparedStatementGrupo.setInt(1, grupo.getId());
            int rowsAffected = preparedStatementGrupo.executeUpdate();

            return rowsAffected > 0;//grupo removido quando for apagada uma linha
        } catch (SQLException e) {
            System.out.println("Erro ao remover grupo: " + e.getMessage());
            return false;
        }
    }



}
