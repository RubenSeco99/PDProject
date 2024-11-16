package Servidor;

import Comunicacao.Comunicacao;
import Entidades.Utilizador;

import java.util.List;

class NotificaCliente implements Runnable{

    List<notificaThread> clienteSockets;
    private final Object lock;

    public NotificaCliente(List<notificaThread> clienteSockets, Object lock){
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
