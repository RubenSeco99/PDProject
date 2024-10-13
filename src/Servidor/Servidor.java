package Servidor;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;

import Cliente.Comunicacao;
import Utilizador.Utilizador;
import Uteis.Funcoes;

import static BaseDeDados.Connect.criarBaseDeDados;

class processaClienteThread implements Runnable {

    private Socket clienteSocket;
    private boolean running;
    private static ArrayList <Utilizador> listaUtilizadores;

    public processaClienteThread(Socket clienteSocket, ArrayList<Utilizador> listaUtilizadores) {
        this.clienteSocket = clienteSocket;
        this.listaUtilizadores = listaUtilizadores;
        this.running = true;
    }

    @Override
    public void run() {

        try(ObjectInputStream Oin = new ObjectInputStream(clienteSocket.getInputStream());
            ObjectOutputStream Oout = new ObjectOutputStream(clienteSocket.getOutputStream())) {

            while(running && !clienteSocket.isClosed()){
                try {

                    Comunicacao pedidoCliente = (Comunicacao) Oin.readObject();
                    Comunicacao respostaSaida = new Comunicacao();

                    System.out.println("\nPedido recebido: " + pedidoCliente.toString());
                    System.out.println("> ");

                    if(!pedidoCliente.getUtilizador().getAtivo()){
                        if(pedidoCliente.getMensagem().equalsIgnoreCase("registo")){
                            /*esta a funcionar, comentado por simplicidade.
                            if(!Funcoes.isValidEmail(comunicacaoRecebida.getUtilizador().getEmail())){
                                comunicacaoSaida = comunicacaoRecebida;
                                comunicacaoSaida.setMensagem("Formato Email Inválido!");
                                ObjectOutputStream Oout = new ObjectOutputStream(clientSocket.getOutputStream());
                                Oout.writeObject(comunicacaoSaida);
                                Oout.flush();
                            }
                            */
                            if(Funcoes.verificaRegisto(listaUtilizadores, pedidoCliente.getUtilizador().getEmail())){
                                listaUtilizadores.add(pedidoCliente.getUtilizador());
                                //TESTE DE CONECTIVIDADE
                                respostaSaida.setUtilizador(pedidoCliente.getUtilizador());
                                respostaSaida.setMensagem("Aceite");
                            }else{
                                //Já existe um email igual, enviar erro
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Email existente");

                            }
                        } else if(pedidoCliente.getMensagem().equalsIgnoreCase("login")){
                            if(Funcoes.verificaLogin(listaUtilizadores, pedidoCliente.getUtilizador().getEmail(), pedidoCliente.getUtilizador().getPassword())) {
                                pedidoCliente.getUtilizador().setAtivo(true);
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Login aceite");
                            } else {
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Credencias incorretas");
                            }
                        } else {
                            respostaSaida = pedidoCliente;
                            respostaSaida.setMensagem("Comando invalido. Efetue o 'registo' ou 'login' primeiro.");
                        }
                    } else {
                        if(pedidoCliente.getMensagem().equalsIgnoreCase("logout")){
                            for(var u: listaUtilizadores){
                                if(u.getEmail().equalsIgnoreCase(pedidoCliente.getUtilizador().getEmail())){
                                    u.setAtivo(false);
                                    respostaSaida = pedidoCliente;
                                    break;
                                }
                            }
                        }
                    }

                    Oout.writeObject(respostaSaida);
                    Oout.flush();

                    for(var u: listaUtilizadores){
                        System.out.println("Users registados : " + u.toString());
                    }

                } catch (SocketException e) {
                    System.out.println("Cliente desconectado: " + clienteSocket.getRemoteSocketAddress());
                    running = false; // Encerrar a thread se o cliente se desconectar
                } catch (EOFException e) {
                    System.out.println("Fim da conexão com o cliente: " + clienteSocket.getRemoteSocketAddress());
                    running = false; // Encerrar a thread no fim da conexão
                } catch (ClassNotFoundException | IOException e) {
                    System.out.println("Erro na comunicação com o cliente:\n\t" + e);
                    running = false; // Encerrar a thread em caso de erro
                }
            }
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
        } finally {
            try{
                 if(clienteSocket != null && !clienteSocket.isClosed()) {
                     clienteSocket.close();
                 }
            } catch (IOException e) {
                System.out.println("Erro ao fechar o socket do cliente:\n\t" + e);
            }
        }
    }
}

public class Servidor {

    public static void main(String[] args) throws IOException {

        criarBaseDeDados(); //Cria base de dados

        Comunicacao comunicacaoRecebida = new Comunicacao(); //Receber
        Comunicacao comunicacaoSaida = new Comunicacao(); //Enviar
        ArrayList<Thread> arrayThreads = new ArrayList<>();
        ArrayList<Utilizador> listaUtilizadores = new ArrayList<>();

        int servicePort = Integer.parseInt(args[0]);
        String caminhoBD = args[1];

        try (ServerSocket serverSocket = new ServerSocket(5000)) {

            System.out.println("Server iniciado...\n");
            //Lançar thread para enviar base de dados atualizada ao servidor de backup atualizando a versão
            while (true) {

                Socket clientSocket = serverSocket.accept();

                Thread td = new Thread(new processaClienteThread(clientSocket, listaUtilizadores));
                td.start();
                arrayThreads.add(td);

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