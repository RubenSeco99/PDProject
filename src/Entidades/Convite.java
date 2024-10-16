package Entidades;

import java.io.Serializable;

public class Convite implements Serializable {
    private int groupID;
    private String nomeGrupo;
    private String estado;
    private String destinatario;
    private String remetente;
    //adicionar destinatario e remetente


    public Convite(String nomeGrupo, String estado) {
        this.nomeGrupo = nomeGrupo;
        this.estado = estado;
    }
    public Convite(String nomeGrupo, String destinatario, String remetente, String estado) {
        this.nomeGrupo = nomeGrupo;
        this.estado = estado;
        this.destinatario = destinatario;
        this.remetente = remetente;
    }
    public Convite(String nomeGrupo) {
        this.nomeGrupo = nomeGrupo;
    }

    public void setNome(String nomeGrupo) { this.nomeGrupo = nomeGrupo; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getNomeGrupo() { return nomeGrupo; }
    public String getEstado() { return estado; }
    public String getDestinatario() { return destinatario; }
    public String getRemetente() { return remetente; }
}
