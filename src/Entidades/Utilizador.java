package Entidades;

import java.io.Serializable;

public class Utilizador implements Serializable {

    private String nome;
    private String password;
    private int telefone;
    private String email;
    private boolean ativo;

    public Utilizador(String nome, String password, int telefone, String email) {
        this.nome = nome;
        this.password = password;
        this.telefone = telefone;
        this.email = email;
        this.ativo = false;
    }

    public Utilizador() {}

    public String getNome() {return nome;}
    public String getPassword() {return password;}
    public int getTelefone() {return telefone;}
    public String getEmail() {return email;}
    public void setNome(String nome) {this.nome = nome;}
    public void setPassword(String password) {this.password = password;}
    public void setTelefone(int telefone) {this.telefone = telefone;}
    public void setEmail(String email) {this.email = email;}
    public boolean getAtivo() {return ativo;}
    public void setAtivo(boolean ativo) {this.ativo = ativo;}



    @Override
    public String toString() {
        return  "Utilizador{" +
                "nome='" + nome + '\'' +
                ", password='" + password + '\'' +
                ", telefone=" + telefone +
                ", email='" + email + '\'' +
                '}';
    }
}
