package Cliente;

import Comunicacao.Comunicacao;
import Entidades.Utilizador;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.*;

import static java.lang.System.exit;

public class ClienteModel {
    private Socket socket;
    private ObjectOutputStream Oout;
    private ObjectInputStream Oin;
    private boolean registado;
    private Utilizador utilizadorAtualizado;
    private final PropertyChangeSupport support;
    private Thread listenerThread;


    public ClienteModel(String serverAddress, int serverPort) {
        this.support = new PropertyChangeSupport(this);
        try {
            this.socket = new Socket(serverAddress, serverPort);
            this.Oout = new ObjectOutputStream(socket.getOutputStream());
            this.Oin = new ObjectInputStream(socket.getInputStream());
            this.utilizadorAtualizado = new Utilizador();
            this.registado = false;

            startListening();
        }catch (IOException e){
            System.out.println("Erro a inicializar o ClienteModel " + e);
            exit(0);
        }
    }

    public void enviarMensagem(Comunicacao comunicacao){
        try {
            Oout.writeObject(comunicacao);
            Oout.flush();
            Oout.reset();
        }catch (IOException e){
            System.out.println("Erro ao enviar mensagem" + e);
        }
    }

    public void logout() throws IOException {
        if (listenerThread != null && listenerThread.isAlive()) {
            listenerThread.interrupt();
        }
        socket.close();
    }

    private void startListening() {
        listenerThread = new Thread(new ServerListener());
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Comunicacao resposta = (Comunicacao) Oin.readObject();
                    processarResposta(resposta);
                    support.firePropertyChange("mensagemRecebida", null, resposta);

                }
            } catch (EOFException e) {
                System.out.println("Conexão terminada pelo servidor.");
            } catch (IOException | ClassNotFoundException e) {
                if (!Thread.currentThread().isInterrupted()) {
                    System.out.println("Erro ao receber dados do servidor: " + e.getMessage());
                }
            }
        }

        private void processarResposta(Comunicacao resposta) {
            try {
                if ("Login aceite".equalsIgnoreCase(resposta.getMensagem())) {
                    registado = true;
                    utilizadorAtualizado.setUtilizador(resposta.getUtilizador());
                } else if ("Aceite".equalsIgnoreCase(resposta.getMensagem())) {
                    utilizadorAtualizado = resposta.getUtilizador();
                } else if(resposta.getMensagem().equalsIgnoreCase("Mudanca nome bem sucedida")) {
                    utilizadorAtualizado.getGrupoAtual().setNome(resposta.getUtilizador().getGrupoAtual().getNome());
                    System.out.println("Novo nome: " + utilizadorAtualizado.getGrupoAtual().getNome());
                }else if ("Logout".equalsIgnoreCase(resposta.getMensagem())) {
                    registado = false;
                    logout();
                } else if ("Lista de convites".equalsIgnoreCase(resposta.getMensagem())) {
                    utilizadorAtualizado.setConvites(resposta.getConvites());
                } else if ("Ver grupos bem sucedido".equalsIgnoreCase(resposta.getMensagem())) {
                    utilizadorAtualizado.setGrupos(resposta.getUtilizador().getGrupos());
                } else if ("Historio de despesas".equalsIgnoreCase(resposta.getMensagem())) {
                    utilizadorAtualizado.setDespesas(resposta.getDespesa());
                } else if ("Sucesso, escolha a divida por id para efetuar pagamento".equalsIgnoreCase(resposta.getMensagem())) {
                    utilizadorAtualizado.setDividas(resposta.getDividas());
                } else if ("Servidor em baixo".equalsIgnoreCase(resposta.getMensagem())) {
                    System.out.println("Servidor em baixo, encerrando conexão.");
                    try {
                        logout();
                    } catch (IOException e) {
                        System.out.println("Erro ao fechar a conexão: " + e.getMessage());
                    }
                }
            }catch (IOException e){
                System.out.println("Erro ao receber dados do servidor: " + e.getMessage());
            }
        }
    }

    public Utilizador getUtilizadorAtualizado() {
        return utilizadorAtualizado;
    }

    public boolean isRegistado() {
        return registado;
    }
}
