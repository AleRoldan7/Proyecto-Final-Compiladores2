package assembler.traductor;

import assembler.generador.ContextoRiscV;
import assembler.generador.TraductorCuarteta;
import c3d.Cuarteta;
import enums.Registro;
import enums.TipoDato;

import java.util.Set;

public class TraductorAritmetico implements TraductorCuarteta {

    private static final Set<String> OPERADORES = Set.of("+", "-", "*", "/", "%", "neg");

    @Override
    public boolean soporta(String operador) {
        return OPERADORES.contains(operador);
    }

    @Override
    public void traducir(Cuarteta cuarteta, ContextoRiscV contextoRiscV) {

        String op = cuarteta.getOperador();
        String izq = cuarteta.getArg1();
        String der = cuarteta.getArg2();
        String res = cuarteta.getResultado();

        if ("neg".equals(op)) {
            if (contextoRiscV.esDecimal(res) || contextoRiscV.esDecimal(izq)) {
                contextoRiscV.operadoresRiscV.cargarDecimal("ft0", izq);
                contextoRiscV.asmContexto.instruccion("fneg.d ft0, ft0");
                contextoRiscV.operadoresRiscV.guardarDecimal("ft0", res);
            } else {
                contextoRiscV.operadoresRiscV.cargarEntero("t0", izq);
                contextoRiscV.asmContexto.instruccion("neg t0, t0");
                contextoRiscV.operadoresRiscV.guardarEntero("t0", res);
            }
            return;
        }

        if ("+".equals(op) && contextoRiscV.esTexto(res)) {
            concatenar(izq, der, res, contextoRiscV);
            return;
        }

        if (contextoRiscV.esDecimal(res) || contextoRiscV.esDecimal(izq) || contextoRiscV.esDecimal(der)) {

            String instruccion = switch (op) {
                case "+" -> "fadd.d";
                case "-" -> "fsub.d";
                case "*" -> "fmul.d";
                case "/" -> "fdiv.d";
                default -> throw new IllegalStateException("El operador '" + op + "' no está definido para decimales");
            };

            contextoRiscV.operadoresRiscV.cargarDecimal("ft0", izq);
            contextoRiscV.operadoresRiscV.cargarDecimal("ft1", der);
            contextoRiscV.asmContexto.instruccion(instruccion + " ft0, ft0, ft1");
            contextoRiscV.operadoresRiscV.guardarDecimal("ft0", res);
            return;
        }

        String instruccion = switch (op) {
            case "+" -> "add";
            case "-" -> "sub";
            case "*" -> "mul";
            case "/" -> "div";
            default -> "rem";
        };

        contextoRiscV.operadoresRiscV.cargarEntero("t0", izq);
        contextoRiscV.operadoresRiscV.cargarEntero("t1", der);
        contextoRiscV.asmContexto.instruccion(instruccion + " t0, t0, t1");
        contextoRiscV.operadoresRiscV.guardarEntero("t0", res);
    }

    private void concatenar(String izquierda, String derecha, String resultado, ContextoRiscV contextoRiscV) {

        cargarComoTexto(izquierda, contextoRiscV);
        contextoRiscV.asmContexto.instruccion("mv " + Registro.TEXTO_TEMPORAL + ", a0");

        cargarComoTexto(derecha, contextoRiscV);
        contextoRiscV.asmContexto.instruccion("mv a1, a0");
        contextoRiscV.asmContexto.instruccion("mv a0, " + Registro.TEXTO_TEMPORAL);

        contextoRiscV.asmContexto.instruccion("jal ra, rt_concat");
        contextoRiscV.operadoresRiscV.guardarEntero("a0", resultado);
    }

    private void cargarComoTexto(String operando, ContextoRiscV contextoRiscV) {
        contextoRiscV.operadoresRiscV.cargarEntero("a0", operando);

        if (contextoRiscV.tipoDe(operando) == TipoDato.CARACTER) {
            contextoRiscV.asmContexto.instruccion("jal ra, rt_char_a_texto");
        } else if (!contextoRiscV.esTexto(operando)) {
            contextoRiscV.asmContexto.instruccion("jal ra, rt_num_a_texto");
        }
    }
}
