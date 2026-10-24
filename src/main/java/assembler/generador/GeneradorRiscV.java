package assembler.generador;

import c3d.Cuarteta;
import enums.TipoDato;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Traductor C3D -> RISC-V (RARS) sin rutinas de runtime: cada operación se
 * expande en línea, con etiquetas propias.
 *
 *   marco en la pila real: -4(s0) = ra, 0(s0) = s0 anterior
 *   llamadas: jal ra, f   (argumentos en a0..a7, decimales en fa0..fa7)
 *   retorno: a0 (fa0 si es decimal)
 *   heap: se reserva una vez en _start; heap_ptr apunta al siguiente byte libre
 */
public class GeneradorRiscV {

    private static final Pattern PATRON_ENTERO   = Pattern.compile("-?\\d+");
    private static final Pattern PATRON_DECIMAL  = Pattern.compile("-?\\d+\\.\\d+");
    private static final Pattern PATRON_CARACTER = Pattern.compile("\"'.'\"");
    private static final Set<String> COMPARACIONES = Set.of("<", "<=", ">", ">=", "==", "!=");
    private static final int ARENA = 1048576;
    private static final int CAMPO = 8;
    private static final int BUFFER_LECTURA = 256;

    private final Map<String, TipoDato> tiposTemporales;
    private final Map<String, TipoDato> tiposGlobales = new HashMap<>();
    private final Map<String, TipoDato> tiposLocales = new HashMap<>();
    private final Set<String> globales = new LinkedHashSet<>();
    private final Set<String> locales = new LinkedHashSet<>();
    private final Set<String> funciones = new LinkedHashSet<>();
    private final Map<String, String> cadenas = new LinkedHashMap<>();   // literal -> etiqueta
    private final Map<String, String> dobles = new LinkedHashMap<>();    // literal -> etiqueta
    private final List<String> pendientes = new ArrayList<>();
    private final Map<String, List<TipoDato>> firmas = new HashMap<>();   // función -> tipos de sus parámetros
    private final StringBuilder codigo = new StringBuilder();
    private Map<String, Integer> offsets = new HashMap<>();
    private int marco;
    private int contadorEtiquetas = 0;

    private GeneradorRiscV(Map<String, TipoDato> tiposTemporales) {
        this.tiposTemporales = tiposTemporales;
    }

    public static String generar(List<Cuarteta> cuartetas, Map<String, TipoDato> tiposTemporales) {
        GeneradorRiscV g = new GeneradorRiscV(tiposTemporales);
        g.programa(cuartetas);
        return g.ensamblar();
    }

    // ============================ programa y funciones ============================

    private void programa(List<Cuarteta> cuartetas) {
        for (Cuarteta q : cuartetas) {
            if ("gdecl".equals(q.getOperador())) {
                globales.add(q.getArg1());
                tiposGlobales.put(q.getArg1(), tipoDeclarado(q));
            }
        }
        recolectarFirmas(cuartetas);
        int i = 0;
        while (i < cuartetas.size()) {
            Cuarteta q = cuartetas.get(i);
            if ("fbegin".equals(q.getOperador())) {
                int fin = i;
                while (!"fend".equals(cuartetas.get(fin).getOperador())) fin++;
                generarFuncion(cuartetas.subList(i, fin + 1));
                i = fin + 1;
            } else {
                String op = q.getOperador();
                if (!"comment".equals(op) && !"gdecl".equals(op)) {
                    throw new IllegalStateException("instrucción fuera de cualquier función: " + op);
                }
                i++;
            }
        }
    }

    private void generarFuncion(List<Cuarteta> bloque) {
        String nombre = bloque.get(0).getArg1();
        List<String> params = new ArrayList<>();
        List<Cuarteta> cuerpo = new ArrayList<>();
        locales.clear();
        tiposLocales.clear();

        for (Cuarteta q : bloque.subList(1, bloque.size() - 1)) {
            switch (q.getOperador()) {
                case "fparam" -> {
                    params.add(q.getArg1());
                    locales.add(q.getArg1());
                    tiposLocales.put(q.getArg1(), tipoDeclarado(q));
                }
                case "ldecl" -> {
                    locales.add(q.getArg1());
                    tiposLocales.put(q.getArg1(), tipoDeclarado(q));
                }
                default -> cuerpo.add(q);
            }
        }
        emitirFuncion(nombre, params, cuerpo);
    }

