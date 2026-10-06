package c3d;

import ast.tipos.Tipo;
import enums.TipoDato;
import java.util.*;

public class GenerarCodigoC {

    private static class Funcion {
        final String nombre;
        final List<String> parametros = new ArrayList<>();
        final List<Cuarteta> cuerpo = new ArrayList<>();
        final Set<String> variablesLocales = new TreeSet<>();
        TipoDato tipoRetorno = TipoDato.VOID;


        Funcion(String nombre) {
            this.nombre = nombre;
        }

        String nombreC() {
            return nombre.equals("main") ? "func_main" : nombre;
        }

        String firma() {
            return tipoRetorno.aTipoC() + " " + nombreC()
                    + "(" + (parametros.isEmpty() ? "void" : String.join(", ", parametros)) + ")";
        }
    }

    public static String traducir(List<Cuarteta> cuartetas, Map<String, TipoDato> tiposTemporales) {

        Map<String, Funcion> funciones = new LinkedHashMap<>();
        List<Cuarteta> sueltas = new ArrayList<>();
        Funcion actual = null;
        Map<String, TipoDato> tipos = new HashMap<>();

        for (Cuarteta q : cuartetas) {

            String op = q.getOperador();
            String res = q.getResultado();

            if ("label".equals(op) && res != null && res.startsWith("func_")) {
                actual = new Funcion(res.substring(5));
                funciones.put(actual.nombre, actual);
                continue;
            }

            if ("label".equals(op) && res != null && res.startsWith("end_")) {
                actual = null;
                continue;
            }

            if (actual == null) {
                sueltas.add(q);
                continue;
            }

            if ("param_decl".equals(op)) {
                String tipo = q.getArg1();
                String nombre = q.getArg2();

                TipoDato tipoDato = convertirTipo(tipo);

                actual.parametros.add(tipoDato.aTipoC() + " " + nombre);
                continue;
            }

            actual.cuerpo.add(q);
        }

        corregirParametrosReferencia(funciones.values());

        Map<String, String> nombreCompleto = new HashMap<>();
        for (String nombre : funciones.keySet()) {
            nombreCompleto.putIfAbsent(nombre, nombre);
            int idx = nombre.lastIndexOf('_');
            if (idx > 0 && idx < nombre.length() - 1) {
                String corto = nombre.substring(idx + 1);
                nombreCompleto.putIfAbsent(corto, nombre);
            }
        }

        Funcion main = funciones.get("main");
        List<Cuarteta> inicializacion = new ArrayList<>();

        for (Cuarteta q : sueltas) {
            if (!"halt".equals(q.getOperador()) && !"comment".equals(q.getOperador())) {
                inicializacion.add(q);
            }
        }

        if (main != null) {
            main.cuerpo.addAll(0, inicializacion);
        }

        Set<String> temporales = new TreeSet<>();

        for (Funcion f : funciones.values()) {

            for (String parametro : f.parametros) {

                int espacio = parametro.indexOf(' ');

                if (espacio <= 0) {
                    continue;
                }

                String tipoTexto = parametro.substring(0, espacio);
                String nombre = parametro.substring(espacio + 1);

                TipoDato tipo = tipoDesdeC(tipoTexto);

                tipos.put(nombre, tipo);
            }
        }


        Map<String, TipoDato> tiposRetorno = inferirTiposYRetornos(cuartetas, funciones, nombreCompleto);

        for (Funcion f : funciones.values()) {

            Set<String> parametrosLocales = new HashSet<>();

            for (String parametro : f.parametros) {

                int espacio = parametro.indexOf(' ');

                if (espacio > 0) {
                    parametrosLocales.add(
                            parametro.substring(espacio + 1)
                    );
                }
            }

            for (Cuarteta q : f.cuerpo) {

                recolectar(
                        q,
                        temporales,
                        f.variablesLocales,
                        parametrosLocales
                );
            }
        }
        StringBuilder sb = new StringBuilder();
        emitIncludes(sb);
        emitHeapGlobals(sb);
        emitRuntimeHelpers(sb);


        sb.append("\n// ==== TEMPORALES ====\n");
        for (String t : temporales) {
            TipoDato tipoT = tiposRetorno.get(t);
            if (tipoT == null || tipoT == TipoDato.DESCONOCIDO) {
                tipoT = TipoDato.ENTERO;
            }
            System.out.println("TEMPORALES: " + t + "Tipos: " + tiposRetorno.get(t));
            sb.append(tipoT.aTipoC()).append(" ").append(t).append(";\n");
        }

        sb.append("\n// ====  DECLRACIONES ====\n");
        for (Funcion f : funciones.values()) {
            sb.append(f.firma()).append(";\n");
        }

        sb.append("\n// ==== FUNCIONES ====\n");
        for (Funcion f : funciones.values()) {

            sb.append(f.firma()).append(" {\n");

            for (String variable : f.variablesLocales) {

                TipoDato tipoVariable = tiposRetorno.get(variable);

                if (tipoVariable == null
                        || tipoVariable == TipoDato.DESCONOCIDO) {
                    tipoVariable = TipoDato.ENTERO;
                }

                sb.append("    ")
                        .append(tipoVariable.aTipoC())
                        .append(" ")
                        .append(variable)
                        .append(";\n");
            }

            List<String> pendientes = new ArrayList<>();

            for (Cuarteta q : f.cuerpo) {

                String linea = traducirCuarteta(
                        q,
                        pendientes,
                        funciones,
                        f.tipoRetorno,
                        tiposRetorno,
                        nombreCompleto
                );

                if (!linea.isEmpty()) {
                    sb.append("    ")
                            .append(linea)
                            .append("\n");
                }
            }

            sb.append("}\n\n");
        }

        if (main == null && !inicializacion.isEmpty()) {
            sb.append("// AVISO: hay cuartetas fuera de función y no existe func_main\n");
        }

        sb.append("int main(void) {\n");
        if (main != null) {
            sb.append("    func_main();\n");
        }
        sb.append("    return 0;\n");
        sb.append("}\n");

        return sb.toString();
    }

