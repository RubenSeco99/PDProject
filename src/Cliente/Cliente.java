package Cliente;

import Entidades.Utilizador;
import Uteis.Funcoes;

import java.io.*;
import java.net.*;

class processServerRequest implements Runnable{
    private final Socket socket;
    private boolean running;


    public processServerRequest(Socket socket) {
        this.socket = socket;
        running = true;
    }

    public void terminate(){
        running = false;
        try {
            socket.shutdownInput();  // Fecha a entrada do socket para desbloquear o `readObject()`
        } catch (IOException e) {
            System.out.println("Erro ao fechar a entrada do socket: " + e.getMessage());
        }
    }

    @Override
    public void run() {

        try(ObjectInputStream Oin = new ObjectInputStream(socket.getInputStream())){

            while(running){

                Comunicacao response = (Comunicacao) Oin.readObject();

                if(response.getMensagem().equalsIgnoreCase("Login aceite")){
                    Cliente.registado = true;
                        Cliente.utilizadorUpdate.setUtilizador(response.getUtilizador());
                }

                System.out.println("\nResponse: " + response);
                System.out.println("> ");
            }

        } catch (EOFException e) {
                System.out.println("Conexão terminada pelo servidor.");//descobrir porque está a ser lançada???
        }
        catch (UnknownHostException e) {
        System.out.println("Destino desconhecido:\n\t" + e);
        } catch (NumberFormatException e) {
            System.out.println("O porto do servidor deve ser um inteiro positivo.");
        } catch (SocketTimeoutException e) {
            System.out.println("Nao foi recebida qualquer resposta:\n\t" + e);
        } catch (SocketException e) {
            System.out.println("Ocorreu um erro ao nivel do socket TCP:\n\t" + e);
        } catch (IOException e) {
            System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
        } catch(ClassNotFoundException e){
            System.out.println("O objecto recebido não é do tipo esperado:\n\t"+e);
        }

    }
}

public class Cliente {
    public static boolean registado = false;
    public static boolean valido = true;
    public static boolean EXIT = false;
    public static Utilizador utilizadorUpdate = new Utilizador();
    public static Comunicacao comunicacao;

    public static void main(String[] args) throws InterruptedException {

       if (args.length != 2) {
            System.out.println("\nNumero de argumentos incorrecto\n");
            return;
       }

        InetAddress serverAddr;
        int serverPort;

        processServerRequest serverRequest;
        Thread td1;

        try {
            serverAddr = InetAddress.getByName(args[0]);
            serverPort = Integer.parseInt(args[1]);

            try (Socket socket = new Socket(serverAddr, serverPort)) {

                ObjectOutputStream Oout = new ObjectOutputStream(socket.getOutputStream());
                serverRequest = new processServerRequest(socket);
                td1 = new Thread(serverRequest);
                td1.start();

                while (true) {
                    comunicacao = new Comunicacao(utilizadorUpdate);
                    Funcoes.Menu(utilizadorUpdate, comunicacao);
                    if (valido) {
                        System.out.println(comunicacao);//so imprime se tiver havido pedido ao server
                        Oout.writeObject(comunicacao);
                        Oout.flush();
                        Oout.reset();
                    }
                    if(EXIT) {
                        serverRequest.terminate();
                        td1.join();
                        socket.close();
                        break;
                    }
                }
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
    }
}