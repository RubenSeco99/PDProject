package BaseDeDados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import Entidades.Pagamento;

public class PagamentoDB {
    private final Connection connection;
    public PagamentoDB(Connection connection) {
        this.connection = connection;
    }




}