    /* =========================================================
       HELPERS
       ========================================================= */

    private static void recolectar(Cuarteta q, Set<String> temporales, Set<String> variables,
                                   Set<String> parametrosLocales) {

        String[] candidatos = switch (q.getOperador()) {
            case "label", "goto", "halt", "comment", "param_decl" -> new String[0];
            case "call", "new", "read"                            -> new String[]{q.getResultado()};
            case "new_array"                                      -> new String[]{q.getArg2(), q.getResultado()};
            case "attr_get"                                       -> new String[]{q.getArg1(), q.getResultado()};
            case "field_set"                                      -> new String[]{q.getResultado(), q.getArg2()};
            case "param", "print", "return", "if_true", "if_false" -> new String[]{q.getArg1()};
            default                                               -> new String[]{q.getArg1(), q.getArg2(), q.getResultado()};
        };

        for (String arg : candidatos) {
            if (arg == null || arg.equals("self")) continue;
            if (parametrosLocales.contains(arg)) continue;   // parámetro de ESTA función, no va como global
            if (arg.matches("t\\d+")) {
                temporales.add(arg);
            } else if (arg.matches("[A-Za-z_]\\w*") && !arg.matches("L\\d+")) {
                variables.add(arg);
            }
        }
    }

    /**
     * Un parámetro que se usa como base de un acceso al heap (arreglo u objeto)
     * es una dirección, no un valor. Se declara como uintptr_t sin importar
     * el tipo de sus elementos (ej. "param flotante datos" en un arreglo).
     */
    private static void corregirParametrosReferencia(Collection<Funcion> funciones) {

        for (Funcion f : funciones) {

            Set<String> bases = new HashSet<>();

            for (Cuarteta q : f.cuerpo) {
                switch (q.getOperador()) {
                    case "index_get", "attr_get" -> bases.add(q.getArg1());
                    case "index_set", "field_set" -> bases.add(q.getResultado());
                    default -> { }
                }
            }

            for (int i = 0; i < f.parametros.size(); i++) {

                String parametro = f.parametros.get(i);
                int espacio = parametro.indexOf(' ');

                if (espacio <= 0) {
                    continue;
                }

                String nombre = parametro.substring(espacio + 1);

                if (bases.contains(nombre)) {
                    f.parametros.set(i, "uintptr_t " + nombre);
                }
            }
        }
    }

