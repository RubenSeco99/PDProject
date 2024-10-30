package BaseDeDados;

import Entidades.Despesas;
import Entidades.Divida;
import ServidorBackup.ServerBackUpSupport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DespesaPagadoresDB {
    private final Connection connection;
    private VersaoDB versaoDB;
    private final ServerBackUpSupport backupSupport;

    public DespesaPagadoresDB(Connection connection, ServerBackUpSupport backupSupport) {
        this.connection = connection;
        versaoDB = new VersaoDB(connection);
        this.backupSupport = backupSupport;
    }

    public boolean inserirDespesaPagadores(int despesaId, List<String> emails, double valorDivida, String estadoPagamento, String pagador) {
        String sql = "INSERT INTO Despesa_Pagadores (despesa_id, utilizador_email, valor_divida, estado_pagamento) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (String email : emails) {
                pstmt.setInt(1, despesaId);
                pstmt.setString(2, email);
                pstmt.setDouble(3, valorDivida);
                String estadoPag = email.equalsIgnoreCase(pagador) ? "Pago" : estadoPagamento;
                pstmt.setString(4, estadoPag);

                pstmt.addBatch();
            }
            int[] results = pstmt.executeBatch(); // Executa o batch após o loop

            for (int result : results) {
                if (result == PreparedStatement.EXECUTE_FAILED) {
                    System.out.println("Erro ao inserir um dos pagadores.");
                    return false;
                }
            }

            System.out.println("Todos os pagadores foram inseridos com sucesso.");
            versaoDB.incrementarVersao();
            for (String email : emails) {
                String estadoPag = email.equalsIgnoreCase(pagador) ? "Pago" : estadoPagamento;
                backupSupport.setQuery(sql);
                backupSupport.setParametros(List.of(despesaId, email, valorDivida, estadoPag));
                backupSupport.setVersao(versaoDB.getVersao());
                backupSupport.sendMessageToBackUpServer();
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir pagadores na tabela Despesa_Pagadores: " + e.getMessage());
        }
        return false;
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

            if (rowsAffected > 0) {
                versaoDB.incrementarVersao();
                backupSupport.setQuery(sql);
                backupSupport.setParametros(List.of(despesaId));
                backupSupport.setVersao(versaoDB.getVersao());
                backupSupport.sendMessageToBackUpServer();

                return true;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao eliminar despesas pagadores: " + e.getMessage());
        }
        return false;
    }

    public boolean atualizarValorDivida(int despesaId, double novoValorTotal) {
        String contarLinhasSql = "SELECT COUNT(*) AS total FROM Despesa_Pagadores WHERE despesa_id = ?";
        String atualizarDividaSql = "UPDATE Despesa_Pagadores SET valor_divida = ? WHERE despesa_id = ?";

        try {
            int numeroLinhas = 0;
            try (PreparedStatement contarStmt = connection.prepareStatement(contarLinhasSql)) {
                contarStmt.setInt(1, despesaId);
                ResultSet rs = contarStmt.executeQuery();
                if (rs.next()) {
                    numeroLinhas = rs.getInt("total");
                }
            }

            if (numeroLinhas == 0) {
                System.out.println("Nenhuma linha encontrada para o despesa_id: " + despesaId);
                return false;
            }

            double valorPorLinha = novoValorTotal / numeroLinhas;

            try (PreparedStatement atualizarStmt = connection.prepareStatement(atualizarDividaSql)) {
                atualizarStmt.setDouble(1, valorPorLinha);
                atualizarStmt.setInt(2, despesaId);
                int rowsAffected = atualizarStmt.executeUpdate();
                if (rowsAffected > 0) {
                    versaoDB.incrementarVersao();
                    backupSupport.setQuery(atualizarDividaSql);
                    backupSupport.setParametros(List.of(valorPorLinha, despesaId));
                    backupSupport.setVersao(versaoDB.getVersao());
                    backupSupport.sendMessageToBackUpServer();
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar valor da dívida: " + e.getMessage());
        }
        return false;
    }

    public boolean atualizarValorDespesa(int idDespesa, double novoValor, String emailQuemPagou) {
        String sql = "UPDATE Despesa_Pagadores SET valor_divida = ? WHERE despesa_id = ? AND utilizador_email = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, novoValor);
            pstmt.setInt(2, idDespesa);
            pstmt.setString(3, emailQuemPagou);

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Valor da despesa atualizado com sucesso.");
            if (rowsAffected > 0) {
                versaoDB.incrementarVersao();
                backupSupport.setQuery(sql);
                backupSupport.setParametros(List.of(novoValor, idDespesa, emailQuemPagou));
                backupSupport.setVersao(versaoDB.getVersao());
                backupSupport.sendMessageToBackUpServer();

                return true;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar valor da despesa: " + e.getMessage());
        }
        return false;
    }

    public Double getDividaPorId(int idDespesa, String email) {
        String sql = "SELECT valor_divida FROM Despesa_Pagadores WHERE despesa_id = ? AND utilizador_email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idDespesa);
            pstmt.setString(2, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("valor_divida");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter valor da dívida: " + e.getMessage());
        }
        return null;
    }

    public boolean atualizarEstadoDivida(int idDespesa, String email, String estado) {
        String sql = "UPDATE Despesa_Pagadores SET estado_pagamento = ? WHERE despesa_id = ? AND utilizador_email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, estado);
            pstmt.setInt(2, idDespesa);
            pstmt.setString(3, email);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                versaoDB.incrementarVersao();
                backupSupport.setQuery(sql);
                backupSupport.setParametros(List.of(estado, idDespesa, email));
                backupSupport.setVersao(versaoDB.getVersao());
                backupSupport.sendMessageToBackUpServer();

                return true;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar estado da dívida: " + e.getMessage());
        }
        return false;
    }

    public ArrayList<Divida> getDividasPorEmail(String email) {
        String sql = "SELECT * FROM Despesa_Pagadores WHERE utilizador_email = ?";
        ArrayList<Divida> dividas = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Divida divida = new Divida();
                    divida.setIdDespesa(rs.getInt("despesa_id"));
                    divida.setUtilizadorEmail(rs.getString("utilizador_email"));
                    divida.setValorDivida(rs.getDouble("valor_divida"));
                    divida.setEstadoPagamento(rs.getString("estado_pagamento"));
                    dividas.add(divida);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter dívidas por email: " + e.getMessage());
        }

        return dividas;
    }

}
