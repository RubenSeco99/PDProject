package BaseDeDados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DespesaPagadoresDB {
    private final Connection connection;
    public DespesaPagadoresDB(Connection connection) {
        this.connection = connection;
    }

    public boolean inserirDespesaPagadores(int despesaId, List<String> emails, double valorDivida, String estadoPagamento) {
        String sql = "INSERT INTO Despesa_Pagadores (despesa_id, utilizador_email, valor_divida, estado_pagamento) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (String email : emails) {
                pstmt.setInt(1, despesaId);
                pstmt.setString(2, email);
                pstmt.setDouble(3, valorDivida);
                pstmt.setString(4, estadoPagamento);
                pstmt.addBatch(); // Adiciona a inserção ao lote (batch)
            }

            int[] results = pstmt.executeBatch(); // Executa todas as inserções em lote
            for (int result : results) {
                if (result == PreparedStatement.EXECUTE_FAILED) {
                    System.out.println("Erro ao inserir um dos pagadores.");
                    return false; // Se alguma inserção falhar, retorna false
                }
            }
            System.out.println("Todos os pagadores foram inseridos com sucesso.");
            return true; // Inserções bem-sucedidas
        } catch (SQLException e) {
            System.out.println("Erro ao inserir pagadores na tabela Despesa_Pagadores: " + e.getMessage());
            return false; // Retorna false se houver uma falha
        }
    }
}
