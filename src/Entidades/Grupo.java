package Entidades;

import java.io.Serializable;

public class Grupo implements Serializable {

    private int id;
    private String nome;
    private String nomeProvisorio;//so usado quando da mudanca do nome do grupo
    public Grupo(){this.nome = "";}//uso no GrupoDB
    public Grupo(String nome) {
        setNome(nome);
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
        return nome;
    }

    public void setNomeProvisorio(String nome) {
        this.nome = nome;
    }

}
