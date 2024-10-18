package BaseDeDados;

import java.sql.Connection;

public class DespesaPagadoresDB {
    private final Connection connection;
    public DespesaPagadoresDB(Connection connection) {
        this.connection = connection;
    }
}
