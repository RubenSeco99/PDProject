package BaseDeDados;

import java.io.Serializable;
import java.sql.*;
import java.text.SimpleDateFormat;

import Entidades.Pagamento;

public class PagamentoDB {
    private final Connection connection;
    public PagamentoDB(Connection connection) {
        this.connection = connection;
    }

    public boolean inserirPagamento(String quemPagou, String quemRecebeu, double valorPagamento, String grupoNome) {
        String sql = "INSERT INTO Pagamento (quem_pagou, quem_recebeu, valor_pagamento, data_pagamento, grupo_nome) VALUES (?, ?, ?, ?, ?)";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        // Criar uma data formatada da data atual do sistema
        String dataFormatada = dateFormat.format(new java.util.Date());
        System.out.println(quemRecebeu);
        System.out.println(quemPagou);
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, quemPagou);
            pstmt.setString(2, quemRecebeu);
            pstmt.setDouble(3, valorPagamento);
            pstmt.setString(4, dataFormatada); // Inserir a data formatada como string
            pstmt.setString(5, grupoNome);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Pagamento inserido com sucesso!");
                return true;
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao inserir pagamento: " + ex.getMessage());
        }
        return false;
    }


}
