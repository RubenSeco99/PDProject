package BaseDeDados;

import Entidades.Utilizador;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilizadorDB {
    private final Connection connection;
    private String querySBS;
    public UtilizadorDB(Connection connection) {
        this.connection = connection;
    }
    public void insertUtilizador(Utilizador utilizador){
        try {
            String query = "INSERT INTO Utilizador (nome, password, telefone, email, ativo) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, utilizador.getNome());
            preparedStatement.setString(2, utilizador.getPassword());
            preparedStatement.setInt(3, utilizador.getTelefone());
            preparedStatement.setString(4, utilizador.getEmail());
            preparedStatement.setInt(5, utilizador.getAtivo());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao inserir utilizador: " + e.getMessage());
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
                utilizador.setAtivo(resultSet.getInt("ativo"));
                return utilizador;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao selecionar utilizador: " + e.getMessage());
        }
        return null;
    }
    public List<Utilizador> selectTodosUtilizadores() {
        List<Utilizador> listaUtilizadores = new ArrayList<>();

        try {
            String query = "SELECT * FROM Utilizador";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                listaUtilizadores.add(selectUtilizador(resultSet.getString("email")));
            }

        } catch (SQLException e) {
            System.out.println("Erro ao selecionar utilizadores: " + e.getMessage());
        }
        return listaUtilizadores;
    }
    public boolean verificaRegisto(String email){//retorna true se existir
        try {
            String query = "SELECT * FROM Utilizador WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();//retorna colunas se existir
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("Erro ao verificar registo: " + e.getMessage());
            return false;
        }
    }
    public boolean verificaLogin(String email, String password){//retorna true se existir
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
    public void updateUtilizador(Utilizador utilizador){
        try {
            String query = "UPDATE Utilizador SET nome = ?, password = ?, telefone = ?, ativo = ? WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, utilizador.getNome());
            preparedStatement.setString(2, utilizador.getPassword());
            preparedStatement.setInt(3, utilizador.getTelefone());
            preparedStatement.setInt(4, utilizador.getAtivo());
            preparedStatement.setString(5, utilizador.getEmail());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar utilizador: " + e.getMessage());
        }
    }
}
