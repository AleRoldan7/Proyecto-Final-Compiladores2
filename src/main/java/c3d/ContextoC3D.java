package c3d;

import enums.TipoDato;
import lombok.Getter;

import java.util.*;
import java.util.regex.Pattern;

@Getter
public class ContextoC3D {

    private static final TipoDato TIPO_ENTERO = TipoDato.ENTERO;
    private static final TipoDato TIPO_BOOLEANO = TipoDato.BOOLEANO;

    private static final int OFFSET_PRIMER_PARAM = 2;   // [0]=id retorno, [1]=fp anterior
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

    // ---------- Inicio / globales ----------
    private boolean programaIniciado = false;
    private boolean programaFinalizado = false;
    private int indiceSaltoInicial = -1;
    private String primeraEtiquetaGlobales;
    private int indiceSaltoSalidaGlobales = -1;
    private int contadorGlobales = 0;

    private final Map<String, TipoDato> tiposLocales = new HashMap<>();
    private final Map<String, TipoDato> tiposRetorno = new HashMap<>();

    // =====================================================
    //  Temporales
    // =====================================================
    public String nuevoTemporal(TipoDato tipoTemporal) {
        String temporal = "t" + contadorTemporales++;
        tipoTemporales.put(temporal, tipoTemporal);
        return temporal;
    }

    /** Temporal de uso inmediato (índices, bases de marco). */
    private String temporalAuxiliar(TipoDato tipo) {
        String temporal = "t" + contadorTemporales++;
        tipoTemporales.put(temporal, tipo);
        return temporal;
    }

