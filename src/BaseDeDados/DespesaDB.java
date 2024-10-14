package BaseDeDados;

import java.sql.*;

public class DespesaDB {
    private final Connection connection;
    public DespesaDB(Connection connection) {
        this.connection = connection;
    }
}