    private static TipoDato tipoDe(String valor, Map<String, TipoDato> tipos) {

        if (valor == null) {
            return TipoDato.DESCONOCIDO;
        }

        if (valor.startsWith("\"")) {
            return TipoDato.TEXTO;
        }

        if (valor.matches("-?\\d+")) {
            return TipoDato.ENTERO;
        }

        if (valor.matches("-?\\d+\\.\\d+")) {
            return TipoDato.DECIMAL;
        }

        TipoDato tipo = tipos.get(valor);

        if (tipo != null) {
            return tipo;
        }

        return TipoDato.DESCONOCIDO;
    }

    private static String aTexto(String valor, Map<String, TipoDato> tipos) {
        return tipoDe(valor, tipos) == TipoDato.TEXTO ? valor : "numAtexto(" + valor + ")";
    }

    private static Map<String, TipoDato> inferirTiposYRetornos(
            List<Cuarteta> cuartetas,
            Map<String, Funcion> funciones,
            Map<String, String> nombreCompleto) {

        Map<String, TipoDato> tipos = new HashMap<>();

        Map<String, TipoDato> retornosFuncion = new HashMap<>();

        for (String nombre : funciones.keySet()) {
            retornosFuncion.put(nombre, TipoDato.VOID);
        }

        for (int pasada = 0; pasada < 10; pasada++) {

            boolean cambio = false;

            for (Cuarteta q : cuartetas) {

                String op = q.getOperador();
                String res = q.getResultado();

                // ---------------------------------------------------------
                // Cuartetas que NO definen el tipo de "res" como valor.
                // Van ANTES del bloque de tipo declarado a propósito.
                // ---------------------------------------------------------
                if ("index_set".equals(op)
                        || "field_set".equals(op)
                        || "label".equals(op)
                        || "goto".equals(op)
                        || "halt".equals(op)
                        || "comment".equals(op)
                        || "param".equals(op)
                        || "param_decl".equals(op)
                        || "print".equals(op)
                        || "return".equals(op)) {
                    continue;
                }

                // ---------------------------------------------------------
                // Referencias al heap: "res" siempre es una dirección,
                // venga o no un tipo declarado en la cuarteta.
                // ---------------------------------------------------------
                if (("new".equals(op) || "new_array".equals(op)) && res != null) {

                    TipoDato referencia = "new".equals(op)
                            ? TipoDato.OBJETO
                            : TipoDato.ESTRUCTURA;

                    if (tipos.get(res) != referencia) {
                        tipos.put(res, referencia);
                        cambio = true;
                    }

                    continue;
                }

                /*
                 * Si la cuarteta ya trae un tipo desde el AST/semántica,
                 * ese tipo tiene prioridad.
                 */
                if (q.getTipoDeclarado() != null
                        && res != null
                        && !res.equals("self")) {

                    TipoDato nuevoTipo = q.getTipoDeclarado();
                    TipoDato anterior = tipos.get(res);

                    if (anterior != nuevoTipo) {
                        tipos.put(res, nuevoTipo);
                        cambio = true;
                    }

                    continue;
                }

                switch (op) {

                    // =====================================================
                    // ASIGNACION
                    // =====================================================
                    case "=" -> {

                        if (res == null) {
                            break;
                        }

                        TipoDato tipoOrigen =
                                tipoDe(q.getArg1(), tipos);

                        if (tipoOrigen != TipoDato.DESCONOCIDO) {

                            TipoDato anterior = tipos.get(res);

                            if (anterior != tipoOrigen) {
                                tipos.put(res, tipoOrigen);
                                cambio = true;
                            }

                        } else if (q.getArg1() != null
                                && q.getArg1().matches("t\\d+")) {

                            // Inferencia hacia atrás: "nombre = t104" con
                            // t104 desconocido (ej. un read) toma el tipo
                            // del destino si ese ya se conoce.
                            TipoDato tipoDestino = tipos.get(res);

                            if (tipoDestino == TipoDato.TEXTO
                                    || tipoDestino == TipoDato.ENTERO
                                    || tipoDestino == TipoDato.DECIMAL
                                    || tipoDestino == TipoDato.BOOLEANO) {

                                tipos.put(q.getArg1(), tipoDestino);
                                cambio = true;
                            }
                        }
                    }

                    // =====================================================
                    // READ
                    // =====================================================
                    case "read" -> {

                        if (res == null) {
                            break;
                        }

                        TipoDato tipoRead = q.getTipoDeclarado();

                        if (tipoRead != null
                                && tipoRead != TipoDato.DESCONOCIDO) {

                            TipoDato anterior = tipos.get(res);

                            if (anterior != tipoRead) {
                                tipos.put(res, tipoRead);
                                cambio = true;
                            }
                        }
                    }

                    // =====================================================
                    // ACCESO A ATRIBUTO / ARREGLO
                    // =====================================================
                    case "attr_get", "index_get" -> {

                        if (res == null) {
                            break;
                        }

                        TipoDato tipoAcceso = q.getTipoDeclarado();

                        if (tipoAcceso != null
                                && tipoAcceso != TipoDato.DESCONOCIDO) {

                            TipoDato anterior = tipos.get(res);

                            if (anterior != tipoAcceso) {
                                tipos.put(res, tipoAcceso);
                                cambio = true;
                            }

                        } else {

                            if (!tipos.containsKey(res)) {

                                tipos.put(
                                        res,
                                        TipoDato.DESCONOCIDO
                                );

                                cambio = true;
                            }
                        }
                    }

                    // =====================================================
                    // CALL
                    // =====================================================
                    case "call" -> {

                        if (res == null) {
                            break;
                        }

                        String nombreReal =
                                nombreCompleto.getOrDefault(
                                        q.getArg1(),
                                        q.getArg1()
                                );

                        TipoDato tipoRetorno =
                                retornosFuncion.get(nombreReal);

                        if (tipoRetorno != null
                                && tipoRetorno != TipoDato.VOID
                                && tipoRetorno != TipoDato.DESCONOCIDO) {

                            TipoDato anterior = tipos.get(res);

                            if (anterior != tipoRetorno) {

                                tipos.put(
                                        res,
                                        tipoRetorno
                                );

                                cambio = true;
                            }
                        }
                    }

                    // =====================================================
                    // OPERACIONES
                    // =====================================================
                    default -> {

                        if (res == null || op.startsWith("if_")) {
                            break;
                        }

                        TipoDato tipoIzquierda =
                                tipoDe(q.getArg1(), tipos);

                        TipoDato tipoDerecha =
                                q.getArg2() != null
                                        ? tipoDe(q.getArg2(), tipos)
                                        : tipoIzquierda;

                        TipoDato resultado;

                        if ("==".equals(op)
                                || "!=".equals(op)
                                || "<".equals(op)
                                || ">".equals(op)
                                || "<=".equals(op)
                                || ">=".equals(op)
                                || "&&".equals(op)
                                || "||".equals(op)) {

                            resultado = TipoDato.BOOLEANO;

                        } else if ("+".equals(op)
                                && (tipoIzquierda == TipoDato.TEXTO
                                || tipoDerecha == TipoDato.TEXTO)) {

                            resultado = TipoDato.TEXTO;

                        } else if (tipoIzquierda == TipoDato.DECIMAL
                                || tipoDerecha == TipoDato.DECIMAL) {

                            resultado = TipoDato.DECIMAL;

                        } else if (tipoIzquierda == TipoDato.DESCONOCIDO
                                || tipoDerecha == TipoDato.DESCONOCIDO) {

                            resultado = TipoDato.DESCONOCIDO;

                        } else {

                            resultado = TipoDato.ENTERO;
                        }

                        TipoDato anterior = tipos.get(res);

                        if (anterior != resultado) {

                            tipos.put(
                                    res,
                                    resultado
                            );

                            cambio = true;
                        }
                    }
                }
            }

            // =============================================================
            // RESOLVER RETORNOS DE FUNCIONES
            // =============================================================

            for (Funcion f : funciones.values()) {

                TipoDato retorno = TipoDato.VOID;

                for (Cuarteta q : f.cuerpo) {

                    if (!"return".equals(q.getOperador())) {
                        continue;
                    }

                    if (q.getArg1() == null) {
                        retorno = TipoDato.VOID;
                        continue;
                    }

                    TipoDato tipoRetorno =
                            tipoDe(q.getArg1(), tipos);

                    if (tipoRetorno != TipoDato.DESCONOCIDO) {
                        retorno = tipoRetorno;
                    }
                }

                TipoDato anterior =
                        retornosFuncion.put(
                                f.nombre,
                                retorno
                        );

                if (anterior != retorno) {
                    cambio = true;
                }
            }

            if (!cambio) {
                break;
            }
        }

        // =============================================================
        // GUARDAR TIPOS DE RETORNO
        // =============================================================

        for (Funcion f : funciones.values()) {

            f.tipoRetorno =
                    retornosFuncion.getOrDefault(
                            f.nombre,
                            TipoDato.VOID
                    );
        }

        return tipos;
    }
    private static String numero(String valor, String operador) {
        if (valor == null || !valor.matches("\\d+")) {
            throw new IllegalStateException("La cuarteta '" + operador + "' necesita un desplazamiento numérico y recibió '"
                    + valor + "'. Revisa AccesoAtributo y Asignacion.");
        }
        return valor;
    }

