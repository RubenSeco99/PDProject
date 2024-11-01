package Cliente;

import java.io.Serializable;
import java.util.ArrayList;
import Entidades.*;

public class Comunicacao implements Serializable {

    private String mensagem;
    private Utilizador utilizador;
    private ArrayList<Grupo> grupos;
    private ArrayList<Convite> convites;
    private ArrayList<Despesas> despesa;
    private ArrayList<Divida> dividas;
    private Pagamento pagamento;
    private int despesaId;

    public Comunicacao() {
    }

    public Comunicacao(Utilizador utilizador) {
        this.utilizador = utilizador;
        this.grupos = new ArrayList<>();
        this.convites = new ArrayList<>();
        this.despesa = new ArrayList<>();
    }

    public String getMensagem() {
        return mensagem;
    }

    public Utilizador getUtilizador() {
        return utilizador;
    }

    public ArrayList<Grupo> getGrupos() {
        return grupos;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public void setUtilizador(Utilizador utilizador) {
        this.utilizador = utilizador;
    }

    public ArrayList<Convite> getConvites() {
        return convites;
    }

    public void setConvites(ArrayList<Convite> convites) {
        this.convites = convites;
    }

    public void setConvite(Convite convite) {
        this.convites.clear();
        this.convites.add(convite);
    }

    public void setGrupos(String nomeGrupo) {
        this.grupos.clear();
        this.grupos.add(new Grupo(nomeGrupo));
    }

    public ArrayList<Despesas> getDespesa() {
        return despesa;
    }

    public ArrayList<Divida> getDividas() {
        return dividas;
    }

    public void setDespesa(ArrayList<Despesas> despesa) {
        this.despesa = despesa;
    }

    public void setDividas(ArrayList<Divida> dividas) {
        this.dividas = dividas;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }

    public void setDespesaId(int despesaId) {this.despesaId = despesaId;}

    @Override
    public String toString() {
        String result = "Comunicacao: mensagem=" + mensagem + "\n";
        if (utilizador != null) {
            result += "Utilizador= " + utilizador.getEmail() + "\n" + "EstaAtivo= " + utilizador.getAtivo() + "\n";
        }
        if (utilizador != null && utilizador.getGrupoAtual() != null) {
            result += "Grupo= " + utilizador.getGrupoAtual().getNome() + "\n";
        }
        return result;
    }
}
