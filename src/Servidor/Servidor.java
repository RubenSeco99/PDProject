package Servidor;

import BaseDeDados.*;
import Cliente.Comunicacao;
import Entidades.Convite;
import Entidades.Grupo;
import Entidades.Utilizador;
import Uteis.Funcoes;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Prototipo
class encerraServerThread implements Runnable{

    List<notificaThread> arraySocketsClientes;
    final Object lock;
    ServerSocket serverS;
    public encerraServerThread(List<notificaThread> arraySocketsClientes, Object lock, ServerSocket serverS){
        this.arraySocketsClientes = arraySocketsClientes;
        this.lock = lock;
        this.serverS = serverS;
    }

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
                    respostaSaida.setMensagem("Convite recebido");
                    System.out.println("Notificação enviada");
                    for(var cli: clienteSockets){
                        if(cli.getEmail().equals("miguel@isec.pt")){
                            cli.getOout().writeObject(respostaSaida);
                            cli.getOout().flush();
                        }
                    }
                    Servidor.EMAILSEND = "";
                    Servidor.NOMEGRUPO = "";
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
        //new objeto convite
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
                            if(grupoDB.deleteGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome())){
                                if(conviteDB.removeTodosConvitesPorGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome())){
                                    if(utilizadorGrupoDB.removeTodosUtilizadoresDoGrupo(pedidoCliente.getUtilizador().getGrupoAtual().getNome())){
                                        pedidoCliente.getUtilizador().setGrupoAtualPorNome("");
                                        respostaSaida = pedidoCliente;
                                        respostaSaida.setMensagem("Apagar todos utilizadores do grupo bem sucedido");
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
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Sair grupo")){//TODO
                            //
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Grupo escolhido");
                             //else {
                                pedidoCliente.getUtilizador().getGrupoAtual().setNome("");
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Utilizador não pertence ao grupo");
                            //}
                        }
                        else if(pedidoCliente.getMensagem().equalsIgnoreCase("Ver grupos")){
                            List<Grupo> gruposUtilizador= utilizadorGrupoDB.selectGruposPorUtilizador(pedidoCliente.getUtilizador().getEmail());
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
                                if(!utilizadorGrupoDB.selectUtilizadorNoGrupo(pedidoCliente.getConvites().getFirst().getDestinatario(),pedidoCliente.getConvites().getFirst().getNomeGrupo()))
                                {
                                    if (!conviteDB.checkConviteExistance(pedidoCliente.getConvites().getFirst())) {
                                        conviteDB.insertInvite(pedidoCliente.getConvites().getFirst());
                                        respostaSaida = pedidoCliente;
                                        respostaSaida.setMensagem("Convite feito com sucesso");
                                        synchronized (lock) {
                                            Servidor.EMAILSEND = pedidoCliente.getConvites().getFirst().getDestinatario();
                                            Servidor.NOMEGRUPO = pedidoCliente.getConvites().getFirst().getNomeGrupo();
                                            lock.notify();//assinalar thread
                                        }
                                    } else {
                                        respostaSaida = pedidoCliente;
                                        respostaSaida.setMensagem("Convite repetido");
                                    }
                                }
                                else{
                                    respostaSaida = pedidoCliente;
                                    respostaSaida.setMensagem("Utilizador ja esta no grupo");
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
                                synchronized (lock) {
                                    lock.notify();//assinalar thread
                                }
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
                                synchronized (lock) {
                                    lock.notify();//assinalar thread
                                }
                                conviteDB.removeConvite(convite.getDestinatario(),convite.getNomeGrupo());
                                pedidoCliente.getUtilizador().setConvites(conviteDB.listarConvitesPendentes(pedidoCliente.getUtilizador().getEmail()));
                                respostaSaida=pedidoCliente;
                                respostaSaida.setMensagem("Convite Rejeitado com sucesso");
                            }else{
                                respostaSaida = pedidoCliente;
                                respostaSaida.setMensagem("Não existem convites");
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

public class Servidor {
    public static String EMAILSEND;
    public static String NOMEGRUPO;
    public static volatile boolean encerraServidor = false;
    public static void main(String[] args) throws IOException {

        java.sql.Connection connection = ConnectDB.criarBaseDeDados();
        ArrayList<Thread> arrayThreads = new ArrayList<>();
        List<notificaThread> clienteSockets = Collections.synchronizedList(new ArrayList<>());
        Object lock = new Object();

        int servicePort = Integer.parseInt(args[0]);
        //String caminhoBD = args[1]; //para uso futuro

        try (ServerSocket serverSocket = new ServerSocket(servicePort)) {

            //Thread notifica = new Thread(new notificaCliente(clienteSockets, lock));
            //notifica.start();
            Thread encerraServidorTh = new Thread(new encerraServerThread(clienteSockets, lock, serverSocket));
            encerraServidorTh.start();

            System.out.println("Server iniciado...\n");
            //Lançar thread para enviar base de dados atualizada ao servidor de backup atualizando a versão
            while (true) {
                if(encerraServidor)
                    break;
                try {
                    Socket clientSocket = serverSocket.accept();
                    Thread td = new Thread(new processaClienteThread(clientSocket,connection, clienteSockets, lock));
                    td.start();
                }catch (IOException e){
                    if (encerraServidor) {
                        // Se o servidor estiver sendo encerrado, saia do loop principal
                        System.out.println("Servidor encerrado com sucesso.");
                        break;
                    } else {
                        System.out.println("Ocorreu um erro no acesso ao serverSocket:\n\t" + e);
                    }
                }
                //arrayThreads.add(td);
            }
        } catch (NumberFormatException e) {
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        } catch (SocketException e) {
            System.out.println("Ocorreu um erro ao nivel do serverSocket TCP:\n\t" + e);
        } catch (IOException e) {
            System.out.println("Ocorreu um erro no acesso ao serverSocket:\n\t" + e);
        }
        finally {
            //ConnectDB.fecharBaseDeDados();
            /*try{
                for(var thread : arrayThreads) {
                    thread.join();
                }
                notifica.join();
                encerraServidorTh.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/
        }
    }
}