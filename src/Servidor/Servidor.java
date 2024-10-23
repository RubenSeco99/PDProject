package Servidor;

import BaseDeDados.*;
import Cliente.Comunicacao;
import Entidades.Convite;
import Entidades.Despesas;
import Entidades.Grupo;
import Entidades.Utilizador;
import Uteis.Funcoes;
import Uteis.FuncoesServidor;
import Uteis.ServerBackUpSupport;

import java.io.*;
import java.net.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class encerraServerThread implements Runnable{

    List<notificaThread> arraySocketsClientes;
    final Object lock;
    ServerSocket serverS;
    public encerraServerThread(List<notificaThread> arraySocketsClientes, Object lock, ServerSocket serverS){
        this.arraySocketsClientes = arraySocketsClientes;
        this.lock = lock;
        this.serverS = serverS;
    }

    @Override
    public void run(){
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String exit;
        try {
            while(true){
                exit = in.readLine();
                if(exit.equalsIgnoreCase("exit") || exit.equalsIgnoreCase("sair")){
                    Servidor.encerraServidor = true;
                    System.out.println("Entrei na condicao de saida");
                    synchronized (lock){
                        lock.notify();
                    }
                    Thread.sleep(100);
                    serverS.close();
                    for(var s:arraySocketsClientes){
                        s.getSocket().close();
                    }
                    break;
                }else{
                    System.out.println("Comando invalido. Escreva (exit) ou (sair) para encerrar o servidor");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }
}

class notificaCliente implements Runnable{

    List <notificaThread> clienteSockets;
    private final Object lock;
    public notificaCliente(List<notificaThread> clienteSockets, Object lock){
        this.clienteSockets = clienteSockets;
        this.lock = lock;
    }

    @Override
    public void run(){
        try{
            synchronized (lock) {
                while (true) {
                    lock.wait();
                    //QUANDO O SERVIDOR FAZ SAIR OU EXIT
                    if(Servidor.encerraServidor) {
                        System.out.println("A sair da notifica");
                        Utilizador utilizador = new Utilizador();
                        Comunicacao respostaSaida = new Comunicacao(utilizador);
                        respostaSaida.setMensagem("Servidor em baixo");
                        for(var cli : clienteSockets){
                            respostaSaida.getUtilizador().setEmail(cli.getEmail());
                            cli.getOout().writeObject(respostaSaida);
                            cli.getOout().flush();
                        }
                        break;
                    }
                    //ENVIA A NOTIFICAÇÃO
                    Comunicacao respostaSaida = new Comunicacao();
                    Utilizador utilizador = new Utilizador();
                    utilizador.setEmail(Servidor.EMAILSEND);
                    utilizador.getGrupoAtual().setNome(Servidor.NOMEGRUPO);
                    respostaSaida.setUtilizador(utilizador);
                    respostaSaida.setMensagem("Convite recebido para o grupo " + Servidor.NOMEGRUPO + "\nEnviado por: " + Servidor.EMAILREMETENTE);
                    System.out.println("Notificação enviada");
                    for(var cli: clienteSockets){
                        if(cli.getEmail().equals(Servidor.EMAILSEND)){
                            cli.getOout().writeObject(respostaSaida);
                            cli.getOout().flush();
                        }
                    }
                    Servidor.EMAILSEND = "";
                    Servidor.NOMEGRUPO = "";
                    Servidor.EMAILREMETENTE = "";
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

class processaClienteThread implements Runnable {

    private Socket clienteSocket;
    private boolean running;
    private java.sql.Connection connection;
    private UtilizadorDB utilizadorDB;
    private GrupoDB grupoDB;
    private ConviteDB conviteDB;
    private UtilizadorGrupoDB utilizadorGrupoDB;
    private DespesaDB despesaDB;
    private DespesaPagadoresDB despesaPagadoresDB;
    private PagamentoDB pagamentoDB;
    private boolean conectado;
    private boolean EXIT = false;
    List <notificaThread> notificaThreads;
    private final Object lock;

    public processaClienteThread(Socket clienteSocket, java.sql.Connection connection, List<notificaThread> clienteSockets, Object lock) {
        this.clienteSocket = clienteSocket;
        this.connection =  connection;
        this.running = true;
        this.utilizadorDB = new UtilizadorDB(connection);
        this.grupoDB=new GrupoDB(connection);
        this.utilizadorGrupoDB=new UtilizadorGrupoDB(connection);
        this.conviteDB=new ConviteDB(connection);
        this.despesaDB=new DespesaDB(connection);
        this.despesaPagadoresDB= new DespesaPagadoresDB(connection);
        this.pagamentoDB = new PagamentoDB(connection);
        this.notificaThreads = clienteSockets;
        this.lock = lock;
    }

    @Override
    public void run() {

        try(ObjectInputStream Oin = new ObjectInputStream(clienteSocket.getInputStream());
            ObjectOutputStream Oout = new ObjectOutputStream(clienteSocket.getOutputStream())) {
            Utilizador utilizadorThread = new Utilizador();

            while(running && !clienteSocket.isClosed()){
                try {

                    Comunicacao pedidoCliente = (Comunicacao) Oin.readObject();
                    Comunicacao respostaSaida = new Comunicacao();
                    utilizadorThread = pedidoCliente.getUtilizador();
                    System.out.println("\nPedido recebido: " + pedidoCliente);
                    System.out.println("> ");

                    if(!conectado) {
                        if (pedidoCliente.getMensagem().equalsIgnoreCase("Sair")) {
                            respostaSaida = pedidoCliente;
                            respostaSaida.setMensagem("Saida bem sucedida");
                            Oout.writeObject(respostaSaida);
                            Oout.flush();
                        }
                        else if (pedidoCliente.getMensagem().equalsIgnoreCase("Registo")) {
                            if (!Funcoes.isValidEmail(pedidoCliente.getUtilizador().getEmail())) {
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Formato Email Inválido!");
                                Oout.writeObject(respostaSaida);
                                Oout.flush();
                            } else if (!utilizadorDB.verificaRegisto(pedidoCliente.getUtilizador().getEmail())) {
                                utilizadorDB.insertUtilizador(pedidoCliente.getUtilizador());
                                respostaSaida.setUtilizador(pedidoCliente.getUtilizador());
                                respostaSaida.setMensagem("Aceite");
                            } else {
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Email existente");
                            }
                        }
                        else if (pedidoCliente.getMensagem().equalsIgnoreCase("login")) {
                            if (utilizadorDB.verificaLogin(pedidoCliente.getUtilizador().getEmail(), pedidoCliente.getUtilizador().getPassword())) {
                                pedidoCliente.setUtilizador(utilizadorDB.selectUtilizador(pedidoCliente.getUtilizador().getEmail()));
                                //Para atualizar a lista de utilizadores logados na thread notificação
                                notificaThread clienteS = new notificaThread(clienteSocket);
                                clienteS.setEmail(pedidoCliente.getUtilizador().getEmail());
                                clienteS.setOout(Oout);
                                notificaThreads.add(clienteS);

                                pedidoCliente.getUtilizador().setAtivo(1);
                                conectado = true;
                                utilizadorDB.updateUtilizador(pedidoCliente.getUtilizador());
                                utilizadorThread = pedidoCliente.getUtilizador();
                                respostaSaida = pedidoCliente;
                                respostaSaida.setUtilizador(pedidoCliente.getUtilizador());
                                respostaSaida.setMensagem("Login aceite");
                            } else {
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Credencias incorretas");
                            }
                        }
                        else {
                            respostaSaida = pedidoCliente;
                            respostaSaida.setMensagem("Comando invalido. Efetue o 'registo' ou 'login' primeiro.");
                        }
                    }
                    else {
                        if (pedidoCliente.getMensagem().equalsIgnoreCase("Logout")) {
                            Utilizador utilizador = utilizadorDB.selectUtilizador(pedidoCliente.getUtilizador().getEmail());
                            if (utilizador != null) {
                                respostaSaida = pedidoCliente;
                                utilizador.setAtivo(0);
                                utilizadorDB.updateUtilizador(utilizador);
                                respostaSaida.setMensagem("Logout aceite");
                                EXIT = true;
                            } else {
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Utilizador não encontrado");
                            }
                        }
                        else if (pedidoCliente.getMensagem().contains("Editar dados")) {
                            Utilizador utilizador = utilizadorDB.selectUtilizador(pedidoCliente.getUtilizador().getEmail());
                            if (utilizador != null) {
                                utilizadorDB.updateUtilizador(pedidoCliente.getUtilizador());
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Edicao utilizador bem sucedida");
                            } else {
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Utilizador não encontrado");
                            }
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Criar grupo")) {
                            Utilizador utilizador = utilizadorDB.selectUtilizador(pedidoCliente.getUtilizador().getEmail());
                            if(utilizador != null) {
                                if(grupoDB.insertGrupo(pedidoCliente.getGrupos().get(0),pedidoCliente.getUtilizador().getEmail())) {
                                    if(utilizadorGrupoDB.insertUtilizadorGrupo(utilizador.getEmail(),pedidoCliente.getGrupos().getFirst().getNome())){
                                        respostaSaida = pedidoCliente;
                                        respostaSaida.setMensagem("Grupo criado");
                                    }
                                    else {
                                        respostaSaida = pedidoCliente;
                                        respostaSaida.setMensagem("Utilizador já existe no grupo");//se não estou em erro nunca vai acontecer
                                    }
                                }
                                else {
                                    respostaSaida = pedidoCliente;
                                    respostaSaida.setMensagem("Grupo nao criado");
                                }


                            }else{
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Utilizador nao encontrado");
                            }
                        }
                        else if (pedidoCliente.getMensagem().equalsIgnoreCase("Apagar grupo")) {
                            if(despesaDB.temDespesas(pedidoCliente.getUtilizador().getGrupoAtual().getNome()) && despesaPagadoresDB.temDespesasPendentes(pedidoCliente.getUtilizador().getGrupoAtual().getNome())) {
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Grupo tem despesas pendentes");
                            } else {
                                if(grupoDB.deleteGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome())){
                                    if(conviteDB.removeTodosConvitesPorGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome()) ||
                                            !conviteDB.checkConviteExistanceByGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome())){
                                        if(utilizadorGrupoDB.removeTodosUtilizadoresDoGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome())){
                                            pedidoCliente.getUtilizador().setGrupoAtualPorNome("");
                                            respostaSaida = pedidoCliente;
                                            respostaSaida.setMensagem("Apagar grupo bem sucedido");
                                        }else{
                                            respostaSaida = pedidoCliente;
                                            respostaSaida.setMensagem("Apagar todos utilizadores do grupo mal sucedido");
                                        }
                                    }else{
                                        respostaSaida = pedidoCliente;
                                        respostaSaida.setMensagem("Apagar todos convites mal sucedido");
                                    }
                                } else {
                                    respostaSaida = pedidoCliente;
                                    respostaSaida.setMensagem("Apagar grupo mal sucedido");
                                }
                            }
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Mudar nome grupo")){
                            if(grupoDB.updateNomeGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome(), pedidoCliente.getUtilizador().getGrupoAtual().getNomeProvisorio())){
                                if(conviteDB.updateNomeConvites(pedidoCliente.getUtilizador().getGrupoAtual().getNome(),
                                                             pedidoCliente.getUtilizador().getGrupoAtual().getNomeProvisorio())) {
                                    if (utilizadorGrupoDB.updateNomeGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome(),
                                            pedidoCliente.getUtilizador().getGrupoAtual().getNomeProvisorio())) {
                                        pedidoCliente.getUtilizador().setConvites(conviteDB.listarConvitesPendentes(pedidoCliente.getUtilizador().getEmail()));
                                        pedidoCliente.getUtilizador().getGrupoAtual().setNome(pedidoCliente.getUtilizador().getGrupoAtual().getNomeProvisorio());
                                        respostaSaida = pedidoCliente;
                                        respostaSaida.setMensagem("Mudanca nome bem sucedida");
                                    } else {
                                        respostaSaida = pedidoCliente;
                                        respostaSaida.setMensagem("Tabela utilizadorGrupo nao atualizada");
                                    }
                                }else{
                                    respostaSaida=pedidoCliente;
                                    respostaSaida.setMensagem("Tabela Convites nao atualizada");
                                }
                                //metodo para alterar todos os utilizadorGrupo
                                //atualizar pedido cliente
                                respostaSaida=pedidoCliente;
                                respostaSaida.setMensagem("Mudar nome grupo bem sucedido");
                            }
                            else{
                                respostaSaida=pedidoCliente;
                                respostaSaida.setMensagem("Mudar nome grupo mal sucedido");
                            }
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Escolher grupo")){
                            if (utilizadorGrupoDB.selectUtilizadorNoGrupo(pedidoCliente.getUtilizador().getEmail(), pedidoCliente.getUtilizador().getGrupoAtual().getNome())) {
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Grupo escolhido");
                            } else {
                                pedidoCliente.getUtilizador().getGrupoAtual().setNome("");
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Utilizador não pertence ao grupo");
                            }
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Sair grupo")){//quando estiverem adicionados as despesas e os pagamentos
                            // temos de verificar se existem pagamentos pendentes antes de deixar sair todo
                            if(utilizadorGrupoDB.removeUtilizadorGrupo(pedidoCliente.getUtilizador().getEmail(),
                                                            pedidoCliente.getUtilizador().getGrupoAtual().getNome()))
                            {
                                List<Utilizador> ul = utilizadorGrupoDB.selectUtilizadoresPorGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome());
                                if(ul.isEmpty()){
                                    if(grupoDB.deleteGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome())){
                                        if(conviteDB.removeTodosConvitesPorGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome())||
                                                !conviteDB.checkConviteExistanceByGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome())){
                                            if(utilizadorGrupoDB.removeTodosUtilizadoresDoGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome()))
                                            {
                                                pedidoCliente.getUtilizador().setGrupoAtualPorNome("");
                                                respostaSaida = pedidoCliente;
                                                respostaSaida.setMensagem("Saida grupo bem sucedida");
                                            }else{
                                                respostaSaida = pedidoCliente;
                                                respostaSaida.setMensagem("Apagar todos utilizadores do grupo mal sucedido");
                                            }
                                        }else{
                                            respostaSaida = pedidoCliente;
                                            respostaSaida.setMensagem("Apagar todos convites mal sucedido");
                                        }
                                    }else{
                                        respostaSaida = pedidoCliente;
                                        respostaSaida.setMensagem("Apagar grupo mal sucedido");
                                    }
                                }else{
                                    pedidoCliente.getUtilizador().setGrupoAtualPorNome("");
                                    respostaSaida = pedidoCliente;
                                    respostaSaida.setMensagem("Saida grupo bem sucedida");
                                }

                            }
                             else {
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Saida Grupo mal sucedida");
                            }
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Ver grupos")){
                            List<Grupo> gruposUtilizador = utilizadorGrupoDB.selectGruposPorUtilizador(pedidoCliente.getUtilizador().getEmail());
                            if (!gruposUtilizador.isEmpty()) {
                                pedidoCliente.getUtilizador().setGrupos(gruposUtilizador);
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Ver grupos bem sucedido");
                            } else {
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Ver grupos mal sucedido");
                            }
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Enviar convite grupo")){
                            if(utilizadorDB.selectUtilizador(pedidoCliente.getConvites().getFirst().getDestinatario())!=null) {
                                if(!pedidoCliente.getConvites().getFirst().getDestinatario().equalsIgnoreCase(pedidoCliente.getConvites().getFirst().getRemetente())) {
                                    if (!utilizadorGrupoDB.selectUtilizadorNoGrupo(pedidoCliente.getConvites().getFirst().getDestinatario(), pedidoCliente.getConvites().getFirst().getNomeGrupo())) {
                                        if (!conviteDB.checkConviteExistance(pedidoCliente.getConvites().getFirst())) {
                                            conviteDB.insertInvite(pedidoCliente.getConvites().getFirst());
                                            respostaSaida = pedidoCliente;
                                            respostaSaida.setMensagem("Convite feito com sucesso");
                                            synchronized (lock) {
                                                Servidor.EMAILSEND = pedidoCliente.getConvites().getFirst().getDestinatario();
                                                Servidor.NOMEGRUPO = pedidoCliente.getConvites().getFirst().getNomeGrupo();
                                                Servidor.EMAILREMETENTE = pedidoCliente.getConvites().getFirst().getRemetente();
                                                lock.notify();//assinalar thread
                                            }
                                        } else {
                                            respostaSaida = pedidoCliente;
                                            respostaSaida.setMensagem("Convite repetido");
                                        }
                                    } else {
                                        respostaSaida = pedidoCliente;
                                        respostaSaida.setMensagem("Utilizador ja esta no grupo");
                                    }
                                }else{
                                    respostaSaida = pedidoCliente;
                                    respostaSaida.setMensagem("Nao pode enviar convites a si mesmo");
                                }
                            }else{
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Destinatario Convite inexistente");
                            }
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Ver convites")){
                            pedidoCliente.setConvites(conviteDB.listarConvitesPendentes(pedidoCliente.getUtilizador().getEmail()));
                            if(!pedidoCliente.getConvites().isEmpty()){
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Lista de convites");
                            }else{
                                respostaSaida=pedidoCliente;
                                respostaSaida.setMensagem("Lista de convites vazia");
                            }
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Aceitar convite")){
                            if(!pedidoCliente.getUtilizador().getConvites().isEmpty()){
                                //ir buscar o convite
                                //colocar  destinatario no grupo
                                //enviar notificacao ao remetente
                                //apagar convite
                                Convite convite = pedidoCliente.getUtilizador().getConvitePorEstado("Aceite");
                                utilizadorGrupoDB.insertUtilizadorGrupo(pedidoCliente.getUtilizador().getEmail(),convite.getNomeGrupo());
                                conviteDB.removeConvite(convite.getDestinatario(),convite.getNomeGrupo());
                                pedidoCliente.getUtilizador().setConvites(conviteDB.listarConvitesPendentes(pedidoCliente.getUtilizador().getEmail()));
                                respostaSaida=pedidoCliente;
                                respostaSaida.setMensagem("Convite Aceite com sucesso");
                            }else{
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Não existem convites");
                            }
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Rejeitar convite")){
                            if(!pedidoCliente.getUtilizador().getConvites().isEmpty()){
                                //ir buscar o convite
                                //enviar notificacao ao remetente
                                //apagar convite
                                Convite convite = pedidoCliente.getUtilizador().getConvitePorEstado("Rejeitado");
                                conviteDB.removeConvite(convite.getDestinatario(),convite.getNomeGrupo());
                                pedidoCliente.getUtilizador().setConvites(conviteDB.listarConvitesPendentes(pedidoCliente.getUtilizador().getEmail()));
                                respostaSaida=pedidoCliente;
                                respostaSaida.setMensagem("Convite Rejeitado com sucesso");
                            }else{
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Não existem convites");
                            }
                        }else if(pedidoCliente.getMensagem().equalsIgnoreCase("Inserir despesa")){
                            respostaSaida = pedidoCliente;
                            int despesaID = despesaDB.inserirDespesa(pedidoCliente.getDespesa().getFirst().getDescricao(), pedidoCliente.getDespesa().getFirst().getValor(), pedidoCliente.getDespesa().getFirst().getData(), pedidoCliente.getUtilizador().getGrupoAtual().getNome(), pedidoCliente.getUtilizador().getEmail());
                            if(despesaID!= -1){
                                List<String> emails = utilizadorGrupoDB.selectEmailsDoGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome());
                                if(emails != null){
                                    double valor = pedidoCliente.getDespesa().getFirst().getValor() / emails.size();
                                    if(despesaPagadoresDB.inserirDespesaPagadores(despesaID,emails,valor,"Pendente", pedidoCliente.getUtilizador().getEmail())){
                                        System.out.println("Despesa inserida com sucesso");
                                        System.out.println("Despesa inserida com sucesso");
                                        respostaSaida.setMensagem("Despesa inserida com sucesso");
                                    }else{
                                        respostaSaida.setMensagem("Erro a distribuir a despesa pelos membros do grupo");
                                    }
                                }else{
                                    respostaSaida.setMensagem("Erro a identificar os membros do grupo");
                                }
                            }else{
                                respostaSaida.setMensagem("Erro a inserir despesa");
                            }
                        }else if(pedidoCliente.getMensagem().equalsIgnoreCase("Total gastos")){
                            respostaSaida = pedidoCliente;
                            double valorTotal = despesaDB.calcularTotalDespesas(pedidoCliente.getUtilizador().getGrupoAtual().getNome());
                            if(valorTotal != -1 && valorTotal != 0){
                                pedidoCliente.setMensagem("Total de gastos do grupo atual: "+ valorTotal);
                            }else if(valorTotal == 0){
                                pedidoCliente.setMensagem("Grupo ainda nao tem gastos");
                            }
                            else{
                                respostaSaida.setMensagem("Erro a calcular o total de gastos");
                            }
                        }else if(pedidoCliente.getMensagem().equalsIgnoreCase("Historio despesas")){
                            ArrayList<Despesas> despesas =  despesaDB.getDespesasPorGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome());
                            respostaSaida = pedidoCliente;
                            if(despesas != null){
                                respostaSaida.setDespesa(despesas);
                                respostaSaida.setMensagem("Historio de despesas");
                            }else{
                                respostaSaida.setMensagem("Erro ao puxar o historio de despesas");
                            }
                        }else if(pedidoCliente.getMensagem().equalsIgnoreCase("Exportar csv")){
                            respostaSaida = pedidoCliente;
                            ArrayList<Despesas> despesaCsv = despesaDB.getDespesasPorNomeGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome());
                            if(despesaCsv !=null){
                                despesaCsv = despesaPagadoresDB.preencherUtilizadoresPartilhados(despesaCsv);
                                if(despesaCsv != null){
                                    List<Utilizador> utilizadoresGrupo = utilizadorGrupoDB.selectUtilizadoresPorGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome());
                                    if(utilizadoresGrupo != null){
                                        String caminhoFicheiroCSV = "src/Servidor/despesas.csv";
                                        FuncoesServidor.exportarDespesasParaCSV(despesaCsv, pedidoCliente.getUtilizador().getGrupoAtual().getNome(), utilizadoresGrupo, caminhoFicheiroCSV);
                                        respostaSaida.setMensagem("CSV gerado com sucesso");
                                    }else{
                                        System.out.println("Erro a selecionar os utilizadores do grupo");
                                        respostaSaida.setMensagem("Erro a selecionar os utilizadores do grupo");
                                    }
                                }else{
                                    System.out.println("Erro ao selecionar os utilizadores com quem partilha despesa");
                                    respostaSaida.setMensagem("Erro ao selecionar os utilizadores com quem partilha despesa");
                                }
                            }else{
                                System.out.println("Erro a selecionar as despesas");
                                respostaSaida.setMensagem("Erro ao selecionar as despesas");
                            }
                        }else if(pedidoCliente.getMensagem().equalsIgnoreCase("Eliminar despesa")){
                            respostaSaida = pedidoCliente;
                            ArrayList<Despesas> despesas = despesaDB.getDespesasPorGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome());
                            if(despesas != null){
                                respostaSaida.setDespesa(despesas);
                                respostaSaida.setMensagem("Sucesso, escolha a despesa por id");
                            }else{
                                System.out.println("Erro ao selecionar as despesas");
                                respostaSaida.setMensagem("Erro ao selecionar as despesas");
                            }
                        }else if(pedidoCliente.getMensagem().contains("Eliminar despesa com id")){
                            respostaSaida = pedidoCliente;
                            String mensagem = pedidoCliente.getMensagem();
                            String[] partes = mensagem.split(" ");
                            int id = Integer.parseInt(partes[partes.length - 1]);
                            if(despesaDB.eliminarDespesaPorId(id)){
                                if(despesaPagadoresDB.eliminarDespesasPagadoresPorIdDespesa(id)) {
                                    respostaSaida.setMensagem("Sucesso a eliminar uma despesa");
                                }else{
                                    System.out.println("Erro ao eliminar despesa com id nos pagadores");
                                    respostaSaida.setMensagem("Erro a eliminar a despesa nos pagadores");
                                }
                            }else{
                                System.out.println("Erro ao eliminar despesa com id");
                                respostaSaida.setMensagem("Erro a eliminar a despesa");
                            }
                        }else if(pedidoCliente.getMensagem().equalsIgnoreCase("Editar despesa")){
                            respostaSaida = pedidoCliente;
                            ArrayList<Despesas> despesas = despesaDB.getDespesasPorGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome());
                            if(despesas != null){
                                respostaSaida.setDespesa(despesas);
                                if(despesas.isEmpty()){
                                    respostaSaida.setMensagem("Nao tem despesas");
                                }else{
                                    respostaSaida.setMensagem("Sucesso, escolha a despesa por id para edicao");
                                }
                            }else{
                                System.out.println("Erro ao selecionar as despesas para editar");
                                respostaSaida.setMensagem("Erro ao selecionar as despesas para editar");
                            }
                        } else if(pedidoCliente.getMensagem().contains("Editar despesa com id")){
                            respostaSaida = pedidoCliente;
                            String mensagem = pedidoCliente.getMensagem();
                            String[] partes = mensagem.split(" ");
                            int id = Integer.parseInt(partes[partes.length - 1]);
                            if(despesaDB.atualizaDespesa(id, pedidoCliente.getDespesa().getFirst().getDescricao(),pedidoCliente.getDespesa().getFirst().getValor(),pedidoCliente.getDespesa().getFirst().getData())){
                                if(despesaPagadoresDB.atualizarValorDivida(id,pedidoCliente.getDespesa().getFirst().getValor())) {
                                    respostaSaida.setMensagem("Sucesso a editar dados da despesa");
                                }else{
                                    respostaSaida.setMensagem("Insucesso a atualizar os valores em divida dos membros");
                                }
                            }else{
                                respostaSaida.setMensagem("Insucesso a editar os dados da despesa");
                            }
                        } else if(pedidoCliente.getMensagem().equalsIgnoreCase("Fazer pagamento")) {
                            respostaSaida = pedidoCliente;
                            int idDespesa = pedidoCliente.getUtilizador().getPagamentoAtual().getIdDespesa();
                            double valorDivida = despesaPagadoresDB.getDividaPorId(idDespesa, pedidoCliente.getUtilizador().getEmail());
                            double valorFinal = valorDivida - pedidoCliente.getUtilizador().getPagamentoAtual().getValorPagamento();
                            if(valorFinal <= 0) {
                                valorFinal = 0;
                                if(despesaPagadoresDB.atualizarEstadoDivida(idDespesa, pedidoCliente.getUtilizador().getEmail(), "Pago")) {
                                    if(despesaPagadoresDB.atualizarValorDespesa(idDespesa, valorFinal, pedidoCliente.getUtilizador().getEmail())) {
                                        pagamentoDB.inserirPagamento(pedidoCliente.getUtilizador().getEmail(),
                                                pedidoCliente.getUtilizador().getEmail(),
                                                pedidoCliente.getUtilizador().getPagamentoAtual().getValorPagamento(),
                                                pedidoCliente.getUtilizador().getPagamentoAtual().getGrupoNome());
                                        respostaSaida.setMensagem("Pagamento completo com sucesso");
                                    } else {
                                        respostaSaida.setMensagem("Erro a efetuar pagamento");
                                    }
                                } else {
                                    respostaSaida.setMensagem("Erro a efetuar pagamento");
                                }
                            } else {
                                if(despesaPagadoresDB.atualizarValorDespesa(idDespesa, valorFinal, pedidoCliente.getUtilizador().getEmail())) {
                                    pagamentoDB.inserirPagamento(pedidoCliente.getUtilizador().getEmail(),
                                            pedidoCliente.getUtilizador().getEmail(),
                                            pedidoCliente.getUtilizador().getPagamentoAtual().getValorPagamento(),
                                            pedidoCliente.getUtilizador().getPagamentoAtual().getGrupoNome());
                                    respostaSaida.setMensagem("Pagamento efetuado com sucesso");
                                } else {
                                    respostaSaida.setMensagem("Erro a efetuar pagamento");
                                }
                            }
                        }
                    }
                    Oout.writeObject(respostaSaida);
                    Oout.flush();

                    if(EXIT){
                        clienteSocket.close();
                        break;
                    }

                    List<Utilizador> listaUtilizadores = utilizadorDB.selectTodosUtilizadores();

                    // Itera sobre a lista de utilizadores e imprime seus dados
                    for (Utilizador utilizador : listaUtilizadores) {
                        System.out.println("Nome: " + utilizador.getNome());
                        System.out.println("Email: " + utilizador.getEmail());
                        System.out.println("Telefone: " + utilizador.getTelefone());
                        System.out.println("Password: " + utilizador.getPassword());
                        System.out.println("Ativo: " + utilizador.getAtivo());
                        System.out.println("---------------");
                    }

                } catch (SocketException e) {
                    System.out.println("Cliente desconectado inesperadamente: " + clienteSocket.getRemoteSocketAddress());
                    if (utilizadorThread != null) {
                        utilizadorThread.setAtivo(0);
                        utilizadorDB.updateUtilizador(utilizadorThread);
                        if(utilizadorThread.getEmail()!=null)
                            System.out.println("Utilizador " + utilizadorThread.getEmail() + " marcado como inativo.");
                    }
                    running = false;
                } catch (EOFException e) {
                    System.out.println("Fim da conexão com o cliente: " + clienteSocket.getRemoteSocketAddress());
                    if (utilizadorThread != null) {
                        utilizadorThread.setAtivo(0);
                        utilizadorDB.updateUtilizador(utilizadorThread);
                        if(utilizadorThread.getEmail()!=null)
                            System.out.println("Utilizador " + utilizadorThread.getEmail() + " marcado como inativo.");
                    }
                    running = false; // Encerrar a thread no fim da conexão
                } catch (ClassNotFoundException | IOException e) {
                    System.out.println("Erro na comunicação com o cliente:\n\t" + e);
                    running = false; // Encerrar a thread em caso de erro
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
        } finally {
            try{
                 if(clienteSocket != null && !clienteSocket.isClosed()) {
                     clienteSocket.close();
                 }
            } catch (IOException e) {
                System.out.println("Erro ao fechar o socket do cliente:\n\t" + e);
            }
        }
    }
}

