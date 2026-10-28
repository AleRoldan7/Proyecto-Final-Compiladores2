package c3d;

import enums.TipoDato;

import java.util.*;
import java.util.regex.Pattern;

public class GenerarCodigoC {

    private static final Pattern PATRON_ENTERO   = Pattern.compile("-?\\d+");
    private static final Pattern PATRON_DECIMAL  = Pattern.compile("-?\\d+\\.\\d+");
    private static final Pattern PATRON_CARACTER = Pattern.compile("\"'.'\"");
    private static final Pattern PATRON_TEMPORAL = Pattern.compile("t\\d+");
    private static final Set<String> COMPARACIONES = Set.of("<", "<=", ">", ">=", "==", "!=");
    private static final Set<String> BINARIOS =
            Set.of("+", "-", "*", "/", "%", "<", "<=", ">", ">=", "==", "!=", "&&", "||", "/=", "%=");

    private static final String RUNTIME = """
            #include <stdio.h>
            #include <stdlib.h>
            #include <string.h>
            #include <stdint.h>

            /* ==== MEMORIA ==== */
            typedef union { long long i; double d; } Campo;

            #define HEAP_CELDAS 1048576
            static Campo heap[HEAP_CELDAS];
            static int hp = 1;   /* 0 = null */

            static int reservar(int n) {
                if (n < 0 || hp + n > HEAP_CELDAS) {
                    printf("Error: memoria agotada\\n");
                    exit(1);
                }
                int base = hp;
                hp += n;
                return base;
            }

            /* ==== TEXTO ==== */
            static const char* txt(const char* s) { return s ? s : "(null)"; }

            static char* concat(const char* a, const char* b) {
                if (!a) a = "null";
                if (!b) b = "null";
                char* buf = malloc(strlen(a) + strlen(b) + 1);
                strcpy(buf, a);
                strcat(buf, b);
                return buf;
            }

            static char* numAtexto(long long n) {
                char* buf = malloc(32);
                snprintf(buf, 32, "%lld", n);
                return buf;
            }

            static char* decAtexto(double d) {
                char* buf = malloc(64);
                snprintf(buf, 64, "%f", d);
                return buf;
            }

            static char* charAtexto(char c) {
                char* buf = malloc(2);
                buf[0] = c;
                buf[1] = '\\0';
                return buf;
            }

            static int igualTexto(const char* a, const char* b) {
                if (a == b) return 1;
                if (!a || !b) return 0;
                return strcmp(a, b) == 0;
            }

            /* ==== ENTRADA (por línea, como RARS) ==== */
            static char* leerLinea(void) {
                char* buf = malloc(256);
                if (!fgets(buf, 256, stdin)) buf[0] = '\\0';
                size_t n = strlen(buf);
                if (n > 0 && buf[n - 1] == '\\n') buf[n - 1] = '\\0';
                return buf;
            }
            static int    leerEntero(void)   { char* l = leerLinea(); int v = atoi(l);  free(l); return v; }
            static double leerDecimal(void)  { char* l = leerLinea(); double v = atof(l); free(l); return v; }
            static char   leerCaracter(void) { char* l = leerLinea(); char c = l[0];    free(l); return c; }
            static char*  leerTexto(void)    { return leerLinea(); }

            """;

    private static final class Funcion {
        final String nombre;
        final List<String> params = new ArrayList<>();
        final Set<String> locales = new LinkedHashSet<>();      // incluye parámetros
        final Map<String, TipoDato> tipos = new HashMap<>();    // locales, parámetros y temporales inferidos
        final Set<String> temporales = new LinkedHashSet<>();
        final List<Cuarteta> cuerpo = new ArrayList<>();
        TipoDato retorno = TipoDato.DESCONOCIDO;

        Funcion(String nombre) {
            this.nombre = nombre;
        }
    }

    private final Map<String, TipoDato> tiposTemporales;
    private final Map<String, TipoDato> tiposGlobales = new HashMap<>();
    private final Set<String> globales = new LinkedHashSet<>();
    private final Set<Integer> camposDecimales = new HashSet<>();
    private final Map<String, Funcion> funciones = new LinkedHashMap<>();
    private final List<String> pendientes = new ArrayList<>();
    private Funcion actual;

