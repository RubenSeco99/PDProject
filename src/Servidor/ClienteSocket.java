package Servidor;

import java.net.Socket;

public class ClienteSocket {
    private String email;
    private Socket socket;

    public ClienteSocket(String email,Socket socket){
        this.email = email;
        this.socket = socket;
    }

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
    public Socket getSocket() {return socket;}
    public void setSocket(Socket socket) {this.socket = socket;}
}
