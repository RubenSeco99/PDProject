package Entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Grupo implements Serializable {
    private static final long serialVersionUID = 6190246476684431756L; // Estava a dar "Conexão terminado pelo servidor" se não tivesse esta linha
    private int id;
    private String nome;
    private String nomeProvisorio;//so usado quando da mudanca do nome do grupo
    private List<Despesas> despesas;
    public Grupo(){this.nome = "";
    despesas = new ArrayList<>();}//uso no GrupoDB
    public Grupo(String nome) {
        setNome(nome);
        despesas = new ArrayList<>();
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getNomeProvisorio() {
        return nomeProvisorio;
    }
    public List<Despesas> getDespesas() { return despesas; }
    public void setNomeProvisorio(String nomeProvisorio) {
        this.nomeProvisorio = nomeProvisorio;
    }

}
