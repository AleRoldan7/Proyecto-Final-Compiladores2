package assembler.generador;

import c3d.Cuarteta;
import enums.RegistroAssembler;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;

@NoArgsConstructor
public class GeneradorAssembler {

    private final ContextoAssembler ctx = new ContextoAssembler();

    private static final Pattern NUMERO = Pattern.compile("-?\\d+");

    public String generar(List<Cuarteta> cuartetas) {
        ctx.agregarTexto("main:");
        for (Cuarteta c : cuartetas) {
            generarCuarteta(c);
        }
        // Salida limpia al terminar
        ctx.agregarTexto("    li a7, 10");
        ctx.agregarTexto("    ecall");
        return ctx.generarCodigo();
    }

    private boolean esNumero(String s) {
        return s != null && NUMERO.matcher(s).matches();
    }

    /** Carga un operando (variable o inmediato) en un registro y lo retorna */
    private RegistroAssembler cargarOperando(String operando) {
        RegistroAssembler r = ctx.getAdministradorRegistro().obtenerRegistro();
        if (esNumero(operando)) {
            ctx.agregarTexto("    li " + r + ", " + operando);
        } else {
            ctx.declararVariable(operando);
            ctx.agregarTexto("    lw " + r + ", " + operando);
        }
        return r;
    }

    private void generarCuarteta(Cuarteta c) {
        String op = c.getOperador();
        String a1 = c.getArg1();
        String a2 = c.getArg2();
        String res = c.getResultado();

        switch (op) {

            // ---------- Etiquetas y saltos ----------
            case "label" -> ctx.agregarTexto(res + ":");
            case "goto" -> ctx.agregarTexto("    j " + res);
            case "if_true" -> {
                RegistroAssembler r = cargarOperando(a1);
                ctx.agregarTexto("    bnez " + r + ", " + res);
                ctx.getAdministradorRegistro().liberarRegistro(r);
            }
            case "if_false" -> {
                RegistroAssembler r = cargarOperando(a1);
                ctx.agregarTexto("    beqz " + r + ", " + res);
                ctx.getAdministradorRegistro().liberarRegistro(r);
            }

            // ---------- Comparaciones que saltan ----------
            case "if_<", "if_>", "if_<=", "if_>=", "if_==", "if_!=" -> {
                RegistroAssembler r1 = cargarOperando(a1);
                RegistroAssembler r2 = cargarOperando(a2);
                String salto = switch (op) {
                    case "if_<"  -> "blt";
                    case "if_>"  -> "bgt";
                    case "if_<=" -> "ble";
                    case "if_>=" -> "bge";
                    case "if_==" -> "beq";
                    case "if_!=" -> "bne";
                    default -> throw new IllegalStateException();
                };
                ctx.agregarTexto("    " + salto + " " + r1 + ", " + r2 + ", " + res);
                ctx.getAdministradorRegistro().liberarRegistro(r1);
                ctx.getAdministradorRegistro().liberarRegistro(r2);
            }

            // ---------- Asignación ----------
            case "=" -> {
                RegistroAssembler r = cargarOperando(a1);
                ctx.declararVariable(res);
                ctx.agregarTexto("    sw " + r + ", " + res + ", t6");
                ctx.getAdministradorRegistro().liberarRegistro(r);
            }

            // ---------- Operaciones binarias ----------
            case "+", "-", "*", "/", "%" -> {
                RegistroAssembler r1 = cargarOperando(a1);
                RegistroAssembler r2 = cargarOperando(a2);
                RegistroAssembler rd = ctx.getAdministradorRegistro().obtenerRegistro();
                String inst = switch (op) {
                    case "+" -> "add";
                    case "-" -> "sub";
                    case "*" -> "mul";
                    case "/" -> "div";
                    case "%" -> "rem";
                    default -> throw new IllegalStateException();
                };
                ctx.agregarTexto("    " + inst + " " + rd + ", " + r1 + ", " + r2);
                ctx.declararVariable(res);
                ctx.agregarTexto("    sw " + rd + ", " + res + ", t6");
                ctx.getAdministradorRegistro().liberarRegistro(r1);
                ctx.getAdministradorRegistro().liberarRegistro(r2);
                ctx.getAdministradorRegistro().liberarRegistro(rd);
            }

            // ---------- Operaciones unarias ----------
            case "neg" -> {
                RegistroAssembler r = cargarOperando(a1);
                ctx.agregarTexto("    neg " + r + ", " + r);
                ctx.declararVariable(res);
                ctx.agregarTexto("    sw " + r + ", " + res + ", t6");
                ctx.getAdministradorRegistro().liberarRegistro(r);
            }
            case "not" -> {
                RegistroAssembler r = cargarOperando(a1);
                ctx.agregarTexto("    seqz " + r + ", " + r);
                ctx.declararVariable(res);
                ctx.agregarTexto("    sw " + r + ", " + res + ", t6");
                ctx.getAdministradorRegistro().liberarRegistro(r);
            }

            // ---------- Comentarios ----------
            case "comment" -> ctx.agregarTexto("    # " + res);

            // ---------- Print ----------
            case "print" -> {
                // Asumimos enteros por simplicidad
                RegistroAssembler r = cargarOperando(a1);
                ctx.agregarTexto("    mv a0, " + r);
                ctx.agregarTexto("    li a7, 1");
                ctx.agregarTexto("    ecall");
                // Salto de línea
                ctx.agregarTexto("    li a0, 10");
                ctx.agregarTexto("    li a7, 11");
                ctx.agregarTexto("    ecall");
                ctx.getAdministradorRegistro().liberarRegistro(r);
            }

            case "halt" -> {
                ctx.agregarTexto("    li a7, 10");
                ctx.agregarTexto("    ecall");
            }

            // ---------- Llamadas (placeholder) ----------
            case "call" -> {
                ctx.agregarTexto("    # TODO: call " + a1 + " con " + a2);
            }
            case "param" -> ctx.agregarTexto("    # TODO: param " + a1);
            case "return" -> ctx.agregarTexto("    # TODO: return " + (a1 == null ? "" : a1));

            // ---------- Otros (placeholders) ----------
            default -> ctx.agregarTexto("    # TODO: " + c);
        }
    }

}
