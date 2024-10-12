package Cliente;

import Utilizador.Utilizador;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class Cliente {

    public static final int TIMEOUT = 3000;

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
            serverAddr = InetAddress.getLocalHost();  //Passar por parametro
            serverPort = 5000; //Integer.parseInt(args[0]);

            System.out.println("Coloque o seu email:");
            utilizador.setEmail(in.readLine());
            System.out.println("Coloque a password:");
            utilizador.setPassword(in.readLine());

            Comunicacao comunicacao = new Comunicacao(utilizador);  //Objeto para comunicação

            try(Socket socket = new Socket(serverAddr, serverPort)) {
                //3 segundos a espera de fazer comunicacao
                socket.setSoTimeout(TIMEOUT);

                while(true) {
                    System.out.println("""
                            O que pretende fazer?
                            1. Registo
                            2. Login""");

                    op = Integer.parseInt(in.readLine());

                    if(op == 1) {
                        comunicacao.setMensaguem("Registo");
                        break;
                    }else if(op == 2) {
                        comunicacao.setMensaguem("Login");
                        break;
                    }else{
                        System.out.println("Opcao invalida!");
                    }
                }

                while(true) {



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