    private void emitirFuncion(String nombre, List<String> params, List<Cuarteta> cuerpo) {
        funciones.add(nombre);
        calcularMarco(params, cuerpo);

        etiquetaCodigo(nombre);
        emitir("addi sp, sp, -" + marco);
        emitir("sw s0, " + (marco - 4) + "(sp)");
        emitir("sw ra, " + (marco - 8) + "(sp)");
        emitir("addi s0, sp, " + (marco - 4));

        for (int k = 0; k < params.size(); k++) {
            if (tipoDe(params.get(k)) == TipoDato.DECIMAL) {
                guardarDoble("fa" + k, params.get(k));      // decimal: registro fa<k>
            } else {
                guardar("a" + k, params.get(k));            // resto: registro a<k>
            }
        }
        for (Cuarteta q : cuerpo) {
            traducir(q);
        }
        epilogo();
    }

    private void calcularMarco(List<String> params, List<Cuarteta> cuerpo) {
        LinkedHashSet<String> nombres = new LinkedHashSet<>(params);
        nombres.addAll(locales);
        for (Cuarteta q : cuerpo) {
            for (String v : variables(q)) {
                if (locales.contains(v) || !globales.contains(v)) nombres.add(v);
            }
        }

        offsets = new HashMap<>();
        int cur = 4;                                  // -4(s0) = ra
        for (String n : nombres) {
            if (tipoDe(n) == TipoDato.DECIMAL) {
                cur += 8;
                if (cur % 8 == 0) cur += 4;           // deja el double alineado a 8
            } else {
                cur += 4;
            }
            offsets.put(n, -cur);
        }
        marco = redondear(cur + 4, 16);
    }

    private void epilogo() {
        emitir("lw ra, -4(s0)");
        emitir("lw s0, 0(s0)");
        emitir("addi sp, sp, " + marco);
        emitir("ret");
    }

    // ============================ traducción de cuartetas ============================

    private void traducir(Cuarteta q) {
        String op = q.getOperador();
        String a1 = q.getArg1(), a2 = q.getArg2(), res = q.getResultado();

        switch (op) {
            case "label" -> etiquetaCodigo(lbl(res));
            case "goto" -> emitir("j " + lbl(res));
            case "if_true" -> {
                cargar(a1, "t0");
                String skip = etiqueta("skip");
                emitir("beqz t0, " + skip);      // si falso, saltar el j
                emitir("j " + lbl(res));          // si verdadero, salto largo
                etiquetaCodigo(skip);
            }
            case "if_false" -> {
                cargar(a1, "t0");
                String skip = etiqueta("skip");
                emitir("bnez t0, " + skip);
                emitir("j " + lbl(res));
                etiquetaCodigo(skip);
            }
            case "if_<", "if_<=", "if_>", "if_>=", "if_==", "if_!=" ->
                    saltoCondicional(op.substring(3), a1, a2, res);

            case "=" -> {
                if (tipoDe(res) == TipoDato.DECIMAL) {
                    cargarDoble(a1, "ft0");
                    guardarDoble("ft0", res);
                } else {
                    cargar(a1, "t0");
                    guardar("t0", res);
                }
            }
            case "!", "not" -> { cargar(a1, "t0"); emitir("seqz t0, t0"); guardar("t0", res); }
            case "neg" -> {
                if (tipoDe(res) == TipoDato.DECIMAL || tipoDeOperando(a1) == TipoDato.DECIMAL) {
                    cargarDoble(a1, "ft0");
                    emitir("fneg.d ft0, ft0");
                    guardarDoble("ft0", res);
                } else {
                    cargar(a1, "t0");
                    emitir("neg t0, t0");
                    guardar("t0", res);
                }
            }
            case "+", "-", "*", "/", "%", "<", "<=", ">", ">=", "==", "!=", "&&", "||" -> binaria(q);

            case "attr_get" -> {
                cargar(a1, "t0");
                int off = Integer.parseInt(a2) * CAMPO;
                if (tipoDe(res) == TipoDato.DECIMAL) {
                    emitir("fld ft0, " + off + "(t0)");
                    guardarDoble("ft0", res);
                } else {
                    emitir("lw t1, " + off + "(t0)");
                    guardar("t1", res);
                }
            }
            case "field_set" -> {                         // res.campo[a1] = a2
                cargar(res, "t0");
                int off = Integer.parseInt(a1) * CAMPO;
                if (tipoDeOperando(a2) == TipoDato.DECIMAL) {
                    cargarDoble(a2, "ft0");
                    emitir("fsd ft0, " + off + "(t0)");
                } else {
                    cargar(a2, "t1");
                    emitir("sw t1, " + off + "(t0)");
                }
            }
            case "new" -> reservarMemoria(Integer.parseInt(a2) * CAMPO, res);
            case "new_array" -> {
                TipoDato el = tipoElementoNuevo(a1);
                cargar(a2, "t3");
                emitir("li t1, " + tamanioElemento(el));
                emitir("mul t3, t3, t1");
                emitir("addi t3, t3, 7");
                emitir("andi t3, t3, -8");
                punteroHeapAlineado("a0");
                emitir("add t4, a0, t3");
                emitir("sw t4, 0(t6)");
                guardar("a0", res);
            }
            case "index_get" -> {
                TipoDato el = tipoElementoAcceso(q);
                direccion(a1, a2, tamanioElemento(el));
                if (el == TipoDato.DECIMAL) {
                    emitir("fld ft0, 0(t0)");
                    guardarDoble("ft0", res);
                } else {
                    emitir((el == TipoDato.CARACTER ? "lbu" : "lw") + " t2, 0(t0)");
                    guardar("t2", res);
                }
            }
            case "index_set" -> {                         // res[a1] = a2
                TipoDato el = tipoElementoEscritura(q);
                direccion(res, a1, tamanioElemento(el));
                if (el == TipoDato.DECIMAL) {
                    cargarDoble(a2, "ft0");
                    emitir("fsd ft0, 0(t0)");
                } else {
                    cargar(a2, "t2");
                    emitir((el == TipoDato.CARACTER ? "sb" : "sw") + " t2, 0(t0)");
                }
            }
            case "param" -> pendientes.add(a1);
            case "call" -> llamar(q);
            case "return" -> {
                if (a1 != null && !"-".equals(a1)) {
                    if (tipoDeOperando(a1) == TipoDato.DECIMAL) cargarDoble(a1, "fa0");
                    else cargar(a1, "a0");
                }
                epilogo();
            }
            case "read" -> leer(q);
            case "print" -> imprimir(a1, "nl".equals(a2));
            case "comment" -> codigo.append("    # ").append(String.valueOf(res).replace('\n', ' ')).append('\n');
            case "ldecl", "gdecl", "halt" -> { }
            default -> throw new IllegalStateException("cuarteta sin traducción: " + op);
        }
    }

