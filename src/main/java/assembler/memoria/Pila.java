package assembler.memoria;

import assembler.generador.ASMContexto;
import assembler.generador.Registros;
import enums.Registro;

public class Pila extends MemoriaCelda {

    public static final int CELDAS = 65_536;

    public Pila(ASMContexto asmContexto) {
        super(asmContexto, Registro.BASE_PILA.getNombreRegistro());
    }

    public void inicializar() {
        asmContexto.comentario("pila manual: " + CELDAS + " celdas");
        reservar(CELDAS);
    }
}
