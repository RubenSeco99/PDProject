package Cliente;

import java.io.Serializable;
import java.util.ArrayList;

import Entidades.Convite;
import Entidades.Grupo;
import Entidades.Utilizador;


public class Comunicacao implements Serializable {

    private String mensagem;
    private Utilizador utilizador;
    private Grupo grupo;//mudar para array
    private ArrayList<Convite> convites;

    public Comunicacao() {
        //this.utilizador= new Utilizador();
        //this.grupo= new Grupo();
    }

    public Comunicacao(Utilizador utilizador) {
        this.utilizador = utilizador;
        this.grupo= new Grupo();
        this.convites= new ArrayList<>();
    }

    public String getMensagem() {return mensagem;}
    public Utilizador getUtilizador() {return utilizador;}
    public Grupo getGrupo(){return grupo;}
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
    public void setUtilizador(Utilizador utilizador) {this.utilizador = utilizador;}

    public ArrayList<Convite> getConvites() {return convites;}

    public void setConvites(ArrayList<Convite> convites) {this.convites = convites;}
    public void setGrupo(Grupo grupo){this.grupo=grupo;}
    public void setNomeGrupo(String nome){this.grupo.setNome(nome);}
    //public ArrayList<String> getConvites() {return convites;}
    //public void setConvites(ArrayList<String> convites) {this.convites = convites;}
    public void setConvite(String nomeConvite) {
        this.convites.clear();
        this.convites.add(new Convite(nomeConvite));}


    @Override
    public String toString() {
        String result = "Comunicacao: mensagem=" + mensagem + "\n";
        if (utilizador != null) {
            result += "Utilizador= " + utilizador.getEmail() + "\n" + "EstaAtivo= " + utilizador.getAtivo() + "\n";
        }
        if (grupo != null) {
            result += "Grupo= " + grupo.getNome() + "\n";
        }
        return result;
    }
}