    /** Emite "aux = base + desplazamiento". */
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
        if (esLocal(destino)) {
            int offset = offsetsLocales.get(destino);
            String idx = sumaAuxiliar("fp", offset);
            agregar("stack_set", idx, valor, null);
        } else if (esAtributo(destino)) {
            int offset = desplazamiento(claseActual, destino);
            String self = leerVariable("self", TipoDato.OBJETO);
            cuartetas.add(new Cuarteta("field_set", String.valueOf(offset), valor, self, null));
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

    /**
     * Devuelve el nombre "Clase_metodo". Si el nodo no conoce la clase del receptor,
     * se busca entre las clases que definen ese método con esa aridad (contando self).
     */
    public String resolverMetodo(String claseReceptor, String metodo, int aridad) {
        if (claseReceptor != null) {
            return nombreFuncion(claseReceptor, metodo);
        }

        Set<String> clases = clasesPorMetodo.get(metodo + "/" + aridad);

        if (clases == null || clases.isEmpty()) {
            return metodo;   // lo reportará la validación final
        }

        if (clases.size() > 1) {
            throw new IllegalStateException("El método '" + metodo + "' existe en varias clases " + clases
                    + " y LlamadaMetodo no trae claseReceptor: el análisis semántico debe asignarlo.");
        }

        return nombreFuncion(clases.iterator().next(), metodo);
    }

    // =====================================================
    //  Inicio y fin del programa
    // =====================================================
    public void iniciarPrograma() {
        if (programaIniciado) return;
        programaIniciado = true;

        agregar("=", "0", null, "fp");
        agregar("=", "0", null, "sp");
        indiceSaltoInicial = cuartetas.size();
        salto("func_main");     // se corrige en finalizarPrograma si hay globales
    }

    public void finalizarPrograma() {
        if (programaFinalizado) return;
        programaFinalizado = true;

        // 1) toda llamada debe tener su función
        List<String> faltantes = new ArrayList<>();
        for (Map.Entry<String, String> e : llamadasPendientes.entrySet()) {
            if (!funcionesDefinidas.contains(e.getKey())) {
                faltantes.add(e.getValue());
            }
        }
        if (!faltantes.isEmpty()) {
            throw new IllegalStateException("Se llama a funciones o métodos que no existen en el C3D:\n  - "
                    + String.join("\n  - ", faltantes)
                    + "\nSi es un método, revisa que LlamadaMetodo traiga claseReceptor y que la clase se haya generado.");
        }

        // 2) el programa empieza en las globales (si hay) y luego en main
        if (indiceSaltoInicial >= 0) {
            String destino = primeraEtiquetaGlobales != null ? primeraEtiquetaGlobales : "func_main";
            cuartetas.set(indiceSaltoInicial, new Cuarteta("goto", null, null, destino, null));
        }

        // 3) retorno: ret_dispatch
        agregarEtiqueta("ret_dispatch");
        for (int id : sitiosRetorno) {
            String cond = temporalAuxiliar(TIPO_BOOLEANO);
            cuartetas.add(new Cuarteta("==", "retaddr", String.valueOf(id), cond, TIPO_BOOLEANO));
            saltoSiVerdadero(cond, "ret_" + id);
        }
        agregarEtiqueta("fin_programa");
    }

    // =====================================================
    //  Variables globales del programa (.pig): se ejecutan antes de main
    // =====================================================
    public void abrirGlobales() {
        String etiqueta = "globales_" + contadorGlobales++;

        if (indiceSaltoSalidaGlobales >= 0) {
            // encadena con el bloque anterior
            cuartetas.set(indiceSaltoSalidaGlobales, new Cuarteta("goto", null, null, etiqueta, null));
        } else {
            primeraEtiquetaGlobales = etiqueta;
        }

        agregarEtiqueta(etiqueta);
        funcionActual = BLOQUE_GLOBAL;
        iniciarMarco();
    }

    public void cerrarGlobales() {
        indiceSaltoSalidaGlobales = cuartetas.size();
        salto("func_main");
        expandirLlamadas();
        parchearMarco();
        funcionActual = null;
    }

    // =====================================================
    //  Funciones / métodos / constructores
    // =====================================================
    public void abrirFuncion(String nombre, List<String> parametros) {
        abrirFuncion(nombre, parametros, null);
    }

    public void abrirFuncion(String nombre, List<String> parametros, List<TipoDato> tipos) {
        String real = nombreReal(nombre, parametros.size());

        if (!funcionesDefinidas.add(real)) {
            throw new IllegalStateException("La función '" + nombre + "' con " + parametros.size()
                    + " parámetro(s) (contando self) está definida más de una vez.");
        }

        registrarFuncion(nombre);
        funcionActual = real;
        agregarEtiqueta("func_" + real);
        iniciarMarco();

        for (int i = 0; i < parametros.size(); i++) {
            String parametro = parametros.get(i);
            offsetsLocales.put(parametro, siguienteOffset++);
            if (tipos != null && i < tipos.size()) {
                tiposLocales.put(parametro, tipos.get(i));
            }
        }
    }

    public void cerrarFuncion(String nombre) {
        String real = funcionActual;
        agregarEtiqueta("end_" + real);

        if ("main".equals(real)) {
            salto("fin_programa");
        } else {
            // retaddr = stack[fp]
            agregar("stack_get", "fp", null, "retaddr");
            // sp = fp (se libera el marco)
            agregar("=", "fp", null, "sp");
            // fp = stack[fp + 1] (marco del llamador)
            String idx = sumaAuxiliar("fp", 1);
            agregar("stack_get", idx, null, "fp");
            salto("ret_dispatch");
        }

        expandirLlamadas();
        parchearMarco();
        funcionActual = null;
    }

    /** true dentro de una función/método/main (no en el bloque de globales). */
    public boolean enFuncion() {
        return funcionActual != null && !BLOQUE_GLOBAL.equals(funcionActual);
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
        offsetsLocales.put(nombre, siguienteOffset++);
        tiposLocales.put(nombre, tipo);
    }

    public TipoDato tipoDeVariable(String nombre) {
        TipoDato local = tiposLocales.get(nombre);
        if (local != null) {
            return local;
        }
        return tipoVariables.getOrDefault(nombre, TipoDato.DESCONOCIDO);
    }

    /** Variable global del programa: su tipo también alimenta la inferencia de los generadores. */
    public void registrarGlobal(String nombre, TipoDato tipo) {
        tipoVariables.put(nombre, tipo);
        tipoTemporales.put(nombre, tipo);
    }

    public boolean esLocal(String nombre) {
        return offsetsLocales.containsKey(nombre);
    }

    public String leerVariable(String nombre, TipoDato tipo) {
        int offset = offsetsLocales.get(nombre);
        String idx = sumaAuxiliar("fp", offset);
        String temporal = nuevoTemporal(tipo);
        agregar("stack_get", idx, null, temporal, tipo);
        return temporal;
    }

    // =====================================================
    //  Llamada y retorno
    // =====================================================
    private void guardarEnPila(String base, int offset, String valor) {
        String idx = sumaAuxiliar(base, offset);
        agregar("stack_set", idx, valor, null);
    }

    public String llamar(String funcion, List<String> argumentos, TipoDato tipoRetorno) {

        if (cuartetas.size() > LIMITE_CUARTETAS) {
            throw new IllegalStateException("El C3D superó " + LIMITE_CUARTETAS
                    + " cuartetas: hay una expansión descontrolada en la generación.");
        }

        String real = nombreReal(funcion, argumentos.size());
        llamadasPendientes.putIfAbsent(real,
                funcion + " con " + argumentos.size() + " argumento(s) (contando self)");

        int id = contadorSitiosRetorno++;
        sitiosRetorno.add(id);

        // Marca: al cerrar la función se reemplaza por el guardado de los temporales vivos
        agregar("call_save", null, null, String.valueOf(id));

        // Marco nuevo en sp
        String base = temporalAuxiliar(TIPO_ENTERO);
        agregar("=", "sp", null, base);
        guardarEnPila(base, 0, String.valueOf(id));
        guardarEnPila(base, 1, "fp");
        for (int i = 0; i < argumentos.size(); i++) {
            guardarEnPila(base, OFFSET_PRIMER_PARAM + i, argumentos.get(i));
        }
        agregar("=", base, null, "fp");

        // Salto y retorno
        salto("func_" + real);
        agregarEtiqueta("ret_" + id);

        // Marca: restauración de los temporales vivos
        agregar("call_restore", null, null, String.valueOf(id));

        TipoDato tipoFinal = tipoRetorno;
        if (tipoFinal == null || tipoFinal == TipoDato.DESCONOCIDO) {
            tipoFinal = tiposRetorno.getOrDefault(real, TipoDato.DESCONOCIDO);
        }

        if (tipoFinal == TipoDato.VOID) {
            return null;
        }

        String resultado = nuevoTemporal(tipoFinal);
        agregar("=", "retval", null, resultado, tipoFinal);
        return resultado;
    }

    public void retornar(String valor) {
        if (valor != null) {
            agregar("=", valor, null, "retval");
        }
        salto("end_" + funcionActual);
    }

    // =====================================================
    //  Guardado de temporales vivos alrededor de las llamadas
    //  (los temporales son globales de C: la recursión los pisaría)
    // =====================================================
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
    public void registrarClase(String clase, List<String> atributos) {
        registrarClase(clase, atributos, null);
    }

    public void registrarClase(String clase, List<String> atributos, List<TipoDato> tipos) {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        Map<String, TipoDato> tiposMapa = new LinkedHashMap<>();
        int desplazamiento = 0;
        for (int i = 0; i < atributos.size(); i++) {
            String atributo = atributos.get(i);
            mapa.put(atributo, desplazamiento++);
            TipoDato tipo = (tipos != null && i < tipos.size()) ? tipos.get(i) : TipoDato.DESCONOCIDO;
            tiposMapa.put(atributo, tipo);
        }
        disposiciones.put(clase, mapa);
        tiposAtributo.put(clase, tiposMapa);
    }

    public TipoDato tipoDeAtributo(String clase, String atributo) {
        Map<String, TipoDato> tiposMapa = tiposAtributo.get(clase);
        if (tiposMapa == null) {
            return TipoDato.DESCONOCIDO;
        }
        return tiposMapa.getOrDefault(atributo, TipoDato.DESCONOCIDO);
    }

    private Map<String, Integer> disposicion(String clase) {
        Map<String, Integer> mapa = disposiciones.get(clase);
        if (mapa == null) {
            throw new IllegalStateException("La clase '" + clase + "' no tiene disposición en el heap (¿se generó antes su archivo?)");
        }
        return mapa;
    }

    public int tamanio(String clase) {
        return disposicion(clase).size();
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

    /*
    public TipoDato tipoDeVariable(String nombre) {
        return tipoVariables.getOrDefault(nombre, TipoDato.DESCONOCIDO);
    }
     */



    /*
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

    public String nuevoTemporal(TipoDato tipoTemporal) {
        String temporal = "t" + contadorTemporales++;
        tipoTemporales.put(temporal, tipoTemporal);
        return temporal;
    }
    public TipoDato getTipoTemporal(String temporal) {
        return tipoTemporales.getOrDefault(temporal, TipoDato.DESCONOCIDO);
    }

    public String nuevaEtiqueta() {
        return "L" + contadorEtiquetas++;
    }

    // ---------- Emisión de cuartetas ----------
    public void agregar(String operador, String arg1, String arg2, String resultado) {
        cuartetas.add(new Cuarteta(operador, arg1, arg2, resultado, null));
    }

    public void agregar(String operador, String arg1, String arg2,
                        String resultado, TipoDato tipo) {
        cuartetas.add(new Cuarteta(operador, arg1, arg2, resultado, tipo));
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


    public void asignar(String destino, String valor) {
        if (esAtributo(destino)) {
            int offset = desplazamiento(claseActual, destino);
            cuartetas.add(new Cuarteta("field_set", String.valueOf(offset), valor, "self", null));
        } else {
            cuartetas.add(new Cuarteta("=", valor, null, destino, null));
        }
    }

    public List<Cuarteta> getCuartetas() {
        return cuartetas;
    }

    // ---------- Funciones ----------
    public static String nombreFuncion(String clase, String metodo) {
        return clase == null ? metodo : clase + "_" + metodo;
    }
    public static String nombreConstructor(String clase) {
        return "init_" + clase;
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

    public void abrirFuncion(String nombre, List<String> parametros) {
        registrarFuncion(nombre);
        agregarEtiqueta("func_" + nombre);
        for (String parametro : parametros) {
            agregar("param_decl", "int", parametro, null);
        }
    }

    public void cerrarFuncion(String nombre) {
        agregarEtiqueta("end_" + nombre);
    }

    // ---------- Cadenas ----------
    public List<String> getCadenas() {
        return cadenas;
    }

    // ---------- Objetos en el heap ----------
    public void registrarClase(String clase, List<String> atributos) {
        registrarClase(clase, atributos, null);
    }

    public void registrarClase(String clase, List<String> atributos, List<TipoDato> tipos) {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        Map<String, TipoDato> tiposMapa = new LinkedHashMap<>();
        int desplazamiento = 0;
        for (int i = 0; i < atributos.size(); i++) {
            String atributo = atributos.get(i);
            mapa.put(atributo, desplazamiento++);
            TipoDato tipo = (tipos != null && i < tipos.size()) ? tipos.get(i) : TipoDato.DESCONOCIDO;
            tiposMapa.put(atributo, tipo);
        }
        disposiciones.put(clase, mapa);
        tiposAtributo.put(clase, tiposMapa);
    }

    public TipoDato tipoDeAtributo(String clase, String atributo) {
        Map<String, TipoDato> tiposMapa = tiposAtributo.get(clase);
        if (tiposMapa == null) {
            return TipoDato.DESCONOCIDO;
        }
        return tiposMapa.getOrDefault(atributo, TipoDato.DESCONOCIDO);
    }

    private Map<String, Integer> disposicion(String clase) {
        Map<String, Integer> mapa = disposiciones.get(clase);
        if (mapa == null) {
            throw new IllegalStateException("La clase '" + clase + "' no tiene disposición en el heap (¿se generó antes su archivo?)");
        }
        return mapa;
    }



    public int tamanio(String clase) {
        return disposicion(clase).size();
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

    // ---------- break / continue ----------
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

    private final Set<String> locales = new HashSet<>();

    public boolean esLocal(String nombre) {
        return locales.contains(nombre);
    }

    public static boolean esImpresion(String nombre) {
        return Set.of("print", "println", "imprimir").contains(nombre);
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

    public TipoDato tipoDeVariable(String nombre) {
        return tipoVariables.getOrDefault(nombre, TipoDato.DESCONOCIDO);
    }
     */
}