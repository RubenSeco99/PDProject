package Uteis;
import Cliente.Cliente;
import Entidades.*;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Pattern;

public class Funcoes {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    public static String estado;

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
                            Cliente.lastCommand="Editar dados";
                        }
                        else{
                            Cliente.valido=false;
                            System.out.println("Nenhuma alteração foi efetuada");
                        }
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
    public static void mudarEstadoConvite(Utilizador utilizador, String estado) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        // Verificar se há convites pendentes
        List<Convite> convitesPendentes = utilizador.getConvites();
        if (convitesPendentes.isEmpty()) {
            System.out.println("Não há convites pendentes.");
            return;
        }
        boolean running = true;
        boolean change = false;
        while (running) {
            System.out.println("Convites pendentes:");
            for (Convite convite : utilizador.getConvites()) {
                System.out.println("Grupo: " + convite.getNomeGrupo() + " | Estado: " + convite.getEstado());
            }
            System.out.print("Coloque o nome do grupo (ou escreva 'sair menu' para sair): ");
            String nomeGrupo = in.readLine().trim();//trim elimina o espaço antes da primeira palavra e o espaço depois da ultima palavra, caso eles existam
            //Convite conviteSelecionado = null;
            for (Convite convite : utilizador.getConvites()) {
                if (convite.getNomeGrupo().equalsIgnoreCase(nomeGrupo)) {
                     convite.setEstado(estado);
                    break;
                }
            }
            if (nomeGrupo.equalsIgnoreCase("sair menu")) {
                System.out.println("Saindo do menu de convites.");
                return;
            }
                if (estado.equalsIgnoreCase("aceitar")) {
                    System.out.println("Convite aceito para o grupo: " + nomeGrupo);
                } else if (estado.equalsIgnoreCase("rejeitar")) {
                    System.out.println("Convite rejeitado para o grupo: " + nomeGrupo);
                }
                change = true;
                running = false;

        }
        if (change) {
            Cliente.valido = true;
            Cliente.comunicacao.setMensagem(estado.equalsIgnoreCase("Aceite") ? "Aceitar convite" : "Rejeitar convite");
            Cliente.lastCommand = estado.equalsIgnoreCase("Aceite") ? "Aceitar convite" : "Rejeitar convite";
        } else {
            Cliente.valido = false;
            System.out.println("Nenhuma alteração foi efetuada.");
        }
    }
    /*
    public static void mudarEstadoConvite(Utilizador utilizador) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        if (utilizador.getConvites().isEmpty()) {
            System.out.println("Não há convites pendentes.");
            return;
        }
        boolean running = true;
        boolean change = false;
        while (running) {
            System.out.println("""
                                [Vizualizar Convites]
                                O que pretende fazer?
                                  1. Aceitar (not done)
                                  2. Rejeitar (not done)
                                  0. Sair""");
            System.out.print("Opção: ");

            String estado = in.readLine();
            if(estado.equals("1")) {
                System.out.println("Indique o nome do grupo do qual pretende aceitar o convite: ");
                String nomeGrupo= in.readLine();
                if(utilizador.checkConviteExiste(nomeGrupo)) {
                    utilizador.getConvite(nomeGrupo).setEstado("Aceite");
                    change=true;
                    Cliente.valido=true;
                    Cliente.comunicacao.setMensagem("Aceitar convite");
                    Cliente.lastCommand="Aceitar convite";
                }
                else{
                    System.out.println("Convite nao existe");
                }
            }else if(estado.equals("2")) {
                System.out.println("Indique o nome do grupo do qual pretende rejeitar o convite: ");
                String nomeGrupo= in.readLine();
                if(utilizador.checkConviteExiste(nomeGrupo)) {
                    utilizador.getConvite(nomeGrupo).setEstado("Aceite");
                    change=true;
                    Cliente.valido=true;
                    Cliente.comunicacao.setMensagem("Aceitar convite");
                    Cliente.lastCommand="Aceitar convite";
                }
                else{
                    System.out.println("Convite nao existe");
                }
            }else {
                System.out.println("A sair para o menu");
            }

        }
    }
    */

    public static void menuConvites(Utilizador utilizador) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean running=true,change=false;//change -> se houver mudancas atualiza o valido
        while (running) {
            try{
                if((Cliente.lastCommand.equalsIgnoreCase("Ver convite") && Cliente.response.getMensagem().equalsIgnoreCase("Lista de convites vazia"))|| !Cliente.lastCommand.contains("convite")) {
                    System.out.println("""
                            [Menu convites]
                              1. Ver convites
                              0. Sair""");
                    System.out.print("Opção: ");
                    String opcao = in.readLine();

                    switch (opcao) {
                        case "1":
                            Cliente.comunicacao.setMensagem("Ver convites");
                            Cliente.lastCommand = "Ver convites";
                            running = false;
                            change=true;
                            break;
                        case "0":
                            running = false;
                            if (change) {
                                Cliente.valido = true;
                            } else {
                                Cliente.valido = false;
                                System.out.println("Nenhuma alteração foi efetuada");
                            }
                            break;
                        default:
                            System.out.println("Opção inválida, tente novamente.");
                    }
                }
                else if(Cliente.lastCommand.contains(" convite")){
                    System.out.println("""
                            [Menu convites]
                              1. Aceitar convite
                              2. Rejeitar convite
                              0. Sair""");
                    System.out.print("Opção: ");
                    String opcao = in.readLine();

                    switch (opcao) {
                        case "1":
                            estado="Aceite";
                            mudarEstadoConvite(utilizador,estado);
                            change=true;
                            running = false;
                            break;
                        case "2":
                            estado="Rejeitado";
                            mudarEstadoConvite(utilizador,estado);
                            change=true;
                            running = false;
                            break;
                        case "0":
                            running = false;
                            if (change) {
                                Cliente.valido = true;
                            } else {
                                Cliente.valido = false;
                                System.out.println("Nenhuma alteração foi efetuada");
                            }
                            break;
                        default:
                            System.out.println("Opção inválida, tente novamente.");
                    }
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
                        [Menu grupo: %s]
                        O que pretende fazer?
                          1. Enviar convite
                          2. Mudar nome grupo
                          3. Apagar grupo
                          4. Nova despesa (NOT DONE)
                          5. ... (NOT DONE)
                          6. ... (NOT DONE)
                          7. Sair do grupo (NOT DONE)
                          0. Sair
                        %n""", utilizador.getGrupoAtual().getNome());
                System.out.print("Opção: ");
                String opcao = in.readLine();
                System.out.println();

                switch (opcao) {
                    case "1":
                        System.out.print("Indique o email do utilizador que deseja convidar: ");
                        Convite convite = new Convite(utilizador.getGrupoAtual().getNome(), utilizador.getEmail(), in.readLine(),"pendente");
                        Cliente.comunicacao.setConvite(convite);
                        Cliente.comunicacao.setMensagem("Enviar convite grupo");
                        Cliente.lastCommand="Enviar convite grupo";
                        Cliente.valido = true;
                        change = true;
                        running = false;
                        break;
                    case "2":
                        System.out.print("Indique o novo nome: ");
                        utilizador.getGrupoAtual().setNomeProvisorio(in.readLine());
                        Cliente.comunicacao.setMensagem("Mudar nome grupo");
                        Cliente.valido=true;
                        change=true;
                        running=false;
                        break;
                    case "3":
                        Cliente.comunicacao.setMensagem("Apagar grupo");
                        Cliente.valido=true;
                        change=true;
                        running=false;
                        break;
                    case "4":

                        break;
                    case "0":
                        running = false;
                        if(change) {
                            Cliente.valido = true;
                            Cliente.comunicacao.setMensagem("Editar convites");
                            Cliente.lastCommand="Editar convites";
                        }
                        else{
                            Cliente.valido=false;}
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
                                  3. Ver grupos pertencentes
                                  0. Sair""");
                System.out.print("Opção: ");
                String opcao = in.readLine();
                System.out.println();

                switch (opcao) {
                    case "1":
                        System.out.print("Indique o nome do grupo que deseja escolher: ");
                        utilizador.getGrupoAtual().setNome(in.readLine());
                        Cliente.comunicacao.setMensagem("Escolher grupo");
                        Cliente.lastCommand="Escolher grupo";
                        change = true;
                        running = false;
                        break;
                    case "2":
                        Cliente.comunicacao.setMensagem("Criar grupo");
                        Cliente.lastCommand="Criar grupo";
                        System.out.print("Indique o nome do grupo: ");
                        Cliente.comunicacao.setGrupos(in.readLine());
                        change = true;
                        running = false;
                        break;
                    case "3":
                        Cliente.comunicacao.setMensagem("Ver grupos");
                        Cliente.lastCommand="Ver grupos";
                        change = true;
                        running = false;
                        break;
                    case "4":

                        break;
                    case "0":
                        running = false;
                        if(change){
                            Cliente.valido = true;

                        }
                        else {
                            Cliente.valido = false;
                        }
                        break;
                    default:
                        System.out.println("Opção inválida, tente novamente.");
                }
                System.out.println();
            } catch (IOException e) {
                System.out.println("Ocorreu um erro ao ler a entrada. Tente novamente.");
            } catch (NumberFormatException e) {
                if(!Cliente.EXIT)
                    System.out.println("Opção inválida. Insira um número válido.");
            }
        }
    }

    public static void menuUtilizadoresComLogin(Utilizador utilizador){
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int op ;

        if(Cliente.lastCommand.equalsIgnoreCase("Ver convites")){
            menuConvites(utilizador);
        }
        else {
            System.out.println("""
                    O que pretende fazer?
                      1. Logout
                      2. Editar dados
                      3. Menu grupos
                      4. Menu convites""");
            System.out.print("> ");
            try {
                op = Integer.parseInt(in.readLine());
                if (op == 1) {
                    Cliente.comunicacao.setMensagem("Logout");
                    Cliente.lastCommand = "Logout";
                    Cliente.valido = true;
                    System.out.println("Logout efetuado com sucesso!");
                    Cliente.EXIT = true;
                } else if (op == 2) {
                    updateUtilizador(utilizador);
                } else if (op == 3) {
                    if (!utilizador.getGrupoAtual().getNome().isEmpty())
                        menuGrupoAtual(utilizador);
                    else
                        menuGrupos(utilizador);
                } else if (op == 4) {
                    menuConvites(utilizador);
                } else {
                    System.out.println("Opcao invalida!");
                    Cliente.valido = false;
                }
            } catch (NumberFormatException | IOException e) {
                if(!Cliente.EXIT)
                    System.out.println("Opcao invalida! Insira uma opção válida.");

            }
        }
    }
    public static void menuUtilizadoresSemLogin(Utilizador utilizador){
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
            if(op==0){
                Cliente.comunicacao.setMensagem("Sair");
                Cliente.valido = true;
                System.out.println("Saida efetuado com sucesso!");
                Cliente.EXIT = true;
            }
            else if (op == 1) {
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
            if(!Cliente.EXIT)
                System.out.println("Formato invalido " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void Menu(Utilizador utilizador) throws IOException, InterruptedException {
        Thread.sleep(150);
        if(!Cliente.registado){
            menuUtilizadoresSemLogin(utilizador);
        } else {
            menuUtilizadoresComLogin(utilizador);
        }
    }

}
