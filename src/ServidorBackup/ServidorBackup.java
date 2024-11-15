package ServidorBackup;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

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
        DatagramPacket receivedPacket;
        Connection backupConnection = null;

        if (localDB.listFiles() != null && localDB.listFiles().length > 0) {
            System.out.println("A directoria " + localDB + " nao está vazia!");
            return;
        }

        if (!localDB.exists()) {
            System.out.println("A directoria " + localDB + " nao existe!");
            return;
        }

        if (!localDB.isDirectory()) {
            System.out.println("O caminho " + localDB + " nao se refere a uma directoria!");
            return;
        }

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
            receivedPacket = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
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
                    byte[] buffer = new byte[MAX_SIZE];
                    int bytesRead;
                    int contador = 0;

                    while ((bytesRead = reader.read(buffer)) > 0) {
                        System.out.println("Bloco lido");
                        localFileOutputStream.write(buffer, 0, bytesRead);
                        contador++;
                    }

                    System.out.println("Transferencia concluida (numero de blocos: " + contador + ")");
                    multiSocket.setSoTimeout(TIMEOUT);

                    backupConnection = conectBackUpDB(localFilePath);

                    int versaoAnterior = -1;

                    try {
                        while (true) {
                            receivedPacket = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
                            multiSocket.receive(receivedPacket);

                            try (ObjectInputStream Oin = new ObjectInputStream(new ByteArrayInputStream(receivedPacket.getData()))) {
                                received = Oin.readObject();

                                if (received instanceof ServerBackUpSupport) {
                                    serverSupport = (ServerBackUpSupport) received;

                                    String query = serverSupport.getQuery();
                                    List<Object> parametros = serverSupport.getParametros();

                                    if (query != null && parametros != null) {
                                        if(versaoAnterior == serverSupport.getVersao()) {
                                            System.out.println("Versao anterior igual a versao atual\nA sair...");
                                            break;
                                        }
                                        executarQueryNoBackup(backupConnection, query, parametros);
                                        System.out.println("Query de backup executada: " + query);
                                    } else {
                                        System.out.println("[HeartBeat recebido]");
                                    }
                                }
                            } catch (ClassNotFoundException e) {
                                System.out.println("Mensagem recebida de tipo inesperado! " + e);
                            } catch (IOException e) {
                                System.out.println("Impossibilidade de aceder ao conteudo da mensagem recebida! " + e);
                            }
                            versaoAnterior = serverSupport.getVersao();
                        }
                    } catch (SocketTimeoutException e) {
                        System.out.println("Timeout ao receber heartbeat, esperando o proximo...");
                    } catch (IOException e) {
                        System.out.println("Erro ao receber o heartbeat via UDP: " + e);
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
        } catch (SocketException e) {
            System.out.println("Ocorreu um erro no socket UDP:\n\t" + e);
        } catch (IOException e) {
            System.out.println("Ocorreu um erro no acesso ao multicast:\n\t" + e);
        }
        System.out.println("Servidor de backup encerrado.");
    }

    private static Connection conectBackUpDB(String localFilePath) {
        Connection backupConnection;
        try {
            String dbUrl = "jdbc:sqlite:" +localFilePath;
            backupConnection = DriverManager.getConnection(dbUrl);
            System.out.println("Conexão com a base de dados de backup estabelecida.");

        } catch (SQLException e) {
            System.out.println("Erro ao conectar com a base de dados backup: " + e.getMessage());
            return null;
        }
        return backupConnection;
    }

    private static void executarQueryNoBackup(Connection connection, String query, List<Object> parametros) {
        if (connection == null) {
            System.out.println("Erro: conexão com a base de dados não está ativa.");
            return;
        }

        // Verifica novamente os parametros recebidos
        if (query == null || parametros == null || query.isEmpty()) {
            System.out.println("Erro: Query ou parâmetros inválidos.");
            return;
        }

        // Conta se o numero de parametros batem
        int numPlaceholders = query.length() - query.replace("?", "").length();
        if (numPlaceholders != parametros.size()) {
            System.out.println("Erro: O número de placeholders não corresponde ao número de parâmetros.");
            return;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            // Executa a query
            pstmt.executeUpdate();
            System.out.println("Query executada com sucesso no backup: " + query);

        } catch (SQLException e) {
            System.out.println("Erro ao executar query no backup: " + e.getMessage());
        }
    }

}