    private void binaria(Cuarteta q) {
        String op = q.getOperador();
        String a1 = q.getArg1(), a2 = q.getArg2(), res = q.getResultado();

        if ("+".equals(op) && (q.getTipoDeclarado() == TipoDato.TEXTO || esTextoOp(a1) || esTextoOp(a2))) {
            concatenar(a1, a2, res);
            return;
        }
        if (("==".equals(op) || "!=".equals(op)) && (esTextoOp(a1) || esTextoOp(a2))) {
            igualdadTexto(a1, a2, res, "!=".equals(op));
            return;
        }
        if (esDecimal(q)) {
            binariaDoble(q);
            return;
        }

        cargar(a1, "t0");
        cargar(a2, "t1");
        switch (op) {
            case "+" -> emitir("add t2, t0, t1");
            case "-" -> emitir("sub t2, t0, t1");
            case "*" -> emitir("mul t2, t0, t1");
            case "/" -> emitir("div t2, t0, t1");
            case "%" -> emitir("rem t2, t0, t1");
            case "<" -> emitir("slt t2, t0, t1");
            case ">" -> emitir("slt t2, t1, t0");
            case "<=" -> { emitir("slt t2, t1, t0"); emitir("xori t2, t2, 1"); }
            case ">=" -> { emitir("slt t2, t0, t1"); emitir("xori t2, t2, 1"); }
            case "==" -> { emitir("xor t2, t0, t1"); emitir("seqz t2, t2"); }
            case "!=" -> { emitir("xor t2, t0, t1"); emitir("snez t2, t2"); }
            case "&&" -> emitir("and t2, t0, t1");
            case "||" -> emitir("or t2, t0, t1");
            default -> throw new IllegalStateException("operador binario sin traducción: " + op);
        }
        guardar("t2", res);
    }

    private void binariaDoble(Cuarteta q) {
        String op = q.getOperador();
        String res = q.getResultado();
        cargarDoble(q.getArg1(), "ft0");
        cargarDoble(q.getArg2(), "ft1");
        switch (op) {
            case "+" -> { emitir("fadd.d ft2, ft0, ft1"); guardarDoble("ft2", res); }
            case "-" -> { emitir("fsub.d ft2, ft0, ft1"); guardarDoble("ft2", res); }
            case "*" -> { emitir("fmul.d ft2, ft0, ft1"); guardarDoble("ft2", res); }
            case "/" -> { emitir("fdiv.d ft2, ft0, ft1"); guardarDoble("ft2", res); }
            case "<" -> { emitir("flt.d t2, ft0, ft1"); guardar("t2", res); }
            case ">" -> { emitir("flt.d t2, ft1, ft0"); guardar("t2", res); }
            case "<=" -> { emitir("fle.d t2, ft0, ft1"); guardar("t2", res); }
            case ">=" -> { emitir("fle.d t2, ft1, ft0"); guardar("t2", res); }
            case "==" -> { emitir("feq.d t2, ft0, ft1"); guardar("t2", res); }
            case "!=" -> { emitir("feq.d t2, ft0, ft1"); emitir("xori t2, t2, 1"); guardar("t2", res); }
            default -> throw new IllegalStateException("operador decimal sin traducción: " + op);
        }
    }

