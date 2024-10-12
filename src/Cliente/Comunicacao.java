package Cliente;

import java.io.Serializable;
import Utilizador.Utilizador;


public class Comunicacao implements Serializable {

    private String mensaguem;
    private Utilizador utilizador;

    public Comunicacao() {}

    public Comunicacao(Utilizador utilizador) {
        this.utilizador = utilizador;
    }
    public String getMensaguem() {return mensaguem;}

    public Utilizador getUtilizador() {return utilizador;}

    public void setMensaguem(String mensaguem) {
        this.mensaguem = mensaguem;
    }
}
