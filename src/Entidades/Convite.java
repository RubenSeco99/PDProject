package Entidades;

import java.io.Serializable;

public class Convite implements Serializable {
    private int groupID;
    private String nomeGrupo;
    private String estado;
    private String destinatario;
    private String remetente;


    public Convite(String nomeGrupo, String estado) {
        this.nomeGrupo = nomeGrupo;
        this.estado = estado;
    }
    public Convite(String nomeGrupo, String remetente, String destinatario, String estado) {
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
    public void setDestinatario(String destinatario){this.destinatario=destinatario;}
    public void setRemetente(String remetente){this.remetente=remetente;}
    public String getRemetente() { return remetente; }

    @Override
    public String toString() {
        return "Convite{" +
                ", nomeGrupo='" + nomeGrupo + '\'' +
                ", estado='" + estado + '\'' +
                ", destinatario='" + destinatario + '\'' +
                ", remetente='" + remetente + '\'' +
                '}'+"\n";
    }
}
