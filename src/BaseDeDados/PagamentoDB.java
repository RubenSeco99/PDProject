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

    /*
    public boolean insertPagamento(Pagamento pagamento){

        try{
            String query = "INSERT INTO Grupo (nome) VALUES (?)";

        }catch (SQLException e){
            System.out.println("Erro ao inserir pagamento: " + e.getMessage());
            return false;

        }
    }
    */
}
