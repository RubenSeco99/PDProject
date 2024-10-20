package Entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Despesas implements Serializable {
    private String descricao;
    private java.sql.Date data;
    private double valor;
    private String pagador;
    private ArrayList<String> utilizadoresPartilhados;
    private int idDespesa;

    public Despesas(){
        this.utilizadoresPartilhados = new ArrayList<>();
    }

    public String getDescricao() {return descricao;}
    public void setDescricao(String descricao) {this.descricao = descricao;}
    public java.sql.Date getData() {return data;}
    public void setData(java.sql.Date data) {this.data = data;}
    public double getValor() {return valor;}
    public void setValor(double valor) {this.valor = valor;}
    public String getPagador() {return pagador;}
    public void setPagador(String pagador) {this.pagador = pagador;}
    public ArrayList<String> getUtilizadoresPartilhados() {return utilizadoresPartilhados;}
    public void setUtilizadoresPartilhados(ArrayList<String> utilizadoresPartilhados) {this.utilizadoresPartilhados = utilizadoresPartilhados;}
    public int getIdDespesa() {return idDespesa;}
    public void setIdDespesa(int idDespesa) {this.idDespesa = idDespesa;}

    @Override
    public String toString() {
        return "Despesas["+
                " idDespesa= " + idDespesa +
                " descricao= " + descricao + '\'' +
                ", data= " + data +
                ", valor= " + valor +
                ']';
    }
}