    private boolean esDecimal(Cuarteta q) {
        if (tipoDeOperando(q.getArg1()) == TipoDato.DECIMAL || tipoDeOperando(q.getArg2()) == TipoDato.DECIMAL) {
            return true;
        }
        return !COMPARACIONES.contains(q.getOperador()) && tipoDe(q.getResultado()) == TipoDato.DECIMAL;
    }

    private void saltoCondicional(String operador, String a1, String a2, String destino) {
        String saltoLargo = lbl(destino);
        String skip = etiqueta("skip");

        if (tipoDeOperando(a1) == TipoDato.DECIMAL || tipoDeOperando(a2) == TipoDato.DECIMAL) {
            cargarDoble(a1, "ft0");
            cargarDoble(a2, "ft1");
            switch (operador) {
                case "<"  -> { emitir("fge.d t2, ft0, ft1"); }
                case ">"  -> { emitir("fle.d t2, ft0, ft1"); }
                case "<=" -> { emitir("fgt.d t2, ft0, ft1"); }
                case ">=" -> { emitir("flt.d t2, ft0, ft1"); }
                case "==" -> { emitir("fne.d t2, ft0, ft1"); }
                default   -> { emitir("feq.d t2, ft0, ft1"); }
            }
            emitir("bnez t2, " + skip);
            emitir("j " + saltoLargo);
            etiquetaCodigo(skip);
            return;
        }

        cargar(a1, "t0");
        cargar(a2, "t1");
        String saltoInverso = switch (operador) {
            case "<"  -> "bge";
            case ">"  -> "ble";
            case "<=" -> "bgt";
            case ">=" -> "blt";
            case "==" -> "bne";
            default   -> "beq";           // "!="
        };
        emitir(saltoInverso + " t0, t1, " + skip);
        emitir("j " + saltoLargo);
        etiquetaCodigo(skip);
    }

    // ============================ texto en línea ============================

    /** res = a + b, copiando ambos operandos (texto, carácter, entero o decimal) al heap. */
    private void concatenar(String a, String b, String res) {
        cargarOperandoTexto(a, 0);
        cargarOperandoTexto(b, 1);
        iniciarTexto();
        agregarOperandoTexto(a, 0);
        agregarOperandoTexto(b, 1);
        terminarTexto(res);
    }

    private void cargarOperandoTexto(String op, int orden) {
        String reg = orden == 0 ? "a0" : "a1";
        String freg = orden == 0 ? "fa0" : "fa1";
        if (tipoDeOperando(op) == TipoDato.DECIMAL) cargarDoble(op, freg);
        else cargar(op, reg);
    }

    private void agregarOperandoTexto(String op, int orden) {
        String reg = orden == 0 ? "a0" : "a1";
        String freg = orden == 0 ? "fa0" : "fa1";
        switch (tipoDeOperando(op)) {
            case TEXTO -> agregarCadena(reg);
            case CARACTER -> agregarCaracter(reg);
            case DECIMAL -> agregarDecimal(freg);
            default -> agregarEntero(reg);
        }
    }

    /** t3 = inicio del texto nuevo, t4 = cursor de escritura. */
    private void iniciarTexto() {
        emitir("la t6, heap_ptr");
        emitir("lw t3, 0(t6)");
        emitir("mv t4, t3");
    }

    private void terminarTexto(String destino) {
        emitir("sb zero, 0(t4)");
        emitir("addi t4, t4, 8");                    // NUL + redondeo: (t4 + 1) subido a múltiplo de 8
        emitir("andi t4, t4, -8");                   // el heap siempre queda alineado a 8
        emitir("la t6, heap_ptr");
        emitir("sw t4, 0(t6)");
        emitir("mv a0, t3");
        guardar("a0", destino);
    }

    private void agregarCadena(String reg) {
        String ciclo = etiqueta("cad_ciclo"), fin = etiqueta("cad_fin");
        emitir("mv t1, " + reg);
        etiquetaCodigo(ciclo);
        emitir("lbu t2, 0(t1)");
        emitir("beqz t2, " + fin);
        emitir("sb t2, 0(t4)");
        emitir("addi t1, t1, 1");
        emitir("addi t4, t4, 1");
        emitir("j " + ciclo);
        etiquetaCodigo(fin);
    }

