package BaseDeDados;

import Entidades.Despesas;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DespesaDB {
    private final Connection connection;
    public DespesaDB(Connection connection) {
        this.connection = connection;
    }
    //metodos criar despesa
    //metodo apagar despesa
    //ver despesa (valores despesa vao ter de ir para a tabela despesapagadores ...)
    public int inserirDespesa(String descricao, double valor, Date data, String grupoNome, String criadorEmail) {
        String sql = "INSERT INTO Despesa (descricao, valor, data, grupo_nome, criador_email) VALUES (?, ?, ?, ?, ?)";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String dataFormatada = dateFormat.format(data);  // Converte a data para String

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, descricao);
            pstmt.setDouble(2, valor);
            pstmt.setString(3, dataFormatada);  // Insere a data formatada
            pstmt.setString(4, grupoNome);
            pstmt.setString(5, criadorEmail);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int despesaId = generatedKeys.getInt(1); // Obtém o ID gerado
                        System.out.println("Despesa inserida com sucesso! ID da despesa: " + despesaId);
                        return despesaId; // Retorna o ID gerado da despesa
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao inserir despesa: " + ex.getMessage());
        }
        return -1;
    }

    public double calcularTotalDespesas(String grupoNome) {
        double total = 0.0;
        String sql = "SELECT SUM(valor) AS total FROM Despesa WHERE grupo_nome = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, grupoNome);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                total = rs.getDouble("total");
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao calcular total de despesas: " + ex.getMessage());
            return -1;
        }
        return total;
    }

    public ArrayList<Despesas> getDespesasPorGrupo(String grupoNome) {
        ArrayList<Despesas> despesasList = new ArrayList<>();
        String sql = "SELECT id, descricao, valor, data FROM Despesa WHERE grupo_nome = ? ORDER BY data DESC"; // Adicionando 'id' à consulta SQL

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, grupoNome);
            ResultSet rs = pstmt.executeQuery();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

            while (rs.next()) {
                Despesas despesa = new Despesas();
                despesa.setIdDespesa(rs.getInt("id")); // Definindo o ID da despesa
                despesa.setDescricao(rs.getString("descricao"));
                despesa.setValor(rs.getDouble("valor"));
                String dataString = rs.getString("data");
                java.util.Date utilDate = null;
                try {
                    utilDate = dateFormat.parse(dataString);
                } catch (ParseException e) {
                    System.out.println("Erro ao converter string para data: " + e.getMessage());
                }

                if (utilDate != null) {
                    java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                    despesa.setData(sqlDate);
                }

                despesasList.add(despesa);
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao obter despesas: " + ex.getMessage());
            return null;
        }
        return despesasList;
    }

    public boolean temDespesas(String grupoNome) {
        String sql = "SELECT * FROM Despesa WHERE grupo_nome = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, grupoNome);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            System.out.println("Erro ao verificar despesas: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<Despesas> getDespesasPorNomeGrupo(String grupoNome) {
        ArrayList<Despesas> despesasList = new ArrayList<>();
        String sql = "SELECT descricao, valor, data, criador_email, id FROM Despesa WHERE grupo_nome = ? ORDER BY data DESC";
        // Ordenar por data, DESC = mais recente primeiro

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, grupoNome);
            ResultSet rs = pstmt.executeQuery();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

            while (rs.next()) {
                // Criar uma nova instância de Despesas
                Despesas despesa = new Despesas();
                despesa.setDescricao(rs.getString("descricao"));
                despesa.setValor(rs.getDouble("valor"));
                despesa.setPagador(rs.getString("criador_email"));
                despesa.setIdDespesa(rs.getInt("id"));
                String dataString = rs.getString("data");
                try {
                    java.util.Date utilDate = dateFormat.parse(dataString);
                    java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                    despesa.setData(sqlDate);
                } catch (ParseException e) {
                    System.out.println("Erro ao converter string para data: " + e.getMessage());
                    return null;
                }
                // Adicionar a despesa à lista
                despesasList.add(despesa);
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao obter despesas: " + ex.getMessage());
            return null;
        }
        return despesasList;
    }

    public boolean eliminarDespesaPorId(int idDespesa) {
        String sql = "DELETE FROM Despesa WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idDespesa);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao eliminar despesa: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizaDespesa(int idDespesa, String novaDescricao, double novoValor, Date novaData) {
        String sql = "UPDATE Despesa SET descricao = ?, valor = ?, data = ? WHERE id = ?";

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String dataFormatada = dateFormat.format(novaData);

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, novaDescricao);
            pstmt.setDouble(2, novoValor);
            pstmt.setString(3, dataFormatada);
            pstmt.setInt(4, idDespesa);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar despesa: " + e.getMessage());
            return false;
        }
    }
}
