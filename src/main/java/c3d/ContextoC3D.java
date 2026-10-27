package c3d;

import enums.TipoDato;
import exceptiones.C3DException;
import lombok.Getter;

import java.util.*;
import java.util.regex.Pattern;

@Getter
public class ContextoC3D {

    private static final TipoDato TIPO_ENTERO = TipoDato.ENTERO;
    private static final TipoDato TIPO_BOOLEANO = TipoDato.BOOLEANO;

    private static final int OFFSET_PRIMER_PARAM = 2;
    private static final int LIMITE_CUARTETAS = 2_000_000;
    private static final String BLOQUE_GLOBAL = "__globales";
    private static final Pattern TEMPORAL = Pattern.compile("t\\d+");

    private final List<Cuarteta> cuartetas = new ArrayList<>();
    private final Set<String> funciones = new LinkedHashSet<>();
    private final List<String> cadenas = new ArrayList<>();
    private final Map<String, Map<String, Integer>> disposiciones = new HashMap<>();
    private final Deque<String[]> destinos = new ArrayDeque<>();
    private final Map<String, List<CampoDef>> definicionesEstructura = new LinkedHashMap<>();
    private int contadorTemporales = 0;
    private int contadorEtiquetas = 0;
    private String claseActual;
    private final Map<String, Map<String, CampoLayout>> layoutsEstructura = new HashMap<>();
    private final Map<String, Integer> tamaniosEstructura = new HashMap<>();
    private final Set<String> nombresEstructuras = new HashSet<>();
    private final Map<String, Map<Integer, TipoDato>> tiposCelda = new HashMap<>();
    private final Map<String, Map<String, TipoDato>> tiposAtributo = new HashMap<>();
    private final Map<String, List<Integer>> tamaniosArreglo = new HashMap<>();

    private final Map<String, TipoDato> tipoVariables = new HashMap<>();
    private final Map<String, TipoDato> tipoTemporales = new HashMap<>();

    // ---------- Estado de la pila manual ----------
    private final Map<String, Integer> offsetsLocales = new HashMap<>();
    private final Map<String, Integer> slotsTemporales = new HashMap<>();
    private int siguienteOffset = OFFSET_PRIMER_PARAM;
    private int indicePrologo = -1;
    private String funcionActual;
    private int contadorSitiosRetorno = 0;
    private final List<Integer> sitiosRetorno = new ArrayList<>();

    // ---------- Registro de funciones y llamadas ----------
    private final Set<String> funcionesDefinidas = new HashSet<>();
    private final Map<String, String> llamadasPendientes = new LinkedHashMap<>();
    private final Map<String, Set<String>> clasesPorMetodo = new HashMap<>();

    private final Map<String, DefinicionClase> definicionesClase = new LinkedHashMap<>();
    private record DefinicionClase(String superclase, List<String> atributos, List<TipoDato> tipos) {}


    // ---------- Inicio / globales ----------
    private boolean programaIniciado = false;
    private boolean programaFinalizado = false;
    private int indiceSaltoInicial = -1;
    private String primeraEtiquetaGlobales;
    private int indiceSaltoSalidaGlobales = -1;
    private int contadorGlobales = 0;

    private final Map<String, TipoDato> tiposLocales = new HashMap<>();
    private final Map<String, TipoDato> tiposRetorno = new HashMap<>();

    private final Map<String, Integer> idsClase = new LinkedHashMap<>();
    private final Set<String> locales = new HashSet<>();
    private final Set<String> globalesDeclaradas = new HashSet<>();
    // =====================================================
    //  Temporales
    // =====================================================
    public String nuevoTemporal(TipoDato tipoTemporal) {
        String temporal = "t" + contadorTemporales++;
        tipoTemporales.put(temporal, tipoTemporal);
        return temporal;
    }

    private String temporalAuxiliar(TipoDato tipo) {
        String temporal = "t" + contadorTemporales++;
        tipoTemporales.put(temporal, tipo);
        return temporal;
    }

    private String sumaAuxiliar(String base, int desplazamiento) {
        String temporal = temporalAuxiliar(TIPO_ENTERO);
        cuartetas.add(new Cuarteta("+", base, String.valueOf(desplazamiento), temporal, TIPO_ENTERO));
        return temporal;
    }

    public TipoDato getTipoTemporal(String temporal) {
        return tipoTemporales.getOrDefault(temporal, TipoDato.DESCONOCIDO);
    }

    public String nuevaEtiqueta() {
        return "L" + contadorEtiquetas++;
    }

    // =====================================================
    //  Emisión de cuartetas
    // =====================================================
    public void agregar(String operador, String arg1, String arg2, String resultado) {
        agregar(operador, arg1, arg2, resultado, null);
    }

