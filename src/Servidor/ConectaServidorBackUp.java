package Servidor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

 public class ConectaServidorBackUp implements Runnable {
    // THREAD QUE ESPERA OS SERVIDORES BACKUP SE CONECTAREM DIRETAMENTE VIA TCP PARA RECEBEREM AS ATUALIZAÇÕES DA BASE DE DADOS
    private static final int PORTOBACKUPTCP = 5001;
    private final File databaseFile = new File("./src/BaseDeDados/BaseDados.db");
    private static final int MAX_SIZE = 4000;
    private boolean isBackupInProgress;
    private final Object databaseLock;

    public ConectaServidorBackUp(boolean isBackupInProgress, Object databaseLock){
        this.isBackupInProgress = isBackupInProgress;
        this.databaseLock = databaseLock;
    }

    @Override
    public void run() {
        byte[] fileChunk = new byte[MAX_SIZE];
        int nbytes;

        // Validar o ficheiro da base de dados
        if (!databaseFile.exists()) {
            System.out.println("O ficheiro " + databaseFile.getAbsolutePath() + " não existe!");
            return;
        }

        if (!databaseFile.canRead()) {
            System.out.println("Sem permissões de leitura no ficheiro " + databaseFile.getAbsolutePath() + "!");
            return;
        }

        try (ServerSocket socketBackup = new ServerSocket(PORTOBACKUPTCP)) {
            System.out.println("Servidor backup à escuta no porto " + PORTOBACKUPTCP + "...");

            while (true) {
                try {
                    Socket backupServerSocket = socketBackup.accept();
                    System.out.println("Conexão estabelecida com o servidor backup: " + backupServerSocket.getInetAddress().getHostAddress());
                    isBackupInProgress = true;
                    //VAI LER O FICHEIRO DA BASE DE DADOS E ENVIA POR CHUNKS
                    try (FileInputStream fileInputStream = new FileInputStream(databaseFile)) {
                        OutputStream out = backupServerSocket.getOutputStream();
                        do {
                            nbytes = fileInputStream.read(fileChunk);
                            if (nbytes != -1) {
                                out.write(fileChunk, 0, nbytes);
                                out.flush();
                            }
                        } while (nbytes > 0);
                        out.close();
                        System.out.println("Transferência concluída para o servidor backup.");

                        // Notifica as threads que atendem clientes que podem continuar
                        isBackupInProgress = false;

                        synchronized (databaseLock){
                            databaseLock.notify();
                        }

                    } catch (IOException e) {
                        System.out.println("Erro ao ler ou enviar o ficheiro: " + e.getMessage());
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Tempo esgotado: o servidor de backup não respondeu.");
                } catch (IOException e) {
                    System.out.println("Erro de I/O ao lidar com o servidor de backup: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao criar o ServerSocket: " + e.getMessage());
        }
    }
}
