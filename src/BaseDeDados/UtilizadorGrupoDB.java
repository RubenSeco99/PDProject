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
    public boolean removeUtilizadorGrupo(int utilizadorId, int grupoId) {
        try {
            String query = "DELETE FROM Utilizador_Grupo WHERE utilizador_id = ? AND grupo_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, utilizadorId);
            preparedStatement.setInt(2, grupoId);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao remover utilizador do grupo: " + e.getMessage());
            return false;
        }
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