    private GenerarCodigoC(Map<String, TipoDato> tiposTemporales) {
        this.tiposTemporales = tiposTemporales;
    }

    public static String traducir(List<Cuarteta> cuartetas) {
        return traducir(cuartetas, new HashMap<>());
    }

    public static String traducir(List<Cuarteta> cuartetas, Map<String, TipoDato> tiposTemporales) {
        GenerarCodigoC g = new GenerarCodigoC(new HashMap<>(tiposTemporales));
        g.analizar(cuartetas);
        return g.emitir();
    }

    // ============================ análisis ============================

    private void analizar(List<Cuarteta> cuartetas) {
        for (Cuarteta q : cuartetas) {
            if ("gdecl".equals(q.getOperador())) {
                globales.add(q.getArg1());
                tiposGlobales.put(q.getArg1(), tipoDeclarado(q));
            }
        }

        int i = 0;
        while (i < cuartetas.size()) {
            Cuarteta q = cuartetas.get(i);
            String op = q.getOperador();
            if ("fbegin".equals(op)) {
                int fin = i;
                while (fin < cuartetas.size() && !"fend".equals(cuartetas.get(fin).getOperador())) fin++;
                if (fin >= cuartetas.size()) {
                    throw new IllegalStateException("la función '" + q.getArg1() + "' no tiene fend");
                }
                registrarFuncion(cuartetas.subList(i, fin + 1));
                i = fin + 1;
            } else {
                if (!"comment".equals(op) && !"gdecl".equals(op)) {
                    throw new IllegalStateException("instrucción fuera de cualquier función: " + op);
                }
                i++;
            }
        }

        for (Funcion f : funciones.values()) {
            actual = f;
            recolectarCamposDecimales(f);
        }
        for (Funcion f : funciones.values()) {
            actual = f;
            clasificarVariables(f);
        }

        boolean cambio;
        int vueltas = 0;
        do {
            cambio = false;
            for (Funcion f : funciones.values()) {
                cambio |= inferir(f);
            }
        } while (cambio && ++vueltas < 10);
    }

    private void registrarFuncion(List<Cuarteta> bloque) {
        Cuarteta inicio = bloque.get(0);
        Funcion f = new Funcion(inicio.getArg1());
        f.retorno = tipoDeclarado(inicio);

        for (Cuarteta q : bloque.subList(1, bloque.size() - 1)) {
            switch (q.getOperador()) {
                case "fparam" -> {
                    f.params.add(q.getArg1());
                    f.locales.add(q.getArg1());
                    f.tipos.put(q.getArg1(), tipoDeclarado(q));
                }
                case "ldecl" -> {
                    f.locales.add(q.getArg1());
                    f.tipos.put(q.getArg1(), tipoDeclarado(q));
                }
                default -> f.cuerpo.add(q);
            }
        }
        funciones.put(f.nombre, f);
    }

    private void recolectarCamposDecimales(Funcion f) {
        for (Cuarteta q : f.cuerpo) {
            if ("field_set".equals(q.getOperador())) {
                TipoDato t = tipoOp(f, q.getArg2());
                if (t == TipoDato.DESCONOCIDO) t = tipoDeclarado(q);
                if (t == TipoDato.DECIMAL) camposDecimales.add(indice(q.getArg1()));
            } else if ("attr_get".equals(q.getOperador())) {
                if (tipoDeclarado(q) == TipoDato.DECIMAL || tipoDe(f, q.getResultado()) == TipoDato.DECIMAL) {
                    camposDecimales.add(indice(q.getArg2()));
                }
            }
        }
    }

    private void clasificarVariables(Funcion f) {
        for (Cuarteta q : f.cuerpo) {
            for (String v : variables(q)) {
                if (f.locales.contains(v) || globales.contains(v)) continue;
                if (PATRON_TEMPORAL.matcher(v).matches() || tiposTemporales.containsKey(v)) {
                    f.temporales.add(v);
                } else {
                    System.err.println("AVISO: '" + v + "' no tiene gdecl; se trata como global");
                    globales.add(v);
                    tiposGlobales.putIfAbsent(v, tipoDe(f, v));
                }
            }
        }
    }

    // ============================ inferencia de tipos ============================

