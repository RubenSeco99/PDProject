package Entidades;

import java.io.Serializable;
import java.util.Date;

public class Despesas implements Serializable {
    private String descricao;
    private java.sql.Date data;
    private double valor;

    public Despesas(){}

    public String getDescricao() {return descricao;}
    public void setDescricao(String descricao) {this.descricao = descricao;}
    public java.sql.Date getData() {return data;}
    public void setData(java.sql.Date data) {this.data = data;}
    public double getValor() {return valor;}
    public void setValor(double valor) {this.valor = valor;}

    @Override
    public String toString() {
        return "Despesas{" +
                "descricao='" + descricao + '\'' +
                ", data=" + data +
                ", valor=" + valor +
                '}';
    }
}