    public void agregar(String operador, String arg1, String arg2,
                        String resultado, TipoDato tipo) {

        if ("=".equals(operador) && arg1 == null) {
            throw new IllegalStateException("asignación a '" + resultado + "' sin valor. "
                    + "Probablemente se asignó el resultado de una llamada void.");
        }

        // "self" vive en la pila: se lee a un temporal antes de usarlo
        if (!operador.startsWith("stack_")) {
            arg1 = resolverSelf(arg1);
            arg2 = resolverSelf(arg2);
            if ("field_set".equals(operador) || "index_set".equals(operador)) {
                resultado = resolverSelf(resultado);
            }
        }

        cuartetas.add(new Cuarteta(operador, arg1, arg2, resultado, tipo));
    }

    private String resolverSelf(String valor) {
        if ("self".equals(valor) && esLocal("self")) {
            return leerVariable("self", TipoDato.OBJETO);
        }
        return valor;
    }

    public void emitir(String operador, String arg1, String arg2, String resultado) {
        agregar(operador, arg1, arg2, resultado);
    }

    public void agregarEtiqueta(String etiqueta) {
        agregar("label", null, null, etiqueta);
    }

    public void salto(String etiqueta) {
        agregar("goto", null, null, etiqueta);
    }

    public void saltoSiVerdadero(String condicion, String etiqueta) {
        agregar("if_true", condicion, null, etiqueta);
    }

    public void saltoSiFalso(String condicion, String etiqueta) {
        agregar("if_false", condicion, null, etiqueta);
    }

    public void emitirComentario(String texto) {
        agregar("comment", null, null, texto);
    }

    public String unaria(String operador, String operando, TipoDato tipo) {
        String temporal = nuevoTemporal(tipo);
        cuartetas.add(new Cuarteta(operador, operando, null, temporal, tipo));
        return temporal;
    }

    public String binaria(String operador, String izquierda, String derecha, TipoDato tipo) {
        String temporal = nuevoTemporal(tipo);
        cuartetas.add(new Cuarteta(operador, izquierda, derecha, temporal, tipo));
        return temporal;
    }

    public List<Cuarteta> getCuartetas() {
        return cuartetas;
    }

    // =====================================================
    //  Asignación
    // =====================================================
    public void asignar(String destino, String valor) {
        if (!esLocal(destino) && esAtributo(destino)) {
            int indice = desplazamiento(claseActual, destino);
            String self = leerVariable("self", TipoDato.OBJETO);
            cuartetas.add(new Cuarteta("field_set", String.valueOf(indice), valor, self, null));
            if (valor == null) {
                throw new IllegalStateException("asignación a '" + destino + "' sin valor.");
            }
        } else {
            cuartetas.add(new Cuarteta("=", valor, null, destino, null));
        }
    }

    // =====================================================
    //  Nombres de funciones (con aridad para soportar sobrecarga)
    // =====================================================
    public static String nombreFuncion(String clase, String metodo) {
        return clase == null ? metodo : clase + "_" + metodo;
    }

    public static String nombreConstructor(String clase) {
        return "init_" + clase;
    }

    /** Nombre real de la etiqueta: nombre__aridad (main no se modifica). */
    public static String nombreReal(String nombre, int aridad) {
        return "main".equals(nombre) ? nombre : nombre + "__" + aridad;
    }

    public void registrarFuncion(String nombre) {
        funciones.add(nombre);
    }

    public boolean existeFuncion(String nombre) {
        return funciones.contains(nombre);
    }

    public Set<String> getFunciones() {
        return funciones;
    }

    /** Se llama desde Clase.registrarDisposicion, antes de generar cualquier código. */
    public void registrarMetodo(String clase, String metodo, int aridad) {
        clasesPorMetodo
                .computeIfAbsent(metodo + "/" + aridad, k -> new LinkedHashSet<>())
                .add(clase);
    }

    public String resolverMetodo(String claseReceptor, String metodo, int aridad) {

        if (claseReceptor != null) {
            String owner = definidorDe(claseReceptor, metodo, aridad);
            if (owner != null) {
                return nombreFuncion(owner, metodo);
            }
        }

        Set<String> clases = clasesPorMetodo.get(metodo + "/" + aridad);

        if (clases == null || clases.isEmpty()) {
            return metodo;
        }

        if (clases.size() > 1) {
            throw new IllegalStateException("El método '" + metodo + "' existe en varias clases " + clases
                    + " y LlamadaMetodo no trae claseReceptor: el análisis semántico debe asignarlo.");
        }

        return nombreFuncion(clases.iterator().next(), metodo);
    }

