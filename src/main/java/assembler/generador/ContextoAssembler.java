package assembler.generador;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class ContextoAssembler {

    private final StringBuilder data = new StringBuilder();
    private final StringBuilder text = new StringBuilder();
    private final AdminsitradorRegistro administradorRegistro = new AdminsitradorRegistro();

    private final Set<String> variablesDeclaradas = new HashSet<>();
    private final Set<String> stringsDeclarados = new HashSet<>();

    private String funcionActual;
    private int contadorStrings = 0;
    private int contadorLabels = 0;

    public void agregarData(String linea) {
        data.append(linea).append("\n");
    }

    public void agregarTexto(String linea) {
        text.append(linea).append("\n");
    }

    public String nuevoLabel() {
        return ".L" + (contadorLabels++);
    }

    /*DECLARA LA VARIBALE GLOBAL SI NO EXISTE EN .data*/
    public void declararVariable(String nombre) {
        if (variablesDeclaradas.add(nombre)) {
            data.append(nombre).append(": .word 0\n");
        }
    }

    /*DECLARA LOS STRING Y DEVUELVE LA ETIQUETA*/
    public String declararString(String contenido) {
        String etiqueta = "str_" + (contadorStrings++);
        if (stringsDeclarados.add(etiqueta)) {
            data.append(etiqueta).append(": .asciz ").append(contenido).append("\n");
        }
        return etiqueta;
    }

    public String generarCodigo() {
        return "# Generado automáticamente\n"
                + ".data\n"
                + data
                + "\n.text\n"
                + ".globl main\n"
                + text;
    }


}
