package c3d;

import enums.TipoDato;

import java.util.*;

public class GenerarCodigoC {

    public static String traducir(List<Cuarteta> cuartetas) {
        return traducir(cuartetas, new HashMap<>());
    }

    public static String traducir(List<Cuarteta> cuartetas, Map<String, TipoDato> tiposTemporales) {

        Map<String, TipoDato> tipos = InferidorTipos.inferir(cuartetas, tiposTemporales);

        // Temporales y variables sueltas (todo es global: no hay funciones de C)
        Set<String> temporales = new TreeSet<>();
        Set<String> variables = new TreeSet<>();
        for (Cuarteta q : cuartetas) {
            recolectar(q, temporales, variables);
        }

        StringBuilder sb = new StringBuilder();
        emitIncludes(sb);
        emitGlobals(sb);
        emitRuntimeHelpers(sb);

        sb.append("\n// ==== TEMPORALES ====\n");
        for (String t : temporales) {
            sb.append(tipoC(tipoDeclaracion(tipos.get(t)))).append(" ").append(t).append(";\n");
        }

        if (!variables.isEmpty()) {
            sb.append("\n// ==== VARIABLES ====\n");
            for (String v : variables) {
                sb.append(tipoC(tipoDeclaracion(tipos.get(v)))).append(" ").append(v).append(";\n");
            }
        }

        sb.append("\nint main(void) {\n");
        for (Cuarteta q : cuartetas) {
            String linea = traducirCuarteta(q, tipos);
            if (!linea.isEmpty()) {
                sb.append("    ").append(linea).append("\n");
            }
        }
        sb.append("    return 0;\n");
        sb.append("}\n");

        return sb.toString();
    }

    /* =========================================================
       RECOLECCIÓN DE NOMBRES
       ========================================================= */

    private static void recolectar(Cuarteta q, Set<String> temporales, Set<String> variables) {

        String[] candidatos = switch (q.getOperador()) {
            case "label", "goto", "halt", "comment"       -> new String[0];
            case "new", "read"                            -> new String[]{q.getResultado()};
            case "new_array"                              -> new String[]{q.getArg2(), q.getResultado()};
            case "attr_get"                               -> new String[]{q.getArg1(), q.getResultado()};
            case "field_set"                              -> new String[]{q.getResultado(), q.getArg2()};
            case "print", "if_true", "if_false"           -> new String[]{q.getArg1()};
            default                                       -> new String[]{q.getArg1(), q.getArg2(), q.getResultado()};
        };

        for (String arg : candidatos) {
            if (arg == null || InferidorTipos.REGISTROS.contains(arg)) continue;
            if (arg.matches("t\\d+")) {
                temporales.add(arg);
            } else if (arg.matches("[A-Za-z_]\\w*") && !arg.matches("L\\d+")) {
                variables.add(arg);
            }
        }
    }

    private static TipoDato tipoDeclaracion(TipoDato tipo) {
        return (tipo == null || tipo == TipoDato.DESCONOCIDO) ? TipoDato.ENTERO : tipo;
    }

    private static TipoDato tipoDe(String valor, Map<String, TipoDato> tipos) {
        return InferidorTipos.tipoDe(valor, tipos);
    }

    /* =========================================================
       TRADUCCIÓN CUARTETA -> C
       ========================================================= */

