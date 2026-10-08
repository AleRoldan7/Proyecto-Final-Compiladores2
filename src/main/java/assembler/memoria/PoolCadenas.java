package assembler.memoria;

import assembler.generador.ASMContexto;

import java.util.LinkedHashMap;
import java.util.Map;

public class PoolCadenas {

    private final Map<String, String> etiquetas = new LinkedHashMap<>();

    public String etiqueta(String literal) {
        String existente = etiquetas.get(literal);
        if (existente != null) {
            return existente;
        }
        String nueva = "str_" + etiquetas.size();
        etiquetas.put(literal, nueva);
        return nueva;
    }

    public void volcar(ASMContexto asmContexto) {
        if (etiquetas.isEmpty()) {
            return;
        }
        asmContexto.dato("# ---- cadenas ----");
        for (Map.Entry<String, String> e : etiquetas.entrySet()) {
            asmContexto.dato(e.getValue() + ": .asciz " + e.getKey());
        }
    }
}
