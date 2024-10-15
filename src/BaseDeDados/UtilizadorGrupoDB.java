package BaseDeDados;
import Entidades.Grupo;
import Entidades.Utilizador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilizadorGrupoDB {
    private final Connection connection;
    public UtilizadorGrupoDB(Connection connection) {
        this.connection = connection;
    }

    public boolean insertUtilizadorGrupo(int utilizadorId, int grupoId) {
        try {
            String query = "INSERT INTO Utilizador_Grupo (utilizador_id, grupo_id) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, utilizadorId);
            preparedStatement.setInt(2, grupoId);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir utilizador no grupo: " + e.getMessage());
            return false;
        }
    }
    public boolean removeUtilizadorGrupo(String email, int grupoId) {
        try {

            String querySelect = "SELECT id FROM Utilizador WHERE email = ?";
            PreparedStatement preparedStatementSelect = connection.prepareStatement(querySelect);
            preparedStatementSelect.setString(1, email);
            ResultSet resultSet = preparedStatementSelect.executeQuery();

            if (resultSet.next()) {
                int utilizadorId = resultSet.getInt("id");

                String queryDelete = "DELETE FROM Utilizador_Grupo WHERE utilizador_id = ? AND grupo_id = ?";
                PreparedStatement preparedStatementDelete = connection.prepareStatement(queryDelete);
                preparedStatementDelete.setInt(1, utilizadorId);
                preparedStatementDelete.setInt(2, grupoId);
                preparedStatementDelete.executeUpdate();

                return true;
            } else {
                System.out.println("Utilizador com email: " + email + " não encontrado.");
                return false; // Utilizador não encontrado
            }
        } catch (SQLException e) {
            System.out.println("Erro ao remover utilizador do grupo: " + e.getMessage());
            return false;
        }
    }
    public boolean removeTodosUtilizadoresDoGrupo(int grupoId, List<Utilizador> utilizadores) {
        boolean sucesso = true;

        // Iterar sobre todos os utilizadores na lista
        for (Utilizador utilizador : utilizadores) {
            String email = utilizador.getEmail(); // Obter o email do utilizador

            // Remover utilizador do grupo usando o email
            boolean removido = removeUtilizadorGrupo(email, grupoId);

            // Se falhar em remover algum utilizador, marcar como falha geral
            if (!removido) {
                sucesso = false;
                System.out.println("Falha ao remover utilizador com email: " + email + " do grupo com ID: " + grupoId);
            }
        }
        return sucesso; // Retorna true se todos forem removidos com sucesso, false se houver falhas
    }
    public boolean selectUtilizadorNoGrupo(int utilizadorId, int grupoId) {
        try {
            String query = "SELECT * FROM Utilizador_Grupo WHERE utilizador_id = ? AND grupo_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, utilizadorId);
            preparedStatement.setInt(2, grupoId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("Erro ao verificar utilizador no grupo: " + e.getMessage());
            return false;
        }
    }
    public List<Grupo> selectGruposPorUtilizador(int utilizadorId) {
        List<Grupo> grupos = new ArrayList<>();
        try {
            String query = "SELECT g.id, g.nome FROM Grupo g " +
                    "JOIN Utilizador_Grupo ug ON g.id = ug.grupo_id " +
                    "WHERE ug.utilizador_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, utilizadorId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Grupo grupo = new Grupo();
                grupo.setId(resultSet.getInt("id"));
                grupo.setNome(resultSet.getString("nome"));
                grupos.add(grupo);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao selecionar grupos por utilizador: " + e.getMessage());
        }
        return grupos;
    }
    public List<Utilizador> selectUtilizadoresPorGrupo(int grupoId) {
        List<Utilizador> utilizadores = new ArrayList<>();
        try {
            String query = "SELECT u.id, u.nome, u.email, u.telefone FROM Utilizador u " +
                    "JOIN Utilizador_Grupo ug ON u.id = ug.utilizador_id " +
                    "WHERE ug.grupo_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, grupoId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Utilizador utilizador = new Utilizador();
                utilizador.setNome(resultSet.getString("nome"));
                utilizador.setEmail(resultSet.getString("email"));
                utilizador.setTelefone(resultSet.getInt("telefone"));
                utilizadores.add(utilizador);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao selecionar utilizadores por grupo: " + e.getMessage());
        }
        return utilizadores;
    }
}
