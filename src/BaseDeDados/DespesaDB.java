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
        String sql = "SELECT descricao, valor, data FROM Despesa WHERE grupo_nome = ? ORDER BY data DESC"; // Ordenar por data, DESC = mais recente primeiro

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, grupoNome);
            ResultSet rs = pstmt.executeQuery();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

            while (rs.next()) {
                Despesas despesa = new Despesas();
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

}
