package Uteis;

import java.io.Serializable;

public class ServerBackUpSupport implements Serializable {

    private int portoTCP;
    private int versao;
    private String query;

    public ServerBackUpSupport(int portoTCP) {
        this.portoTCP = portoTCP;
    }

    public int getPortoTCP() {
        return portoTCP;
    }

    public void setPortoTCP(int portoTCP) {
        this.portoTCP = portoTCP;
    }

    public int getVersao() {
        return versao;
    }

    public void setVersao(int versao) {
        this.versao = versao;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
