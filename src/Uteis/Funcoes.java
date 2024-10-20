package Uteis;
import Cliente.Cliente;
import Entidades.*;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Funcoes {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    public static String estado;

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
            System.out.println("Cheguei aqui");
            Cliente.valido = true;
            Cliente.comunicacao.setMensagem(estado.equalsIgnoreCase("Aceite") ? "Aceitar convite" : "Rejeitar convite");
            Cliente.lastCommand = estado.equalsIgnoreCase("Aceite") ? "Aceitar convite" : "Rejeitar convite";
        } else {
            Cliente.valido = false;
            System.out.println("Nenhuma alteração foi efetuada.");
        }
    }
    public static void eliminaDespesa(Utilizador utilizador) throws IOException, InterruptedException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean repeat = false;
        Thread.sleep(200);
        if(utilizador.getDespesas() == null || utilizador.getDespesas().isEmpty()){
            System.out.println("\nNao tem nenhuma despesa\n");
            Cliente.lastCommand ="";
            return;
        }
        System.out.println("Escolha o id da despesa que pretende eliminar");
        String opcao = in.readLine().trim();
        int opID;
        while(true) {
            opID = Integer.parseInt(opcao);
            for(var d: utilizador.getDespesas()){
                if(opID ==d.getIdDespesa()) {
                    repeat = true;
                    break;
                }
            }
            if(repeat)
                break;
            System.out.println("Opcao invalida, escolha uma opcao valida ou escreva 'sair'");
            opcao = in.readLine().trim();
            if(opcao.equalsIgnoreCase("sair")) {
                break;
            }
        }

        Cliente.lastCommand ="";
        if(opcao.equalsIgnoreCase("sair")) {
            Cliente.valido = false;
            return;
        }

        Cliente.comunicacao.setMensagem("Eliminar despesa com id " + opID);
        Cliente.valido = true;
    }
    public static void editarDespesa(Utilizador utilizador) throws InterruptedException, IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean repeat = false;
        Thread.sleep(200);
        if(utilizador.getDespesas() == null || utilizador.getDespesas().isEmpty()){
            System.out.println("\nNao tem nenhuma despesa\n");
            Cliente.lastCommand ="";
            return;
        }

        System.out.println("Escolha o id da despesa que pretende editar");
        String opcao = in.readLine().trim();
        int opID;
        while(true) {
            opID = Integer.parseInt(opcao);
            for(var d: utilizador.getDespesas()){
                if(opID ==d.getIdDespesa()) {
                    repeat = true;
                    break;
                }
            }
            if(repeat)
                break;
            System.out.println("Opcao invalida, escolha uma opcao valida ou escreva 'sair'");
            opcao = in.readLine().trim();
            if(opcao.equalsIgnoreCase("sair")) {
                Cliente.valido = false;
                return;
            }
        }
        if (Cliente.comunicacao.getDespesa() == null) {
            Cliente.comunicacao.setDespesa(new ArrayList<>());
        }
        if (Cliente.comunicacao.getDespesa().isEmpty()) {
            Cliente.comunicacao.getDespesa().add(new Despesas());
        }
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"); // Define o formato da data
            System.out.print("Insira o novo valor da despesa: ");
            String input = in.readLine();
            double valor = Double.parseDouble(input);
            Cliente.comunicacao.getDespesa().getFirst().setValor(valor);
            System.out.print("Nova descricao: ");
            Cliente.comunicacao.getDespesa().getFirst().setDescricao(in.readLine());
            System.out.print("Nova data (formato: dd-MM-yyyy): ");
            String dataInput = in.readLine();
            System.out.println(dataInput);
            System.out.println(dateFormat.parse(dataInput));
            Cliente.comunicacao.getDespesa().getFirst().setData(new java.sql.Date(dateFormat.parse(dataInput).getTime()));
            String dataFormatada = dateFormat.format(Cliente.comunicacao.getDespesa().getFirst().getData());
            System.out.println("Data inserida: " + dataFormatada);
        } catch (IOException e) {
            System.out.println("Erro ao ler a entrada: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Erro: entrada inválida. Por favor, insira um número válido.");
        } catch (ParseException e) {
            System.out.println("Erro: formato de data inválido. Use o formato dd-MM-yyyy.");
        }

        Cliente.lastCommand ="";
        Cliente.comunicacao.setMensagem("Editar despesa com id " + opID);
        Cliente.valido = true;
    }

    public static void menuConvites(Utilizador utilizador) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        Cliente.valido = false;
        boolean running = true;
        while (running) {
            try{
                if(!Cliente.lastCommand.equalsIgnoreCase("Ver convites") || (Cliente.utilizadorUpdate.getConvites() == null && Cliente.lastCommand.equalsIgnoreCase("Ver convites"))){
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
                            Cliente.valido = true;
                            running = false;
                            break;
                        case "0":
                            Cliente.valido = false;
                            running = false;
                            Cliente.lastCommand ="";
                            System.out.println("Nenhuma alteração foi efetuada");
                            break;
                        default:
                            System.out.println("Opção inválida, tente novamente.");
                    }
                }else if(Cliente.lastCommand.equalsIgnoreCase("Ver convites")){
                    Cliente.lastCommand = "";
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
                            Cliente.valido = true;
                            running = false;
                            break;
                        case "2":
                            estado="Rejeitado";
                            mudarEstadoConvite(utilizador,estado);
                            Cliente.valido = true;
                            running = false;
                            break;
                        case "0":
                            Cliente.valido = false;
                            running = false;
                            System.out.println("Nenhuma alteração foi efetuada");
                            break;
                        default:
                            System.out.println("Opção inválida, tente novamente.");
                    }
                }
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
                          4. Nova despesa
                          5. Ver gastos totais
                          6. Ver historico de despesas
                          7. Exportar despesas (csv)
                          8. Editar despesa
                          9. Eliminar despesa
                          10. Sair do grupo
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
                        running = false;
                        break;
                    case "2":
                        System.out.print("Indique o novo nome: ");
                        utilizador.getGrupoAtual().setNomeProvisorio(in.readLine());
                        Cliente.comunicacao.setMensagem("Mudar nome grupo");
                        Cliente.valido=true;
                        running=false;
                        break;
                    case "3":
                        Cliente.comunicacao.setMensagem("Apagar grupo");
                        Cliente.valido=true;
                        running=false;
                        break;
                    case "4":
                        if (Cliente.comunicacao.getDespesa() == null) {
                            Cliente.comunicacao.setDespesa(new ArrayList<>());
                        }
                        if (Cliente.comunicacao.getDespesa().isEmpty()) {
                            Cliente.comunicacao.getDespesa().add(new Despesas());
                        }
                        Cliente.comunicacao.setMensagem("Inserir despesa");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"); // Define o formato da data
                        while(true) {
                            try {
                                System.out.print("Insira o valor da despesa: ");
                                String input = in.readLine();
                                double valor = Double.parseDouble(input);
                                Cliente.comunicacao.getDespesa().getFirst().setValor(valor);
                                System.out.print("Descricao: ");
                                Cliente.comunicacao.getDespesa().getFirst().setDescricao(in.readLine());
                                System.out.print("Insira a data (formato: dd-MM-yyyy): ");
                                String dataInput = in.readLine();
                                System.out.println(dataInput);
                                System.out.println(dateFormat.parse(dataInput));
                                Cliente.comunicacao.getDespesa().getFirst().setData(new java.sql.Date(dateFormat.parse(dataInput).getTime()));
                                String dataFormatada = dateFormat.format(Cliente.comunicacao.getDespesa().getFirst().getData());
                                System.out.println("Data inserida: " + dataFormatada);
                                break;
                            } catch (IOException e) {
                                System.out.println("Erro ao ler a entrada: " + e.getMessage());
                            } catch (NumberFormatException e) {
                                System.out.println("Erro: entrada inválida. Por favor, insira um número válido.");
                            } catch (ParseException e) {
                                System.out.println("Erro: formato de data inválido. Use o formato dd-MM-yyyy.");
                            }
                        }
                        Cliente.lastCommand="Nova despesa";
                        Cliente.valido=true;
                        running=false;
                        break;
                    case "5":
                        Cliente.comunicacao.setMensagem("Total gastos");
                        Cliente.lastCommand="Total gastos";
                        Cliente.valido=true;
                        running=false;
                        break;
                    case "6":
                        Cliente.comunicacao.setMensagem("Historio despesas");
                        Cliente.lastCommand="Historio despesas";
                        Cliente.valido=true;
                        running=false;
                        break;
                    case "7":
                        Cliente.comunicacao.setMensagem("Exportar csv");
                        Cliente.lastCommand="Exportar csv";
                        Cliente.valido=true;
                        running=false;
                        break;
                    case "8":
                        Cliente.comunicacao.setMensagem("Editar despesa");
                        Cliente.lastCommand="Editar despesa";
                        Cliente.valido=true;
                        running=false;
                        break;
                    case "9":
                        Cliente.comunicacao.setMensagem("Eliminar despesa");
                        Cliente.lastCommand="Eliminar despesa";
                        Cliente.valido=true;
                        running=false;
                        break;
                    case "10":
                        Cliente.comunicacao.setMensagem("Sair grupo");
                        Cliente.valido=true;
                        running=false;
                        break;
                    case "0":
                        running = false;
                        menuGrupos(utilizador);
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

    public static void menuUtilizadoresComLogin(Utilizador utilizador) throws IOException, InterruptedException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int op ;
        if(Cliente.lastCommand.equalsIgnoreCase("Ver convites")){
            menuConvites(utilizador);
        } else if(Cliente.lastCommand.equalsIgnoreCase("Escolher grupo")) {
            menuGrupoAtual(utilizador);
        } else if(Cliente.lastCommand.equalsIgnoreCase("Enviar convite grupo")) {
            menuGrupoAtual(utilizador);
        } else if(Cliente.lastCommand.equalsIgnoreCase("Eliminar despesa")){
            eliminaDespesa(utilizador);
        } else if(Cliente.lastCommand.equalsIgnoreCase("Mudar nome grupo")){
            menuGrupoAtual(utilizador);
        } else if(Cliente.lastCommand.equalsIgnoreCase("Nova despesa")){
            menuGrupoAtual(utilizador);
        } else if(Cliente.lastCommand.equalsIgnoreCase("Total gastos")){
            menuGrupoAtual(utilizador);
        } else if(Cliente.lastCommand.equalsIgnoreCase("Historio despesas")){
            menuGrupoAtual(utilizador);
        } else if(Cliente.lastCommand.equalsIgnoreCase("Exportar csv")){
            menuGrupoAtual(utilizador);
        } else if(Cliente.lastCommand.equalsIgnoreCase("Editar despesa")){
            editarDespesa(utilizador);
        } else {
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
