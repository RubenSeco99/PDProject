package Cliente;

import Utilizador.Utilizador;

import java.io.*;
import java.net.*;
import java.sql.Time;

class processServerRequest implements Runnable{

    private Socket socket;
    private Comunicacao response;
    private boolean running;

    public processServerRequest(Socket socket){
        this.socket = socket;
        running = true;
    }

    public void terminate(){running = false;}

    @Override
    public void run() {

        try {
            while(running){

                try(ObjectInputStream Oin = new ObjectInputStream(socket.getInputStream())){

                    response = (Comunicacao) Oin.readObject();

                    System.out.println("\nResponse: " + response.toString());
                    System.out.println("> ");

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
        int op = 0;
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

            Comunicacao comunicacao = new Comunicacao(utilizador);  //Objeto para comunicação

            try(Socket socket = new Socket(serverAddr, serverPort)) {
                //3 segundos a espera de fazer comunicacao
                socket.setSoTimeout(TIMEOUT);

                while(true) {
                    System.out.println("""
                            O que pretende fazer?
                            1. Registo
                            2. Login""");
                    System.out.println("> ");

                    op = Integer.parseInt(in.readLine());

                    if(op == 1) {
                        comunicacao.setMensagem("Registo");
                        break;
                    }else if(op == 2) {
                        comunicacao.setMensagem("Login");
                        break;
                    }else{
                        System.out.println("Opcao invalida!");
                    }
                }

                ObjectOutputStream Oout = new ObjectOutputStream(socket.getOutputStream());
                Oout.writeObject(comunicacao);
                Oout.flush();

                Thread td1 = new Thread(new processServerRequest(socket));
                td1.start();
                String pedido;

                while(true) {
                    System.out.print("> ");
                    pedido = in.readLine();

                    if (pedido.equalsIgnoreCase(EXIT)) {
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