package assembler.memoria;

import assembler.generador.ASMContexto;
import enums.Registro;
import enums.TipoDato;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TablaVariables {

    private final Map<String, TipoDato> tipos;
    private final Map<String, String> etiquetas = new LinkedHashMap<>();
    private final Map<String, String> decimales = new LinkedHashMap<>();

    public TablaVariables(Map<String, TipoDato> tipos) {
        this.tipos = tipos;
    }

    public String etiqueta(String nombre) {

        return etiquetas.computeIfAbsent(nombre, n -> {

            if (esTemporal(n)) {
                return "tmp_" + n.substring(1);
            }

            return "var_" + limpiarNombre(n);
        });
    }

    public String constanteDecimal(String literal) {
        String existente = decimales.get(literal);
        if (existente != null) {
            return existente;
        }
        String nueva = "dbl_" + decimales.size();
        decimales.put(literal, nueva);
        return nueva;
    }

    public void volcar(ASMContexto asmContexto) {

        asmContexto.dato("# ---- celda de retorno (entero en +0, decimal en +8) ----");
        asmContexto.dato(".align 3");
        asmContexto.dato("retval: .space 16");

        asmContexto.dato("# ---- temporales y variables ----");
        for (Map.Entry<String, String> e : etiquetas.entrySet()) {
            boolean decimal = tipos.getOrDefault(e.getKey(), TipoDato.DESCONOCIDO) == TipoDato.DECIMAL;
            asmContexto.dato(decimal ? ".align 3" : ".align 2");
            asmContexto.dato(e.getValue() + (decimal ? ": .double 0.0" : ": .word 0"));
        }

        if (!decimales.isEmpty()) {
            asmContexto.dato("# ---- constantes decimales ----");
            for (Map.Entry<String, String> e : decimales.entrySet()) {
                asmContexto.dato(".align 3");
                asmContexto.dato(e.getValue() + ": .double " + e.getKey());
            }
        }
    }

    private boolean esTemporal(String nombre) {

        if (nombre == null || nombre.length() < 2) {
            return false;
        }

        if (nombre.charAt(0) != 't') {
            return false;
        }

        for (int i = 1; i < nombre.length(); i++) {

            if (!Character.isDigit(nombre.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private String limpiarNombre(String nombre) {

        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < nombre.length(); i++) {

            char c = nombre.charAt(i);

            if (Character.isLetterOrDigit(c) || c == '_') {
                resultado.append(c);
            } else {
                resultado.append('_');
            }
        }

        return resultado.toString();
    }
}
