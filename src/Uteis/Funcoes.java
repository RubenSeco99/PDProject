package Uteis;
import Utilizador.Utilizador;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Funcoes {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    // Precompiled Pattern for efficiency
    private static final Pattern pattern = Pattern.compile(EMAIL_REGEX);

    public static boolean verificaRegisto (ArrayList<Utilizador> arrayList, String email){
        for(var u: arrayList){
            if(u.getEmail().equals(email)){
                return false;
            }
        }
        return true;
    }
    public static boolean isValidEmail(String email) {
        if (email == null)
            return false;
        return pattern.matcher(email).matches();
    }

    public static boolean verificaLogin (ArrayList<Utilizador> arrayList, String email, String password){
        for(var u: arrayList){
            if(u.getEmail().equals(email) && u.getPassword().equals(password)){
                return true;
            }
        }
        return false;
    }

}
