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

            String tabelaPagamento = "CREATE TABLE IF NOT EXISTS Pagamento ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "data TEXT NOT NULL, "
                    + "valor REAL NOT NULL);";
            stmt.executeUpdate(tabelaPagamento);

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
