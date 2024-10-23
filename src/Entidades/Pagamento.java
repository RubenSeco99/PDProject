package Entidades;

import java.io.Serializable;
import java.sql.Date;

public class Pagamento implements Serializable {

    private String quemPagou;
    private String quemRecebeu;
    private double valorPagamento;
    private java.sql.Date dataPagamento;
    private String grupoNome;
    private int idDespesa;

    public Pagamento() {}

    public String getQuemPagou() {return quemPagou;}
    public void setQuemPagou(String quemPagou) {this.quemPagou = quemPagou;}
    public String getQuemRecebeu() {return quemRecebeu;}
    public void setQuemRecebeu(String quemRecebeu) {this.quemRecebeu = quemRecebeu;}
    public double getValorPagamento() {return valorPagamento;}
    public void setValorPagamento(double valorPagamento) {this.valorPagamento = valorPagamento;}
    public Date getDataPagamento() {return dataPagamento;}
    public void setDataPagamento(Date dataPagamento) {this.dataPagamento = dataPagamento;}
    public int getIdDespesa() {return idDespesa;}
    public void setIdDespesa(int idDespesa) {this.idDespesa = idDespesa;}
    public String getGrupoNome() {return grupoNome;}
    public void setGrupoNome(String grupoNome) {this.grupoNome = grupoNome;}
}