    private void agregarCaracter(String reg) {
        emitir("sb " + reg + ", 0(t4)");
        emitir("addi t4, t4, 1");
    }

    /** Escribe el entero de 'reg' en decimal, con signo si es negativo. */
    private void agregarEntero(String reg) {
        String pos = etiqueta("ent_pos"), dig = etiqueta("ent_dig");
        String inv = etiqueta("ent_inv"), invFin = etiqueta("ent_invfin");

        emitir("mv t0, " + reg);
        emitir("bgez t0, " + pos);
        emitir("neg t0, t0");
        emitir("li t5, 45");
        emitir("sb t5, 0(t4)");
        emitir("addi t4, t4, 1");
        etiquetaCodigo(pos);

        emitir("mv t1, t4");                         // inicio de los dígitos
        etiquetaCodigo(dig);
        emitir("li t5, 10");
        emitir("rem t2, t0, t5");
        emitir("div t0, t0, t5");
        emitir("addi t2, t2, 48");
        emitir("sb t2, 0(t4)");
        emitir("addi t4, t4, 1");
        emitir("bnez t0, " + dig);

        emitir("addi t5, t4, -1");                   // invertir los dígitos escritos
        etiquetaCodigo(inv);
        emitir("bge t1, t5, " + invFin);
        emitir("lbu t2, 0(t1)");
        emitir("lbu t0, 0(t5)");
        emitir("sb t0, 0(t1)");
        emitir("sb t2, 0(t5)");
        emitir("addi t1, t1, 1");
        emitir("addi t5, t5, -1");
        emitir("j " + inv);
        etiquetaCodigo(invFin);
    }

    /** Escribe un decimal con una cifra después del punto (truncado). */
    private void agregarDecimal(String freg) {
        String pos = etiqueta("dec_pos");
        emitir("li t5, 10");
        emitir("fcvt.d.w ft7, t5");
        emitir("fmul.d ft7, " + freg + ", ft7");
        emitir("fcvt.w.d t0, ft7, rtz");
        emitir("bgez t0, " + pos);
        emitir("neg t0, t0");
        emitir("li t5, 45");
        emitir("sb t5, 0(t4)");
        emitir("addi t4, t4, 1");
        etiquetaCodigo(pos);

        emitir("li t5, 10");
        emitir("rem t6, t0, t5");                    // cifra decimal (t6 no lo usa agregarEntero)
        emitir("div t0, t0, t5");                    // parte entera
        agregarEntero("t0");
        emitir("li t5, 46");
        emitir("sb t5, 0(t4)");
        emitir("addi t4, t4, 1");
        emitir("addi t6, t6, 48");
        emitir("sb t6, 0(t4)");
        emitir("addi t4, t4, 1");
    }

    private void igualdadTexto(String a, String b, String res, boolean negado) {
        cargar(a, "a0");
        cargar(b, "a1");
        String ciclo = etiqueta("txt_ciclo"), igual = etiqueta("txt_igual");
        String distinto = etiqueta("txt_distinto"), fin = etiqueta("txt_fin");

        etiquetaCodigo(ciclo);
        emitir("lbu t0, 0(a0)");
        emitir("lbu t1, 0(a1)");
        emitir("bne t0, t1, " + distinto);
        emitir("beqz t0, " + igual);
        emitir("addi a0, a0, 1");
        emitir("addi a1, a1, 1");
        emitir("j " + ciclo);

        etiquetaCodigo(igual);
        emitir("li t2, 1");
        emitir("j " + fin);
        etiquetaCodigo(distinto);
        emitir("li t2, 0");
        etiquetaCodigo(fin);
        if (negado) emitir("xori t2, t2, 1");
        guardar("t2", res);
    }

    // ============================ memoria y E/S ============================

    /** Reserva 'bytes' en el heap y deja la dirección en 'destino'. */
    private void reservarMemoria(int bytes, String destino) {
        punteroHeapAlineado("a0");
        emitir("addi t4, a0, " + redondear(bytes, 8));
        emitir("sw t4, 0(t6)");
        guardar("a0", destino);
    }

    private void leer(Cuarteta q) {
        String res = q.getResultado();
        TipoDato t = tipoDeclarado(q) != TipoDato.DESCONOCIDO ? tipoDeclarado(q) : tipoDe(res);
        switch (t) {
            case DECIMAL -> {
                emitir("li a7, 7");
                emitir("ecall");
                guardarDoble("fa0", res);
                consumirLinea();
            }
            case TEXTO -> leerTexto(res);
            case CARACTER -> {
                emitir("li a7, 12");
                emitir("ecall");
                guardar("a0", res);
                String fin = etiqueta("lec_fin");
                emitir("li t0, 10");
                emitir("beq a0, t0, " + fin);
                consumirLinea();
                etiquetaCodigo(fin);
            }
            case ENTERO, BOOLEANO -> {
                emitir("li a7, 5");
                emitir("ecall");
                guardar("a0", res);
                consumirLinea();
            }
            default -> throw new IllegalStateException("readln() hacia '" + res + "' con tipo " + t
                    + ". El tipo del destino no se reconoce: revisa la conversión de tipos.");
        }
    }

