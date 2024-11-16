package BaseDeDados;

import java.io.Serializable;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Entidades.Pagamento;
import ServidorBackup.ServerBackUpSupport;

public class PagamentoDB {
    private final Connection connection;
    private VersaoDB versaoDB;
    private final ServerBackUpSupport backupSupport;

    public PagamentoDB(Connection connection, ServerBackUpSupport backupSupport) {
        this.connection = connection;
        this.versaoDB = new VersaoDB(connection);
        this.backupSupport = backupSupport;
    }

    public boolean inserirPagamento(String quemPagou, String quemRecebeu, double valorPagamento, String grupoNome) {
        String sql = "INSERT INTO Pagamento (quem_pagou, quem_recebeu, valor_pagamento, data_pagamento, grupo_nome) VALUES (?, ?, ?, ?, ?)";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

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
                versaoDB.incrementarVersao();
                backupSupport.setQuery(sql);
                backupSupport.setParametros(List.of(quemPagou, quemRecebeu, valorPagamento, dataFormatada, grupoNome));
                backupSupport.setVersao(versaoDB.getVersao());
                backupSupport.sendMessageToBackUpServer();

                System.out.println("Pagamento inserido com sucesso!");
                return true;
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao inserir pagamento: " + ex.getMessage());
        }
        return false;
    }
    public boolean updateNomeGrupo(String nomeAtual,String nomeNovo) {
        try {
            String query= "UPDATE Despesa_Pagadores SET grupo_nome=? WHERE grupo_nome = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1,nomeNovo);
            preparedStatement.setString(2,nomeAtual);
            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                versaoDB.incrementarVersao();
                backupSupport.setQuery(query);
                backupSupport.setParametros(List.of(nomeNovo, nomeAtual));
                backupSupport.setVersao(versaoDB.getVersao());
                backupSupport.sendMessageToBackUpServer();

                return true;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar convites pendentes: " + e.getMessage());
        }
        return false;
    }
    public ArrayList<Pagamento> getPagamentosPorGrupo(String grupoNome) {
        ArrayList<Pagamento> pagamentos = new ArrayList<>();
        String query = "SELECT quem_pagou, quem_recebeu, valor_pagamento, grupo_nome " +
                "FROM Pagamento WHERE grupo_nome = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, grupoNome);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Pagamento pagamento = new Pagamento();
                pagamento.setQuemPagou(rs.getString("quem_pagou"));
                pagamento.setQuemRecebeu(rs.getString("quem_recebeu"));
                pagamento.setValorPagamento(rs.getDouble("valor_pagamento"));
                pagamento.setGrupoNome(rs.getString("grupo_nome"));

                pagamentos.add(pagamento);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar pagamentos: " + e.getMessage());
        }
        return pagamentos;
    }
    public double calcularTotalReceberPorUtilizador(String email, String nomeGrupo) {
        String sql = "SELECT SUM(dp.valor_divida) AS total_receber " +
                "FROM Despesa_Pagadores dp " +
                "JOIN Despesa d ON dp.despesa_id = d.id " +
                "WHERE d.criador_email = ? AND d.grupo_nome = ? AND dp.utilizador_email != ? AND dp.estado_pagamento = 'Pendente'";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, nomeGrupo);
            pstmt.setString(3, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_receber");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao calcular o total a receber por utilizador: " + e.getMessage());
        }
        return 0.0;
    }

    public Map<String, Double> calcularReceberDeCada(String email, String nomeGrupo) {
        Map<String, Double> receberDeCada = new HashMap<>();
        String sql = "SELECT quem_pagou, SUM(valor_pagamento) AS total_receber " +
                "FROM Pagamento " +
                "WHERE quem_recebeu = ? AND grupo_nome = ? " +
                "GROUP BY quem_pagou";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, nomeGrupo);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String pagador = rs.getString("quem_pagou");
                double valorReceber = rs.getDouble("total_receber");
                receberDeCada.put(pagador, valorReceber);
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao calcular valores a receber de cada membro: " + ex.getMessage());
        }
        return receberDeCada;
    }


}
