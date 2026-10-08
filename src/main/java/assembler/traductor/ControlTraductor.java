package assembler.traductor;

import assembler.generador.ContextoRiscV;
import assembler.generador.TraductorCuarteta;
import c3d.Cuarteta;
import exceptiones.ComparacionException;

import java.util.Set;

public class ControlTraductor implements TraductorCuarteta {

    private static final Set<String> SIMPLES = Set.of("label", "goto", "comment", "halt");

    @Override
    public boolean soporta(String operador) {
        return SIMPLES.contains(operador) || operador.startsWith("if_");
    }

    @Override
    public void traducir(Cuarteta cuarteta, ContextoRiscV contextoRiscV) {

        String op = cuarteta.getOperador();
        String destino = cuarteta.getResultado();

        switch (op) {
            case "label" -> contextoRiscV.asmContexto.etiqueta(destino);
            case "goto" -> contextoRiscV.asmContexto.instruccion("j " + destino);
            case "comment" -> contextoRiscV.asmContexto.comentario(String.valueOf(destino));
            case "halt" -> { }
            case "if_true" -> {
                contextoRiscV.operadoresRiscV.cargarEntero("t0", cuarteta.getArg1());
                saltoLejano(contextoRiscV, "beqz t0, ", destino);
            }
            case "if_false" -> {
                contextoRiscV.operadoresRiscV.cargarEntero("t0", cuarteta.getArg1());
                saltoLejano(contextoRiscV, "bnez t0, ", destino);
            }
            default -> saltoComparando(op.substring(3), cuarteta, contextoRiscV);
        }
    }

    private void saltoComparando(String cmp, Cuarteta cuarteta, ContextoRiscV contextoRiscV) {

        String izq = cuarteta.getArg1();
        String der = cuarteta.getArg2();
        String destino = cuarteta.getResultado();

        // texto == texto
        if (("==".equals(cmp) || "!=".equals(cmp)) && contextoRiscV.esTexto(izq) && contextoRiscV.esTexto(der)) {
            contextoRiscV.operadoresRiscV.cargarEntero("a0", izq);
            contextoRiscV.operadoresRiscV.cargarEntero("a1", der);
            contextoRiscV.asmContexto.instruccion("jal ra, rt_texto_igual");
            saltoLejano(contextoRiscV, "==".equals(cmp) ? "beqz a0, " : "bnez a0, ", destino);
            return;
        }

        // decimales: se calcula 0/1 y se salta si es 1
        if (contextoRiscV.esDecimal(izq) || contextoRiscV.esDecimal(der)) {
            contextoRiscV.operadoresRiscV.cargarDecimal("ft0", izq);
            contextoRiscV.operadoresRiscV.cargarDecimal("ft1", der);
            switch (cmp) {
                case "<" -> contextoRiscV.asmContexto.instruccion("flt.d t0, ft0, ft1");
                case ">" -> contextoRiscV.asmContexto.instruccion("flt.d t0, ft1, ft0");
                case "<=" -> contextoRiscV.asmContexto.instruccion("fle.d t0, ft0, ft1");
                case ">=" -> contextoRiscV.asmContexto.instruccion("fle.d t0, ft1, ft0");
                case "==" -> contextoRiscV.asmContexto.instruccion("feq.d t0, ft0, ft1");
                case "!=" -> {
                    contextoRiscV.asmContexto.instruccion("feq.d t0, ft0, ft1");
                    contextoRiscV.asmContexto.instruccion("xori t0, t0, 1");
                }
                default -> throw new ComparacionException("Comparación desconocida: if_" + cmp);
            }
            saltoLejano(contextoRiscV, "beqz t0, ", destino);
            return;
        }

        // enteros: rama inversa corta + j (los b* solo alcanzan ±4 KB)
        contextoRiscV.operadoresRiscV.cargarEntero("t0", izq);
        contextoRiscV.operadoresRiscV.cargarEntero("t1", der);

        String inversa = switch (cmp) {
            case "<" -> "bge";
            case ">" -> "ble";
            case "<=" -> "bgt";
            case ">=" -> "blt";
            case "==" -> "bne";
            case "!=" -> "beq";
            default -> throw new ComparacionException("Comparación desconocida: if_" + cmp);
        };

        saltoLejano(contextoRiscV, inversa + " t0, t1, ", destino);
    }

    private void saltoLejano(ContextoRiscV contextoRiscV, String ramaInversa, String destino) {
        String omitir = contextoRiscV.asmContexto.nuevaEtiqueta("omitir");
        contextoRiscV.asmContexto.instruccion(ramaInversa + omitir);
        contextoRiscV.asmContexto.instruccion("j " + destino);
        contextoRiscV.asmContexto.etiqueta(omitir);
    }
}