    public void iniciarPrograma() {
        programaIniciado = true;
    }

    public void finalizarPrograma() {
        if (programaFinalizado) return;
        programaFinalizado = true;

        List<String> faltantes = new ArrayList<>();
        for (Map.Entry<String, String> e : llamadasPendientes.entrySet()) {
            if (!funcionesDefinidas.contains(e.getKey())) {
                faltantes.add(e.getValue());
            }
        }
        if (!faltantes.isEmpty()) {
            throw new IllegalStateException("Se llama a funciones o métodos que no existen en el C3D:\n  - "
                    + String.join("\n  - ", faltantes));
        }
    }

    public void abrirGlobales() {
        funcionActual = "__init";
        locales.clear();
        tiposLocales.clear();
        cuartetas.add(new Cuarteta("fbegin", "__init", null, null, null));
    }

    public void cerrarGlobales() {
        cuartetas.add(new Cuarteta("fend", "__init", null, null, null));
        funcionActual = null;
        locales.clear();
    }

    public void abrirFuncion(String nombre, List<String> parametros, List<TipoDato> tipos) {
        String real = nombreReal(nombre, parametros.size());

        if (!funcionesDefinidas.add(real)) {
            throw new IllegalStateException("La función '" + nombre + "' con " + parametros.size()
                    + " parámetro(s) (contando self) está definida más de una vez.");
        }

        registrarFuncion(nombre);
        funcionActual = real;
        locales.clear();
        tiposLocales.clear();
        cuartetas.add(new Cuarteta("fbegin", real, null, null, null));

        for (int i = 0; i < parametros.size(); i++) {
            String p = parametros.get(i);
            TipoDato t = (tipos != null && i < tipos.size()) ? tipos.get(i) : TipoDato.DESCONOCIDO;
            locales.add(p);
            tiposLocales.put(p, t);
            cuartetas.add(new Cuarteta("fparam", p, null, null, t));
        }
    }

    public void abrirFuncion(String nombre, List<String> parametros) {
        abrirFuncion(nombre, parametros, null);
    }

    public void cerrarFuncion(String nombre) {
        cuartetas.add(new Cuarteta("fend", funcionActual, null, null, null));
        funcionActual = null;
        locales.clear();
    }

    public boolean enFuncion() {
        return funcionActual != null && !"__init".equals(funcionActual);
    }


    public String getFuncionActual() {
        return funcionActual;
    }

    private void iniciarMarco() {
        offsetsLocales.clear();
        slotsTemporales.clear();
        tiposLocales.clear();
        siguienteOffset = OFFSET_PRIMER_PARAM;
        indicePrologo = cuartetas.size();
        agregar("=", "fp", null, "sp");
    }

    private void parchearMarco() {
        cuartetas.set(indicePrologo,
                new Cuarteta("+", "fp", String.valueOf(siguienteOffset), "sp", TIPO_ENTERO));
    }

    // =====================================================
    //  Variables locales
    // =====================================================
    public void declararLocal(String nombre, TipoDato tipo) {
        locales.add(nombre);
        tiposLocales.put(nombre, tipo);
        cuartetas.add(new Cuarteta("ldecl", nombre, null, null, tipo));
    }

    public void declararGlobal(String nombre, TipoDato tipo) {
        registrarGlobal(nombre, tipo);
        if (globalesDeclaradas.add(nombre)) {
            cuartetas.add(new Cuarteta("gdecl", nombre, null, null, tipo));
        }
    }

    public TipoDato tipoDeVariable(String nombre) {
        TipoDato local = tiposLocales.get(nombre);
        if (local != null) return local;
        return tipoVariables.getOrDefault(nombre, TipoDato.DESCONOCIDO);
    }

    public void registrarGlobal(String nombre, TipoDato tipo) {
        tipoVariables.put(nombre, tipo);
        tipoTemporales.put(nombre, tipo);
    }

    public boolean esLocal(String nombre) {
        return locales.contains(nombre);
    }

    public String leerVariable(String nombre, TipoDato tipo) {
        return nombre;
    }


    // =====================================================
    //  Llamada y retorno
    // =====================================================
    private void guardarEnPila(String base, int offset, String valor) {
        String idx = sumaAuxiliar(base, offset);
        agregar("stack_set", idx, valor, null);
    }

