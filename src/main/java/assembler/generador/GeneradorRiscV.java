package assembler.generador;

import assembler.runtime.Ecall;
import assembler.runtime.RuntimeRiscV;
import assembler.traductor.*;
import c3d.Cuarteta;
import c3d.InferidorTipos;
import enums.TipoDato;

import java.util.List;
import java.util.Map;

public class GeneradorRiscV {

    private static final List<TraductorCuarteta> TRADUCTORES = List.of(
            new ControlTraductor(),
            new TraductorAsignacion(),
            new TraductorAritmetico(),
            new TraductorLogico(),
            new TraductorPila(),
            new TraductorHeap(),
            new TraductorES()
    );

    public static String generar(List<Cuarteta> cuartetas, Map<String, TipoDato> tiposTemporales) {

        Map<String, TipoDato> tipos = InferidorTipos.inferir(cuartetas, tiposTemporales);
        ContextoRiscV contextoRiscV = new ContextoRiscV(tipos);

        // ---- inicio: memoria de la máquina C3D ----
        contextoRiscV.asmContexto.etiqueta("main");
        contextoRiscV.asmContexto.comentario("---- inicialización de memoria ----");
        contextoRiscV.pila.inicializar();
        contextoRiscV.heap.inicializar();
        contextoRiscV.asmContexto.comentario("---- programa ----");

        // ---- cuerpo: cada cuarteta la traduce su familia ----
        for (Cuarteta q : cuartetas) {

            String op = q.getOperador();

            if (!"label".equals(op) && !"comment".equals(op)) {
                contextoRiscV.asmContexto.comentario(q.toString());
            }

            TraductorCuarteta traductor = TRADUCTORES.stream()
                    .filter(t -> t.soporta(op))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No hay traductor RISC-V para la cuarteta '" + op + "': " + q));

            traductor.traducir(q, contextoRiscV);
        }

        // ---- fin ----
        contextoRiscV.asmContexto.comentario("---- fin del programa ----");
        Ecall.emitir(contextoRiscV.asmContexto, Ecall.SALIR);

        RuntimeRiscV.emitir(contextoRiscV.asmContexto);
        contextoRiscV.variables.volcar(contextoRiscV.asmContexto);
        contextoRiscV.cadenas.volcar(contextoRiscV.asmContexto);

        return contextoRiscV.asmContexto.construir();
    }
}
