package Cliente;

import java.io.Serializable;

import Entidades.Grupo;
import Entidades.Utilizador;


public class Comunicacao implements Serializable {

    private String mensagem;
    private Utilizador utilizador;
    private Grupo grupo;

    public Comunicacao() {}

    public Comunicacao(Utilizador utilizador) {
        this.utilizador = utilizador;
        this.grupo= new Grupo();
    }

    public String getMensagem() {return mensagem;}
    public Utilizador getUtilizador() {return utilizador;}
    public Grupo getGrupo(){return grupo;}
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
    public void setUtilizador(Utilizador utilizador) {this.utilizador = utilizador;}
    public void setGrupo(Grupo grupo){this.grupo=grupo;}
    public void setNomeGrupo(String nome){this.grupo.setNome(nome);}
    @Override
    public String toString() {
        return "Comunicacao : mensagem=" + mensagem +"\n " +
               "Utilizador= " + utilizador.getEmail() + "\n" +
               "EstaAtivo= " + utilizador.getAtivo() + "\n";
    }
}
