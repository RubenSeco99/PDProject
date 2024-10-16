package Uteis;
import Cliente.Cliente;
import Cliente.Comunicacao;
import Entidades.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
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
                        change=true;
                        break;
                    case "2":
                        System.out.print("Coloque a sua nova password: ");
                        utilizador.setPassword(in.readLine());
                        System.out.println("Password alterada com sucesso!");
                        change=true;
                        break;
                    case "3":
                        System.out.print("Coloque o seu novo telefone: ");
                        utilizador.setTelefone(Integer.parseInt(in.readLine()));
                        System.out.println("Telefone alterado com sucesso!");
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
    public static void menuConvites(Utilizador utilizador) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean running=true,change=false;//change -> se houver mudancas atualiza o valido
        while (running) {
            try{
                System.out.println("""
                                [Menu convites]
                                O que pretende fazer?
                                  1. Ver convites
                                  2. Aceitar
                                  3. Rejeitar
                                  4. Fazer convite
                                  0. Sair""");
                System.out.print("Opção: ");
                String opcao = in.readLine();
                System.out.println();

                switch (opcao) {
                    case "1":
                        Cliente.comunicacao.setMensagem("Ver convites");
                        Cliente.valido=true;
                        running = false;
                        break;
                    case "2":

                        break;
                    case "3":

                        break;
                    case "4":
                        Cliente.comunicacao.setMensagem("Enviar convite");
                        Cliente.valido=true;
                        System.out.print("Indique o grupo: ");
                        //fazer validação do grupo (se existe e se está nele)
                        System.out.print("Indique o email do utilizador que quer convidar: ");
                        //criar um novo convite com esses dados
                        change=true;
                        break;
                    case "0":
                        running = false;
                        if(change) {
                            Cliente.valido = true;
                            Cliente.comunicacao.setMensagem("Editar convites");
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
    public static void menuGrupoAtual(Utilizador utilizador) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean running=true,change=false;//change -> se houver mudancas atualiza o valido
        while (running) {
            try{
                System.out.printf("""
                        Grupo atual: %s
                        [Menu grupo atual]
                        O que pretende fazer?
                          1. Enviar convite
                          2. Nova despesa
                          3. Fazer pagamento
                          4. ...
                          5. ...
                          6. ...
                          7. Sair do grupo
                          0. Sair
                        %n""", utilizador.getGrupoAtual().getNome());
                System.out.print("Opção: ");
                String opcao = in.readLine();
                System.out.println();

                switch (opcao) {
                    case "1":
                        System.out.print("Indique o email do utilizador que deseja convidar: ");
                        Convite convite = new Convite(utilizador.getGrupoAtual().getNome(), in.readLine(), utilizador.getEmail(),"pendente");
                        Cliente.comunicacao.setConvite(convite);
                        Cliente.comunicacao.setMensagem("Enviar convite");
                        Cliente.valido = true;
                        change = true;
                        running = false;
                        break;
                    case "2":

                        break;
                    case "3":

                        break;
                    case "4":

                        break;
                    case "0":
                        running = false;
                        if(change) {
                            Cliente.valido = true;
                            Cliente.comunicacao.setMensagem("Editar convites");
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
    public static void menuGrupos(Utilizador utilizador) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean running=true,change=false;//change -> se houver mudancas atualiza o valido
        while (running) {
            try{
                System.out.println("""
                                [Menu grupos]
                                O que pretende fazer?
                                  1. Escolher grupo
                                  2. Criar grupo
                                  0. Sair""");
                System.out.print("Opção: ");
                String opcao = in.readLine();
                System.out.println();

                switch (opcao) {
                    case "1":
                        System.out.print("Indique o nome do grupo que deseja escolher: ");
                        utilizador.getGrupoAtual().setNome(in.readLine());
                        Cliente.comunicacao.setMensagem("Escolher grupo");
                        Cliente.valido = true;
                        change = true;
                        running = false;
                        break;
                    case "2":
                        Cliente.comunicacao.setMensagem("Criar grupo");
                        System.out.print("Indique o nome do grupo: ");
                        Cliente.comunicacao.setGrupos(in.readLine());
                        Cliente.valido = true;
                        running = false;
                        break;
                    case "3":

                        break;
                    case "4":

                        break;
                    case "0":
                        running = false;
                        if(change) {
                            Cliente.valido = true;
                            Cliente.comunicacao.setMensagem("Editar convites");
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
                                  3. Menu grupos
                                  4. Menu convites""");
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
            }else if(op==3){
                if(!utilizador.getGrupoAtual().getNome().isEmpty())
                    menuGrupoAtual(utilizador);
                else
                    menuGrupos(utilizador);
            }else if(op == 4){
                menuConvites(utilizador);
            } else{
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