    private void consumirLinea() {
        String ciclo = etiqueta("lin_ciclo"), fin = etiqueta("lin_fin");
        etiquetaCodigo(ciclo);
        emitir("li a7, 12");
        emitir("ecall");
        emitir("li t0, 10");
        emitir("beq a0, t0, " + fin);
        emitir("li t0, -1");
        emitir("bne a0, t0, " + ciclo);
        etiquetaCodigo(fin);
    }

    /** Lee una línea en un bloque nuevo del heap y quita el salto final. */
    private void leerTexto(String destino) {
        emitir("la t6, heap_ptr");
        emitir("lw t3, 0(t6)");
        emitir("addi t4, t3, " + BUFFER_LECTURA);
        emitir("sw t4, 0(t6)");
        emitir("mv a0, t3");
        emitir("li a1, " + BUFFER_LECTURA);
        emitir("li a7, 8");
        emitir("ecall");

        String ciclo = etiqueta("txt_quitar"), corta = etiqueta("txt_corta"), fin = etiqueta("txt_quitar_fin");
        emitir("mv t1, t3");
        etiquetaCodigo(ciclo);
        emitir("lbu t2, 0(t1)");
        emitir("beqz t2, " + fin);
        emitir("li t0, 10");
        emitir("beq t2, t0, " + corta);
        emitir("addi t1, t1, 1");
        emitir("j " + ciclo);
        etiquetaCodigo(corta);
        emitir("sb zero, 0(t1)");
        etiquetaCodigo(fin);

        emitir("mv a0, t3");
        guardar("a0", destino);
    }

    private void llamar(Cuarteta q) {
        String funcion = q.getArg1();
        int n = Integer.parseInt(q.getArg2());
        if (pendientes.size() != n) {
            throw new IllegalStateException("llamada a '" + funcion + "' con " + pendientes.size()
                    + " param(s) pendientes, se esperaban " + n);
        }
        if (n > 8) throw new IllegalStateException("más de 8 argumentos en " + funcion);

        List<TipoDato> tiposParam = firmas.getOrDefault(funcion, List.of());
        for (int k = 0; k < n; k++) {
            String arg = pendientes.get(k);
            TipoDato esperado = k < tiposParam.size() ? tiposParam.get(k) : tipoDeOperando(arg);
            if (esperado == TipoDato.DECIMAL) {
                cargarDoble(arg, "fa" + k);
            } else {
                cargar(arg, "a" + k);
            }
        }
        pendientes.clear();
        emitir("jal ra, " + funcion);

        String res = q.getResultado();
        if (res == null || "-".equals(res)) return;

        TipoDato t = tipoDeclarado(q) != TipoDato.DESCONOCIDO ? tipoDeclarado(q) : tipoDe(res);
        if (t == TipoDato.DECIMAL) guardarDoble("fa0", res);
        else guardar("a0", res);
    }

    private void imprimir(String op, boolean saltoLinea) {
        switch (tipoDeOperando(op)) {
            case DECIMAL -> { cargarDoble(op, "fa0"); emitir("li a7, 3"); emitir("ecall"); }
            case CARACTER -> { cargar(op, "a0"); emitir("li a7, 11"); emitir("ecall"); }
            case TEXTO -> { cargar(op, "a0"); emitir("li a7, 4"); emitir("ecall"); }
            default -> { cargar(op, "a0"); emitir("li a7, 1"); emitir("ecall"); }
        }
        if (saltoLinea) {
            emitir("li a0, 10");
            emitir("li a7, 11");
            emitir("ecall");
        }
    }


    private void direccion(String base, String indice, int tam) {
        cargar(base, "t0");
        cargar(indice, "t1");
        int sh = tam == 8 ? 3 : tam == 4 ? 2 : 0;
        if (sh > 0) emitir("slli t1, t1, " + sh);
        emitir("add t0, t0, t1");
    }

    private static int tamanioElemento(TipoDato t) {
        return switch (t) {
            case CARACTER -> 1;
            case DECIMAL -> 8;
            default -> 4;
        };
    }

    private static TipoDato tipoElementoNuevo(String nombre) {
        String n = nombre == null ? "" : nombre.toLowerCase();
        if (Set.of("char", "caracter", "littera").contains(n)) return TipoDato.CARACTER;
        if (Set.of("decimal", "double", "float", "fractum").contains(n)) return TipoDato.DECIMAL;
        return TipoDato.ENTERO;
    }

