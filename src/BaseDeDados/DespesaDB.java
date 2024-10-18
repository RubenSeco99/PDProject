package BaseDeDados;

import java.sql.*;
import java.util.List;

public class DespesaDB {
    private final Connection connection;
    public DespesaDB(Connection connection) {
        this.connection = connection;
    }
    //metodos criar despesa
    //metodo apagar despesa
    //ver despesa (valores despesa vao ter de ir para a tabela despesapagadores ...)
    public boolean criarDespesa(String descricao, double valor, String data, String grupoNome, String criadorEmail, List<String> pagadores) {
        try {//verifica se esta correto, segue sempre o MODELO SEPARAÇÃO DE DADOS   
            // Inserir a despesa na tabela "Despesa"
            String queryDespesa = "INSERT INTO Despesa (descricao, valor, data, grupo_nome, criador_email) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatementDespesa = connection.prepareStatement(queryDespesa, Statement.RETURN_GENERATED_KEYS);
            preparedStatementDespesa.setString(1, descricao);
            preparedStatementDespesa.setDouble(2, valor);
            preparedStatementDespesa.setString(3, data);
            preparedStatementDespesa.setString(4, grupoNome);
            preparedStatementDespesa.setString(5, criadorEmail);

            int rowsAffected = preparedStatementDespesa.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = preparedStatementDespesa.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int despesaId = generatedKeys.getInt(1);
                    String queryPagadores = "INSERT INTO Despesa_Pagadores (despesa_id, utilizador_email, estado_pagamento) VALUES (?, ?, ?)";
                    PreparedStatement preparedStatementPagadores = connection.prepareStatement(queryPagadores);

                    for (String pagador : pagadores) {
                        preparedStatementPagadores.setInt(1, despesaId);
                        preparedStatementPagadores.setString(2, pagador);
                        preparedStatementPagadores.setString(3, "não pago");  // Estado inicial como "não pago"
                        preparedStatementPagadores.addBatch();  // Adicionar ao lote de inserção
                    }

                    preparedStatementPagadores.executeBatch();  // Executar o lote de inserções
                }
                return true;  // Inserção bem-sucedida
            }
        } catch (SQLException e) {
            System.out.println("Erro ao criar despesa: " + e.getMessage());
        }
        return false;
    }

}
