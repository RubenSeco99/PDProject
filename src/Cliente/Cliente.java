package Cliente;

import Entidades.Utilizador;

import java.io.*;
import java.net.*;

class processServerRequest implements Runnable{

    private Socket socket;
    private Comunicacao response;
    private boolean running;

    public processServerRequest(Socket socket){
        this.socket = socket;
        running = true;
    }

    public void terminate(){
        running = false;
        try {
            socket.shutdownInput();  // Fecha a entrada do socket para desbloquear o `readObject()`
        } catch (IOException e) {
            System.out.println("Erro ao fechar a entrada do socket: " + e.getMessage());
        }}

    @Override
    public void run() {


        try(ObjectInputStream Oin = new ObjectInputStream(socket.getInputStream())){

            while(running){
                response = (Comunicacao) Oin.readObject();
                System.out.println("\nResponse: " + response.toString());
                System.out.println("> ");
            }
        } catch (EOFException e) {
                System.out.println("Conexão terminada pelo servidor.");
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

    public static final int TIMEOUT = 5000;
    public static String EXIT = "EXIT";

    public static void main(String[] args) {

//        if (args.length != 2) {
//            System.out.println("\nNumero de argumentos incorrecto\n");
//            return;
//        }

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        InetAddress serverAddr;
        int serverPort;
        int op;
        Utilizador utilizador = new Utilizador();

        try {
            serverAddr = InetAddress.getByName(args[0]);  //Passar por parametro
            serverPort = Integer.parseInt(args[1]);

            System.out.print("Coloque o seu nome: ");
            utilizador.setNome(in.readLine());
            System.out.println();
            System.out.print("Coloque o seu email: ");
            utilizador.setEmail(in.readLine());
            System.out.println();
            System.out.print("Coloque a password: ");
            utilizador.setPassword(in.readLine());
            System.out.println();

            //Objeto para comunicação
            Comunicacao response = new Comunicacao();

            try(Socket socket = new Socket(serverAddr, serverPort)) {
                ObjectOutputStream Oout = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream Oin = new ObjectInputStream(socket.getInputStream());
                boolean registado=false;
                while(true) {
                    if (!registado) {
                        System.out.println("""
                        O que pretende fazer?
                          1. Registo
                          2. Login""");
                    } else {
                        System.out.println("""
                        O que pretende fazer?
                          2. Login""");
                    }
                    System.out.println("> ");

                    Comunicacao comunicacao = new Comunicacao(utilizador);
                    try {
                        op = Integer.parseInt(in.readLine());
                        if(op == 1 && !registado) {
                            comunicacao.setMensagem("Registo");

                        }else if(op == 1 && registado) {
                            System.out.println("Já se encontra registado!");
                            continue;
                        }else if(op == 2) {
                            comunicacao.setMensagem("Login");
                        }else{
                            System.out.println("Opcao invalida!");
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Opcao invalida! Insira uma opção válida.");
                        continue;  // Volta ao início do ciclo se a entrada não for válida
                    }

                    System.out.println(comunicacao.getMensagem());
                    Oout.writeObject(comunicacao);
                    Oout.flush();

                    response = (Comunicacao) Oin.readObject();
                    System.out.println("\nResponse: " + response.toString());

                    //falta o formato email invalido
                    if (op == 1 && response.getMensagem().equalsIgnoreCase("Aceite")) {
                        registado = true;
                        System.out.println("Registo efetuado com sucesso!");
                    }else if (op == 1 && response.getMensagem().equalsIgnoreCase("Email existente")) {
                        System.out.println("Email já existente!");
                    }else if (op == 2 && response.getMensagem().equalsIgnoreCase("Credencias incorretas")) {
                        System.out.println("Credenciais incorretas!");
                    }else if (op == 2 && response.getMensagem().equalsIgnoreCase("Login aceite")) {
                        break;
                    }
                }

                processServerRequest serverRequest = new processServerRequest(socket);//conversão para garantir saída no logout
                Thread td1 = new Thread(serverRequest);
                td1.start();

                while(true) {
                    System.out.println("""
                        O que pretende fazer?
                          1. Logout""");
                    System.out.print("> ");
                    Comunicacao comunicacao = new Comunicacao(utilizador);
                    try {
                        op = Integer.parseInt(in.readLine());
                        if(op == 1) {
                            comunicacao.setMensagem("Logout");

                        }else{
                            System.out.println("Opcao invalida!");
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Opcao invalida! Insira uma opção válida.");
                        continue;  // Volta ao início do ciclo se a entrada não for válida
                    }
                    System.out.println(comunicacao.getMensagem());
                    Oout.writeObject(comunicacao);
                    Oout.flush();
                    if(op!=1){
                    response = (Comunicacao) Oin.readObject();
                    System.out.println("\nResponse: " + response.toString());
                    }


                    if (op==1) {
                        System.out.println("Logout efetuado com sucesso!");
                        serverRequest.terminate();
                        td1.join();
                        socket.close();  // Agora pode fechar o socket
                        break;  // Sai do loop
                    }

                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {//erro no join
                throw new RuntimeException(e);
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