package Uteis;
import Cliente.Cliente;
import Cliente.Comunicacao;
import Entidades.Utilizador;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Funcoes {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    // Precompiled Pattern for efficiency
    private static final Pattern pattern = Pattern.compile(EMAIL_REGEX);

    public static boolean verificaRegisto (ArrayList<Utilizador> arrayList, String email){
        for(var u: arrayList){
            if(u.getEmail().equals(email)){
                return false;
            }
        }
        return true;
    }
    public static boolean isValidEmail(String email) {
        if (email == null)
            return false;
        return pattern.matcher(email).matches();
    }
    public static boolean verificaLogin (ArrayList<Utilizador> arrayList, String email, String password){
        for(var u: arrayList){
            if(u.getEmail().equals(email) && u.getPassword().equals(password)){
                return true;
            }
        }
        return false;
    }
    public static void camposMenuRegisto (Utilizador utilizador) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Coloque o seu nome: ");
        utilizador.setNome(in.readLine());
        System.out.println();
        System.out.print("Coloque o seu email: ");
        utilizador.setEmail(in.readLine());
        System.out.println();
        System.out.print("Coloque a password: ");
        utilizador.setPassword(in.readLine());
        System.out.println();
    }
    public static void camposMenuLogin (Utilizador utilizador) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Coloque o seu email: ");
        utilizador.setEmail(in.readLine());
        System.out.println();
        System.out.print("Coloque a password: ");
        utilizador.setPassword(in.readLine());
        System.out.println();
    }
    public static void updateUtilizador(Utilizador utilizador) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean running = true;
        while (running) {
            try{
                System.out.println("Escolha o que deseja alterar:");
                System.out.println("1 - Nome");
                System.out.println("2 - Password");
                System.out.println("3 - Nome e Password");
                System.out.println("0 - Sair");
                System.out.print("Opção: ");
                String opcao = in.readLine();
                System.out.println();

                switch (opcao) {
                    case "1":
                        System.out.print("Coloque o seu novo nome: ");
                        utilizador.setNome(in.readLine());
                        System.out.println("Nome alterado com sucesso!");
                        break;
                    case "2":
                        System.out.print("Coloque a sua nova password: ");
                        utilizador.setPassword(in.readLine());
                        System.out.println("Password alterada com sucesso!");
                        break;
                    case "3":
                        System.out.print("Coloque o seu novo nome: ");
                        utilizador.setNome(in.readLine());
                        System.out.print("Coloque a sua nova password: ");
                        utilizador.setPassword(in.readLine());
                        System.out.println("Nome e password alterados com sucesso!");
                        break;
                    case "0":
                        // Sair da atualização
                        System.out.println("Nenhuma alteração foi feita.");
                        running = false;
                        break;
                    default:
                        // Caso o utilizador insira uma opção inválida
                        System.out.println("Opção inválida, tente novamente.");
                }
                System.out.println();
            } catch (IOException e) {
                System.out.println("Ocorreu um erro ao ler a entrada. Tente novamente.");
            } catch (NumberFormatException e) {
                System.out.println("Opção inválida. Insira um número válido.");
            }
        }
    }
    public static void Menu(Utilizador utilizador, Comunicacao comunicacao) throws IOException, InterruptedException {

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int op = 0;
        Thread.sleep(150);

        if(!Cliente.registado){
            System.out.println("""
                                        O que pretende fazer?
                                          1. Registo
                                          2. Login""");
            System.out.print("> ");
            try {
                op = Integer.parseInt(in.readLine());

                if (op == 1) {
                    Funcoes.camposMenuRegisto(utilizador);
                    comunicacao.setMensagem("Registo");
                    Cliente.valido = true;
                } else if (op == 2) {
                    Funcoes.camposMenuLogin(utilizador);
                    comunicacao.setMensagem("Login");
                    Cliente.valido = true;
                } else {
                    System.out.println("Opcao invalida");
                    Cliente.valido = false;
                }
            } catch (NumberFormatException e) {
                System.out.println("Formato invalido " + e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            System.out.println("""
                                O que pretende fazer?
                                  1. Logout
                                  2. Editar dados""");
            System.out.print("> ");

            try {
                op = Integer.parseInt(in.readLine());
                if(op == 1) {
                    comunicacao.setMensagem("Logout");
                    Cliente.valido = true;
                    System.out.println("Logout efetuado com sucesso!");
                    Cliente.EXIT = true;
                }else if(op == 2) {

                    System.out.println("""
                                Que dados pretende editar?
                                  1. Nome
                                  2. Password
                                  3. Telefone""");
                    System.out.print("> ");
                    Cliente.valido = true;
                    op = Integer.parseInt(in.readLine());
                    if(op == 1) {
                        comunicacao.setMensagem("Editar dados nome");
                        System.out.print("Novo nome: ");
                        utilizador.setNome(in.readLine());
                    }else if(op == 2) {
                        comunicacao.setMensagem("Editar dados password");
                        System.out.print("Nova password: ");
                        utilizador.setPassword(in.readLine());
                    }else if(op == 3) {
                        comunicacao.setMensagem("Editar dados telefone");
                        System.out.print("Novo numero de telefone: ");
                        utilizador.setTelefone(Integer.parseInt(in.readLine()));
                    }else
                        Cliente.valido = false;
                }else{
                    System.out.println("Opcao invalida!");
                    Cliente.valido = false;
                }
            } catch (NumberFormatException e) {
                System.out.println("Opcao invalida! Insira uma opção válida.");
            }
        }
    }

}