    public String llamar(String funcion, List<String> argumentos, TipoDato tipoRetorno) {

        if (funcion.equals("read") || funcion.equals("leer") || funcion.equals("readln")) {
            String resultado = nuevoTemporal(tipoRetorno != null ? tipoRetorno : TipoDato.DESCONOCIDO);
            agregar("read", null, null, resultado, tipoRetorno);
            return resultado;
        }

        String real = nombreReal(funcion, argumentos.size());
        llamadasPendientes.putIfAbsent(real,
                funcion + " con " + argumentos.size() + " argumento(s) (contando self)");

        for (String arg : argumentos) {
            cuartetas.add(new Cuarteta("param", arg, null, null, null));
        }

        TipoDato tipoFinal = tipoRetorno;
        if (tipoFinal == null || tipoFinal == TipoDato.DESCONOCIDO) {
            tipoFinal = tiposRetorno.getOrDefault(real, TipoDato.DESCONOCIDO);
        }

        if (tipoFinal == TipoDato.VOID) {
            cuartetas.add(new Cuarteta("call", real, String.valueOf(argumentos.size()), "-", null));
            return null;
        }

        String resultado = nuevoTemporal(tipoFinal);
        cuartetas.add(new Cuarteta("call", real, String.valueOf(argumentos.size()), resultado, tipoFinal));
        return resultado;
    }

    public void retornar(String valor) {
        cuartetas.add(new Cuarteta("return", valor == null ? "-" : valor, null, null, null));
    }


    private void expandirLlamadas() {

        int ini = indicePrologo;
        int fin = cuartetas.size();
        List<Cuarteta> seg = new ArrayList<>(cuartetas.subList(ini, fin));
        int n = seg.size();

        boolean hayLlamadas = false;
        Map<String, Integer> posEtiqueta = new HashMap<>();
        for (int i = 0; i < n; i++) {
            String op = seg.get(i).getOperador();
            if ("call_save".equals(op)) hayLlamadas = true;
            if ("label".equals(op)) posEtiqueta.put(seg.get(i).getResultado(), i);
        }
        if (!hayLlamadas) {
            return;
        }

        // ---- vida de temporales (análisis hacia atrás hasta punto fijo) ----
        List<Set<String>> vivasEntrada = new ArrayList<>(n);
        for (int i = 0; i < n; i++) vivasEntrada.add(new HashSet<>());

        boolean cambio = true;
        while (cambio) {
            cambio = false;
            for (int i = n - 1; i >= 0; i--) {
                Cuarteta q = seg.get(i);

                Set<String> entrada = new HashSet<>();
                for (int s : sucesores(seg, i, posEtiqueta)) {
                    entrada.addAll(vivasEntrada.get(s));
                }

                String def = definicion(q);
                if (def != null) entrada.remove(def);
                entrada.addAll(usos(q));

                if (!entrada.equals(vivasEntrada.get(i))) {
                    vivasEntrada.set(i, entrada);
                    cambio = true;
                }
            }
        }

        Map<String, Set<String>> vivasPorLlamada = new HashMap<>();
        for (int i = 0; i < n; i++) {
            if ("call_restore".equals(seg.get(i).getOperador())) {
                vivasPorLlamada.put(seg.get(i).getResultado(), vivasEntrada.get(i));
            }
        }

        // ---- reescritura del tramo ----
        List<Cuarteta> nuevo = new ArrayList<>(n);
        for (Cuarteta q : seg) {
            String op = q.getOperador();

            if ("call_save".equals(op) || "call_restore".equals(op)) {
                boolean guardar = "call_save".equals(op);
                Set<String> vivas = vivasPorLlamada.getOrDefault(q.getResultado(), Set.of());

                List<String> ordenadas = new ArrayList<>(vivas);
                ordenadas.sort(Comparator.comparingInt(t -> Integer.parseInt(t.substring(1))));

                for (String t : ordenadas) {
                    int slot = slotsTemporales.computeIfAbsent(t, k -> siguienteOffset++);
                    String idx = temporalAuxiliar(TIPO_ENTERO);
                    nuevo.add(new Cuarteta("+", "fp", String.valueOf(slot), idx, TIPO_ENTERO));
                    if (guardar) {
                        nuevo.add(new Cuarteta("stack_set", idx, t, null, null));
                    } else {
                        nuevo.add(new Cuarteta("stack_get", idx, null, t, null));
                    }
                }
            } else {
                nuevo.add(q);
            }
        }

        cuartetas.subList(ini, fin).clear();
        cuartetas.addAll(ini, nuevo);
    }

    private static List<Integer> sucesores(List<Cuarteta> seg, int i, Map<String, Integer> pos) {

        Cuarteta q = seg.get(i);
        String op = q.getOperador();
        boolean sigue = i + 1 < seg.size();
        List<Integer> r = new ArrayList<>(2);

        if ("goto".equals(op)) {
            String destino = q.getResultado();
            if (destino != null && destino.startsWith("func_")) {
                // llamada: la ejecución vuelve a la etiqueta ret_N que sigue
                if (sigue) r.add(i + 1);
            } else {
                Integer p = pos.get(destino);
                if (p != null) r.add(p);
            }
        } else if (op.startsWith("if_")) {
            if (sigue) r.add(i + 1);
            Integer p = pos.get(q.getResultado());
            if (p != null) r.add(p);
        } else if (sigue) {
            r.add(i + 1);
        }

        return r;
    }

