package BaseDeDados;

import Entidades.Utilizador;
import java.sql.*;
public class UtilizadorDB {
    private final Connection connection;
    public UtilizadorDB(Connection connection) {
        this.connection = connection;
    }
    public boolean insertUtilizador(Utilizador utilizador){
        try {
            String query = "INSERT INTO Utilizador (nome, password, telefone, email, ativo) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, utilizador.getNome());
            preparedStatement.setString(2, utilizador.getPassword());
            preparedStatement.setInt(3, utilizador.getTelefone());
            preparedStatement.setString(4, utilizador.getEmail());
            preparedStatement.setBoolean(5, utilizador.getAtivo());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir utilizador: " + e.getMessage());
            return false;
        }
    }
    public Utilizador selectUtilizador(String email){
        try {
            String query = "SELECT * FROM Utilizador WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Utilizador utilizador = new Utilizador();
                utilizador.setNome(resultSet.getString("nome"));
                utilizador.setPassword(resultSet.getString("password"));
                utilizador.setTelefone(resultSet.getInt("telefone"));
                utilizador.setEmail(resultSet.getString("email"));
                utilizador.setAtivo(resultSet.getBoolean("ativo"));
                return utilizador;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao selecionar utilizador: " + e.getMessage());
        }
        return null;
    }
    public boolean verificaRegisto(String email){
        try {
            String query = "SELECT * FROM Utilizador WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            return !resultSet.next();
        } catch (SQLException e) {
            System.out.println("Erro ao verificar registo: " + e.getMessage());
            return false;
        }
    }
    public boolean verificaLogin(String email, String password){
        try {
            String query = "SELECT * FROM Utilizador WHERE email = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("Erro ao verificar login: " + e.getMessage());
            return false;
        }
    }
    public boolean updateUtilizador(Utilizador utilizador){
        try {
            String query = "UPDATE Utilizador SET nome = ?, password = ?, telefone = ?, ativo = ? WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, utilizador.getNome());
            preparedStatement.setString(2, utilizador.getPassword());
            preparedStatement.setInt(3, utilizador.getTelefone());
            preparedStatement.setBoolean(4, utilizador.getAtivo());
            preparedStatement.setString(5, utilizador.getEmail());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar utilizador: " + e.getMessage());
            return false;
        }
    }
}
