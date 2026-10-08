package assembler.generador;

import assembler.memoria.PoolCadenas;
import assembler.memoria.TablaVariables;
import c3d.InferidorTipos;
import enums.TipoDato;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class OperadoresRiscV {

    private final ASMContexto asmContexto;
    private final TablaVariables tablaVariables;
    private final PoolCadenas poolCadenas;
    private final Map<String, TipoDato> tipos;


    private boolean esDecimal(String nombre) {
        return InferidorTipos.tipoDe(nombre, tipos) == TipoDato.DECIMAL;
    }

    public void cargarEntero(String reg, String operando) {

        if (operando == null) {
            asmContexto.instruccion("li " + reg + ", 0");
            return;
        }

        String valor = InferidorTipos.normalizar(operando);

        if (esEntero(valor)) {
            asmContexto.instruccion("li " + reg + ", " + valor);
        } else if ("true".equals(valor)) {
            asmContexto.instruccion("li " + reg + ", 1");
        } else if ("false".equals(valor) || "null".equals(valor)) {
            asmContexto.instruccion("li " + reg + ", 0");
        } else if (esCaracter(valor)) {
            asmContexto.instruccion("li " + reg + ", " + codigoCaracter(valor));
        } else if (valor.startsWith("\"")) {
            asmContexto.instruccion("la " + reg + ", " + poolCadenas.etiqueta(valor));
        } else if (recibeDecimal(valor)) {
            asmContexto.instruccion("li " + reg + ", " + (int) Double.parseDouble(valor));
        } else if (Registros.esRegistroC3D(valor)) {
            asmContexto.instruccion("mv " + reg + ", " + Registros.deC3D(valor));
        } else {
            String etiqueta = tablaVariables.etiqueta(valor);
            asmContexto.instruccion("la " + reg + ", " + etiqueta);
            if (esDecimal(valor)) {
                asmContexto.instruccion("fld ft2, 0(" + reg + ")");
                asmContexto.instruccion("fcvt.w.d " + reg + ", ft2");
            } else {
                asmContexto.instruccion("lw " + reg + ", 0(" + reg + ")");
            }
        }
    }

    public void cargarDecimal(String freg, String operando) {

        String valor = InferidorTipos.normalizar(operando);

        if (recibeDecimal(valor)) {
            asmContexto.instruccion("la t6, " + tablaVariables.constanteDecimal(valor));
            asmContexto.instruccion("fld " + freg + ", 0(t6)");
        } else if (esEntero(valor)) {
            asmContexto.instruccion("li t6, " + valor);
            asmContexto.instruccion("fcvt.d.w " + freg + ", t6");
        } else if (Registros.esRegistroC3D(valor)) {
            asmContexto.instruccion("fcvt.d.w " + freg + ", " + Registros.deC3D(valor));
        } else {
            String etiqueta = tablaVariables.etiqueta(valor);
            asmContexto.instruccion("la t6, " + etiqueta);
            if (esDecimal(valor)) {
                asmContexto.instruccion("fld " + freg + ", 0(t6)");
            } else {
                asmContexto.instruccion("lw t6, 0(t6)");
                asmContexto.instruccion("fcvt.d.w " + freg + ", t6");
            }
        }
    }

    public void guardarEntero(String reg, String destino) {

        if (destino == null) {
            return;
        }
        if (Registros.esRegistroC3D(destino)) {
            asmContexto.instruccion("mv " + Registros.deC3D(destino) + ", " + reg);
            return;
        }

        asmContexto.instruccion("la t5, " + tablaVariables.etiqueta(destino));
        asmContexto.instruccion("sw " + reg + ", 0(t5)");
    }

    public void guardarDecimal(String freg, String destino) {

        if (destino == null) {
            return;
        }

        asmContexto.instruccion("la t5, " + tablaVariables.etiqueta(destino));
        asmContexto.instruccion("fsd " + freg + ", 0(t5)");
    }

    private static boolean esEntero(String valor) {

        if (valor == null || valor.isEmpty()) {
            return false;
        }

        int inicio = 0;

        if (valor.charAt(0) == '-') {

            if (valor.length() == 1) {
                return false;
            }

            inicio = 1;
        }

        for (int i = inicio; i < valor.length(); i++) {

            if (!Character.isDigit(valor.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean recibeDecimal(String valor) {

        if (valor == null || valor.isEmpty()) {
            return false;
        }

        int inicio = 0;
        boolean punto = false;
        boolean digito = false;

        if (valor.charAt(0) == '-') {

            if (valor.length() == 1) {
                return false;
            }

            inicio = 1;
        }

        for (int i = inicio; i < valor.length(); i++) {

            char c = valor.charAt(i);

            if (Character.isDigit(c)) {
                digito = true;
            } else if (c == '.' && !punto) {
                punto = true;
            } else {
                return false;
            }
        }

        return punto && digito;
    }

    private static boolean esCaracter(String v) {
        return v.length() >= 3 && v.charAt(0) == '\'' && v.charAt(v.length() - 1) == '\'';
    }

    private static int codigoCaracter(String v) {
        String c = v.substring(1, v.length() - 1);

        if (c.startsWith("\\") && c.length() >= 2) {
            return switch (c) {
                case "\\n" -> 10;
                case "\\t" -> 9;
                case "\\r" -> 13;
                case "\\0" -> 0;
                case "\\\\" -> 92;
                case "\\'" -> 39;
                default -> c.charAt(1);
            };
        }
        return c.codePointAt(0);
    }

}
