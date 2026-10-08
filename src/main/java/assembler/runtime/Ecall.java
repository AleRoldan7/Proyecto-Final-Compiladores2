package assembler.runtime;

import assembler.generador.ASMContexto;

public class Ecall {

    public static final int IMPRIMIR_ENTERO = 1;
    public static final int IMPRIMIR_DECIMAL = 3;
    public static final int IMPRIMIR_TEXTO = 4;
    public static final int LEER_ENTERO = 5;
    public static final int LEER_DECIMAL = 7;
    public static final int LEER_TEXTO = 8;
    public static final int RESERVAR_MEMORIA = 9;
    public static final int SALIR = 10;
    public static final int IMPRIMIR_CARACTER = 11;

    private Ecall() {
    }

    public static void emitir(ASMContexto asmContexto, int servicio) {
        asmContexto.instruccion("li a7, " + servicio);
        asmContexto.instruccion("ecall");
    }
}
