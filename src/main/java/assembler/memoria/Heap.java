package assembler.memoria;

import assembler.generador.ASMContexto;
import assembler.generador.Registros;
import enums.Registro;

public class Heap extends MemoriaCelda{

    public static final int CELDAS = 65_536;

    public Heap(ASMContexto asmContexto) {
        super(asmContexto, Registro.BASE_HEAP.getNombreRegistro());
    }

    public void inicializar() {
        asmContexto.comentario("heap: " + CELDAS + " celdas (0 = null)");
        reservar(CELDAS);
        asmContexto.instruccion("li " + Registro.HP + ", 1");
    }
}
