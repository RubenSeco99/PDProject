package Cliente;

import java.io.IOException;

public class ClienteMain {
        public static void main(String[] args) {
            ClienteFacade clienteFacade = new ClienteFacade("localhost", 5000);
            ClienteUI clientUI = new ClienteUI(clienteFacade);
            clientUI.start();
        }
    }

