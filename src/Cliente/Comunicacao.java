package Cliente;

import java.io.Serializable;
import java.util.ArrayList;

import Entidades.Convite;
import Entidades.Grupo;
import Entidades.Utilizador;


public class Comunicacao implements Serializable {

    private String mensagem;
    private Utilizador utilizador;
    private ArrayList<Grupo> grupos;
    private ArrayList<Convite> convites;

    public Comunicacao() {
        //this.utilizador= new Utilizador();
        //this.grupo= new Grupo();
    }

    public Comunicacao(Utilizador utilizador) {
        this.utilizador = utilizador;
        this.grupos = new ArrayList<>();
        this.convites= new ArrayList<>();
    }

    public String getMensagem() {return mensagem;}
    public Utilizador getUtilizador() {return utilizador;}
    public ArrayList <Grupo> getGrupos(){return grupos;}
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
    public void setUtilizador(Utilizador utilizador) {this.utilizador = utilizador;}

    public ArrayList<Convite> getConvites() {return convites;}

    public void setConvites(ArrayList<Convite> convites) {this.convites = convites;}
    //public ArrayList<String> getConvites() {return convites;}
    //public void setConvites(ArrayList<String> convites) {this.convites = convites;}
    public void setConvite(Convite convite) {
        this.convites.clear();
        this.convites.add(convite);
    }
    public void setGrupos(String nomeGrupo) {
        this.grupos.clear();
        this.grupos.add(new Grupo(nomeGrupo));
    }


    @Override
    public String toString() {
        String result = "Comunicacao: mensagem=" + mensagem + "\n";
        if (utilizador != null) {
            result += "Utilizador= " + utilizador.getEmail() + "\n" + "EstaAtivo= " + utilizador.getAtivo() + "\n";
        }
        if (utilizador.getGrupoAtual() != null) {
            result += "Grupo= " + utilizador.getGrupoAtual().getNome() + "\n";
        }
        return result;
    }
}
