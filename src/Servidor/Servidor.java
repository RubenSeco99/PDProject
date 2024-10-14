package Servidor;

import BaseDeDados.ConnectDB;
import BaseDeDados.UtilizadorDB;
import Cliente.Comunicacao;
import Entidades.Utilizador;
import Uteis.Funcoes;
import com.sun.jdi.connect.spi.Connection;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

class processaClienteThread implements Runnable {

    private Socket clienteSocket;
    private boolean running;
    private java.sql.Connection connection;
    private UtilizadorDB utilizadorDB;

    public processaClienteThread(Socket clienteSocket, java.sql.Connection connection) {
        this.clienteSocket = clienteSocket;
        this.connection =  connection;
        this.running = true;
        this.utilizadorDB = new UtilizadorDB(connection);
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

                    if(pedidoCliente.getUtilizador().getAtivo()==0){
                        if(pedidoCliente.getMensagem().equalsIgnoreCase("Registo")){
                            if(!Funcoes.isValidEmail(pedidoCliente.getUtilizador().getEmail())){
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Formato Email Inválido!");
                                Oout.writeObject(respostaSaida);
                                Oout.flush();
                            } else
                            if(!utilizadorDB.verificaRegisto(pedidoCliente.getUtilizador().getEmail())){
                                utilizadorDB.insertUtilizador(pedidoCliente.getUtilizador());
                                respostaSaida.setUtilizador(pedidoCliente.getUtilizador());
                                respostaSaida.setMensagem("Aceite");
                            }else{
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Email existente");
                            }
                        } else if(pedidoCliente.getMensagem().equalsIgnoreCase("login")){
                            if(utilizadorDB.verificaLogin(pedidoCliente.getUtilizador().getEmail(), pedidoCliente.getUtilizador().getPassword())) {
                                pedidoCliente.getUtilizador().setAtivo(1);
                                utilizadorDB.updateUtilizador(pedidoCliente.getUtilizador());
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
                            Utilizador utilizador = utilizadorDB.selectUtilizador(pedidoCliente.getUtilizador().getEmail());
                            if (utilizador != null) {
                                utilizador.setAtivo(0);
                                utilizadorDB.updateUtilizador(utilizador);
                                respostaSaida.setMensagem("Logout aceite");
                            } else {
                                respostaSaida.setMensagem("Utilizador não encontrado.");
                            }
                        }
                    }

                    Oout.writeObject(respostaSaida);
                    Oout.flush();
                    List<Utilizador> listaUtilizadores = utilizadorDB.selectTodosUtilizadores();

                    // Itera sobre a lista de utilizadores e imprime seus dados
                    for (Utilizador utilizador : listaUtilizadores) {
                        System.out.println("Nome: " + utilizador.getNome());
                        System.out.println("Email: " + utilizador.getEmail());
                        System.out.println("Telefone: " + utilizador.getTelefone());
                        System.out.println("Ativo: " + utilizador.getAtivo());
                        System.out.println("---------------");
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

        java.sql.Connection connection = ConnectDB.criarBaseDeDados();
        Comunicacao comunicacaoRecebida = new Comunicacao(); //Receber
        Comunicacao comunicacaoSaida = new Comunicacao(); //Enviar
        ArrayList<Thread> arrayThreads = new ArrayList<>();

        int servicePort = Integer.parseInt(args[0]);
        //String caminhoBD = args[1]; //para uso futuro

        try (ServerSocket serverSocket = new ServerSocket(5000)) {

            System.out.println("Server iniciado...\n");
            //Lançar thread para enviar base de dados atualizada ao servidor de backup atualizando a versão
            while (true) {

                Socket clientSocket = serverSocket.accept();

                Thread td = new Thread(new processaClienteThread(clientSocket, (java.sql.Connection) connection));
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
        finally {
            ConnectDB.criarBaseDeDados();
        }
    }
}