    private static String traducirCuarteta(Cuarteta q, Map<String, TipoDato> tipos) {

        String op = q.getOperador();
        String a1 = InferidorTipos.normalizar(q.getArg1());
        String a2 = InferidorTipos.normalizar(q.getArg2());
        String res = q.getResultado();

        return switch (op) {

            case "label"    -> res + ":;";
            case "goto"     -> "goto " + res + ";";
            case "if_true"  -> "if (" + a1 + ") goto " + res + ";";
            case "if_false" -> "if (!" + a1 + ") goto " + res + ";";
            case "neg"      -> res + " = -" + a1 + ";";
            case "not"      -> res + " = !" + a1 + ";";
            case "comment"  -> "// " + String.valueOf(res).replace('\n', ' ');
            case "halt"     -> "";

            // ---- Estas operaciones ya no existen: indican un nodo sin migrar ----
            case "call", "param", "param_decl", "return" ->
                    throw new IllegalStateException("La cuarteta '" + op + "' ya no está permitida (C3D sin funciones). "
                            + "Revisa el nodo que la genera: debe usar contexto.llamar(...) / retornar(...) / abrirFuncion(...).");

            // ---- Asignación (con el registro retval) ----
            case "=" -> {
                if ("retval".equals(res)) {
                    yield escribirCelda("retval", a1, tipoDe(a1, tipos));
                }
                if ("retval".equals(a1)) {
                    yield res + " = " + leerCelda("retval", tipoDe(res, tipos)) + ";";
                }
                yield res + " = " + a1 + ";";
            }

            // ---- Pila manual ----
            case "stack_get" -> res + " = " + leerCelda("stack[(int)" + a1 + "]", tipoDe(res, tipos)) + ";";
            case "stack_set" -> escribirCelda("stack[(int)" + a1 + "]", a2, tipoDe(a2, tipos));

            case "print" -> {
                String salto = "nl".equals(a2) ? "\\n" : "";
                yield switch (tipoDe(a1, tipos)) {
                    case TEXTO    -> "printf(\"%s" + salto + "\", " + literalC(a1) + ");";
                    case DECIMAL  -> "printf(\"%f" + salto + "\", " + a1 + ");";
                    case CARACTER -> "printf(\"%c" + salto + "\", " + a1 + ");";
                    default       -> "printf(\"%d" + salto + "\", " + a1 + ");";
                };
            }

            case "read" -> generarLectura(res, tipos);

            // ---- Heap ----
            case "new" -> {
                if (a2 == null || !a2.matches("\\d+")) {
                    throw new IllegalStateException("La cuarteta 'new' no trae el tamaño del objeto. "
                            + "Usa el CrearObjeto que llama a contexto.tamanio(clase).");
                }
                yield res + " = hp; hp = hp + " + a2 + ";";
            }

            case "new_array" -> res + " = hp; hp = hp + " + a2 + ";";

            case "attr_get" -> {
                String cast = tipoDe(res, tipos) == TipoDato.TEXTO ? "(char*)" : "";
                yield res + " = " + cast + "heap[" + a1 + " + " + numero(a2, op) + "];";
            }

            case "field_set" -> {
                TipoDato tipoValor = tipoDe(a2, tipos);
                String valor = a2;
                if (tipoValor == TipoDato.TEXTO || tipoValor == TipoDato.OBJETO || tipoValor == TipoDato.ESTRUCTURA) {
                    valor = "(uintptr_t)" + a2;
                }
                yield "heap[" + res + " + " + numero(a1, op) + "] = " + valor + ";";
            }

            case "index_get" -> {
                String cast = tipoDe(res, tipos) == TipoDato.TEXTO ? "(char*)" : "";
                yield res + " = " + cast + "heap[" + a1 + " + " + a2 + "];";
            }

            case "index_set" -> "heap[" + res + " + " + a1 + "] = " + a2 + ";";

            default -> {
                if ("+".equals(op) && tipoDe(res, tipos) == TipoDato.TEXTO) {
                    yield res + " = concat(" + aTexto(a1, tipos) + ", " + aTexto(a2, tipos) + ");";
                }
                yield op.startsWith("if_")
                        ? "if (" + a1 + " " + op.substring(3) + " " + a2 + ") goto " + res + ";"
                        : res + " = " + a1 + " " + op + " " + a2 + ";";
            }
        };
    }

    /* =========================================================
       CELDAS DE PILA (stack y retval)
       ========================================================= */

    private static String escribirCelda(String destino, String valor, TipoDato tipo) {
        if (tipo == TipoDato.TEXTO) {
            return destino + ".s = " + literalC(valor) + ";";
        }
        return destino + ".i = (long long)(" + valor + "); "
                + destino + ".d = (double)(" + valor + ");";
    }

    private static String leerCelda(String origen, TipoDato tipo) {
        return switch (tipo) {
            case DECIMAL -> origen + ".d";
            case TEXTO   -> origen + ".s";
            default      -> origen + ".i";
        };
    }

    /* =========================================================
       HELPERS
       ========================================================= */