    private static boolean esTemporal(String s) {
        return s != null && TEMPORAL.matcher(s).matches();
    }

    private static void agregarUso(List<String> lista, String s) {
        if (esTemporal(s)) lista.add(s);
    }

    private static List<String> usos(Cuarteta q) {
        List<String> r = new ArrayList<>(3);

        switch (q.getOperador()) {
            case "label", "goto", "halt", "comment", "call_save", "call_restore", "new", "read" -> { }
            case "if_true", "if_false", "print" -> agregarUso(r, q.getArg1());
            case "field_set" -> {
                agregarUso(r, q.getArg2());
                agregarUso(r, q.getResultado());
            }
            case "index_set" -> {
                agregarUso(r, q.getArg1());
                agregarUso(r, q.getArg2());
                agregarUso(r, q.getResultado());
            }
            case "new_array" -> agregarUso(r, q.getArg2());
            default -> {
                agregarUso(r, q.getArg1());
                agregarUso(r, q.getArg2());
            }
        }

        return r;
    }

    private static String definicion(Cuarteta q) {
        String op = q.getOperador();

        switch (op) {
            case "label", "goto", "halt", "comment", "call_save", "call_restore",
                 "stack_set", "field_set", "index_set", "print", "if_true", "if_false":
                return null;
            default:
                if (op.startsWith("if_")) return null;
                return esTemporal(q.getResultado()) ? q.getResultado() : null;
        }
    }

    // =====================================================
    //  Cadenas
    // =====================================================
    public List<String> getCadenas() {
        return cadenas;
    }

    // =====================================================
    //  Objetos en el heap
    // =====================================================

    public void registrarClase(String clase, String superclase, List<String> atributos, List<TipoDato> tipos) {
        definicionesClase.put(clase, new DefinicionClase(superclase, atributos, tipos));
        disposiciones.remove(clase);
        tiposAtributo.remove(clase);
    }

    public void registrarClase(String clase, List<String> atributos) {
        registrarClase(clase, null, atributos, null);
    }

    public void registrarClase(String clase, List<String> atributos, List<TipoDato> tipos) {
        registrarClase(clase, null, atributos, tipos);
    }

    public TipoDato tipoDeAtributo(String clase, String atributo) {
        if (definicionesClase.containsKey(clase)) {
            asegurarDisposicion(clase, new HashSet<>());
        }
        Map<String, TipoDato> tiposMapa = tiposAtributo.get(clase);
        if (tiposMapa == null) {
            return TipoDato.DESCONOCIDO;
        }
        return tiposMapa.getOrDefault(atributo, TipoDato.DESCONOCIDO);
    }

    /*
    private Map<String, Integer> disposicion(String clase) {
        Map<String, Integer> mapa = disposiciones.get(clase);
        if (mapa == null) {
            throw new IllegalStateException("La clase '" + clase + "' no tiene disposición en el heap (¿se generó antes su archivo?)");
        }
        return mapa;
    }
     */

    private void asegurarDisposicion(String clase, Set<String> visitando) {
        if (disposiciones.containsKey(clase)) {
            return;
        }

        DefinicionClase def = definicionesClase.get(clase);
        if (def == null) {
            throw new IllegalStateException("La clase '" + clase
                    + "' no tiene disposición en el heap (¿se registró su archivo?)");
        }

        if (!visitando.add(clase)) {
            throw new IllegalStateException("Herencia circular en la clase '" + clase + "'");
        }

        Map<String, Integer> mapa = new LinkedHashMap<>();
        Map<String, TipoDato> tiposMapa = new LinkedHashMap<>();
        int desplazamiento = 1;

        // 1) Atributos heredados, en las mismas posiciones que en el padre
        String padre = def.superclase();
        if (padre != null) {
            if (!definicionesClase.containsKey(padre)) {
                throw new IllegalStateException("La superclase '" + padre
                        + "' de '" + clase + "' no existe");
            }
            asegurarDisposicion(padre, visitando);

            Map<String, Integer> mapaPadre = disposiciones.get(padre);
            Map<String, TipoDato> tiposPadre = tiposAtributo.get(padre);

            for (Map.Entry<String, Integer> e : mapaPadre.entrySet()) {
                mapa.put(e.getKey(), e.getValue());
                tiposMapa.put(e.getKey(), tiposPadre.getOrDefault(e.getKey(), TipoDato.DESCONOCIDO));
            }
            desplazamiento = 1 + mapaPadre.size();
        }

        // 2) Atributos propios, a partir de la posición siguiente
        List<String> atributos = def.atributos();
        List<TipoDato> tipos = def.tipos();
        for (int i = 0; i < atributos.size(); i++) {
            String atributo = atributos.get(i);
            if (mapa.containsKey(atributo)) {
                throw new IllegalStateException("La clase '" + clase + "' redeclara el atributo '"
                        + atributo + "' (ocultamiento no soportado)");
            }
            mapa.put(atributo, desplazamiento++);
            TipoDato tipo = (tipos != null && i < tipos.size()) ? tipos.get(i) : TipoDato.DESCONOCIDO;
            tiposMapa.put(atributo, tipo);
        }

        disposiciones.put(clase, mapa);
        tiposAtributo.put(clase, tiposMapa);
        visitando.remove(clase);
    }