    private static String traducirCuarteta(Cuarteta q, List<String> pendientes, Map<String, Funcion> funciones, TipoDato tipoRetornoFuncion,
                                           Map<String, TipoDato> tipos, Map<String, String> nombreCompleto) {

        String op = q.getOperador();
        String a1 = q.getArg1();
        String a2 = q.getArg2();
        String res = q.getResultado();

        return switch (op) {

            case "label"    -> res + ":;";
            case "goto"     -> "goto " + res + ";";
            case "if_true"  -> "if (" + a1 + ") goto " + res + ";";
            case "if_false" -> "if (!" + a1 + ") goto " + res + ";";
            case "="        -> res + " = " + a1 + ";";
            case "neg"      -> res + " = -" + a1 + ";";
            case "not"      -> res + " = !" + a1 + ";";
            case "comment"  -> "// " + String.valueOf(res).replace('\n', ' ');
            case "halt"     -> "";

            case "param" -> {
                pendientes.add(a1);
                yield "";
            }

            case "call" -> {
                String nombreReal = nombreCompleto.getOrDefault(a1, a1);
                String llamada = nombreReal + "(" + String.join(", ", pendientes) + ")";
                pendientes.clear();
                Funcion destino = funciones.get(nombreReal);
                boolean descartar = (res == null) || destino == null || destino.tipoRetorno == TipoDato.VOID;
                yield (descartar ? llamada : res + " = " + llamada) + ";";
            }

            case "return" -> a1 != null
                    ? "return " + a1 + ";"
                    : (tipoRetornoFuncion == TipoDato.VOID ? "return;" : "return 0;");

            case "print" -> switch (tipoDe(a1, tipos)) {
                case TEXTO   -> "printf(\"%s\", " + a1 + ");";
                case DECIMAL -> "printf(\"%f\\n\", " + a1 + ");";
                default      -> "printf(\"%d\\n\", " + a1 + ");";
            };

            case "read" -> generarLectura(res, tipos);

            case "new" -> {
                if (a2 == null || !a2.matches("\\d+")) {
                    throw new IllegalStateException("La cuarteta 'new' no trae el tamaño del objeto. "
                            + "Usa el CrearObjeto que llama a contexto.tamanio(clase).");
                }
                yield res + " = hp; hp = hp + " + a2 + ";";
            }

            case "new_array" -> res + " = hp; hp = hp + " + a2 + ";";
            case "attr_get" -> {

                TipoDato tipoResultado = tipoDe(res, tipos);

                String cast = "";

                if (tipoResultado == TipoDato.TEXTO) {
                    cast = "(char*)";
                }

                yield res + " = " + cast + "heap[" + a1 + " + " + numero(a2, op) + "];";
            }
            case "field_set" -> {

                TipoDato tipoValor =
                        tipoDe(a2, tipos);

                String valor = a2;

                if (tipoValor == TipoDato.TEXTO
                        || tipoValor == TipoDato.OBJETO
                        || tipoValor == TipoDato.ESTRUCTURA) {

                    valor = "(uintptr_t)" + a2;
                }

                yield "heap[" + res + " + " + numero(a1, op) + "] = " + valor + ";";
            }
            case "index_get" -> {

                TipoDato tipoResultado =
                        tipoDe(res, tipos);

                String cast = "";

                if (tipoResultado == TipoDato.TEXTO) {
                    cast = "(char*)";
                }

                yield res + " = " + cast + "heap[" + a1 + " + " + a2 + "];";
            }
            case "index_set" -> "heap[" + res + " + " + a1 + "] = " + a2 + ";";

            default -> {
                if ("+".equals(op) && tipoDe(res, tipos) == TipoDato.TEXTO) {
                    String argA = aTexto(a1, tipos);
                    String argB = aTexto(a2, tipos);
                    yield res + " = concat(" + argA + ", " + argB + ");";
                }
                yield op.startsWith("if_")
                        ? "if (" + a1 + " " + op.substring(3) + " " + a2 + ") goto " + res + ";"
                        : res + " = " + a1 + " " + op + " " + a2 + ";";
            }
        };
    }

