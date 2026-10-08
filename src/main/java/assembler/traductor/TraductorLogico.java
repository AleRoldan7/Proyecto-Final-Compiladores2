package assembler.traductor;

import assembler.generador.ContextoRiscV;
import assembler.generador.TraductorCuarteta;
import c3d.Cuarteta;

import java.util.Set;

public class TraductorLogico implements TraductorCuarteta {

    private static final Set<String> OPERADORES = Set.of("==", "!=", "<", ">", "<=", ">=", "&&", "||", "not");


    @Override
    public boolean soporta(String operador) {
        return OPERADORES.contains(operador);
    }

    @Override
    public void traducir(Cuarteta cuarteta, ContextoRiscV contextoRiscV) {

        String operador = cuarteta.getOperador();
        String izquierda = cuarteta.getArg1();
        String derecha = cuarteta.getArg2();
        String resultado = cuarteta.getResultado();

        switch (operador) {

            case "not" -> {
                contextoRiscV.operadoresRiscV.cargarEntero("t0", izquierda);
                contextoRiscV.asmContexto.instruccion("seqz t0, t0");
                contextoRiscV.operadoresRiscV.guardarEntero("t0", resultado);
            }

            case "&&" -> {
                contextoRiscV.operadoresRiscV.cargarEntero("t0", izquierda);
                contextoRiscV.operadoresRiscV.cargarEntero("t1", derecha);
                contextoRiscV.asmContexto.instruccion("snez t0, t0");
                contextoRiscV.asmContexto.instruccion("snez t1, t1");
                contextoRiscV.asmContexto.instruccion("and t0, t0, t1");
                contextoRiscV.operadoresRiscV.guardarEntero("t0", resultado);
            }

            case "||" -> {
                contextoRiscV.operadoresRiscV.cargarEntero("t0", izquierda);
                contextoRiscV.operadoresRiscV.cargarEntero("t1", derecha);
                contextoRiscV.asmContexto.instruccion("or t0, t0, t1");
                contextoRiscV.asmContexto.instruccion("snez t0, t0");
                contextoRiscV.operadoresRiscV.guardarEntero("t0", resultado);
            }

            default -> comparar(operador, izquierda, derecha, resultado, contextoRiscV);

        }
    }

    private void comparar(String operador, String izquierda, String derecha, String resultado, ContextoRiscV contextoRiscV) {

        // texto == texto
        if (("==".equals(operador) || "!=".equals(operador)) && contextoRiscV.esTexto(izquierda) && contextoRiscV.esTexto(derecha)) {
            contextoRiscV.operadoresRiscV.cargarEntero("a0", izquierda);
            contextoRiscV.operadoresRiscV.cargarEntero("a1", derecha);
            contextoRiscV.asmContexto.instruccion("jal ra, rt_texto_igual");
            if ("!=".equals(operador)) {
                contextoRiscV.asmContexto.instruccion("xori a0, a0, 1");
            }
            contextoRiscV.operadoresRiscV.guardarEntero("a0", resultado);
            return;
        }

        if (contextoRiscV.esDecimal(izquierda) || contextoRiscV.esDecimal(derecha)) {
            contextoRiscV.operadoresRiscV.cargarDecimal("ft0", izquierda);
            contextoRiscV.operadoresRiscV.cargarDecimal("ft1", derecha);
            switch (operador) {
                case "==" -> contextoRiscV.asmContexto.instruccion("feq.d t0, ft0, ft1");
                case "!=" -> {
                    contextoRiscV.asmContexto.instruccion("feq.d t0, ft0, ft1");
                    contextoRiscV.asmContexto.instruccion("xori t0, t0, 1");
                }
                case "<" -> contextoRiscV.asmContexto.instruccion("flt.d t0, ft0, ft1");
                case ">" -> contextoRiscV.asmContexto.instruccion("flt.d t0, ft1, ft0");
                case "<=" -> contextoRiscV.asmContexto.instruccion("fle.d t0, ft0, ft1");
                default -> contextoRiscV.asmContexto.instruccion("fle.d t0, ft1, ft0");   // >=
            }
            contextoRiscV.operadoresRiscV.guardarEntero("t0", resultado);
            return;
        }

        contextoRiscV.operadoresRiscV.cargarEntero("t0", izquierda);
        contextoRiscV.operadoresRiscV.cargarEntero("t1", derecha);

        switch (operador) {
            case "==" -> {
                contextoRiscV.asmContexto.instruccion("sub t0, t0, t1");
                contextoRiscV.asmContexto.instruccion("seqz t0, t0");
            }
            case "!=" -> {
                contextoRiscV.asmContexto.instruccion("sub t0, t0, t1");
                contextoRiscV.asmContexto.instruccion("snez t0, t0");
            }
            case "<" -> contextoRiscV.asmContexto.instruccion("slt t0, t0, t1");
            case ">" -> contextoRiscV.asmContexto.instruccion("slt t0, t1, t0");
            case "<=" -> {
                contextoRiscV.asmContexto.instruccion("slt t0, t1, t0");
                contextoRiscV.asmContexto.instruccion("xori t0, t0, 1");
            }
            default -> {   // >=
                contextoRiscV.asmContexto.instruccion("slt t0, t0, t1");
                contextoRiscV.asmContexto.instruccion("xori t0, t0, 1");
            }
        }

        contextoRiscV.operadoresRiscV.guardarEntero("t0", resultado);
    }
}