    private boolean inferir(Funcion f) {
        boolean cambio = false;
        List<String> args = new ArrayList<>();

        for (Cuarteta q : f.cuerpo) {
            String op = q.getOperador();
            String a1 = q.getArg1(), a2 = q.getArg2(), res = q.getResultado();

            switch (op) {
                case "=" -> {
                    cambio |= fijar(f, res, tipoOp(f, a1));
                    if (a1 != null && PATRON_TEMPORAL.matcher(a1).matches()) {
                        cambio |= fijar(f, a1, tipoDe(f, res));   // p. ej. el destino de un read
                    }
                }
                case "neg" -> cambio |= fijar(f, res, tipoOp(f, a1));
                case "!", "not" -> cambio |= fijar(f, res, TipoDato.ENTERO);
                case "param" -> args.add(a1);
                case "call" -> {
                    Funcion d = funciones.get(a1);
                    if (d != null) {
                        for (int k = 0; k < args.size() && k < d.params.size(); k++) {
                            String p = d.params.get(k);
                            cambio |= fijar(d, p, tipoOp(f, args.get(k)));
                            cambio |= fijar(f, args.get(k), tipoDe(d, p));
                        }
                    }
                    args.clear();
                    if (res != null && !"-".equals(res)) {
                        TipoDato t = tipoDeclarado(q);
                        if (t == TipoDato.DESCONOCIDO && d != null) t = d.retorno;
                        cambio |= fijar(f, res, t);
                    }
                }
                case "return" -> {
                    if (a1 != null && !"-".equals(a1) && f.retorno == TipoDato.DESCONOCIDO) {
                        TipoDato t = tipoDeclarado(q);
                        if (t == TipoDato.DESCONOCIDO) t = tipoOp(f, a1);
                        if (t != TipoDato.DESCONOCIDO) {
                            f.retorno = t;
                            cambio = true;
                        }
                    }
                }
                case "attr_get" -> {
                    int campo = indice(a2);
                    TipoDato t = tipoDeclarado(q);
                    if (t == TipoDato.DESCONOCIDO && camposDecimales.contains(campo)) t = TipoDato.DECIMAL;
                    cambio |= fijar(f, res, t);
                    if (tipoDe(f, res) == TipoDato.DECIMAL && camposDecimales.add(campo)) cambio = true;
                }
                case "field_set" -> {
                    TipoDato t = tipoOp(f, a2);
                    if (t == TipoDato.DESCONOCIDO) t = tipoDeclarado(q);
                    if (t == TipoDato.DECIMAL && camposDecimales.add(indice(a1))) cambio = true;
                }
                case "new", "new_array" -> cambio |= fijar(f, res, TipoDato.ENTERO);
                case "index_get" -> {
                    TipoDato t = tipoDeclarado(q);
                    if (t == TipoDato.CARACTER || t == TipoDato.DECIMAL) cambio |= fijar(f, res, t);
                }
                default -> {
                    if (BINARIOS.contains(op)) {
                        cambio |= fijar(f, res, tipoBinaria(f, q));
                    }
                }
            }
        }
        return cambio;
    }

    private TipoDato tipoBinaria(Funcion f, Cuarteta q) {
        String op = q.getOperador();
        if (COMPARACIONES.contains(op) || "&&".equals(op) || "||".equals(op)) return TipoDato.ENTERO;

        TipoDato t1 = tipoOp(f, q.getArg1());
        TipoDato t2 = tipoOp(f, q.getArg2());
        if ("+".equals(op) && (tipoDeclarado(q) == TipoDato.TEXTO || t1 == TipoDato.TEXTO || t2 == TipoDato.TEXTO)) {
            return TipoDato.TEXTO;
        }
        if (t1 == TipoDato.DECIMAL || t2 == TipoDato.DECIMAL) return TipoDato.DECIMAL;
        if (t1 != TipoDato.DESCONOCIDO && t2 != TipoDato.DESCONOCIDO) return TipoDato.ENTERO;
        return TipoDato.DESCONOCIDO;
    }

    private boolean fijar(Funcion f, String nombre, TipoDato t) {
        if (nombre == null || "-".equals(nombre) || t == null || t == TipoDato.DESCONOCIDO) return false;
        if (esLiteral(nombre)) return false;
        if (tipoDe(f, nombre) != TipoDato.DESCONOCIDO) return false;

        if (f.locales.contains(nombre)) f.tipos.put(nombre, t);
        else if (globales.contains(nombre)) tiposGlobales.put(nombre, t);
        else f.tipos.put(nombre, t);
        return true;
    }

