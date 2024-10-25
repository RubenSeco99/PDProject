package BaseDeDados;

import java.sql.*;

public class ConnectDB {

    // Define o caminho para o arquivo da base de dados
    static String DB_URL = "jdbc:sqlite:";
    static Connection conn = null;
    static Statement stmt = null;

    public static Connection criarBaseDeDados(String DBPATH) {
        DB_URL += DBPATH;
        System.out.println(DB_URL);
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
                    + "grupo_nome TEXT NOT NULL, "
                    + "criador_email TEXT NOT NULL, "
                    + "FOREIGN KEY (grupo_nome) REFERENCES Grupo(nome) ON DELETE CASCADE);";
            stmt.executeUpdate(tabelaDespesa);

            String tabelaDespesaPagadores = "CREATE TABLE IF NOT EXISTS Despesa_Pagadores ("
                    + "despesa_id INTEGER NOT NULL, "
                    + "utilizador_email TEXT NOT NULL, "
                    + "valor_divida REAL NOT NULL CHECK (valor_divida >= 0), "
                    + "estado_pagamento TEXT NOT NULL CHECK (estado_pagamento IN ('Pago', 'Pendente')), "
                    + "FOREIGN KEY (despesa_id) REFERENCES Despesa(id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (utilizador_email) REFERENCES Utilizador(email) ON DELETE CASCADE, "
                    + "PRIMARY KEY (despesa_id, utilizador_email));";
            stmt.executeUpdate(tabelaDespesaPagadores);

            String tabelaPagamento = "CREATE TABLE IF NOT EXISTS Pagamento ("
                    + "id_pagamento INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "quem_pagou TEXT NOT NULL, "
                    + "quem_recebeu TEXT NOT NULL, "
                    + "valor_pagamento REAL NOT NULL CHECK (valor_pagamento > 0),\n "
                    + "data_pagamento TEXT NOT NULL, "
                    + "grupo_nome TEXT NOT NULL, "
                    + "FOREIGN KEY (quem_pagou) REFERENCES Utilizador(email) ON DELETE CASCADE, "
                    + "FOREIGN KEY (quem_recebeu) REFERENCES Utilizador(email) ON DELETE CASCADE, "
                    + "FOREIGN KEY (grupo_nome) REFERENCES Grupo(nome) ON DELETE CASCADE);";
            stmt.executeUpdate(tabelaPagamento);

            String tabelaVersao = "CREATE TABLE IF NOT EXISTS Versao ("
                    + "versao_numero INTEGER NOT NULL);";
            stmt.executeUpdate(tabelaVersao);

            String verificaVazio = "SELECT COUNT(*) FROM Versao";
            ResultSet resultSet = stmt.executeQuery(verificaVazio);
            if (resultSet.next() && resultSet.getInt(1) == 0) {
                String inserirValorInicial = "INSERT INTO Versao (versao_numero) VALUES (1)";
                stmt.executeUpdate(inserirValorInicial);
            }

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