    private Map<String, Integer> disposicion(String clase) {
        asegurarDisposicion(clase, new HashSet<>());
        return disposiciones.get(clase);
    }

    public int tamanio(String clase) {
        return disposicion(clase).size() + 1;
    }

    public String getClaseActual() {
        return claseActual;
    }

    public void setClaseActual(String claseActual) {
        this.claseActual = claseActual;
    }

    public boolean esAtributo(String nombre) {
        return claseActual != null && disposicion(claseActual).containsKey(nombre);
    }

    // =====================================================
    //  break / continue
    // =====================================================
    public void entrarCiclo(String etiquetaContinue, String etiquetaBreak) {
        destinos.push(new String[]{etiquetaContinue, etiquetaBreak});
    }

    public void salirCiclo() {
        destinos.pop();
    }

    public void entrarSwitch(String etiquetaBreak) {
        destinos.push(new String[]{null, etiquetaBreak});
    }

    public void salirSwitch() {
        destinos.pop();
    }

    public String etiquetaBreak() {
        return destinos.isEmpty() ? null : destinos.peek()[1];
    }

    public String etiquetaContinue() {
        for (String[] destino : destinos) {          // recorre desde el más interno
            if (destino[0] != null) {
                return destino[0];
            }
        }
        return null;
    }

    public static boolean esImpresion(String nombre) {
        return Set.of("print", "println", "imprimir").contains(nombre);
    }

    public static boolean imprimeConSalto(String nombre) {
        return Set.of("println", "imprimir").contains(nombre);
    }

    public static boolean esLectura(String nombre) {
        return Set.of("readln", "leer").contains(nombre);
    }

    public String registrarString(String texto) {
        cadenas.add(texto);
        String contenido = texto.replace("\r", "").replace("\n", "\\n").replace("\t", "\\t");
        boolean yaTieneComillas = contenido.length() >= 2 && contenido.startsWith("\"") && contenido.endsWith("\"");
        return yaTieneComillas ? contenido : "\"" + contenido + "\"";
    }

    public int desplazamiento(String clase, String atributo) {
        Integer d = disposicion(clase).get(atributo);
        if (d == null) {
            throw new IllegalStateException("La clase '" + clase + "' no tiene el atributo '" + atributo + "'");
        }
        return d;
    }

    public int desplazamientoDe(String clase, String atributo) {

        if (clase != null) {
            return desplazamiento(clase, atributo);
        }

        for (String c : definicionesClase.keySet()) {
            asegurarDisposicion(c, new HashSet<>());
        }

        Integer encontrado = null;
        for (Map<String, Integer> mapa : disposiciones.values()) {
            Integer d = mapa.get(atributo);
            if (d == null) continue;
            if (encontrado != null && !encontrado.equals(d)) {
                throw new IllegalStateException("El atributo '" + atributo
                        + "' está en varias clases con distinta posición: falta conocer la clase del objeto");
            }
            encontrado = d;
        }

        if (encontrado == null) {
            throw new IllegalStateException("Ninguna clase tiene el atributo '" + atributo + "'");
        }
        return encontrado;
    }

    public void registrarTamaniosArreglo(String nombreVariable, List<Integer> tamanios) {
        tamaniosArreglo.put(nombreVariable, tamanios);
    }

    public List<Integer> tamaniosDeArreglo(String nombreVariable) {
        return tamaniosArreglo.get(nombreVariable);
    }

    public record CampoLayout(int offset, int ancho, boolean esEmbebido, String tipoAnidado) {}

