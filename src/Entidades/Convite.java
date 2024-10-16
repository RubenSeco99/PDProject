package Entidades;

public class Convite {
    private int id;
    private String nomeGrupo;
    private String estado;
    private String destinatario;
    private String remetente;
    //adicionar destinatario e remetente


    public Convite(String nomeGrupo, String estado) {
        this.nomeGrupo = nomeGrupo;
        this.estado = estado;
    }
    public Convite(String nomeGrupo) {
        this.nomeGrupo = nomeGrupo;
    }

    public void setNome(String nomeGrupo) { this.nomeGrupo = nomeGrupo; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getNome() { return nomeGrupo; }
    public String getEstado() { return estado; }
    public String getDestinatario() { return destinatario; }
    public String getRemetente() { return remetente; }
}
