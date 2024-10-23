package Entidades;

import java.io.Serializable;

public class Divida implements Serializable {
    private int id;
    private String utilizador_email;
    private double valor;
    private String estado;


    public void setIdDespesa(int despesaId) {
        this.id = despesaId;
    }

    public void setUtilizadorEmail(String utilizadorEmail) {
        this.utilizador_email = utilizadorEmail;
    }

    public void setValorDivida(double valorDivida) {
        this.valor = valorDivida;
    }

    public void setEstadoPagamento(String estadoPagamento) {
        this.estado = estadoPagamento;
    }

    public int getIdDivida() {
        return id;
    }

    @Override
    public String toString() {
        return "Divida {" +
                "id=" + id +
                ", utilizador_email='" + utilizador_email + '\'' +
                ", valor=" + valor +
                ", estado='" + estado + '\'' +
                '}';
    }
}
