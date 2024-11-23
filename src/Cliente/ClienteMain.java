package Cliente;

import java.io.IOException;

public class ClienteMain {
        public static void main(String[] args) {
            ClienteFacade clienteFacade = new ClienteFacade(args[0], Integer.parseInt(args[1]));
            ClienteUI clientUI = new ClienteUI(clienteFacade);
            clientUI.start();
        }
    }

