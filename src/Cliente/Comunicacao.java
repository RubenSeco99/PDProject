package Cliente;

import java.io.Serializable;
import Entidades.Utilizador;


public class Comunicacao implements Serializable {

    private String mensagem;
    private Utilizador utilizador;

    public Comunicacao() {}

    public Comunicacao(Utilizador utilizador) {
        this.utilizador = utilizador;
    }

    public String getMensagem() {return mensagem;}
    public Utilizador getUtilizador() {return utilizador;}
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
    public void setUtilizador(Utilizador utilizador) {this.utilizador = utilizador;}

    @Override
    public String toString() {
        return "Comunicacao : mensagem=" + mensagem +"\n " +
               "Utilizador= " + utilizador.getEmail() + "\n" +
               "EstaAtivo= " + utilizador.getAtivo() + "\n";
    }
}
