package Uteis;
import Cliente.Cliente;
import Cliente.Comunicacao;
import Entidades.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class Funcoes {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    // Precompiled Pattern for efficiency
    private static final Pattern pattern = Pattern.compile(EMAIL_REGEX);
    public static boolean isValidEmail(String email) {
        if (email == null)
            return false;
        return pattern.matcher(email).matches();
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
    public static void updateUtilizador(Utilizador utilizador) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean running=true,change=false;//change -> se houver mudancas atualiza o valido
        while (running) {
            try{
                System.out.println("""
                                Que dados pretende editar?
                                  1. Nome
                                  2. Password
                                  3. Telefone
                                  0. Sair""");
                System.out.print("Opção: ");
                String opcao = in.readLine();
                System.out.println();

                switch (opcao) {
                    case "1":
                        System.out.print("Coloque o seu novo nome: ");
                        utilizador.setNome(in.readLine());
                        System.out.println("Nome alterado com sucesso!");
                        Cliente.valido = true;
                        change=true;
                        break;
                    case "2":
                        System.out.print("Coloque a sua nova password: ");
                        utilizador.setPassword(in.readLine());
                        System.out.println("Password alterada com sucesso!");
                        Cliente.valido = true;
                        change=true;
                        break;
                    case "3":
                        System.out.print("Coloque o seu novo telefone: ");
                        utilizador.setTelefone(Integer.parseInt(in.readLine()));
                        System.out.println("Telefone alterado com sucesso!");
                        Cliente.valido = true;
                        change=true;
                        break;
                    case "0":
                        running = false;
                        if(change) {
                            Cliente.valido = true;
                            Cliente.comunicacao.setMensagem("Editar dados");
                        }
                        else{
                            Cliente.valido=false;
                            System.out.println("Nenhuma alteração foi efetuada");}
                        break;
                    default:
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
    public static void menuUtilizadoresComLogin(Utilizador utilizador,Comunicacao comunicacao){
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int op ;
        System.out.println("""
                                O que pretende fazer?
                                  1. Logout
                                  2. Editar dados
                                  3. Adicionar grupo""");
        System.out.print("> ");
        try {
            op = Integer.parseInt(in.readLine());
            if(op == 1) {
                Cliente.comunicacao.setMensagem("Logout");
                Cliente.valido = true;
                System.out.println("Logout efetuado com sucesso!");
                Cliente.EXIT = true;
            }else if(op == 2) {
                updateUtilizador(utilizador);
            }else if(op == 3) {
                Cliente.comunicacao.setMensagem("Criar grupo");
                Cliente.valido = true;
                System.out.println("Indique o nome do grupo");
                Cliente.comunicacao.getGrupo().setNome(in.readLine());
            }else{
                System.out.println("Opcao invalida!");
                Cliente.valido = false;
            }
        } catch (NumberFormatException | IOException e) {
            System.out.println("Opcao invalida! Insira uma opção válida.");
        }
    }
    public static void menuUtilizadoresSemLogin(Utilizador utilizador,Comunicacao comunicacao){
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int op ;
        System.out.println("""
                                        O que pretende fazer?
                                          0. Sair
                                          1. Registo
                                          2. Login""");
        System.out.print("> ");
        try {
            op = Integer.parseInt(in.readLine());

            if (op == 1) {
                Funcoes.camposMenuRegisto(utilizador);
                Cliente.comunicacao.setMensagem("Registo");
                Cliente.valido = true;
            } else if (op == 2) {
                Funcoes.camposMenuLogin(utilizador);
                Cliente.comunicacao.setMensagem("Login");
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
    }
    public static void Menu(Utilizador utilizador, Comunicacao comunicacao) throws IOException, InterruptedException {
        Thread.sleep(150);
        if(!Cliente.registado){
            menuUtilizadoresSemLogin(utilizador,comunicacao);

        } else {
            menuUtilizadoresComLogin(utilizador,comunicacao);
        }//se for necessario criar uma variavel para ver se um utilizador esta num grupo e dividir as opcoes também se pode fazer
    }

}
