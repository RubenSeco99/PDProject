package Servidor;

import ServidorBackup.ServerBackUpSupport;

public class HeartBeats implements Runnable{
    //THREAD PARA EMITIR HEARTBEATS DE 10 EM 10 SEGUNDOS
    ServerBackUpSupport messageSBS;
    public HeartBeats(ServerBackUpSupport messageSBS) {
        this.messageSBS = messageSBS;
    }

    @Override
    public void run(){
        try {
            while (true) {
                Thread.sleep(10000);
                messageSBS.sendMessageToBackUpServer();
            }
        }catch (InterruptedException e) {
            System.out.println("Sleep interrompido");
        }
    }
}