class heartBeats implements Runnable{
    //THREAD PARA EMITIR HEARTBEATS DE 10 EM 10 SEGUNDOS
    static final int PORTOBACKUPUDP = 4444;

    @Override
    public void run(){
        try(MulticastSocket multiSocket = new MulticastSocket(PORTOBACKUPUDP)) {
            DatagramPacket dgram;
            InetAddress groupAdress;
            groupAdress = InetAddress.getByName("230.44.44.44");
            NetworkInterface nif;
            try {
                nif = NetworkInterface.getByInetAddress(InetAddress.getByName(String.valueOf(groupAdress))); //230.44.44.44
            } catch (SocketException | NullPointerException | UnknownHostException | SecurityException ex) {
                nif = NetworkInterface.getByName("wlan0");
            }

            multiSocket.joinGroup(new InetSocketAddress(groupAdress, PORTOBACKUPUDP),nif);
            ServerBackUpSupport messageBackUp = new ServerBackUpSupport(5001);  //PREENCHE LOGO O PORTO PARA FAZER HEARTBEATS

            while(true){
                Thread.sleep(10000);
                //Colocar aqui a versao => messageBackUp.setVersao();
                try (ByteArrayOutputStream Bout = new ByteArrayOutputStream();
                     ObjectOutputStream Oout = new ObjectOutputStream(Bout)) {
                    Oout.writeObject(messageBackUp);
                    dgram = new DatagramPacket(Bout.toByteArray(), Bout.size(), groupAdress, PORTOBACKUPUDP);
                }
                multiSocket.send(dgram);
            }
        }catch (NumberFormatException e) {
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        } catch (SocketException e) {
            System.out.println("Ocorreu um erro ao nivel do serverSocket TCP:\n\t" + e);
        } catch (IOException e) {
            System.out.println("Ocorreu um erro no acesso ao serverSocket:\n\t" + e);
        } catch (InterruptedException e) {
            System.out.println("Sleep interrompido");
        }
    }
}

