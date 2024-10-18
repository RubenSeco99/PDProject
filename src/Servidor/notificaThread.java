package Servidor;

import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class notificaThread {
    private String email;
    private Socket socket;


    ObjectOutputStream Oout;

    public notificaThread(String email, Socket socket){
        this.email = email;
        this.socket = socket;
    }

    public notificaThread(Socket socket){this.socket = socket;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
    public Socket getSocket() {return socket;}
    public void setSocket(Socket socket) {this.socket = socket;}
    public ObjectOutputStream getOout() {return Oout;}
    public void setOout(ObjectOutputStream oout) {Oout = oout;}
}
