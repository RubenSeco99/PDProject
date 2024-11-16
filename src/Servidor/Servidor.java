package Servidor;

import BaseDeDados.*;
import ServidorBackup.ServerBackUpSupport;
import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    public static String EMAILSEND;
    public static String NOMEGRUPO;
    public static String EMAILREMETENTE;
    public static volatile boolean encerraServidor = false;
    private static volatile boolean isBackupInProgress = false;
    private static final Object databaseLock = new Object();

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("\nNumero de argumentos incorrecto\n");
            return;
        }

        int servicePort = Integer.parseInt(args[0]);
        final String DBPATH = args[1];
        java.sql.Connection connection = ConnectDB.criarBaseDeDados(DBPATH);

        ServerBackUpSupport messageSBS = new ServerBackUpSupport(5001);
        messageSBS.inicializeMulticast();

        List<notificaThread> clienteSockets = Collections.synchronizedList(new ArrayList<>());
        Object lock = new Object();

        try (ServerSocket serverSocket = new ServerSocket(servicePort)) {

            Thread notifica = new Thread(new NotificaCliente(clienteSockets, lock));
            notifica.setDaemon(true);
            notifica.start();

            Thread encerraServidorTh = new Thread(new EncerraServerThread(clienteSockets, lock, serverSocket));
            encerraServidorTh.setDaemon(true);
            encerraServidorTh.start();

            Thread heartbeats = new Thread(new HeartBeats(messageSBS));
            heartbeats.setDaemon(true);
            heartbeats.start();

            Thread conectaServidoresBackup = new Thread(new ConectaServidorBackUp(isBackupInProgress,databaseLock));
            conectaServidoresBackup.setDaemon(true);
            conectaServidoresBackup.start();

            System.out.println("Server iniciado...\n");

            while (true) {
                if(encerraServidor)
                    break;
                try {
                    Socket clientSocket = serverSocket.accept();
                    Thread td = new Thread(new ProcessaClienteThread(clientSocket,connection, clienteSockets,
                                                                        lock, messageSBS, isBackupInProgress, databaseLock));
                    td.setDaemon(true);
                    td.start();
                }catch (IOException e){
                    if (encerraServidor) {
                        System.out.println("Servidor encerrado com sucesso.");
                        break;
                    } else {
                        System.out.println("Ocorreu um erro no acesso ao serverSocket:\n\t" + e);
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        } catch (SocketException e) {
            System.out.println("Ocorreu um erro ao nivel do serverSocket TCP:\n\t" + e);
        } catch (IOException e) {
            System.out.println("Ocorreu um erro no acesso ao serverSocket:\n\t" + e);
        }
    }
}