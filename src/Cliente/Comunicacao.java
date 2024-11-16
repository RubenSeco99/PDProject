package Cliente;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Entidades.*;

public class Comunicacao implements Serializable {

    private String mensagem;
    private Utilizador utilizador;
    private ArrayList<Grupo> grupos;
    private ArrayList<Convite> convites;
    private ArrayList<Despesas> despesa;
    private ArrayList<Divida> dividas;
    private Pagamento pagamento;
    private ArrayList<Pagamento> pagamentos;
    private int despesaId;

    // Mapas para funcionamento do comando "Visualizar saldos"
    private Map<String, Double> gastosTotais;
    private Map<String, Double> valoresDevidos;
    private Map<String, Map<String, Double>> deveParaCada;
    private Map<String, Double> totalReceber;
    private Map<String, Map<String, Double>> receberDeCada;

    public Comunicacao() {
    }

    public Comunicacao(Utilizador utilizador) {
        this.utilizador = utilizador;
        this.grupos = new ArrayList<>();
        this.convites = new ArrayList<>();
        this.despesa = new ArrayList<>();
        this.pagamentos = new ArrayList<>();
        this.gastosTotais = new HashMap<>();
        this.valoresDevidos = new HashMap<>();
        this.deveParaCada = new HashMap<>();
        this.totalReceber = new HashMap<>();
        this.receberDeCada = new HashMap<>();
    }

    public Map<String, Double> getGastosTotais() {
        return gastosTotais;
    }

    public void setGastosTotais(Map<String, Double> gastosTotais) {
        this.gastosTotais = gastosTotais;
    }

    public Map<String, Double> getValoresDevidos() {
        return valoresDevidos;
    }

    public void setValoresDevidos(Map<String, Double> valoresDevidos) {
        this.valoresDevidos = valoresDevidos;
    }

    public Map<String, Map<String, Double>> getDeveParaCada() {
        return deveParaCada;
    }

    public void setDeveParaCada(Map<String, Map<String, Double>> deveParaCada) {
        this.deveParaCada = deveParaCada;
    }

    public Map<String, Double> getTotalReceber() {
        return totalReceber;
    }

    public void setTotalReceber(Map<String, Double> totalReceber) {
        this.totalReceber = totalReceber;
    }

    public Map<String, Map<String, Double>> getReceberDeCada() {
        return receberDeCada;
    }

    public void setReceberDeCada(Map<String, Map<String, Double>> receberDeCada) {
        this.receberDeCada = receberDeCada;
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

    public ArrayList<Pagamento> getPagamentos() {
        return pagamentos;
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

    public void setPagamentos(ArrayList<Pagamento> pagamentos) {
        this.pagamentos = pagamentos;
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
