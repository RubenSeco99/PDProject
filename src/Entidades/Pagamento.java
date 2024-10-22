package Entidades;

import java.sql.Date;

public class Pagamento {

    private String quemPagou;
    private String quemRecebeu;
    private double valorPagamento;
    private java.sql.Date dataPagamento;

    public Pagamento() {}

    public String getQuemPagou() {return quemPagou;}
    public void setQuemPagou(String quemPagou) {this.quemPagou = quemPagou;}
    public String getQuemRecebeu() {return quemRecebeu;}
    public void setQuemRecebeu(String quemRecebeu) {this.quemRecebeu = quemRecebeu;}
    public double getValorPagamento() {return valorPagamento;}
    public void setValorPagamento(double valorPagamento) {this.valorPagamento = valorPagamento;}
    public Date getDataPagamento() {return dataPagamento;}
    public void setDataPagamento(Date dataPagamento) {this.dataPagamento = dataPagamento;}
}
