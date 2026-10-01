package assembler;

import assembler.generador.GeneradorAssembler;
import c3d.ContextoC3D;

public class test {

    public static void main(String[] args) {
        ContextoC3D contextoC3D = new ContextoC3D();
        String t0 = contextoC3D.binaria("+", "5", "4", null);
        contextoC3D.asignar("resultado", t0);
        contextoC3D.agregar("print", "resultado", null, null);
        contextoC3D.agregar("halt", null, null, null);

        GeneradorAssembler generadorAssembler = new GeneradorAssembler();
        System.out.println(generadorAssembler.generar(contextoC3D.getCuartetas()));
    }
}
