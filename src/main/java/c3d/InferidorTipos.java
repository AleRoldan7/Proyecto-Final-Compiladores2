package c3d;

import enums.TipoDato;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
public class InferidorTipos {

    public static final Set<String> REGISTROS = Set.of("fp", "sp", "retaddr", "retval", "self");
    private static final Set<String> NO_DEFINEN = Set.of("index_set", "field_set", "stack_set", "label", "goto", "halt", "comment", "print");
    private static final Set<String> BOOLEANAS = Set.of("==", "!=", "<", ">", "<=", ">=", "&&", "||", "not");

    public static Map<String,TipoDato> inferir(List<Cuarteta> cuartetas) {
        return inferir(cuartetas, Map.of());
    }

    public static Map<String, TipoDato> inferir(List<Cuarteta> cuartetas, Map<String, TipoDato> iniciales) {

        Map<String, TipoDato> tipos = new HashMap<>();

        if (iniciales != null) {

            iniciales.forEach((nombre, tipo) -> {
                if (tipo != null && tipo != TipoDato.DESCONOCIDO) {
                    tipos.put(nombre, tipo);
                }
            });
        }

        tipos.put("fp", TipoDato.ENTERO);
        tipos.put("sp", TipoDato.ENTERO);
        tipos.put("retaddr", TipoDato.ENTERO);

        for (int pasada = 0; pasada < 10; pasada++) {

            boolean cambio = false;

            for (Cuarteta cuarteta : cuartetas) {

                String op = cuarteta.getOperador();
                String res = cuarteta.getResultado();

                if (NO_DEFINEN.contains(op) || res == null || REGISTROS.contains(res)) {
                    continue;
                }

                if ("new".equals(op) || "new_array".equals(op)) {
                    cambio |= fijar(tipos, res, "new".equals(op) ? TipoDato.OBJETO : TipoDato.ESTRUCTURA);
                    continue;
                }

                if (cuarteta.getTipoDeclarado() != null) {
                    cambio |= fijar(tipos, res, cuarteta.getTipoDeclarado());
                    continue;
                }

                switch (op) {

                    case "=" -> {
                        TipoDato origen = tipoDe(cuarteta.getArg1(), tipos);

                        if (origen != TipoDato.DESCONOCIDO) {
                            cambio |= fijar(tipos, res, origen);
                        } else if (cuarteta.getArg1() != null && esTemporal(cuarteta.getArg1())) {
                            // inferencia hacia atrás: "nombre = t104" con t104 desconocido
                            TipoDato destino = tipos.get(res);
                            if (destino == TipoDato.TEXTO || destino == TipoDato.ENTERO || destino == TipoDato.DECIMAL
                                    || destino == TipoDato.BOOLEANO || destino == TipoDato.CARACTER) {
                                cambio |= fijar(tipos, cuarteta.getArg1(), destino);
                            }
                        }
                    }

                    case "read", "stack_get" -> {
                        if (cuarteta.getTipoDeclarado() != null) {
                            cambio |= fijar(tipos, res, cuarteta.getTipoDeclarado());
                        }
                    }

                    case "attr_get", "index_get" -> {
                        if (!tipos.containsKey(res)) {
                            cambio |= fijar(tipos, res, TipoDato.DESCONOCIDO);
                        }
                    }

                    default -> {
                        if (!op.startsWith("if_")) {
                            cambio |= fijar(tipos, res, tipoOperacion(op, cuarteta, tipos));
                        }
                    }
                }
            }

            if (!cambio) {
                break;
            }
        }

        return tipos;
    }

    private static TipoDato tipoOperacion(String op, Cuarteta q, Map<String, TipoDato> tipos) {

        TipoDato izq = tipoDe(q.getArg1(), tipos);
        TipoDato der = q.getArg2() != null ? tipoDe(q.getArg2(), tipos) : izq;

        if (BOOLEANAS.contains(op)) {
            return TipoDato.BOOLEANO;
        }
        if ("+".equals(op) && (izq == TipoDato.TEXTO || der == TipoDato.TEXTO)) {
            return TipoDato.TEXTO;
        }
        if (izq == TipoDato.DECIMAL || der == TipoDato.DECIMAL) {
            return TipoDato.DECIMAL;
        }
        if (izq == TipoDato.DESCONOCIDO || der == TipoDato.DESCONOCIDO) {
            return TipoDato.DESCONOCIDO;
        }
        return TipoDato.ENTERO;
    }

    private static boolean fijar(Map<String, TipoDato> tipos, String nombre, TipoDato nuevo) {

        if (nuevo == null) {
            return false;
        }

        TipoDato anterior = tipos.get(nombre);

        if (nuevo == TipoDato.DESCONOCIDO && anterior != null) {
            return false;
        }
        if (anterior == nuevo) {
            return false;
        }

        tipos.put(nombre, nuevo);
        return true;
    }

    public static TipoDato tipoDe(String valor, Map<String, TipoDato> tipos) {

        if (valor == null) {
            return TipoDato.DESCONOCIDO;
        }

        String v = normalizar(valor);

        if (v.length() >= 3 && v.startsWith("'") && v.endsWith("'")) {
            return TipoDato.CARACTER;
        }
        if (valor.startsWith("\"")) {
            return TipoDato.TEXTO;
        }
        if (esEntero(valor)) {
            return TipoDato.ENTERO;
        }
        if (esDecimal(valor)) {
            return TipoDato.DECIMAL;
        }

        TipoDato tipo = tipos.get(valor);
        return tipo != null ? tipo : TipoDato.DESCONOCIDO;
    }

    public static String normalizar(String v) {
        if (v == null || v.length() < 4) return v;
        if (v.startsWith("\"'") && v.endsWith("'\"")) {
            return v.substring(1, v.length() - 1);
        }
        return v;
    }

    private static boolean esTemporal(String valor) {

        if (valor == null || valor.length() < 2) {
            return false;
        }

        if (valor.charAt(0) != 't') {
            return false;
        }

        for (int i = 1; i < valor.length(); i++) {
            if (!Character.isDigit(valor.charAt(i))) {
                return false;
            }
        }

        return true;
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

    private static boolean esDecimal(String valor) {

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
}