    private TipoDato tipoElementoAcceso(Cuarteta q) {
        TipoDato t = tipoDeclarado(q);
        if (t == TipoDato.CARACTER || t == TipoDato.DECIMAL) return t;
        TipoDato r = tipoDe(q.getResultado());
        return (r == TipoDato.CARACTER || r == TipoDato.DECIMAL) ? r : TipoDato.ENTERO;
    }

    private TipoDato tipoElementoEscritura(Cuarteta q) {
        TipoDato t = tipoDeclarado(q);
        if (t == TipoDato.CARACTER || t == TipoDato.DECIMAL) return t;
        TipoDato v = tipoDeOperando(q.getArg2());
        return (v == TipoDato.CARACTER || v == TipoDato.DECIMAL) ? v : TipoDato.ENTERO;
    }

    // ============================ carga y guardado ============================

    private void cargar(String op, String reg) {
        if (op == null) throw new IllegalStateException("operando nulo al cargar en " + reg);

        if (PATRON_CARACTER.matcher(op).matches()) {
            emitir("li " + reg + ", " + (int) op.charAt(2));
            return;
        }
        if (op.startsWith("\"")) {
            emitir("la " + reg + ", " + etiquetaCadena(op));
            return;
        }
        if (esCaracterSimple(op)) {
            emitir("li " + reg + ", " + (int) op.charAt(1));
            return;
        }
        if ("true".equals(op)) { emitir("li " + reg + ", 1"); return; }
        if ("false".equals(op) || "null".equals(op)) { emitir("li " + reg + ", 0"); return; }
        if (PATRON_ENTERO.matcher(op).matches()) { emitir("li " + reg + ", " + op); return; }
        if (PATRON_DECIMAL.matcher(op).matches()) {
            throw new IllegalStateException("literal decimal en contexto entero: " + op);
        }
        if (esLocal(op)) {
            emitir("lw " + reg + ", " + ubicacion(op) + "(s0)");
            return;
        }
        if (globales.contains(op)) {
            emitir("la t6, g_" + op);
            emitir("lw " + reg + ", 0(t6)");
            return;
        }
        throw new IllegalStateException("variable sin ranura en el marco: " + op);
    }

    private void guardar(String reg, String destino) {
        if (esLocal(destino)) {
            emitir("sw " + reg + ", " + ubicacion(destino) + "(s0)");
            return;
        }
        if (globales.contains(destino)) {
            emitir("la t6, g_" + destino);
            emitir("sw " + reg + ", 0(t6)");
            return;
        }
        throw new IllegalStateException("destino sin ranura: " + destino);
    }

    /** Carga un operando como decimal en 'freg', convirtiendo enteros si hace falta. */
    private void cargarDoble(String op, String freg) {
        if (PATRON_DECIMAL.matcher(op).matches()) {
            emitir("la t6, " + etiquetaDoble(op));
            emitir("fld " + freg + ", 0(t6)");
            return;
        }
        if (tipoDeOperando(op) == TipoDato.DECIMAL) {
            if (esLocal(op)) {
                emitir("fld " + freg + ", " + ubicacion(op) + "(s0)");
            } else {
                emitir("la t6, g_" + op);
                emitir("fld " + freg + ", 0(t6)");
            }
            return;
        }
        cargar(op, "t5");
        emitir("fcvt.d.w " + freg + ", t5");
    }

    private void guardarDoble(String freg, String destino) {
        if (esLocal(destino)) {
            emitir("fsd " + freg + ", " + ubicacion(destino) + "(s0)");
            return;
        }
        if (globales.contains(destino)) {
            emitir("la t6, g_" + destino);
            emitir("fsd " + freg + ", 0(t6)");
            return;
        }
        throw new IllegalStateException("destino decimal sin ranura: " + destino);
    }

    // ============================ tipos de operandos ============================

    private TipoDato tipoDeOperando(String op) {
        if (op == null) return TipoDato.DESCONOCIDO;
        if (PATRON_CARACTER.matcher(op).matches() || esCaracterSimple(op)) return TipoDato.CARACTER;
        if (op.startsWith("\"")) return TipoDato.TEXTO;
        if (PATRON_DECIMAL.matcher(op).matches()) return TipoDato.DECIMAL;
        if (PATRON_ENTERO.matcher(op).matches() || "true".equals(op) || "false".equals(op) || "null".equals(op)) {
            return TipoDato.ENTERO;
        }
        return tipoDe(op);
    }

    private boolean esTextoOp(String op) {
        return tipoDeOperando(op) == TipoDato.TEXTO;
    }

