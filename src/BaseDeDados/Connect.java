package BaseDeDados;

import java.sql.*;

public class Connect {

    static final String DB_URL = "jdbc:sqlite:src/BaseDeDados/BaseDados.db";
    // Define o caminho para o arquivo da base de dados

    static Connection conn = null;
    static Statement stmt = null;

    public static void criarBaseDeDados() {

        try {
            // Conectar a base de dados (será criada se não existir)
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();

            // Criação das tabelas
            String tabelaUtilizador = "CREATE TABLE IF NOT EXISTS Utilizador ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "nome TEXT NOT NULL, "
                    + "telefone TEXT, "
                    + "email TEXT NOT NULL UNIQUE, "
                    + "password TEXT NOT NULL);";
            stmt.executeUpdate(tabelaUtilizador);

            String tabelaGrupo = "CREATE TABLE IF NOT EXISTS Grupo ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "nome TEXT NOT NULL);";
            stmt.executeUpdate(tabelaGrupo);

            String tabelaUtilizadorGrupo = "CREATE TABLE IF NOT EXISTS Utilizador_Grupo ("
                    + "utilizador_id INTEGER NOT NULL, "
                    + "grupo_id INTEGER NOT NULL, "
                    + "PRIMARY KEY (utilizador_id, grupo_id), "
                    + "FOREIGN KEY (utilizador_id) REFERENCES Utilizador(id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (grupo_id) REFERENCES Grupo(id) ON DELETE CASCADE);";
            stmt.executeUpdate(tabelaUtilizadorGrupo);

            String tabelaDespesa = "CREATE TABLE IF NOT EXISTS Despesa ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "descricao TEXT NOT NULL, "
                    + "valor REAL NOT NULL, "
                    + "data TEXT NOT NULL, "
                    + "grupo_id INTEGER NOT NULL, "
                    + "FOREIGN KEY (grupo_id) REFERENCES Grupo(id) ON DELETE CASCADE);";
            stmt.executeUpdate(tabelaDespesa);

            String tabelaUtilizadorDespesa = "CREATE TABLE IF NOT EXISTS Utilizador_Despesa ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "utilizador_id INTEGER NOT NULL, "
                    + "despesa_id INTEGER NOT NULL, "
                    + "tipo_relacionamento TEXT NOT NULL CHECK (tipo_relacionamento IN ('inserir', 'pagar', 'dividir')), "
                    + "FOREIGN KEY (utilizador_id) REFERENCES Utilizador(id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (despesa_id) REFERENCES Despesa(id) ON DELETE CASCADE);";
            stmt.executeUpdate(tabelaUtilizadorDespesa);


            String tabelaPagamento = "CREATE TABLE IF NOT EXISTS Pagamento ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "data TEXT NOT NULL, "
                    + "valor REAL NOT NULL, "
                    + "utilizador_pagador_id INTEGER NOT NULL, "
                    + "utilizador_recebedor_id INTEGER NOT NULL, "
                    + "FOREIGN KEY (utilizador_pagador_id) REFERENCES Utilizador(id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (utilizador_recebedor_id) REFERENCES Utilizador(id) ON DELETE CASCADE);";
            stmt.executeUpdate(tabelaPagamento);

            String tabelaVersao = "CREATE TABLE IF NOT EXISTS Versao ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " // Momento da criação da versão
                    + "estado TEXT DEFAULT 'pendente' CHECK (estado IN ('pendente', 'bem sucedido', 'mal sucedido')), " // Estado da versão,
                    //  ⬆️⬆️⬆️ atualizar para bem sucedido/mal sucedido depois do envio para o backup
                    + "versao_numero INTEGER NOT NULL);";  // Número sequencial da versão
            stmt.executeUpdate(tabelaVersao);


            System.out.println("Todas as tabelas foram criadas com sucesso!");

        } catch (SQLException ex) {
            System.out.println("Erro de SQL: " + ex.getMessage());
        } finally {
            // Fechar os recursos
            fecharBaseDeDados();
        }
    }
    public static void fecharBaseDeDados(){
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException ex) {
            System.out.println("Erro ao fechar os recursos: " + ex.getMessage());
        }
    }
}
