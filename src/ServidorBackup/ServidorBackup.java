package ServidorBackup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

class updateBDBKUP implements Runnable{

    InetAddress serverAddr;
    int serverPort;
    public updateBDBKUP(InetAddress inetAddress,int srvrPort){
        serverAddr=inetAddress;
        serverPort=srvrPort;
    }

    @Override
    public void run(){
        final String BKUP_SERVER_REQUEST = "BKUPCONNECT";
        String response;//tornar public para a thread aceder

        while(true) {
            try {

                try (Socket socket = new Socket(serverAddr, serverPort)) {
                    ObjectOutputStream Oout = new ObjectOutputStream(socket.getOutputStream());
                    Oout.writeObject(BKUP_SERVER_REQUEST);
                    Oout.flush();

                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    response = (String) in.readObject();
                    System.out.println("Resposta: " + response);
                    if(response.equalsIgnoreCase("EXIT")){
                        //mudar para variavel que quando deixar de receber heardbeats saí
                        break;
                    }
                    //atualiza a base de dados bkup
                }

            } catch (UnknownHostException e) {
                System.out.println("Destino desconhecido:\n\t" + e);
            } catch (NumberFormatException e) {
                System.out.println("O porto do servidor deve ser um inteiro positivo.");
            } catch (SocketTimeoutException e) {
                System.out.println("Nao foi recebida qualquer resposta:\n\t" + e);
            } catch (SocketException e) {
                System.out.println("Ocorreu um erro ao nivel do socket TCP:\n\t" + e);
            } catch (IOException e) {
                System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
            }catch (ClassNotFoundException e) {
                System.out.println("O objecto recebido não é do tipo esperado:\n\t"+e);
            }
        }

    }

}

public class ServidorBackup {
    static final int PORTOBACKUPUDP = 4444;
    static final int TIMEOUT = 10000;
    public static void main(String[] args) {//reajustar o main, colocar o que está
        // no main numa thread e serverBKUP main vai ficar a receber os heardBeats
        if (args.length != 2) {
            System.out.println("Sintaxe: java Client serverAddress serverPort");
            return;
        }
        Thread updateBDBKUP;
        try {
            InetAddress serverAddr = InetAddress.getByName(args[0]);
            int serverPort = Integer.parseInt(args[1]);
            updateBDBKUP = new Thread(new updateBDBKUP(serverAddr,serverPort));
            updateBDBKUP.setDaemon(true);
            updateBDBKUP.start();
        }catch(UnknownHostException e){
            System.out.println("Destino desconhecido:\n\t" + e);
        }
        //here
        try (MulticastSocket multiSocket = new MulticastSocket(PORTOBACKUPUDP)) {
            InetAddress groupAddress = InetAddress.getByName("230.44.44.44");
            NetworkInterface nif;
            try {
                nif = NetworkInterface.getByInetAddress(InetAddress.getByName(String.valueOf(groupAddress)));
            } catch (SocketException | NullPointerException | UnknownHostException | SecurityException ex) {
                nif = NetworkInterface.getByName("wlan0"); // e.g., lo, eth0, wlan0, en0...
            }

            multiSocket.joinGroup(new InetSocketAddress(groupAddress, PORTOBACKUPUDP), nif);
            byte[] buffer = new byte[1024];
            long lastReceivedTime;
            while (true) {
                multiSocket.setSoTimeout(TIMEOUT);
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multiSocket.receive(packet);

                    lastReceivedTime = System.currentTimeMillis();

                    try (ByteArrayInputStream Bin = new ByteArrayInputStream(packet.getData());
                         ObjectInputStream Oin = new ObjectInputStream(Bin)) {
                        String receivedMessage = (String) Oin.readObject();
                        System.out.println("Heartbeat recebido da porta: " + receivedMessage);
                    } catch (ClassNotFoundException e) {
                        System.out.println("Erro ao desserializar o objeto: " + e);
                    }
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastReceivedTime > TIMEOUT) {
                        throw new SocketTimeoutException();
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Nenhum heartbeat recebido nos ultimos 10 segundos. A fechar o servidor backup...");
                    break;
                }
            }

        } catch (SocketException e) {
            System.out.println("Ocorreu um erro no socket UDP:\n\t" + e);
        } catch (IOException e) {
            System.out.println("Ocorreu um erro no acesso ao multicast:\n\t" + e);
        }
        System.out.println("Servidor de backup encerrado.");
    }
}