    private TipoDato tipoDe(Funcion f, String s) {
        if (s == null) return TipoDato.DESCONOCIDO;
        TipoDato t = f == null ? null : f.tipos.get(s);
        if (conocido(t)) return t;
        t = tiposTemporales.get(s);
        if (conocido(t)) return t;
        t = tiposGlobales.get(s);
        if (conocido(t)) return t;
        return TipoDato.DESCONOCIDO;
    }

    private TipoDato tipoOp(Funcion f, String op) {
        if (op == null) return TipoDato.DESCONOCIDO;
        if (PATRON_CARACTER.matcher(op).matches() || esCaracterSimple(op)) return TipoDato.CARACTER;
        if (op.startsWith("\"")) return TipoDato.TEXTO;
        if (PATRON_DECIMAL.matcher(op).matches()) return TipoDato.DECIMAL;
        if (PATRON_ENTERO.matcher(op).matches() || "true".equals(op) || "false".equals(op) || "null".equals(op)) {
            return TipoDato.ENTERO;
        }
        return tipoDe(f, op);
    }

    private static boolean conocido(TipoDato t) {
        return t != null && t != TipoDato.DESCONOCIDO;
    }

    private static TipoDato tipoDeclarado(Cuarteta q) {
        return q.getTipoDeclarado() == null ? TipoDato.DESCONOCIDO : q.getTipoDeclarado();
    }

    // ============================ emisión ============================

    private String emitir() {
        if (!funciones.containsKey("main")) {
            throw new IllegalStateException("el programa no define la función main");
        }

        StringBuilder s = new StringBuilder(RUNTIME);

        if (!globales.isEmpty()) {
            s.append("/* ==== GLOBALES ==== */\n");
            for (String g : globales) {
                TipoDato t = tipoDe(null, g);
                s.append(tipoC(t)).append(" g_").append(sanitizar(g)).append(" = ").append(defecto(t)).append(";\n");
            }
            s.append('\n');
        }

        s.append("/* ==== PROTOTIPOS ==== */\n");
        for (Funcion f : funciones.values()) {
            s.append(firma(f)).append(";\n");
        }
        s.append('\n');

        for (Funcion f : funciones.values()) {
            emitirFuncion(s, f);
        }

        s.append("int main(void) {\n");
        if (funciones.containsKey("__init")) {
            s.append("    ").append(fn("__init")).append("();\n");
        }
        s.append("    ").append(fn("main")).append("();\n");
        s.append("    return 0;\n}\n");
        return s.toString();
    }

    private void emitirFuncion(StringBuilder s, Funcion f) {
        actual = f;
        pendientes.clear();

        s.append("/* ---- ").append(f.nombre).append(" ---- */\n");
        s.append(firma(f)).append(" {\n");

        for (String l : f.locales) {
            if (f.params.contains(l)) continue;
            TipoDato t = tipoDe(f, l);
            s.append("    ").append(tipoC(t)).append(' ').append(nombre(l)).append(" = ").append(defecto(t)).append(";\n");
        }
        for (String t : f.temporales) {
            TipoDato tp = tipoDe(f, t);
            s.append("    ").append(tipoC(tp)).append(' ').append(nombre(t)).append(" = ").append(defecto(tp)).append(";\n");
        }

        for (Cuarteta q : f.cuerpo) {
            String linea = cuarteta(q);
            if (!linea.isEmpty()) {
                s.append("    ").append(linea).append('\n');
            }
        }

        s.append("    return ").append(defecto(f.retorno)).append(";\n");
        s.append("}\n\n");
    }

    private String firma(Funcion f) {
        StringBuilder sb = new StringBuilder();
        sb.append(tipoC(f.retorno)).append(' ').append(fn(f.nombre)).append('(');
        if (f.params.isEmpty()) {
            sb.append("void");
        } else {
            for (int i = 0; i < f.params.size(); i++) {
                if (i > 0) sb.append(", ");
                String p = f.params.get(i);
                sb.append(tipoC(tipoDe(f, p))).append(" v_").append(sanitizar(p));
            }
        }
        return sb.append(')').toString();
    }

