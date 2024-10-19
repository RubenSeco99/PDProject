package Cliente;

import Entidades.Despesas;
import Entidades.Utilizador;
import Uteis.Funcoes;

import java.io.*;
import java.net.*;

class processServerRequest implements Runnable{
    private final Socket socket;
    private boolean running;
    public Comunicacao response;

    public processServerRequest(Socket socket,Comunicacao response) {
        this.socket = socket;
        this.response=response;
        running = true;
    }

    public void terminate(){
        running = false;
    }

    @Override
    public void run() {

        try(ObjectInputStream Oin = new ObjectInputStream(socket.getInputStream())){

            while(running){
                response = (Comunicacao) Oin.readObject();
                if(response.getMensagem().equalsIgnoreCase("Login aceite")) {
                    Cliente.registado = true;
                    Cliente.utilizadorUpdate.setUtilizador(response.getUtilizador());
                } else if(response.getMensagem().equalsIgnoreCase("Lista de convites")) {
                    Cliente.utilizadorUpdate.setConvites(response.getConvites());
                    System.out.println("Convites pendentes:");
                    if(!Cliente.utilizadorUpdate.getConvites().isEmpty()) {
                        for (var convite : Cliente.utilizadorUpdate.getConvites()) {
                            System.out.println("Convite para o grupo: " + convite.getNomeGrupo() + " (de: " + convite.getRemetente() + ")");
                        }
                    } else {
                        System.out.println("Não tem convites pendentes.");
                    }
                } else if(response.getMensagem().equalsIgnoreCase("Utilizador não pertence ao grupo")) {
                    Cliente.utilizadorUpdate.setUtilizador(response.getUtilizador());
                }else if(response.getMensagem().equalsIgnoreCase("Ver grupos bem sucedido")){
                    System.out.println("Lista de Grupos pertencentes: ");
                    for(var g:response.getUtilizador().getGrupos())
                        System.out.println("Nome: "+g.getNome());
                }else if(response.getMensagem().equalsIgnoreCase("Historio de despesas")){
                    for(var d: response.getDespesa()){
                        System.out.println(d.toString());
                    }
                }

                if(response.getMensagem().equalsIgnoreCase("Servidor em baixo")){
                    System.out.println("\nServidor em baixo, clique em alguma tecla para sair\n");
                    Cliente.EXIT = true;
                    Cliente.valido = false;
                }else{
                    System.out.println("\nResponse: " + response);
                    System.out.println("> ");
                }
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
    public static Comunicacao response;
    public  static String lastCommand="";

    public static void main(String[] args) throws InterruptedException {

       if (args.length != 2) {
            System.out.println("\nNumero de argumentos incorrecto\n");
            return;
       }

        InetAddress serverAddr;
        int serverPort;

        Despesas despesas;

        processServerRequest serverRequest;
        Thread td1;

        try {
            serverAddr = InetAddress.getByName(args[0]);
            serverPort = Integer.parseInt(args[1]);

            try (Socket socket = new Socket(serverAddr, serverPort)) {

                ObjectOutputStream Oout = new ObjectOutputStream(socket.getOutputStream());
                response=new Comunicacao();
                serverRequest = new processServerRequest(socket,response);
                td1 = new Thread(serverRequest);
                td1.start();

                while (true) {
                    comunicacao = new Comunicacao(utilizadorUpdate);
                    if(lastCommand.equalsIgnoreCase("Aceitar convite"))
                        System.out.println(utilizadorUpdate.getConvites());
                    Funcoes.Menu(utilizadorUpdate);
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