    private static String numero(String valor, String operador) {
        if (valor == null || !valor.matches("\\d+")) {
            throw new IllegalStateException("La cuarteta '" + operador + "' necesita un desplazamiento numérico y recibió '"
                    + valor + "'. Revisa AccesoAtributo y Asignacion.");
        }
        return valor;
    }

    private static String aTexto(String valor, Map<String, TipoDato> tipos) {
        TipoDato tipo = tipoDe(valor, tipos);
        if (tipo == TipoDato.TEXTO) {
            return valor;
        }
        if (tipo == TipoDato.CARACTER) {
            return "charAtexto(" + valor + ")";
        }
        return "numAtexto(" + valor + ")";
    }

    private static String literalC(String valor) {
        if (valor == null || !valor.startsWith("\"")) {
            return valor;
        }
        return valor.replace("\r", "")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private static String tipoC(TipoDato tipo) {
        return switch (tipo) {
            case OBJETO, ESTRUCTURA -> "uintptr_t";
            case CARACTER           -> "char";
            default                 -> tipo.aTipoC();
        };
    }

    private static String generarLectura(String destino, Map<String, TipoDato> tipos) {

        TipoDato tipo = tipoDe(destino, tipos);

        return switch (tipo) {

            case ENTERO, BOOLEANO ->
                    "if (scanf(\"%d\", &" + destino + ") != 1) { "
                            + "int c; "
                            + "while ((c = getchar()) != '\\n' && c != EOF) {} "
                            + destino + " = 0; "
                            + "}";

            case DECIMAL ->
                    "if (scanf(\"%lf\", &" + destino + ") != 1) { "
                            + "int c; "
                            + "while ((c = getchar()) != '\\n' && c != EOF) {} "
                            + destino + " = 0.0; "
                            + "}";

            case TEXTO ->
                    destino + " = malloc(256); "
                            + "if (scanf(\"%255s\", " + destino + ") != 1) { "
                            + destino + "[0] = '\\0'; "
                            + "}";

            case CARACTER ->
                    "scanf(\" %c\", &" + destino + ");";

            default ->
                    "if (scanf(\"%d\", &" + destino + ") != 1) { "
                            + "int c; "
                            + "while ((c = getchar()) != '\\n' && c != EOF) {} "
                            + destino + " = 0; "
                            + "}";
        };
    }

    /* =========================================================
       ENCABEZADO Y RUNTIME
       ========================================================= */

    private static void emitIncludes(StringBuilder sb) {
        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdlib.h>\n");
        sb.append("#include <string.h>\n");
        sb.append("#include <stdint.h>\n\n");
    }

    private static void emitGlobals(StringBuilder sb) {
        sb.append("// ==== MEMORIA ====\n");
        sb.append("uintptr_t heap[100000];\n");
        sb.append("int hp = 1;   // 0 = null\n\n");

        sb.append("typedef struct { long long i; double d; char* s; } Celda;\n");
        sb.append("Celda stack[100000];\n");
        sb.append("long long fp = 0, sp = 0, retaddr = 0;\n");
        sb.append("Celda retval;\n\n");
    }

    private static void emitRuntimeHelpers(StringBuilder sb) {
        emitConcatHelper(sb);
        emitCharAtextoHelper(sb);
        emitNumAtextoHelper(sb);
    }

    private static void emitConcatHelper(StringBuilder sb) {
        sb.append("char* concat(const char* a, const char* b) {\n");
        sb.append("    char* buf = malloc(strlen(a) + strlen(b) + 1);\n");
        sb.append("    strcpy(buf, a);\n");
        sb.append("    strcat(buf, b);\n");
        sb.append("    return buf;\n");
        sb.append("}\n\n");
    }

    private static void emitNumAtextoHelper(StringBuilder sb) {
        sb.append("char* numAtexto(int n) {\n");
        sb.append("    char* buf = malloc(32);\n");
        sb.append("    snprintf(buf, 32, \"%d\", n);\n");
        sb.append("    return buf;\n");
        sb.append("}\n\n");
    }

    private static void emitCharAtextoHelper(StringBuilder sb) {
        sb.append("char* charAtexto(char c) {\n");
        sb.append("    char* buf = malloc(2);\n");
        sb.append("    buf[0] = c;\n");
        sb.append("    buf[1] = '\\0';\n");
        sb.append("    return buf;\n");
        sb.append("}\n\n");
    }
}