    // ============================ cuarteta -> C ============================

    private String cuarteta(Cuarteta q) {
        String op = q.getOperador();
        String a1 = q.getArg1(), a2 = q.getArg2(), res = q.getResultado();

        return switch (op) {
            case "label" -> etq(res) + ":;";
            case "goto" -> "goto " + etq(res) + ";";
            case "if_true" -> "if (" + operando(a1) + ") goto " + etq(res) + ";";
            case "if_false" -> "if (!" + operando(a1) + ") goto " + etq(res) + ";";
            case "if_<", "if_<=", "if_>", "if_>=", "if_==", "if_!=" -> saltoCondicional(q);

            case "=" -> nombre(res) + " = " + operando(a1) + ";";
            case "!", "not" -> nombre(res) + " = !" + operando(a1) + ";";
            case "neg" -> nombre(res) + " = -" + operando(a1) + ";";
            case "+", "-", "*", "/", "%", "<", "<=", ">", ">=", "==", "!=", "&&", "||", "/=", "%=" -> binaria(q);

            case "attr_get" -> atributoLeer(q);
            case "field_set" -> atributoEscribir(q);
            case "new" -> nombre(res) + " = reservar(" + numero(a2, op) + ");";
            case "new_array" -> nombre(res) + " = reservar(" + operando(a2) + ");";
            case "index_get" -> indiceLeer(q);
            case "index_set" -> indiceEscribir(q);

            case "param" -> {
                pendientes.add(operando(a1));
                yield "";
            }
            case "call" -> llamar(q);
            case "return" -> (a1 == null || "-".equals(a1))
                    ? "return " + defecto(actual.retorno) + ";"
                    : "return " + operando(a1) + ";";

            case "read" -> leer(q);
            case "print" -> imprimir(q);
            case "comment" -> "// " + String.valueOf(res).replace('\n', ' ');
            case "ldecl", "gdecl", "halt" -> "";
            default -> throw new IllegalStateException("cuarteta sin traducción a C: " + op);
        };
    }

    private String binaria(Cuarteta q) {
        String op = q.getOperador();
        String a1 = q.getArg1(), a2 = q.getArg2();
        String r = nombre(q.getResultado());
        TipoDato t1 = tipoOp(actual, a1), t2 = tipoOp(actual, a2);

        if ("+".equals(op) && (tipoDeclarado(q) == TipoDato.TEXTO || t1 == TipoDato.TEXTO || t2 == TipoDato.TEXTO)) {
            return r + " = concat(" + aTexto(a1) + ", " + aTexto(a2) + ");";
        }
        if (("==".equals(op) || "!=".equals(op)) && (t1 == TipoDato.TEXTO || t2 == TipoDato.TEXTO)) {
            return r + " = " + ("!=".equals(op) ? "!" : "")
                    + "igualTexto(" + operando(a1) + ", " + operando(a2) + ");";
        }

        String x = operando(a1), y = operando(a2);
        String c = switch (op) {
            case "/=" -> "/";
            case "%=" -> "%";
            default -> op;
        };

        boolean decimal = t1 == TipoDato.DECIMAL || t2 == TipoDato.DECIMAL
                || (!COMPARACIONES.contains(op) && tipoDe(actual, q.getResultado()) == TipoDato.DECIMAL);
        if (decimal) {
            if ("%".equals(c)) throw new IllegalStateException("operador decimal sin traducción: " + op);
            x = "((double)" + x + ")";
            y = "((double)" + y + ")";
        }
        return r + " = " + x + " " + c + " " + y + ";";
    }

    private String saltoCondicional(Cuarteta q) {
        String c = q.getOperador().substring(3);
        String a1 = q.getArg1(), a2 = q.getArg2();
        TipoDato t1 = tipoOp(actual, a1), t2 = tipoOp(actual, a2);

        String cond;
        if (("==".equals(c) || "!=".equals(c)) && (t1 == TipoDato.TEXTO || t2 == TipoDato.TEXTO)) {
            cond = ("!=".equals(c) ? "!" : "") + "igualTexto(" + operando(a1) + ", " + operando(a2) + ")";
        } else {
            cond = operando(a1) + " " + c + " " + operando(a2);
        }
        return "if (" + cond + ") goto " + etq(q.getResultado()) + ";";
    }

