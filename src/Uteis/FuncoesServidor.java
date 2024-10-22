package Uteis;
import Entidades.Despesas;
import Entidades.Utilizador;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FuncoesServidor {

    public static void exportarDespesasParaCSV(ArrayList<Despesas> despesasList, String nomeGrupo, List<Utilizador> elementosGrupo, String caminhoFicheiroCSV) {
        try (FileWriter writer = new FileWriter(caminhoFicheiroCSV)) {

            writer.append("\"Nome do grupo\"\n");
            writer.append("\"").append(nomeGrupo).append("\"\n");
            writer.append("\"Elementos\"\n");

            for (var elemento : elementosGrupo) {
                writer.append("\"").append(elemento.getEmail()).append("\";");
            }
            writer.append("\n");

            writer.append("\"Data\"; \"Responsável pelo registo da despesa\"; \"Valor\"; \"Pago por\"; \"A dividir com\"\n");

            for (Despesas despesa : despesasList) {
                writer.append("\"").append(despesa.getData().toString()).append("\";");
                writer.append("\"").append(despesa.getPagador()).append("\";");
                writer.append("\"").append(String.valueOf(despesa.getValor())).append("\";");
                writer.append("\"").append(despesa.getPagador()).append("\";");

                ArrayList<String> utilizadoresPartilhados = despesa.getUtilizadoresPartilhados();
                for (String utilizador : utilizadoresPartilhados) {
                    writer.append("\"").append(utilizador).append("\";");
                }

                writer.append("\n");
            }

            System.out.println("Arquivo CSV gerado com sucesso: " + caminhoFicheiroCSV);
        } catch (IOException e) {
            System.out.println("Erro ao criar arquivo CSV: " + e.getMessage());
        }
    }
}
