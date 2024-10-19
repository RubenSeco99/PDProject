package Entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Utilizador implements Serializable {

    private String nome;
    private String password;
    private int telefone;
    private String email;
    private int ativo;
    private List<Grupo> grupos;
    private List<Convite> convites;
    private Grupo grupo_atual;
    private Despesas despesas;

    public Utilizador(String nome, String password, int telefone, String email) {
        this.nome = nome;
        this.password = password;
        this.telefone = telefone;
        this.email = email;
        this.ativo = 0;
        this.grupos = new ArrayList<>();
        this.grupo_atual = new Grupo();
        this.despesas = new Despesas();
    }
    public Utilizador() {
        this.grupos = new ArrayList<>();
        this.grupo_atual = new Grupo();
    }

    public String getNome() {return nome;}
    public String getPassword() {return password;}
    public int getTelefone() {return telefone;}
    public String getEmail() {return email;}
    public List<Grupo> getGrupos() { return grupos; }
    public void setNome(String nome) {this.nome = nome;}
    public void setPassword(String password) {this.password = password;}
    public void setTelefone(int telefone) {this.telefone = telefone;}
    public void setEmail(String email) {this.email = email;}
    public int getAtivo() {return ativo;}
    public void setAtivo(int ativo) {this.ativo = ativo;}
    public void setGrupos(List<Grupo> grupos) { this.grupos = grupos; }
    public List<Convite> getConvites() {return convites;}
    public Grupo getGrupoAtual() {return grupo_atual;}
    public boolean checkConviteExiste(String nome){
        for(var convite:this.getConvites())
            if(convite.getNomeGrupo().equalsIgnoreCase(nome)) {
                return true;
            }
        return false;
    }
    public Convite getConvite(String nome){
        for(var convite:this.getConvites())
            if(convite.getNomeGrupo().equalsIgnoreCase(nome)) {
                return convite;
            }
        return null;
    }
    public void setGrupoAtual(Grupo grupo) { this.grupo_atual = grupo; }

    public void setUtilizador(Utilizador utilizador){
        this.nome = utilizador.nome;
        this.password = utilizador.password;
        this.telefone = utilizador.telefone;
        this.email = utilizador.email;
        this.ativo = utilizador.ativo;
        this.grupo_atual = utilizador.grupo_atual;
        this.grupos = utilizador.grupos;
        this.convites = utilizador.convites;
    }
    public void addGrupo(Grupo grupo) {
        if (!this.grupos.contains(grupo)) {
            this.grupos.add(grupo);
        }
    }

    public void setConvites(ArrayList<Convite> convites) {this.convites = convites;}
    public Convite getConvitePorEstado(String estado){
        for(var convite:convites)
            if(convite.getEstado().equalsIgnoreCase(estado))
                return convite;
        return null;
    }
    public void setGrupoAtualPorNome(String nome){
        this.grupo_atual.setNomeProvisorio(nome);
    }
    public void removeGrupo(Grupo grupo) {
        this.grupos.remove(grupo);
    }
    @Override
    public String toString() {
        return  "Utilizador{" +
                "nome='" + nome + '\'' +
                ", password='" + password + '\'' +
                ", telefone=" + telefone +
                ", email='" + email + '\'' +
                ", ativo=" + ativo +
                ", grupos=" + grupos +  // Exibe os grupos
                '}';
    }
}
