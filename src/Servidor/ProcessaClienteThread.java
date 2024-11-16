package Servidor;

import BaseDeDados.*;
import Comunicacao.Comunicacao;
import Entidades.*;
import ServidorBackup.ServerBackUpSupport;
import Uteis.Funcoes;
import Uteis.FuncoesServidor;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ProcessaClienteThread implements Runnable {

    private Socket clienteSocket;
    private boolean running;
    private UtilizadorDB utilizadorDB;
    private GrupoDB grupoDB;
    private ConviteDB conviteDB;
    private UtilizadorGrupoDB utilizadorGrupoDB;
    private DespesaDB despesaDB;
    private DespesaPagadoresDB despesaPagadoresDB;
    private PagamentoDB pagamentoDB;
    private boolean conectado;
    private boolean EXIT = false;
    List<notificaThread> notificaThreads;
    private final Object lock;
    private ServerBackUpSupport messageSBS;
    private boolean isBackupInProgress;
    private final Object databaseLock;

    public ProcessaClienteThread(Socket clienteSocket, Connection connection, List<notificaThread> clienteSockets, Object lock, ServerBackUpSupport messageSBS, boolean isBackupInProgress, Object databaseLock) {
        this.messageSBS         = messageSBS;
        this.utilizadorDB       = new UtilizadorDB(connection, messageSBS);
        this.grupoDB            = new GrupoDB(connection, messageSBS);
        this.utilizadorGrupoDB  = new UtilizadorGrupoDB(connection, messageSBS);
        this.conviteDB          = new ConviteDB(connection, messageSBS);
        this.despesaDB          = new DespesaDB(connection, messageSBS);
        this.despesaPagadoresDB = new DespesaPagadoresDB(connection, messageSBS);
        this.pagamentoDB        = new PagamentoDB(connection, messageSBS);
        this.clienteSocket      = clienteSocket;
        this.running            = true;
        this.notificaThreads    = clienteSockets;
        this.lock               = lock;
        this.isBackupInProgress = isBackupInProgress;
        this.databaseLock       = databaseLock;
    }

    @Override
    public void run() {

        try {
            clienteSocket.setSoTimeout(60000);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

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

                    if(isBackupInProgress){
                        synchronized (databaseLock){
                            databaseLock.wait();
                        }
                    }

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

                                try {
                                    clienteSocket.setSoTimeout(0); // Remove o timeout
                                } catch (SocketException e) {
                                    throw new RuntimeException(e);
                                }

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

                                try {
                                    clienteSocket.setSoTimeout(0); // Remove o timeout
                                } catch (SocketException e) {
                                    throw new RuntimeException(e);
                                }

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
                                        pagamentoDB.updateNomeGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome(),
                                                pedidoCliente.getUtilizador().getGrupoAtual().getNomeProvisorio());
                                        despesaDB.updateNomeGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome(),
                                                pedidoCliente.getUtilizador().getGrupoAtual().getNomeProvisorio());
                                        pedidoCliente.getUtilizador().setConvites(conviteDB.listarConvitesPendentes(pedidoCliente.getUtilizador().getEmail()));
                                        pedidoCliente.getUtilizador().getGrupoAtual().setNome(pedidoCliente.getUtilizador().getGrupoAtual().getNomeProvisorio());
                                        respostaSaida = pedidoCliente;
                                        respostaSaida.setMensagem("Mudanca nome bem sucedida");
                                        System.out.println("Nome atual: "+respostaSaida.getUtilizador().getGrupoAtual().getNome());
                                        System.out.println("Nome novo: "+respostaSaida.getUtilizador().getGrupoAtual().getNomeProvisorio());
                                    } else {
                                        respostaSaida = pedidoCliente;
                                        respostaSaida.setMensagem("Tabela utilizadorGrupo nao atualizada");
                                    }
                                }else{
                                    if (utilizadorGrupoDB.updateNomeGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome(),
                                            pedidoCliente.getUtilizador().getGrupoAtual().getNomeProvisorio())) {
                                        pagamentoDB.updateNomeGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome(),
                                                pedidoCliente.getUtilizador().getGrupoAtual().getNomeProvisorio());
                                        despesaDB.updateNomeGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome(),
                                                pedidoCliente.getUtilizador().getGrupoAtual().getNomeProvisorio());
                                        pedidoCliente.getUtilizador().setConvites(conviteDB.listarConvitesPendentes(pedidoCliente.getUtilizador().getEmail()));
                                        pedidoCliente.getUtilizador().getGrupoAtual().setNome(pedidoCliente.getUtilizador().getGrupoAtual().getNomeProvisorio());
                                        respostaSaida = pedidoCliente;
                                        respostaSaida.setMensagem("Mudanca nome bem sucedida");
                                        System.out.println("Nome atual: "+respostaSaida.getUtilizador().getGrupoAtual().getNome());
                                        System.out.println("Nome novo: "+respostaSaida.getUtilizador().getGrupoAtual().getNomeProvisorio());
                                    } else {
                                        respostaSaida = pedidoCliente;
                                        respostaSaida.setMensagem("Tabela utilizadorGrupo nao atualizada");
                                    }
                                }
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
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Sair grupo")) {
                            ArrayList<Despesas> despesasList = despesaDB.getDespesasPorNomeGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome());
                            if (!despesasList.isEmpty() && despesaPagadoresDB.checkUserDivida(despesasList, pedidoCliente.getUtilizador().getEmail())) {
                                respostaSaida.setMensagem("Nao pode sair, ainda tem pagamentos pendentes");
                            } else {
                                if (utilizadorGrupoDB.removeUtilizadorGrupo(pedidoCliente.getUtilizador().getEmail(),
                                        pedidoCliente.getUtilizador().getGrupoAtual().getNome())) {
                                    List<Utilizador> ul = utilizadorGrupoDB.selectUtilizadoresPorGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome());
                                    if (ul.isEmpty()) {
                                        if (grupoDB.deleteGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome())) {
                                            if (conviteDB.removeTodosConvitesPorGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome()) ||
                                                    !conviteDB.checkConviteExistanceByGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome())) {
                                                if (utilizadorGrupoDB.removeTodosUtilizadoresDoGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome())) {
                                                    pedidoCliente.getUtilizador().setGrupoAtualPorNome("");
                                                    respostaSaida = pedidoCliente;
                                                    respostaSaida.setMensagem("Saida grupo bem sucedida");
                                                } else {
                                                    respostaSaida = pedidoCliente;
                                                    respostaSaida.setMensagem("Apagar todos utilizadores do grupo mal sucedido");
                                                }
                                            } else {
                                                respostaSaida = pedidoCliente;
                                                respostaSaida.setMensagem("Apagar todos convites mal sucedido");
                                            }
                                        } else {
                                            respostaSaida = pedidoCliente;
                                            respostaSaida.setMensagem("Apagar grupo mal sucedido");
                                        }
                                    } else {
                                        pedidoCliente.getUtilizador().setGrupoAtualPorNome("");
                                        respostaSaida = pedidoCliente;
                                        respostaSaida.setMensagem("Saida grupo bem sucedida");
                                    }

                                } else {
                                    respostaSaida = pedidoCliente;
                                    respostaSaida.setMensagem("Saida Grupo mal sucedida");
                                }
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
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Inserir despesa")){
                            respostaSaida = pedidoCliente;
                            int despesaID = despesaDB.inserirDespesa(pedidoCliente.getDespesa().getFirst().getDescricao(), pedidoCliente.getDespesa().getFirst().getValor(), pedidoCliente.getDespesa().getFirst().getData(), pedidoCliente.getUtilizador().getGrupoAtual().getNome(), pedidoCliente.getUtilizador().getEmail());
                            if(despesaID!= -1){
                                List<String> emails = utilizadorGrupoDB.selectEmailsDoGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome());
                                System.out.println(pedidoCliente.getUtilizador().getGrupoAtual().getNome());
                                if(emails != null){
                                    double valor = pedidoCliente.getDespesa().getFirst().getValor() / emails.size();
                                    if(despesaPagadoresDB.inserirDespesaPagadores(despesaID,emails,valor,"Pendente", pedidoCliente.getUtilizador().getEmail(),pedidoCliente.getUtilizador().getEmail())){
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
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Total gastos")){
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
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Historio despesas")){
                            ArrayList<Despesas> despesas =  despesaDB.getDespesasPorGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome());
                            respostaSaida = pedidoCliente;
                            if(despesas != null){
                                respostaSaida.setDespesa(despesas);
                                respostaSaida.setMensagem("Historio de despesas");
                            }else{
                                respostaSaida.setMensagem("Erro ao puxar o historio de despesas");
                            }
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Exportar csv")){
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
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Eliminar despesa")){
                            respostaSaida = pedidoCliente;
                            ArrayList<Despesas> despesas = despesaDB.getDespesasPorGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome());
                            if(despesas != null){
                                respostaSaida.setDespesa(despesas);
                                respostaSaida.setMensagem("Sucesso, escolha a despesa por id");
                            }else{
                                System.out.println("Erro ao selecionar as despesas");
                                respostaSaida.setMensagem("Erro ao selecionar as despesas");
                            }
                        }
                        else if(pedidoCliente.getMensagem().contains("Eliminar despesa com id")){
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
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Editar despesa")){
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
                        }
                        else if(pedidoCliente.getMensagem().contains("Editar despesa com id")){
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
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Fazer pagamento")) {
                            respostaSaida = pedidoCliente;
                            ArrayList<Divida> dividas = despesaPagadoresDB.getDividasPorEmail(pedidoCliente.getUtilizador().getEmail());
                            if(dividas != null){
                                respostaSaida.setDividas(dividas);
                                if(dividas.isEmpty()){
                                    respostaSaida.setMensagem("Nao tem dividas");
                                }else{
                                    respostaSaida.setMensagem("Sucesso, escolha a divida por id para efetuar pagamento");
                                }
                            }else{
                                System.out.println("Erro ao selecionar as despesas para efetuar pagamento");
                                respostaSaida.setMensagem("Erro ao selecionar as despesas para efetuar pagamento");
                            }
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Fazer pagamento com id")) {
                            respostaSaida = pedidoCliente;
                            int idDespesa = pedidoCliente.getUtilizador().getPagamentoAtual().getIdDespesa();
                            if(despesaDB.checkDespesaExiste(idDespesa)) {
                                double valorDivida = despesaPagadoresDB.getDividaPorId(idDespesa, pedidoCliente.getUtilizador().getEmail());
                                double valorPagamento = pedidoCliente.getUtilizador().getPagamentoAtual().getValorPagamento();
                                double remanescente=0;
                                if(valorPagamento>valorDivida)
                                    remanescente = valorPagamento - valorDivida;
                                double valorFinal = valorDivida - pedidoCliente.getUtilizador().getPagamentoAtual().getValorPagamento();
                                if (valorFinal <= 0) {
                                    valorFinal = 0;
                                    if (despesaPagadoresDB.atualizarEstadoDivida(idDespesa, pedidoCliente.getUtilizador().getEmail(), "Pago")) {
                                        if (despesaPagadoresDB.atualizarValorDespesa(idDespesa, valorFinal, pedidoCliente.getUtilizador().getEmail())) {
                                            pagamentoDB.inserirPagamento(pedidoCliente.getUtilizador().getEmail(),
                                                    despesaDB.getDonoDespesa(idDespesa),
                                                    pedidoCliente.getUtilizador().getPagamentoAtual().getValorPagamento()-remanescente,
                                                    pedidoCliente.getUtilizador().getPagamentoAtual().getGrupoNome());
                                            if (remanescente==0)
                                                respostaSaida.setMensagem("Pagamento completo com sucesso");
                                            else
                                                respostaSaida.setMensagem("Pagamento completo com sucesso, troco "+remanescente);
                                        } else {
                                            respostaSaida.setMensagem("Erro a efetuar pagamento");
                                        }
                                    } else {
                                        respostaSaida.setMensagem("Erro a efetuar pagamento");
                                    }
                                } else {
                                    if (despesaPagadoresDB.atualizarValorDespesa(idDespesa, valorFinal, pedidoCliente.getUtilizador().getEmail())) {
                                        pagamentoDB.inserirPagamento(pedidoCliente.getUtilizador().getEmail(),
                                                despesaDB.getDonoDespesa(idDespesa),
                                                pedidoCliente.getUtilizador().getPagamentoAtual().getValorPagamento(),
                                                pedidoCliente.getUtilizador().getPagamentoAtual().getGrupoNome());
                                        respostaSaida.setMensagem("Pagamento efetuado com sucesso");
                                    } else {
                                        respostaSaida.setMensagem("Erro a efetuar pagamento");
                                    }
                                }
                            }
                            else{
                                respostaSaida.setMensagem("Id despesa nao existente");
                            }
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Listar pagamentos")){
                            respostaSaida = pedidoCliente;
                            ArrayList<Pagamento> pagamentos = pagamentoDB.getPagamentosPorGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome());
                            if(pagamentos!= null){
                                respostaSaida.setMensagem("Lista de pagamentos");
                                respostaSaida.setPagamentos(pagamentos);
                            }else{
                                respostaSaida.setMensagem("Insucesso a listar pagamentos");
                            }
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Visualizar saldos")){
                            respostaSaida = pedidoCliente;
                            String nomeGrupo = pedidoCliente.getUtilizador().getGrupoAtual().getNome();
                            List<Utilizador> utilizadores = utilizadorGrupoDB.selectUtilizadoresPorGrupo(nomeGrupo);
                            Map<String, Double> gastosTotais = new HashMap<>();
                            Map<String, Double> valoresDevidos = new HashMap<>();
                            Map<String, Map<String, Double>> deveParaCada = new HashMap<>();
                            Map<String, Double> totalReceber = new HashMap<>();
                            Map<String, Map<String, Double>> receberDeCada = new HashMap<>();

                            // Calcular o gasto total por elemento
                            for (Utilizador u : utilizadores) {
                                double gastoTotal = despesaDB.calcularGastoTotalPorUtilizador(u.getEmail(), nomeGrupo);
                                gastosTotais.put(u.getEmail(), gastoTotal);
                            }

                            // Calcular o valor total que cada elemento deve e detalhar quanto deve a cada um
                            for (Utilizador u : utilizadores) {
                                double totalDevido = despesaPagadoresDB.calcularTotalDevidoPorUtilizador(u.getEmail(), nomeGrupo);
                                valoresDevidos.put(u.getEmail(), totalDevido);

                                // Chama o método e atribui o resultado ao mapa
                                Map<String, Double> deveA = despesaPagadoresDB.calcularDeveParaCada(u.getEmail(), nomeGrupo);
                                if (!deveA.isEmpty()) { // Apenas adiciona se houver devedores
                                    deveParaCada.put(u.getEmail(), deveA);
                                }
                            }

                            // Calcular o valor total que cada elemento tem a receber e detalhar quanto tem a receber de cada um
                            for (Utilizador u : utilizadores) {
                                double totalAReceber = pagamentoDB.calcularTotalReceberPorUtilizador(u.getEmail(), nomeGrupo);
                                totalReceber.put(u.getEmail(), totalAReceber);
                                Map<String, Double> receberDe = pagamentoDB.calcularReceberDeCada(u.getEmail(), nomeGrupo);
                                receberDeCada.put(u.getEmail(), receberDe);
                            }

                            // Montar a resposta com os dados calculados
                            respostaSaida.setMensagem("Saldos do grupo");
                            respostaSaida.setGastosTotais(gastosTotais);
                            respostaSaida.setValoresDevidos(valoresDevidos);
                            respostaSaida.setDeveParaCada(deveParaCada);
                            respostaSaida.setTotalReceber(totalReceber);
                            respostaSaida.setReceberDeCada(receberDeCada);
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
                        if(utilizadorThread.getAtivo() == 1 && utilizadorThread.getEmail() != null) {
                            utilizadorThread.setAtivo(0);
                            utilizadorDB.updateUtilizador(utilizadorThread);
                            System.out.println("Utilizador " + utilizadorThread.getEmail() + " marcado como inativo.");
                        }
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
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
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
