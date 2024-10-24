package ServidorBackup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ServerBackUpSupport implements Serializable {
    static final int PORTOBACKUPUDP = 4444;
    private int portoTCP;
    private int versao;
    private String query;
    private MulticastSocket socket;
    private DatagramPacket packet;
    private InetAddress groupAdress;

    public ServerBackUpSupport(int portoTCP) {
        this.portoTCP = portoTCP;
    }

    public void sendQueryToBackUpServer(String query, int versao) throws IOException {
        this.query = query;
        this.versao = versao;
        try (ByteArrayOutputStream Bout = new ByteArrayOutputStream();
             ObjectOutputStream Oout = new ObjectOutputStream(Bout)) {
            Oout.writeObject(this);
            packet = new DatagramPacket(Bout.toByteArray(), Bout.size(), groupAdress, PORTOBACKUPUDP);
            System.out.println("ENVIEI HEARTBEAT");
        }
        socket.send(packet);
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
