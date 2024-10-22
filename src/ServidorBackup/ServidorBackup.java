package ServidorBackup;

import Uteis.ServerBackUpSupport;

import java.io.*;
import java.net.*;

public class ServidorBackup {
    private static final int PORTOBACKUPUDP = 4444;
    private static final int TIMEOUT = 30000;
    public static final int MAX_SIZE = 4000;

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Sintaxe: Java Client serverAddress serverPort");
            return;
        }

        String fileName,localFilePath = args[0];
        fileName = "BaseDadosBackup.db";
        File localDB = new File(localFilePath);
        FileOutputStream localFileOutputStream = null;

        if (localDB.listFiles() != null && localDB.listFiles().length > 0) {
            System.out.println("A directoria " + localDB + " nao está vazia!");
            return;
        }

        if (!localDB.exists()) {
            System.out.println("A directoria " + localDB + " nao existe!");
            return;
        }
        // [PT] Verificar se a directoria é mesmo uma directoria
        if (!localDB.isDirectory()) {
            System.out.println("O caminho " + localDB + " nao se refere a uma directoria!");
            return;
        }
        // [PT] Verificar se temos acesso de escrita na directoria
        if (!localDB.canWrite()) {
            System.out.println("Sem permissoes de escrita na directoria " + localDB);
            return;
        }

        ServerBackUpSupport serverSupport = new ServerBackUpSupport(PORTOBACKUPUDP);
        Object received;

        try (MulticastSocket multiSocket = new MulticastSocket(PORTOBACKUPUDP)) {
            InetAddress groupAddress = InetAddress.getByName("230.44.44.44");
            NetworkInterface nif;
            try {
                nif = NetworkInterface.getByInetAddress(InetAddress.getByName(String.valueOf(groupAddress)));
            } catch (SocketException | NullPointerException | UnknownHostException | SecurityException ex) {
                nif = NetworkInterface.getByName("wlan0"); // e.g., lo, eth0, wlan0, en0...
            }

            multiSocket.joinGroup(new InetSocketAddress(groupAddress, PORTOBACKUPUDP), nif);
            DatagramPacket receivedPacket = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
            multiSocket.receive(receivedPacket);
            try(ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(receivedPacket.getData()))) {
                received = oin.readObject();
                if(received instanceof ServerBackUpSupport) {
                    serverSupport = (ServerBackUpSupport) received;
                }
                System.out.println("Porto recebido: " + serverSupport.getPortoTCP());

                try(Socket socketTCP = new Socket(InetAddress.getLocalHost(), serverSupport.getPortoTCP())) {
                    socketTCP.setSoTimeout(TIMEOUT);

                    try {
                        localFilePath = localDB.getCanonicalPath() + File.separator + fileName;
                        localFileOutputStream = new FileOutputStream(localFilePath);
                        System.out.println("Ficheiro " + localFilePath + " criado.");
                    }catch (IOException e) {
                        if (localFilePath == null) {
                            System.out.println("Ocorreu a excepcao {" + e + "} ao obter o caminho canonico para o ficheiro local!");
                        } else {
                            System.out.println("Ocorreu a excepcao {" + e + "} ao tentar criar o ficheiro " + localFilePath + "!");
                        }
                        return;
                    }

                    InputStream reader = socketTCP.getInputStream();
                    byte[] buffer = new byte[MAX_SIZE];  // Buffer para armazenar os dados recebidos
                    int bytesRead;
                    int contador = 0;

                    while ((bytesRead = reader.read(buffer)) > 0) {  // Lê blocos de dados do servidor
                        System.out.println("Bloco lido");
                        localFileOutputStream.write(buffer, 0, bytesRead);  // Escreve no ficheiro local
                        contador++;  // Contador de blocos
                    }

                    System.out.println("Transferencia concluida (numero de blocos: " + contador + ")");

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
                }
            } catch (ClassNotFoundException e) {
                System.out.println();
                System.out.println("Mensagem recebida de tipo inesperado! " + e);
            } catch (IOException e) {
                System.out.println();
                System.out.println("Impossibilidade de aceder ao conteudo da mensagem recebida! " + e);
            } catch (Exception e) {
                System.out.println();
                System.out.println("Excepcao: " + e);
            }


            byte[] buffer = new byte[MAX_SIZE];
            while (true) {
                multiSocket.setSoTimeout(TIMEOUT);
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multiSocket.receive(packet);

                    try (ByteArrayInputStream Bin = new ByteArrayInputStream(packet.getData());
                         ObjectInputStream Oin = new ObjectInputStream(Bin)) {
                        String receivedMessage = (String) Oin.readObject();
                        System.out.println("Heartbeat recebido da porta: " + receivedMessage);
                    } catch (ClassNotFoundException e) {
                        System.out.println("Erro ao desserializar o objeto: " + e);
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Nenhum heartbeat recebido nos ultimos 30 segundos. A fechar o servidor backup...");
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
