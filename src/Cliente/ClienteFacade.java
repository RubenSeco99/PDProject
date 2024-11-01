package Cliente;

import Entidades.Convite;
import Entidades.Despesas;
import Entidades.Pagamento;
import Entidades.Utilizador;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.Date;

public class ClienteFacade {
    private final ClienteModel clienteModel;
    private Utilizador utilizador;

    public ClienteFacade(String serverAddress, int serverPort) {
        clienteModel = new ClienteModel(serverAddress, serverPort, this);
        utilizador = new Utilizador();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        clienteModel.addPropertyChangeListener(listener);
    }

    public void login(String email, String password) {
        utilizador = clienteModel.getUtilizadorAtualizado();
        utilizador.setEmail(email);
        utilizador.setPassword(password);
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Login");
        clienteModel.enviarMensagem(comunicacao);
    }

    public void register(String nome, String email, String password) {
        utilizador.setNome(nome);
        utilizador.setEmail(email);
        utilizador.setPassword(password);
        utilizador.setTelefone(911111111);
        utilizador.setAtivo(0);
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Registo");
        clienteModel.enviarMensagem(comunicacao);
    }

    public void createGroup(String groupName) {
        utilizador = clienteModel.getUtilizadorAtualizado();
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Criar grupo");
        comunicacao.setGrupos(groupName);
        clienteModel.enviarMensagem(comunicacao);
    }

    public void chooseGroup(String groupName) {
        utilizador = clienteModel.getUtilizadorAtualizado();
        utilizador.getGrupoAtual().setNome(groupName);
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Escolher grupo");
        clienteModel.enviarMensagem(comunicacao);
    }

    public void viewGroups() {
        utilizador = clienteModel.getUtilizadorAtualizado();
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Ver grupos");
        clienteModel.enviarMensagem(comunicacao);
    }

    public void viewTotalExpenses(){
        utilizador = clienteModel.getUtilizadorAtualizado();
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Total gastos");
        clienteModel.enviarMensagem(comunicacao);
    }

    public void viewExpenses() {
        utilizador = clienteModel.getUtilizadorAtualizado();
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Historio despesas");
        clienteModel.enviarMensagem(comunicacao);
    }

    public void addExpense(String descricao, double valor, Date data) {
        utilizador = clienteModel.getUtilizadorAtualizado();
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Inserir despesa");
        comunicacao.getDespesa().add(new Despesas(descricao, valor, data));
        clienteModel.enviarMensagem(comunicacao);
    }

    public void updateExpense(String descricao, double valor, Date data, int id) {
        utilizador = clienteModel.getUtilizadorAtualizado();
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Editar despesa com id " + id);
        comunicacao.getDespesa().add(new Despesas(descricao, valor, data));
        clienteModel.enviarMensagem(comunicacao);
    }

    public void deleteExpense(int id) {
        utilizador = clienteModel.getUtilizadorAtualizado();
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Eliminar despesa com id " + id);
        clienteModel.enviarMensagem(comunicacao);
    }

    public void payDebt(double valor, int id) {
        utilizador = clienteModel.getUtilizadorAtualizado();
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Fazer pagamento com id");
        comunicacao.getUtilizador().setPagamentoAtual(new Pagamento());
        comunicacao.getUtilizador().getPagamentoAtual().setValorPagamento(valor);
        comunicacao.getUtilizador().getPagamentoAtual().setIdDespesa(id);
        comunicacao.getUtilizador().getPagamentoAtual().setGrupoNome(utilizador.getGrupoAtual().getNome());
        clienteModel.enviarMensagem(comunicacao);
    }

    public void acceptInvite(String nomeGrupo) {
        utilizador = clienteModel.getUtilizadorAtualizado();
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Aceitar convite");
        comunicacao.setGrupos(nomeGrupo);
        clienteModel.enviarMensagem(comunicacao);
    }

    public void rejectInvite(String nomeGrupo) {
        utilizador = clienteModel.getUtilizadorAtualizado();
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Rejeitar convite");
        comunicacao.setGrupos(nomeGrupo);
        clienteModel.enviarMensagem(comunicacao);
    }

    public void sendGroupInvite(String email) {
        utilizador = clienteModel.getUtilizadorAtualizado();
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Enviar convite grupo");
        comunicacao.setConvite( new Convite(utilizador.getGrupoAtual().getNome(), utilizador.getEmail(), email,"pendente"));
        clienteModel.enviarMensagem(comunicacao);
    }

    public void updateGroupName(String novoNome) {
        utilizador = clienteModel.getUtilizadorAtualizado();
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Mudar nome grupo");
        comunicacao.getUtilizador().getGrupoAtual().setNomeProvisorio(novoNome);
        clienteModel.enviarMensagem(comunicacao);
    }

    public void deleteGroup() {
        utilizador = clienteModel.getUtilizadorAtualizado();
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Apagar grupo");
        clienteModel.enviarMensagem(comunicacao);
    }

    public void exportExpensesToCSV() {
        utilizador = clienteModel.getUtilizadorAtualizado();
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Exportar csv");
        clienteModel.enviarMensagem(comunicacao);
    }

    public void exitGroup() {
        utilizador = clienteModel.getUtilizadorAtualizado();
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Sair grupo");
        clienteModel.enviarMensagem(comunicacao);
    }

    public void updateUserName(String nome) {
        utilizador = clienteModel.getUtilizadorAtualizado();
        utilizador.setNome(nome);
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Atualizar Nome Utilizador");
        clienteModel.enviarMensagem(comunicacao);
    }

    public void updateUserPassword(String password) {
        utilizador = clienteModel.getUtilizadorAtualizado();
        utilizador.setPassword(password);
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Atualizar Password Utilizador");
        clienteModel.enviarMensagem(comunicacao);
    }

    public void updateUserPhone(String telefone) {
        utilizador = clienteModel.getUtilizadorAtualizado();
        utilizador.setTelefone(Integer.parseInt(telefone));
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Atualizar Telefone Utilizador");
        clienteModel.enviarMensagem(comunicacao);
    }

    public void logout() {
        utilizador = clienteModel.getUtilizadorAtualizado();
        Comunicacao comunicacao = new Comunicacao(utilizador);
        comunicacao.setMensagem("Logout");
        clienteModel.enviarMensagem(comunicacao);
    }

    public boolean isRegistado() {
        return clienteModel.isRegistado();
    }

    public Utilizador getUtilizador() {
        return clienteModel.getUtilizadorAtualizado();
    }
}
