package Cliente;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ClienteUI implements PropertyChangeListener {
    private final ClienteFacade clienteFacade;
    private final BufferedReader reader;
    private boolean running;
    private String lastCommand;
    private Comunicacao resposta;
    final Object sincroniza;

    public ClienteUI(ClienteFacade clienteFacade) {
        this.clienteFacade = clienteFacade;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.clienteFacade.addPropertyChangeListener(this);
        this.running = true;
        this.lastCommand = "";
        sincroniza = new Object();
    }

    public void start() {
        try {
            synchronized (sincroniza) {
                while (running) {
                    if (!clienteFacade.getSincronizado()) {
                        sincroniza.wait();
                    }
                    if (!clienteFacade.isRegistado()) {
                        menuUtilizadoresSemLogin();
                    } else {
                        menuUtilizadoresComLogin();
                    }
                }
            }
        } catch (InterruptedException | IOException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void menuUtilizadoresSemLogin() throws IOException {
        System.out.println("""
                O que pretende fazer?
                  0. Sair
                  1. Registo
                  2. Login
                """);
        String option = reader.readLine().trim();
        switch (option) {
            case "1" -> handleRegister();
            case "2" -> handleLogin();
            case "0" -> exit();
            default -> System.out.println("Opção inválida, tente novamente.");
        }
    }

    private void menuUtilizadoresComLogin() throws IOException {
        if((lastCommand.equalsIgnoreCase("Escolher grupo") && resposta.getMensagem().equalsIgnoreCase("Grupo escolhido")) ||
        lastCommand.equalsIgnoreCase("Enviar convite grupo") ||
        lastCommand.equalsIgnoreCase("Mudar nome grupo") ||
        lastCommand.equalsIgnoreCase("Apagar grupo") && resposta.getMensagem().equalsIgnoreCase("Apagar grupo mal sucedido") ||
        lastCommand.equalsIgnoreCase("Sair grupo") && resposta.getMensagem().equalsIgnoreCase("Saida grupo mal sucedido") ||
        lastCommand.equalsIgnoreCase("Inserir despesa") ||
        lastCommand.equalsIgnoreCase("Total gastos") ||
        lastCommand.equalsIgnoreCase("Historio despesas") ||
        lastCommand.equalsIgnoreCase("Exportar csv")
        ){
            menuGrupoAtual();
        }else if(lastCommand.equalsIgnoreCase("Eliminar despesa")){
            handleRemoveExpense();
        }else if(lastCommand.equalsIgnoreCase("Fazer pagamento")){
            handlePayDebt();
        } else if(lastCommand.equalsIgnoreCase("Editar despesa")){
            handleEditExpense();
        }else if((lastCommand.equalsIgnoreCase("Escolher grupo")  && resposta.getMensagem().equalsIgnoreCase("Utilizador não pertence ao grupo")) ||
                  lastCommand.equalsIgnoreCase("Criar grupo") ||
                  lastCommand.equalsIgnoreCase("Ver grupos") ||
                 (lastCommand.equalsIgnoreCase("Apagar grupo") && resposta.getMensagem().equalsIgnoreCase("Apagar grupo bem sucedido")) ||
                  lastCommand.equalsIgnoreCase("Sair grupo") && resposta.getMensagem().equalsIgnoreCase("Saida grupo bem sucedida")
        ){
            menuGrupos();
        } else if(lastCommand.equalsIgnoreCase("Ver convites") ||
                lastCommand.equalsIgnoreCase("Aceitar convite")||
        lastCommand.equalsIgnoreCase("Rejeitar convite")){
            menuConvites();
        }else {
            System.out.println("""
                    O que pretende fazer?
                      1. Logout
                      2. Editar dados
                      3. Menu grupos
                      4. Menu convites
                    """);
            String option = reader.readLine().trim();
            switch (option) {
                case "1" -> clienteFacade.logout();
                case "2" -> handleUpdateUserData();
                case "3" -> menuGrupos();
                case "4" -> menuConvites();
                default -> System.out.println("Opção inválida, tente novamente.");
            }
        }
    }

    private void menuGrupos() throws IOException {
        System.out.println("""
                
                [Menu grupos]
                O que pretende fazer?
                  1. Escolher grupo
                  2. Criar grupo
                  3. Ver grupos pertencentes
                  0. Sair
                """);
        String option = reader.readLine().trim();
        switch (option) {
            case "1" -> handleChooseGroup();
            case "2" -> {lastCommand="Criar grupo";handleCreateGroup();}
            case "3" -> {lastCommand="Ver grupos";clienteFacade.viewGroups();}
            case "0" -> menuUtilizadoresComLogin();
            default -> System.out.println("Opção inválida, tente novamente.");
        }
    }

    private void menuGrupoAtual(){
        try {
            System.out.println(String.format("""
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
                      10. Fazer pagamento
                      11. Sair do grupo
                      0. Sair
                    """, clienteFacade.getUtilizador().getGrupoAtual().getNome()));
            String option = reader.readLine().trim();
            switch (option) {
                case "1" -> {lastCommand = "Enviar convite grupo";handleSendInvite();}
                case "2" -> {lastCommand = "Mudar nome grupo";handleUpdateGroupName();}
                case "3" -> {lastCommand = "Apagar grupo";clienteFacade.deleteGroup();}
                case "4" -> {lastCommand = "Inserir despesa"; handleAddExpense();}
                case "5" -> {lastCommand = "Total gastos";clienteFacade.viewTotalExpenses();}
                case "6" -> {lastCommand = "Historio despesas";clienteFacade.viewExpenses();}
                case "7" -> {lastCommand = "Exportar csv";clienteFacade.exportExpensesToCSV();}
                case "8" -> {lastCommand = "Editar despesa"; clienteFacade.viewExpenses();}
                case "9" -> {lastCommand = "Eliminar despesa"; clienteFacade.viewExpenses();}
                case "10" -> {lastCommand = "Fazer pagamento";clienteFacade.viewExpenses();}
                case "11" -> {lastCommand = "Sair grupo";clienteFacade.exitGroup();}
                case "0" -> {lastCommand = "";menuUtilizadoresComLogin();}
                default -> System.out.println("Opção inválida, tente novamente.");
            }
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    private void menuConvites() throws IOException {
        System.out.println("ultimo comando: "+lastCommand+" \nresposta"+resposta.getMensagem());
        if(!lastCommand.equalsIgnoreCase("Ver convites") || (lastCommand.equalsIgnoreCase("Ver convites") && resposta.getMensagem().equalsIgnoreCase("Lista de convites vazia"))){
            System.out.println("AQUI");
            System.out.println("""
                            [Menu convites]
                              1. Ver convites
                              0. Sair""");
            String option = reader.readLine().trim();
            switch (option) {
                case "1" ->{lastCommand="Ver convites";handleSeeInvites();}
                case "0" -> menuUtilizadoresComLogin();
            }
        }
        if(lastCommand.equalsIgnoreCase("Ver convites")) {
            System.out.println("""
                    [Menu convites]
                      1. Aceitar convite
                      2. Rejeitar convite
                      0. Sair
                    """);
            String option = reader.readLine().trim();
            switch (option) {
                case "1" -> {
                    lastCommand = "Aceitar convite";
                    handleChangeInviteState("Aceite");
                }
                case "2" -> {
                    lastCommand = "Rejeitar convite";
                    handleChangeInviteState("Rejeitado");
                }
                case "0" ->{lastCommand = ""; menuUtilizadoresComLogin();}
                default -> System.out.println("Opção inválida, tente novamente.");
            }
        }
    }

    private void handleLogin() throws IOException {
        System.out.print("Email: ");
        String email = reader.readLine().trim();
        System.out.print("Password: ");
        String password = reader.readLine().trim();
        clienteFacade.login(email, password);
    }

    private void handleRegister() throws IOException {
        System.out.print("Nome: ");
        String nome = reader.readLine().trim();
        System.out.print("Email: ");
        String email = reader.readLine().trim();
        System.out.print("Password: ");
        String password = reader.readLine().trim();
        clienteFacade.register(nome, email, password);
    }

    private void handleUpdateUserData() throws IOException {
        System.out.println("""
                Que dados pretende editar?
                  1. Nome
                  2. Password
                  3. Telefone
                  0. Sair
                """);
        String option = reader.readLine().trim();
        switch (option) {
            case "1" -> updateUserName();
            case "2" -> updateUserPassword();
            case "3" -> updateUserPhone();
            case "0" -> System.out.println("Nenhuma alteração foi efetuada.");
            default -> System.out.println("Opção inválida, tente novamente.");
        }
    }

    private void handleChooseGroup() throws IOException {
        System.out.print("Indique o nome do grupo que deseja escolher: ");
        String nomeGrupo = reader.readLine().trim();
        lastCommand = "Escolher grupo";
        clienteFacade.chooseGroup(nomeGrupo);
    }

    private void handleCreateGroup() throws IOException {
        System.out.print("Nome do Grupo: ");
        String groupName = reader.readLine().trim();
        clienteFacade.createGroup(groupName);
    }

    private void handleSendInvite() throws IOException {
        System.out.print("Email do Utilizador a Convidar: ");
        String email = reader.readLine().trim();
        clienteFacade.sendGroupInvite(email);
    }

    private void handleUpdateGroupName() throws IOException {
        System.out.print("Novo Nome do Grupo: ");
        String novoNome = reader.readLine().trim();
        clienteFacade.updateGroupName(novoNome);
    }

    private void handleAddExpense() {
        try {
            System.out.print("Descrição da Despesa: ");
            String descricao = reader.readLine().trim();
            System.out.print("Valor da Despesa: ");
            double valor = Double.parseDouble(reader.readLine().trim());
            System.out.print("Data (dd-MM-yyyy): ");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String dataInput = reader.readLine().trim();
            Date data = new Date(dateFormat.parse(dataInput).getTime());
            clienteFacade.addExpense(descricao, valor, data);
        }catch (ParseException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleEditExpense(){
        try {
            System.out.println("Escolha o id da despesa que pretende editar");
            int opcao = Integer.parseInt(reader.readLine().trim());
            System.out.print("Nova descrição da Despesa: ");
            String descricao = reader.readLine().trim();
            System.out.print("Novo Valor: ");
            double valor = Double.parseDouble(reader.readLine().trim());
            System.out.print("Nova Data (dd-MM-yyyy): ");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String dataInput = reader.readLine().trim();
            Date data = new Date(dateFormat.parse(dataInput).getTime());
            clienteFacade.updateExpense(descricao, valor, data, opcao);
            lastCommand="";
        }catch (ParseException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void handleRemoveExpense() throws IOException {
        System.out.println("Escolha o id da despesa que pretende eliminar");
        int opcao = Integer.parseInt(reader.readLine().trim());
        clienteFacade.deleteExpense(opcao);
        lastCommand="";
    }

    private void handlePayDebt() throws IOException {
        System.out.print("Indique o id da divida que deseja pagar: ");
        int opcao = Integer.parseInt(reader.readLine().trim());
        System.out.print("Valor a Pagar: ");
        double valor = Double.parseDouble(reader.readLine().trim());
        clienteFacade.payDebt(valor, opcao);
        lastCommand="";
    }
    private void handleSeeInvites() {
        clienteFacade.seeInvites();
    }
    private void handleChangeInviteState(String estado) throws IOException {
        if(estado.equalsIgnoreCase("Aceite"))
            System.out.print("Nome do Grupo para Aceitar o Convite: ");
        else
            System.out.print("Nome do Grupo para Rejeitar o Convite: ");
        String nomeGrupo = reader.readLine().trim();
        if(!clienteFacade.handleChangeInviteState(estado, nomeGrupo)){
            System.out.println("Não foi encontrado nenhum convite para o grupo "+nomeGrupo+"!");
           lastCommand="";
            menuUtilizadoresComLogin();
        }
    }

    private void updateUserName() throws IOException {
        System.out.print("Novo Nome: ");
        String nome = reader.readLine().trim();
        clienteFacade.updateUserName(nome);
    }

    private void updateUserPassword() throws IOException {
        System.out.print("Nova Password: ");
        String password = reader.readLine().trim();
        clienteFacade.updateUserPassword(password);
    }

    private void updateUserPhone() throws IOException {
        System.out.print("Novo Telefone: ");
        String telefone = reader.readLine().trim();
        clienteFacade.updateUserPhone(telefone);
    }

    private void exit() {
        System.out.println("Encerrando o cliente...");
        running = false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("mensagemRecebida".equals(evt.getPropertyName())) {
            resposta = (Comunicacao) evt.getNewValue();
            System.out.println("\n[Resposta do Servidor]: " + resposta.getMensagem());
            if(resposta.getMensagem().equalsIgnoreCase("Historio de despesas")
               || resposta.getMensagem().equalsIgnoreCase("Sucesso, escolha a despesa por id")
               || resposta.getMensagem().equalsIgnoreCase("Sucesso, escolha a despesa por id para edicao")){
                for(var d: resposta.getDespesa()){
                    System.out.println(d.toString());
                }
            }
            if(resposta.getMensagem().equalsIgnoreCase("Lista de convites")){
                System.out.println("Convites pendentes:");
                for (var convite: clienteFacade.getUtilizador().getConvites())
                    System.out.println("Convite para o grupo: " + convite.getNomeGrupo() + " (de: " + convite.getRemetente() + ")");
            }
            if(resposta.getMensagem().equalsIgnoreCase("Ver grupos bem sucedido")){
                System.out.println("Lista de Grupos pertencentes: ");
                for(var g:resposta.getUtilizador().getGrupos())
                    System.out.println("Nome: "+g.getNome());
            }

            if(resposta.getMensagem().equalsIgnoreCase("Logout aceite")){
                running = false;
            }
            if(resposta.getMensagem().equalsIgnoreCase("Mudanca nome bem sucedida"))
                clienteFacade.getUtilizador().getGrupoAtual().setNome(resposta.getUtilizador().getGrupoAtual().getNome());

            // Este metodo tem que ficar sempre no final
            synchronized (sincroniza){
                sincroniza.notify();
            }
        }
    }
}