    public record CampoDef(String nombre, boolean esEmbebido, String tipoAnidado, TipoDato tipo, int anchoDeclarado) {
        public CampoDef(String nombre, boolean esEmbebido, String tipoAnidado, TipoDato tipo) {
            this(nombre, esEmbebido, tipoAnidado, tipo, 1);
        }
    }

    public void registrarEstructura(String nombre, List<CampoDef> campos) {
        nombresEstructuras.add(nombre);
        definicionesEstructura.put(nombre, campos);
    }

    public boolean esEstructura(String tipo) {
        return nombresEstructuras.contains(tipo);
    }

    public int tamanioEstructura(String tipo) {
        Integer t = tamaniosEstructura.get(tipo);
        if (t == null) {
            throw new IllegalStateException("La estructura '" + tipo + "' no tiene tamaño registrado "
                    + "(¿se registró antes de usarla? el orden del .y importa)");
        }
        return t;
    }

    public CampoLayout layoutEstructura(String tipo, String campo) {
        Map<String, CampoLayout> layout = layoutsEstructura.get(tipo);
        if (layout == null) {
            throw new IllegalStateException("La estructura '" + tipo + "' no tiene layout registrado");
        }
        CampoLayout c = layout.get(campo);
        if (c == null) {
            throw new IllegalStateException("La estructura '" + tipo + "' no tiene el campo '" + campo + "'");
        }
        return c;
    }

    public List<String> camposDeEstructura(String tipo) {
        Map<String, CampoLayout> layout = layoutsEstructura.get(tipo);
        if (layout == null) {
            throw new IllegalStateException("La estructura '" + tipo + "' no tiene layout registrado");
        }
        return new ArrayList<>(layout.keySet());
    }

    public void resolverEstructuras() {
        for (String nombre : definicionesEstructura.keySet()) {
            calcularLayoutEstructura(nombre, new HashSet<>());
        }
    }

    private void calcularLayoutEstructura(String nombre, Set<String> visitando) {

        if (tamaniosEstructura.containsKey(nombre)) {
            return;
        }

        if (!visitando.add(nombre)) {
            throw new IllegalStateException("Referencia circular entre estructuras: " + nombre);
        }

        List<CampoDef> campos = definicionesEstructura.get(nombre);

        if (campos == null) {
            throw new IllegalStateException("No existe la definición de la estructura '" + nombre + "'");
        }

        Map<String, CampoLayout> layout = new LinkedHashMap<>();
        Map<Integer, TipoDato> celdas = new HashMap<>();
        int offset = 0;

        for (CampoDef campo : campos) {

            int ancho = 1;
            boolean embebidoFinal = campo.esEmbebido();
            String tipoAnidadoFinal = campo.tipoAnidado();

            if (campo.esEmbebido() && campo.tipoAnidado() != null) {

                String tipoAnidado = campo.tipoAnidado();

                if (!definicionesEstructura.containsKey(tipoAnidado)) {
                    throw new IllegalStateException(
                            "La estructura '" + nombre + "' utiliza la estructura '"
                                    + tipoAnidado + "', pero no existe"
                    );
                }

                calcularLayoutEstructura(tipoAnidado, visitando);
                ancho = tamanioEstructura(tipoAnidado);

                Map<Integer, TipoDato> celdasAnidadas = tiposCelda.get(tipoAnidado);
                for (int k = 0; k < ancho; k++) {
                    celdas.put(offset + k, celdasAnidadas.get(k));
                }

            } else if (campo.anchoDeclarado() > 1) {
                ancho = 1;
                embebidoFinal = false;
                tipoAnidadoFinal = null;

                celdas.put(offset, TipoDato.ESTRUCTURA);
            } else {
                celdas.put(offset, campo.tipo());
            }

            layout.put(
                    campo.nombre(),
                    new CampoLayout(offset, ancho, embebidoFinal, tipoAnidadoFinal)
            );

            offset = offset + ancho;
        }
        tiposCelda.put(nombre, celdas);
        layoutsEstructura.put(nombre, layout);
        tamaniosEstructura.put(nombre, offset);

        visitando.remove(nombre);
    }

