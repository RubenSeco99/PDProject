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
    public boolean insertUtilizadorGrupo(String utilizadorEmail, String nomeGrupo) {
        try {
            String queryInsert = "INSERT INTO Utilizador_Grupo (utilizador_email, grupo_nome) VALUES (?, ?)";
            PreparedStatement preparedStatementInsert = connection.prepareStatement(queryInsert);
            preparedStatementInsert.setString(1, utilizadorEmail); // Email do utilizador
            preparedStatementInsert.setString(2, nomeGrupo); // Nome do grupo
            preparedStatementInsert.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir utilizador no grupo: " + e.getMessage());
            return false;
        }
    }
    public boolean removeUtilizadorGrupo(String email, String grupoNome) {
        try {
            String querySelect = "SELECT email FROM Utilizador WHERE email = ?";
            PreparedStatement preparedStatementSelect = connection.prepareStatement(querySelect);
            preparedStatementSelect.setString(1, email);
            ResultSet resultSet = preparedStatementSelect.executeQuery();
            if (resultSet.next()) {
                String queryDelete = "DELETE FROM Utilizador_Grupo WHERE utilizador_email = ? AND grupo_nome = ?";
                PreparedStatement preparedStatementDelete = connection.prepareStatement(queryDelete);
                preparedStatementDelete.setString(1, resultSet.getString("email"));
                preparedStatementDelete.setString(2, grupoNome);
                preparedStatementDelete.executeUpdate();
                return true;
            } else {
                System.out.println("Utilizador com email: " + email + " não encontrado.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao remover utilizador do grupo: " + e.getMessage());
            return false;
        }
    }
    public boolean removeTodosUtilizadoresDoGrupo(String grupoNome) {
        boolean sucesso = true;
        try {
            String querySelect = "SELECT utilizador_email FROM Utilizador_Grupo WHERE grupo_nome = ?";
            PreparedStatement preparedStatementSelect = connection.prepareStatement(querySelect);
            preparedStatementSelect.setString(1, grupoNome);
            ResultSet resultSet = preparedStatementSelect.executeQuery();

            while (resultSet.next()) {
                String email = resultSet.getString("utilizador_email");
                if (!removeUtilizadorGrupo(email, grupoNome)) {
                    sucesso = false;
                    System.out.println("Falha ao remover utilizador com email: " + email + " do grupo: " + grupoNome);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao remover utilizadores do grupo: " + e.getMessage());
            sucesso = false;
        }
        return sucesso; // Retorna true se todos forem removidos com sucesso, false se houver falhas
    }
    public boolean updateNomeGrupo(String nomeAtual,String nomeNovo) {
        try {
            String query= "UPDATE Utilizador_Grupo SET grupo_nome=? WHERE grupo_nome = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1,nomeNovo);
            preparedStatement.setString(2,nomeAtual);
            int rowsUpdated = preparedStatement.executeUpdate();

            if(rowsUpdated>0)
                return true;

        } catch (SQLException e) {
            System.out.println("Erro ao listar convites pendentes: " + e.getMessage());
        }
        return false;
    }
    public boolean selectUtilizadorNoGrupo(String utilizadorEmail, String grupoNome) {//verifica se o utilizador esta no grupo
        try {
            String query = "SELECT * FROM Utilizador_Grupo WHERE utilizador_email = ? AND grupo_nome = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, utilizadorEmail);
            preparedStatement.setString(2, grupoNome);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("Erro ao verificar utilizador no grupo: " + e.getMessage());
            return false;
        }
    }
    public List<Utilizador> selectUtilizadoresGrupo(String grupoNome) {//erro aqui , whatsapp
        //devolve a lista dos utilizadores num grupo para depois poder manipular(ex.:mudar nome do grupo)
        try {
            String query = "SELECT * FROM Utilizador_Grupo WHERE grupo_nome = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, grupoNome);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Utilizador> utilizadores=new ArrayList<>();
            while (resultSet.next()) {
                    Utilizador utilizador=new Utilizador();
                    utilizador.setAtivo(resultSet.getInt("ativo"));
                    utilizador.setPassword(resultSet.getString("password"));
                    utilizador.setTelefone(resultSet.getInt("telefone"));
                    utilizador.setEmail(resultSet.getString("email"));
                    utilizador.setNome(resultSet.getString("nome"));
                    utilizadores.add(utilizador);
            }
            return utilizadores;
        } catch (SQLException e) {
            System.out.println("Erro ao verificar utilizador no grupo: " + e.getMessage());
            return null;
        }
    }
    public List<Grupo> selectGruposPorUtilizador(String utilizadorEmail) {
        List<Grupo> grupos = new ArrayList<>();
        try {
            String query = "SELECT g.nome FROM Grupo g " +
                    "JOIN Utilizador_Grupo ug ON g.nome = ug.grupo_nome " +
                    "JOIN Utilizador u ON ug.utilizador_email = u.email " +
                    "WHERE u.email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, utilizadorEmail);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Grupo grupo = new Grupo();
                // Agora estamos apenas pegando o nome, não o id
                grupo.setNome(resultSet.getString("nome"));
                grupos.add(grupo);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao selecionar grupos por utilizador: " + e.getMessage());
        }
        return grupos;
    }

    public List<Utilizador> selectUtilizadoresPorGrupo(String grupoNome) {
        List<Utilizador> utilizadores = new ArrayList<>();
        try {
            String query = "SELECT u.id, u.nome, u.email, u.telefone FROM Utilizador u " +
                    "JOIN Utilizador_Grupo ug ON u.id = ug.utilizador_id " +
                    "JOIN Grupo g ON g.nome = ug.grupo_nome " +
                    "WHERE g.nome = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, grupoNome);

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
