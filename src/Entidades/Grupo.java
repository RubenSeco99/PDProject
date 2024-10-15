package Entidades;

import java.io.Serializable;

public class Grupo implements Serializable {

    private int id;
    private String nome;
    public Grupo(){}//uso no GrupoDB
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
}
