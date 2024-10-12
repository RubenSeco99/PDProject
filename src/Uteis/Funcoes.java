package Uteis;

import Utilizador.Utilizador;

import java.util.ArrayList;

public class Funcoes {

    public static boolean verificaRegisto (ArrayList<Utilizador> arrayList, String email){
        for(var u: arrayList){
            if(u.getEmail().equals(email)){
                return false;
            }
        }
        return true;
    }



}
