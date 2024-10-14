package BaseDeDados;
import java.sql.*;

public class UtilizadorGrupoDB {
    private final Connection connection;
    public UtilizadorGrupoDB(Connection connection) {
        this.connection = connection;
    }
}
