package BaseDeDados;

import java.sql.*;

public class ConnectDB {

    static final String DB_URL = "jdbc:sqlite:src/BaseDeDados/BaseDados.db";
    // Define o caminho para o arquivo da base de dados

    static Connection conn = null;
    static Statement stmt = null;

    public static Connection criarBaseDeDados() {

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
                    + "password TEXT NOT NULL, "
                    + "ativo INTEGER NOT NULL DEFAULT 0);";//0==false, 1==true

            stmt.executeUpdate(tabelaUtilizador);

            String tabelaGrupo = "CREATE TABLE IF NOT EXISTS Grupo ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "nome TEXT NOT NULL UNIQUE, "
                    + "criador_email TEXT NOT NULL, "
                    + "FOREIGN KEY (criador_email) REFERENCES Utilizador(email) ON DELETE CASCADE);";
            stmt.executeUpdate(tabelaGrupo);

            String tabelaConvites = "CREATE TABLE IF NOT EXISTS Convites_Grupo ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "nome_grupo TEXT NOT NULL, "
                    + "remetente_email TEXT NOT NULL, "
                    + "destinatario_email TEXT NOT NULL, "
                    + "estado TEXT NOT NULL DEFAULT 'pendente', "
                    + "FOREIGN KEY (remetente_email) REFERENCES Utilizador(email) ON DELETE CASCADE, "
                    + "FOREIGN KEY (destinatario_email) REFERENCES Utilizador(email) ON DELETE CASCADE, "
                    + "FOREIGN KEY (nome_grupo) REFERENCES Grupo(nome) ON DELETE CASCADE);";
            stmt.executeUpdate(tabelaConvites);

            String tabelaUtilizadorGrupo = "CREATE TABLE IF NOT EXISTS Utilizador_Grupo ("
                    + "utilizador_email TEXT NOT NULL, "
                    + "grupo_nome TEXT NOT NULL, "
                    + "PRIMARY KEY (utilizador_email, grupo_nome), "
                    + "FOREIGN KEY (utilizador_email) REFERENCES Utilizador(email) ON DELETE CASCADE, "
                    + "FOREIGN KEY (grupo_nome) REFERENCES Grupo(nome) ON DELETE CASCADE);";

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
                    + "versao_numero INTEGER NOT NULL);";  // Número sequencial da versão
            stmt.executeUpdate(tabelaVersao);


            System.out.println("Todas as tabelas foram criadas com sucesso!");

        } catch (SQLException ex) {
            System.out.println("Erro de SQL: " + ex.getMessage());
        }

        return conn;

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