    private String atributoLeer(Cuarteta q) {
        int campo = indice(q.getArg2());
        String celda = "heap[" + operando(q.getArg1()) + " + " + campo + "]";
        String r = nombre(q.getResultado());

        if (camposDecimales.contains(campo)) return r + " = " + celda + ".d;";
        if (tipoDe(actual, q.getResultado()) == TipoDato.TEXTO) return r + " = (char*)(uintptr_t)" + celda + ".i;";
        return r + " = " + celda + ".i;";
    }

    private String atributoEscribir(Cuarteta q) {
        int campo = indice(q.getArg1());
        String celda = "heap[" + operando(q.getResultado()) + " + " + campo + "]";
        String v = operando(q.getArg2());

        if (camposDecimales.contains(campo)) return celda + ".d = (double)(" + v + ");";
        if (tipoOp(actual, q.getArg2()) == TipoDato.TEXTO) return celda + ".i = (long long)(uintptr_t)" + v + ";";
        return celda + ".i = (long long)(" + v + ");";
    }

    private String indiceLeer(Cuarteta q) {
        String celda = "heap[" + operando(q.getArg1()) + " + " + operando(q.getArg2()) + "]";
        String r = nombre(q.getResultado());
        TipoDato el = tipoDeclarado(q);
        TipoDato tr = tipoDe(actual, q.getResultado());
        if (el != TipoDato.CARACTER && el != TipoDato.DECIMAL) {
            el = (tr == TipoDato.CARACTER || tr == TipoDato.DECIMAL) ? tr : TipoDato.ENTERO;
        }

        if (el == TipoDato.DECIMAL) return r + " = " + celda + ".d;";
        if (tr == TipoDato.TEXTO) return r + " = (char*)(uintptr_t)" + celda + ".i;";
        return r + " = " + celda + ".i;";
    }

    private String indiceEscribir(Cuarteta q) {
        String celda = "heap[" + operando(q.getResultado()) + " + " + operando(q.getArg1()) + "]";
        String v = operando(q.getArg2());
        TipoDato el = tipoDeclarado(q);
        TipoDato tv = tipoOp(actual, q.getArg2());
        if (el != TipoDato.CARACTER && el != TipoDato.DECIMAL) {
            el = (tv == TipoDato.CARACTER || tv == TipoDato.DECIMAL) ? tv : TipoDato.ENTERO;
        }

        if (el == TipoDato.DECIMAL) return celda + ".d = (double)(" + v + ");";
        if (tv == TipoDato.TEXTO) return celda + ".i = (long long)(uintptr_t)" + v + ";";
        return celda + ".i = (long long)(" + v + ");";
    }

    private String llamar(Cuarteta q) {
        String funcion = q.getArg1();
        int n = Integer.parseInt(q.getArg2());
        if (pendientes.size() != n) {
            throw new IllegalStateException("llamada a '" + funcion + "' con " + pendientes.size()
                    + " param(s) pendientes, se esperaban " + n);
        }
        if (!funciones.containsKey(funcion)) {
            throw new IllegalStateException("llamada a función no definida: " + funcion);
        }

        String llamada = fn(funcion) + "(" + String.join(", ", pendientes) + ")";
        pendientes.clear();

        String res = q.getResultado();
        if (res == null || "-".equals(res)) return llamada + ";";
        return nombre(res) + " = " + llamada + ";";
    }

    private String leer(Cuarteta q) {
        String res = q.getResultado();
        TipoDato t = tipoDeclarado(q) != TipoDato.DESCONOCIDO ? tipoDeclarado(q) : tipoDe(actual, res);
        String funcion = switch (t) {
            case DECIMAL -> "leerDecimal";
            case TEXTO -> "leerTexto";
            case CARACTER -> "leerCaracter";
            case ENTERO, BOOLEANO -> "leerEntero";
            default -> throw new IllegalStateException("readln() hacia '" + res + "' con tipo " + t
                    + ". El tipo del destino no se reconoce: revisa la conversión de tipos.");
        };
        return nombre(res) + " = " + funcion + "();";
    }

