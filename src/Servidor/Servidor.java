package Servidor;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import Cliente.Comunicacao;
import Utilizador.Utilizador;
import Uteis.Funcoes;

import static BaseDeDados.Connect.criarBaseDeDados;

class processaClienteThread implements Runnable {

    Socket clienteSocket;

    public processaClienteThread(Socket clienteSocket) {
        this.clienteSocket = clienteSocket;
    }

    @Override
    public void run() {
        //logica para atender o cliente
    }
}

public class Servidor {

    public static void main(String[] args) throws IOException {

        criarBaseDeDados(); //Cria base de dados

        Comunicacao comunicacaoRecebida = new Comunicacao(); //Receber
        Comunicacao comunicacaoSaida = new Comunicacao(); //Enviar
        ArrayList<Utilizador> listaUtilizadores = new ArrayList<>();
        int servicePort = Integer.parseInt(args[0]);
        String caminhoBD = args[1];

        try (ServerSocket serverSocket = new ServerSocket(servicePort)) {

            System.out.println("Server iniciado...\n");

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    //[PT] Deserializar o objecto
                    InputStream in = clientSocket.getInputStream();  // Obtém o InputStream a partir do socket
                    ObjectInputStream objStream = new ObjectInputStream(in);
                    comunicacaoRecebida = (Comunicacao) objStream.readObject();

                    System.out.println("Recebido \"" + comunicacaoRecebida.toString() + "\" de "
                            + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

                    if(comunicacaoRecebida.getMensaguem().equalsIgnoreCase("registo")){
                        if(Funcoes.verificaRegisto(listaUtilizadores, comunicacaoRecebida.getUtilizador().getEmail())){
                            listaUtilizadores.add(comunicacaoRecebida.getUtilizador());
                        }else{
                            comunicacaoSaida.setMensaguem("Email existente");
                            //Já existe um email igual, enviar erro
                        }
                    }else if(comunicacaoRecebida.getMensaguem().equalsIgnoreCase("login")){

                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        } catch (SocketException e) {
            System.out.println("Ocorreu um erro ao nivel do serverSocket TCP:\n\t" + e);
        } catch (IOException e) {
            System.out.println("Ocorreu um erro no acesso ao serverSocket:\n\t" + e);
        } catch(ClassNotFoundException e){
            System.out.println("O objecto recebido não é do tipo esperado:\n\t"+e);
        }
    }
}