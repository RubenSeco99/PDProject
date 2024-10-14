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

    public boolean insertGrupo(Grupo grupo) {
        try {
            String query = "INSERT INTO Grupo (id, nome) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, grupo.getId());
            preparedStatement.setString(2, grupo.getNome());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir grupo: " + e.getMessage());
            return false;
        }
    }

    public Grupo selectGrupo(String nome){
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

}