    private String imprimir(Cuarteta q) {
        String a1 = q.getArg1();
        String salto = "nl".equals(q.getArg2()) ? "\\n" : "";
        String x = operando(a1);

        TipoDato t = q.getTipoDeclarado();
        if (t == null || t == TipoDato.DESCONOCIDO) t = tipoOp(actual, a1);

        return switch (t) {
            case TEXTO -> "printf(\"%s" + salto + "\", txt(" + x + "));";
            case DECIMAL -> "printf(\"%f" + salto + "\", (double)(" + x + "));";
            case CARACTER -> "printf(\"%c" + salto + "\", (char)(" + x + "));";
            default -> "printf(\"%d" + salto + "\", (int)(" + x + "));";
        };
    }

    // ============================ operandos y nombres ============================

    private String operando(String op) {
        if (op == null) throw new IllegalStateException("operando nulo");
        if (PATRON_CARACTER.matcher(op).matches()) return op.substring(1, op.length() - 1);
        if (op.startsWith("\"")) return literalC(op);
        if (esCaracterSimple(op)) return op;
        if ("true".equals(op)) return "1";
        if ("false".equals(op) || "null".equals(op)) return "0";
        if (PATRON_ENTERO.matcher(op).matches() || PATRON_DECIMAL.matcher(op).matches()) return op;
        return nombre(op);
    }

    private String aTexto(String op) {
        String x = operando(op);
        return switch (tipoOp(actual, op)) {
            case TEXTO -> x;
            case CARACTER -> "charAtexto(" + x + ")";
            case DECIMAL -> "decAtexto(" + x + ")";
            default -> "numAtexto(" + x + ")";
        };
    }

    private String nombre(String v) {
        if (actual.locales.contains(v)) return "v_" + sanitizar(v);
        if (globales.contains(v)) return "g_" + sanitizar(v);
        if (PATRON_TEMPORAL.matcher(v).matches()) return v;
        return "x_" + sanitizar(v);
    }

    private static String fn(String nombre) {
        return "f_" + sanitizar(nombre);
    }

    private static String etq(String etiqueta) {
        return "etq_" + sanitizar(etiqueta);
    }

    private static String sanitizar(String s) {
        return s.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private static String literalC(String valor) {
        return valor.replace("\r", "")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private static String tipoC(TipoDato t) {
        if (t == null) return "int";
        return switch (t) {
            case DECIMAL -> "double";
            case TEXTO -> "char*";
            case CARACTER -> "char";
            default -> "int";
        };
    }

    private static String defecto(TipoDato t) {
        if (t == null) return "0";
        return switch (t) {
            case DECIMAL -> "0.0";
            case TEXTO -> "NULL";
            default -> "0";
        };
    }

    private static int indice(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalStateException("se esperaba un índice numérico y llegó '" + s + "'");
        }
    }

    private static String numero(String valor, String operador) {
        if (valor == null || !valor.matches("\\d+")) {
            throw new IllegalStateException("La cuarteta '" + operador + "' necesita un valor numérico y recibió '" + valor + "'.");
        }
        return valor;
    }

    private static boolean esCaracterSimple(String op) {
        return op.length() == 3 && op.charAt(0) == '\'' && op.charAt(2) == '\'';
    }

    private static boolean esLiteral(String s) {
        return PATRON_ENTERO.matcher(s).matches() || PATRON_DECIMAL.matcher(s).matches()
                || PATRON_CARACTER.matcher(s).matches() || s.startsWith("\"")
                || esCaracterSimple(s) || "true".equals(s) || "false".equals(s) || "null".equals(s);
    }

    private List<String> variables(Cuarteta q) {
        String op = q.getOperador();
        List<String> r = new ArrayList<>();
        switch (op) {
            case "label", "goto", "comment", "gdecl", "ldecl", "fparam", "fbegin", "fend", "halt" -> { }
            case "if_true", "if_false", "print", "param", "return" -> r.add(q.getArg1());
            case "call", "read", "new" -> r.add(q.getResultado());
            case "new_array" -> { r.add(q.getArg2()); r.add(q.getResultado()); }
            default -> {
                r.add(q.getArg1());
                r.add(q.getArg2());
                if (!op.startsWith("if_")) r.add(q.getResultado());
            }
        }
        r.removeIf(s -> s == null || "-".equals(s) || esLiteral(s));
        return r;
    }
}