    private TipoDato tipoDe(String s) {
        if (s == null) return TipoDato.DESCONOCIDO;
        TipoDato t = tiposTemporales.get(s);
        if (t == null) t = tiposLocales.get(s);
        if (t == null) t = tiposGlobales.get(s);
        return t == null ? TipoDato.DESCONOCIDO : t;
    }

    private static TipoDato tipoDeclarado(Cuarteta q) {
        return q.getTipoDeclarado() == null ? TipoDato.DESCONOCIDO : q.getTipoDeclarado();
    }

    private boolean esCaracterSimple(String op) {
        return op.length() == 3 && op.charAt(0) == '\'' && op.charAt(2) == '\'';
    }

    private boolean esLiteral(String s) {
        return PATRON_ENTERO.matcher(s).matches() || PATRON_DECIMAL.matcher(s).matches()
                || PATRON_CARACTER.matcher(s).matches() || s.startsWith("\"")
                || esCaracterSimple(s) || "true".equals(s) || "false".equals(s) || "null".equals(s);
    }

    /** Operandos que una cuarteta usa como variable (no etiquetas, funciones ni literales). */
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

    // ============================ etiquetas, marco y utilidades ============================

    private String etiquetaCadena(String literal) {
        String e = cadenas.get(literal);
        if (e == null) {
            e = "str_" + cadenas.size();
            cadenas.put(literal, e);
        }
        return e;
    }

    private String etiquetaDoble(String valor) {
        String e = dobles.get(valor);
        if (e == null) {
            e = "dbl_" + dobles.size();
            dobles.put(valor, e);
        }
        return e;
    }

    private boolean esLocal(String nombre) {
        return offsets.containsKey(nombre);
    }

    private int ubicacion(String v) {
        Integer off = offsets.get(v);
        if (off == null) throw new IllegalStateException("variable sin ranura en el marco: " + v);
        return off;
    }

    private String etiqueta(String base) {
        return "_" + base + (contadorEtiquetas++);
    }

    private static String lbl(String nombre) {
        return "." + nombre;
    }

    private void emitir(String instruccion) {
        codigo.append("    ").append(instruccion).append('\n');
    }

    private void etiquetaCodigo(String nombre) {
        codigo.append(nombre).append(":\n");
    }

    private static int redondear(int n, int m) {
        return (n + m - 1) / m * m;
    }

    /*HEAP*/
    private void punteroHeapAlineado(String reg) {
        emitir("la t6, heap_ptr");
        emitir("lw " + reg + ", 0(t6)");
        emitir("addi " + reg + ", " + reg + ", 7");
        emitir("andi " + reg + ", " + reg + ", -8");
    }

    /** Guarda el tipo de cada parámetro de cada función, para pasar los decimales en fa0..fa7. */
    private void recolectarFirmas(List<Cuarteta> cuartetas) {
        for (int i = 0; i < cuartetas.size(); i++) {
            Cuarteta q = cuartetas.get(i);
            if (!"fbegin".equals(q.getOperador())) continue;

            List<TipoDato> tipos = new ArrayList<>();
            int j = i + 1;
            while (j < cuartetas.size() && !"fend".equals(cuartetas.get(j).getOperador())) {
                if ("fparam".equals(cuartetas.get(j).getOperador())) {
                    tipos.add(tipoDeclarado(cuartetas.get(j)));
                }
                j++;
            }
            firmas.put(q.getArg1(), tipos);
        }
    }

    // ============================ ensamblado final ============================

    private String ensamblar() {
        StringBuilder s = new StringBuilder();
        s.append(".data\n");
        s.append("heap_ptr: .word 0\n");
        for (String g : globales) {
            s.append(".align 3\ng_").append(g).append(": .space 8\n");
        }
        for (Map.Entry<String, String> e : dobles.entrySet()) {
            s.append(".align 3\n").append(e.getValue()).append(": .double ").append(e.getKey()).append('\n');
        }
        for (Map.Entry<String, String> e : cadenas.entrySet()) {
            s.append(e.getValue()).append(": .asciz ").append(e.getKey()).append('\n');
        }

        s.append("\n.text\n.globl _start\n_start:\n");
        s.append("    li t0, -16\n    and sp, sp, t0\n");
        s.append("    li a0, ").append(ARENA).append("\n    li a7, 9\n    ecall\n");
        s.append("    la t0, heap_ptr\n    sw a0, 0(t0)\n");
        if (funciones.contains("__init")) {
            s.append("    jal ra, __init\n");
        }
        s.append("    jal ra, main\n");
        s.append("    li a7, 10\n    ecall\n\n");

        s.append(codigo);
        return s.toString();
    }
}