class conectaServidoresBackup implements Runnable {
    // THREAD QUE ESPERA OS SERVIDORES BACKUP SE CONECTAREM DIRETAMENTE VIA TCP PARA RECEBEREM AS ATUALIZAÇÕES DA BASE DE DADOS
    private static final int PORTOBACKUPTCP = 5001;
    private final File databaseFile = new File("./src/BaseDeDados/BaseDados.db");
    public static final int MAX_SIZE = 4000;

    @Override
    public void run() {
        byte[] fileChunk = new byte[MAX_SIZE];
        int nbytes;

        // Validar o ficheiro da base de dados
        if (!databaseFile.exists()) {
            System.out.println("O ficheiro " + databaseFile.getAbsolutePath() + " não existe!");
            return;
        }

        if (!databaseFile.canRead()) {
            System.out.println("Sem permissões de leitura no ficheiro " + databaseFile.getAbsolutePath() + "!");
            return;
        }

        try (ServerSocket socketBackup = new ServerSocket(PORTOBACKUPTCP)) {
            System.out.println("Servidor backup à escuta no porto " + PORTOBACKUPTCP + "...");

            while (true) {
                try {
                    Socket backupServerSocket = socketBackup.accept();
                    System.out.println("Conexão estabelecida com o servidor backup: " + backupServerSocket.getInetAddress().getHostAddress());

                    //VAI LER O FICHEIRO DA BASE DE DADOS E ENVIA POR CHUNKS
                    try (FileInputStream fileInputStream = new FileInputStream(databaseFile)) {
                        OutputStream out = backupServerSocket.getOutputStream();

                        do {
                            nbytes = fileInputStream.read(fileChunk);
                            if (nbytes != -1) {
                                out.write(fileChunk, 0, nbytes);
                                out.flush();
                            }
                        } while (nbytes > 0);

                        System.out.println("Transferência concluída para o servidor backup.");
                    } catch (IOException e) {
                        System.out.println("Erro ao ler ou enviar o ficheiro: " + e.getMessage());
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Tempo esgotado: o servidor de backup não respondeu.");
                } catch (IOException e) {
                    System.out.println("Erro de I/O ao lidar com o servidor de backup: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao criar o ServerSocket: " + e.getMessage());
        }
    }
}

public class Servidor {
    public static String EMAILSEND;
    public static String NOMEGRUPO;
    public static String EMAILREMETENTE;
    public static String queryBackupServer;
    public static volatile boolean encerraServidor = false;

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.out.println("\nNumero de argumentos incorrecto\n");
            return;
        }

        int servicePort = Integer.parseInt(args[0]);
        final String DBPATH = args[1];
        java.sql.Connection connection = ConnectDB.criarBaseDeDados(DBPATH);

        List<notificaThread> clienteSockets = Collections.synchronizedList(new ArrayList<>());
        Object lock = new Object();

        try (ServerSocket serverSocket = new ServerSocket(servicePort)) {

            Thread notifica = new Thread(new notificaCliente(clienteSockets, lock));
            notifica.setDaemon(true);
            notifica.start();

            Thread encerraServidorTh = new Thread(new encerraServerThread(clienteSockets, lock, serverSocket));
            encerraServidorTh.setDaemon(true);
            encerraServidorTh.start();

            Thread heartbeats = new Thread(new heartBeats());
            heartbeats.setDaemon(true);
            heartbeats.start();

            Thread conectaServidoresBackup = new Thread(new conectaServidoresBackup());
            conectaServidoresBackup.setDaemon(true);
            conectaServidoresBackup.start();


            System.out.println("Server iniciado...\n");

            while (true) {
                if(encerraServidor)
                    break;
                try {
                    Socket clientSocket = serverSocket.accept();
                    Thread td = new Thread(new processaClienteThread(clientSocket,connection, clienteSockets, lock));
                    td.setDaemon(true);
                    td.start();
                }catch (IOException e){
                    if (encerraServidor) {

                        System.out.println("Servidor encerrado com sucesso.");
                        break;
                    } else {
                        System.out.println("Ocorreu um erro no acesso ao serverSocket:\n\t" + e);
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        } catch (SocketException e) {
            System.out.println("Ocorreu um erro ao nivel do serverSocket TCP:\n\t" + e);
        } catch (IOException e) {
            System.out.println("Ocorreu um erro no acesso ao serverSocket:\n\t" + e);
        }
    }
}