    public String llamarVirtual(String claseEstatica, String metodo,
                                List<String> argumentos, TipoDato tipoRetorno) {

        int aridad = argumentos.size();
        String self = argumentos.get(0);

        String ownerEstatico = definidorDe(claseEstatica, metodo, aridad);
        String base = nombreFuncion(ownerEstatico != null ? ownerEstatico : claseEstatica, metodo);
        TipoDato tipoReal = tipoRetornoDe(base, aridad, tipoRetorno);
        boolean esVoid = tipoReal == TipoDato.VOID;

        String resultado = esVoid ? null : nuevoTemporal(tipoReal);
        String fin = nuevaEtiqueta();

        String id = temporalAuxiliar(TIPO_ENTERO);
        agregar("attr_get", self, "0", id, TIPO_ENTERO);

        for (String clase : definicionesClase.keySet()) {
            if (!esSubtipo(clase, claseEstatica)) continue;

            String owner = definidorDe(clase, metodo, aridad);
            if (owner == null) continue;

            String siguiente = nuevaEtiqueta();
            String cond = binaria("==", id, String.valueOf(idDeClase(clase)), TIPO_BOOLEANO);
            saltoSiFalso(cond, siguiente);

            String r = llamar(nombreFuncion(owner, metodo), argumentos, tipoReal);
            if (!esVoid) agregar("=", r, null, resultado, tipoReal);
            salto(fin);

            agregarEtiqueta(siguiente);
        }

        String r = llamar(base, argumentos, tipoReal);
        if (!esVoid) agregar("=", r, null, resultado, tipoReal);

        agregarEtiqueta(fin);
        return resultado;
    }

    private TipoDato tipoRetornoDe(String funcion, int aridad, TipoDato dado) {
        if (dado != null && dado != TipoDato.DESCONOCIDO) {
            return dado;
        }
        return tiposRetorno.getOrDefault(nombreReal(funcion, aridad), TipoDato.DESCONOCIDO);
    }

    public TipoDato tipoDeCelda(String tipoEstructura, int offsetLocal) {
        return tiposCelda.getOrDefault(tipoEstructura, Map.of())
                .getOrDefault(offsetLocal, TipoDato.DESCONOCIDO);
    }

    private final Map<String, String> tipoBaseArreglo = new HashMap<>();

    public void registrarTipoBaseArreglo(String nombreVariable, String tipoBase) {
        tipoBaseArreglo.put(nombreVariable, tipoBase);
    }

    public String tipoBaseDeArreglo(String nombreVariable) {
        return tipoBaseArreglo.get(nombreVariable);
    }

    public void registrarVariable(String nombre, TipoDato dato) {
        tipoVariables.put(nombre, dato);
    }

    public void registrarRetorno(String nombre, int aridad, TipoDato tipo) {
        tiposRetorno.put(nombreReal(nombre, aridad), tipo);
    }

    public String resolverMetodoEstatico(String clase, String metodo, int aridad) {
        if (clase == null) {
            throw new C3DException("'super' solo puede usarse en una clase que tiene extends");
        }
        String owner = definidorDe(clase, metodo, aridad);
        if (owner == null) {
            throw new C3DException("La clase '" + clase + "' no define el método '"
                    + metodo + "' con " + (aridad - 1) + " parámetro(s)");
        }
        return nombreFuncion(owner, metodo);
    }

    private boolean esSubtipo(String clase, String ancestro) {
        for (String x = clase; x != null; ) {
            if (x.equals(ancestro)) return true;
            DefinicionClase d = definicionesClase.get(x);
            x = (d == null) ? null : d.superclase();
        }
        return false;
    }

    public int idDeClase(String clase) {
        Integer id = idsClase.get(clase);
        if (id == null) {
            id = idsClase.size() + 1;
            idsClase.put(clase, id);
        }
        return id;
    }

    private String definidorDe(String clase, String metodo, int aridad) {
        Set<String> definen = clasesPorMetodo.getOrDefault(metodo + "/" + aridad, Set.of());

        for (String actual = clase; actual != null; ) {
            if (definen.contains(actual)) {
                return actual;
            }
            DefinicionClase def = definicionesClase.get(actual);
            actual = (def == null) ? null : def.superclase();
        }
        return null;
    }

    public String copiarArreglo(String origen, int celdas, String tipoBase, TipoDato tipoElemento) {
        String copia = nuevoTemporal(TipoDato.ESTRUCTURA);
        agregar("new_array", tipoBase, String.valueOf(celdas), copia);

        String i = nuevoTemporal(TipoDato.ENTERO);
        agregar("=", "0", null, i, TipoDato.ENTERO);

        String inicio = nuevaEtiqueta();
        String fin = nuevaEtiqueta();

        agregarEtiqueta(inicio);
        String cond = binaria("<", i, String.valueOf(celdas), TipoDato.BOOLEANO);
        saltoSiFalso(cond, fin);

        String elem = nuevoTemporal(tipoElemento);
        agregar("index_get", origen, i, elem, tipoElemento);
        agregar("index_set", i, elem, copia, tipoElemento);

        String sig = binaria("+", i, "1", TipoDato.ENTERO);
        agregar("=", sig, null, i, TipoDato.ENTERO);
        salto(inicio);

        agregarEtiqueta(fin);
        return copia;
    }
}