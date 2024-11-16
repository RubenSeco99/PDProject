package Servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.List;

public class EncerraServerThread implements Runnable{

    List<notificaThread> arraySocketsClientes;
    final Object lock;
    ServerSocket serverS;
    public EncerraServerThread(List<notificaThread> arraySocketsClientes, Object lock, ServerSocket serverS){
        this.arraySocketsClientes = arraySocketsClientes;
        this.lock = lock;
        this.serverS = serverS;
    }

    @Override
    public void run(){
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String exit;
        try {
            while(true){
                exit = in.readLine();
                if(exit.equalsIgnoreCase("exit") || exit.equalsIgnoreCase("sair")){
                    Servidor.encerraServidor = true;
                    System.out.println("Entrei na condicao de saida");
                    synchronized (lock){
                        lock.notify();
                    }
                    Thread.sleep(100);
                    serverS.close();
                    for(var s:arraySocketsClientes){
                        s.getSocket().close();
                    }
                    break;
                }else{
                    System.out.println("Comando invalido. Escreva (exit) ou (sair) para encerrar o servidor");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }
}