    private static void emitIncludes(StringBuilder sb) {
        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdlib.h>\n");
        sb.append("#include <string.h>\n");
        sb.append("#include <stdint.h>\n\n");
    }

    private static void emitHeapGlobals(StringBuilder sb) {
        sb.append("uintptr_t heap[100000];\n");
        sb.append("int hp = 1;   // 0 = null\n\n");
    }

    private static void emitRuntimeHelpers(StringBuilder sb) {
        emitConcatHelper(sb);
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

    private static TipoDato convertirTipo(String tipo) {

        if (tipo == null) {
            return TipoDato.DESCONOCIDO;
        }

        return switch (tipo.toLowerCase()) {
            case "entero", "int" -> TipoDato.ENTERO;
            case "flotante", "decimal", "double" -> TipoDato.DECIMAL;
            case "cadena", "texto", "string" -> TipoDato.TEXTO;
            case "boolean", "booleano" -> TipoDato.BOOLEANO;
            case "objeto", "direccion", "referencia" -> TipoDato.OBJETO;
            default -> TipoDato.OBJETO;
        };
    }

    private static TipoDato tipoDesdeC(String tipo) {

        if (tipo == null) {
            return TipoDato.DESCONOCIDO;
        }

        return switch (tipo) {
            case "int" -> TipoDato.ENTERO;
            case "double" -> TipoDato.DECIMAL;
            case "char*" -> TipoDato.TEXTO;
            case "uintptr_t" -> TipoDato.OBJETO;
            case "void" -> TipoDato.VOID;
            default -> TipoDato.DESCONOCIDO;
        };
    }

    private static String generarLectura(
            String destino,
            Map<String, TipoDato> tipos
    ) {

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

            default ->
                    "if (scanf(\"%d\", &" + destino + ") != 1) { "
                            + "int c; "
                            + "while ((c = getchar()) != '\\n' && c != EOF) {} "
                            + destino + " = 0; "
                            + "}";
        };
    }
}