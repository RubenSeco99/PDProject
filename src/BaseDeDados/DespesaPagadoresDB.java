package BaseDeDados;

import Entidades.Despesas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DespesaPagadoresDB {
    private final Connection connection;
    public DespesaPagadoresDB(Connection connection) {
        this.connection = connection;
    }

    public boolean inserirDespesaPagadores(int despesaId, List<String> emails, double valorDivida, String estadoPagamento, String pagador) {
        String sql = "INSERT INTO Despesa_Pagadores (despesa_id, utilizador_email, valor_divida, estado_pagamento) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (String email : emails) {
                pstmt.setInt(1, despesaId);
                pstmt.setString(2, email);
                pstmt.setDouble(3, valorDivida);
                if(email.equalsIgnoreCase(pagador)){
                    pstmt.setString(4, "Pago");
                }else{
                    pstmt.setString(4, estadoPagamento);
                }
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

    public boolean temDespesasPendentes(String grupoNome) {
        String sql = "SELECT * FROM Despesa_Pagadores dp " +
                     "JOIN Despesa d ON dp.despesa_id = d.id " +
                     "WHERE d.grupo_nome = ? AND dp.estado_pagamento = 'Pendente'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, grupoNome);
            return pstmt.executeQuery().next(); // Retorna true se houver despesas pendentes
        } catch (SQLException e) {
            System.out.println("Erro ao verificar despesas pendentes: " + e.getMessage());
            return false; // Retorna false em caso de erro
        }
    }

    public ArrayList<Despesas> preencherUtilizadoresPartilhados(ArrayList<Despesas> despesasList) {
        String sql = "SELECT utilizador_email FROM Despesa_Pagadores WHERE despesa_id = ? AND utilizador_email != ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Despesas despesa : despesasList) {
                int idDespesa = despesa.getIdDespesa();
                String criador = despesa.getPagador();
                pstmt.setInt(1, idDespesa);
                pstmt.setString(2, criador);

                try (ResultSet rs = pstmt.executeQuery()) {
                    ArrayList<String> utilizadoresPartilhados = new ArrayList<>();

                    while (rs.next()) {
                        utilizadoresPartilhados.add(rs.getString("utilizador_email"));
                    }

                    despesa.setUtilizadoresPartilhados(utilizadoresPartilhados);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao preencher utilizadores partilhados: " + ex.getMessage());
            return null;
        }

        // Retornar o array de despesas atualizado
        return despesasList;
    }

    public boolean eliminarDespesasPagadoresPorIdDespesa(int despesaId) {
        String sql = "DELETE FROM Despesa_Pagadores WHERE despesa_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, despesaId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; //
        } catch (SQLException e) {
            System.out.println("Erro ao eliminar despesas pagadores: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizarValorDivida(int despesaId, double novoValorTotal) {
        String contarLinhasSql = "SELECT COUNT(*) AS total FROM Despesa_Pagadores WHERE despesa_id = ?";
        String atualizarDividaSql = "UPDATE Despesa_Pagadores SET valor_divida = ? WHERE despesa_id = ?";

        try {
            // Passo 1: Contar o número de linhas associadas ao despesa_id
            int numeroLinhas = 0;
            try (PreparedStatement contarStmt = connection.prepareStatement(contarLinhasSql)) {
                contarStmt.setInt(1, despesaId);
                ResultSet rs = contarStmt.executeQuery();
                if (rs.next()) {
                    numeroLinhas = rs.getInt("total");
                }
            }

            // Verificar se há linhas para evitar divisão por zero
            if (numeroLinhas == 0) {
                System.out.println("Nenhuma linha encontrada para o despesa_id: " + despesaId);
                return false;
            }

            // Passo 2: Calcular o valor médio
            double valorPorLinha = novoValorTotal / numeroLinhas;

            // Passo 3: Atualizar o valor_divida de cada linha
            try (PreparedStatement atualizarStmt = connection.prepareStatement(atualizarDividaSql)) {
                atualizarStmt.setDouble(1, valorPorLinha);
                atualizarStmt.setInt(2, despesaId);
                int rowsAffected = atualizarStmt.executeUpdate();
                // Verifica se alguma linha foi atualizada
                return rowsAffected > 0;
            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar valor da dívida: " + e.getMessage());
            return false;
        }
    }


}
