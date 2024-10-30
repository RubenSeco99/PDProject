package ServidorBackup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ServerBackUpSupport implements Serializable {
    static final int PORTOBACKUPUDP = 4444;
    private int portoTCP;
    private int versao;
    private String query;
    private List<Object> parametros;  // Atributo para os parâmetros da query
    private transient MulticastSocket socket;  // Marcado como transient para evitar serialização
    private InetAddress groupAddress;

    public ServerBackUpSupport(int portoTCP) {
        this.portoTCP = portoTCP;
        this.query = "";
        this.parametros = null;
    }

    public void inicializeMulticast() {
        try {
            groupAddress = InetAddress.getByName("230.44.44.44");
            socket = new MulticastSocket(PORTOBACKUPUDP);
            NetworkInterface nif;
            try {
                nif = NetworkInterface.getByInetAddress(groupAddress);  // Tenta obter a interface pela rede
            } catch (SocketException | NullPointerException | SecurityException ex) {
                nif = NetworkInterface.getByName("wlan0"); // Alternativa de fallback
            }

            socket.joinGroup(new InetSocketAddress(groupAddress, PORTOBACKUPUDP), nif);
            System.out.println("MulticastSocket inicializado e inserido no grupo: " + groupAddress);

        } catch (IOException e) {
            System.out.println("Erro ao inicializar o MulticastSocket: " + e.getMessage());
        }
    }

    public void sendMessageToBackUpServer() {
        if (socket == null || socket.isClosed()) {
            System.out.println("Socket de multicast não inicializado corretamente.");
            inicializeMulticast();
        }

        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             ObjectOutputStream oout = new ObjectOutputStream(bout)) {

            oout.writeObject(this);
            DatagramPacket packet = new DatagramPacket(bout.toByteArray(), bout.size(), groupAddress, PORTOBACKUPUDP);
            socket.send(packet);
            System.out.println("Mensagem enviada ao backup com sucesso.");

            this.query = "";
            this.parametros = null;
        } catch (IOException e) {
            System.out.println("Erro ao enviar mensagem para o servidor de backup: " + e.getMessage());
        }
    }

    public int getPortoTCP() { return portoTCP; }

    public void setPortoTCP(int portoTCP) { this.portoTCP = portoTCP; }

    public int getVersao() { return versao; }

    public void setVersao(int versao) { this.versao = versao; }

    public String getQuery() { return query; }

    public void setQuery(String query) { this.query = query; }

    public List<Object> getParametros() {return parametros; }

    public void setParametros(List<Object> parametros) { this.parametros = parametros; }
}
