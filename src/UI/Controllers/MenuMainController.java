package UI.Controllers;

import Cliente.ClienteFacade;

public class MenuMainController  {
    private ClienteFacade facade;


    public MenuMainController() {}

    public MenuMainController(ClienteFacade facade) {
        this.facade = facade;
    }

    public void setFacade(ClienteFacade facade) {
        this.facade = facade;
    }
}
