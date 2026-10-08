package assembler.memoria;

import assembler.generador.ASMContexto;
import assembler.runtime.Ecall;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MemoriaCelda {

    public static final int BYTES_POR_CELDA = 16;

    protected final ASMContexto asmContexto;
    protected final String registro;

    public void direccion(String registroDestino, String registroIndice) {
        asmContexto.instruccion("slli " + registroDestino + ", " + registroIndice + ", 4");
        asmContexto.instruccion("add " + registroDestino + ", " + registroDestino + ", " + registro);
    }

    public void leerEntero(String registroValor, String registroDireccion) {
        asmContexto.instruccion("lw " + registroValor + ", 0(" + registroDireccion + ")");
    }

    public void leerDecimal(String fregValor, String registroDireccion) {
        asmContexto.instruccion("fld " + fregValor + ", 8(" + registroDireccion + ")");
    }

    public void escribirEntero(String regValor, String registroDireccion) {
        asmContexto.instruccion("sw " + regValor + ", 0(" + registroDireccion + ")");
        asmContexto.instruccion("fcvt.d.w ft2, " + regValor);
        asmContexto.instruccion("fsd ft2, 8(" + registroDireccion + ")");
    }

    public void escribirDecimal(String fregValor, String registroDireccion) {
        asmContexto.instruccion("fsd " + fregValor + ", 8(" + registroDireccion + ")");
        asmContexto.instruccion("fcvt.w.d t4, " + fregValor);
        asmContexto.instruccion("sw t4, 0(" + registroDireccion + ")");
    }

    protected void reservar(int celdas) {
        asmContexto.instruccion("li a0, " + (celdas * BYTES_POR_CELDA));
        Ecall.emitir(asmContexto, Ecall.RESERVAR_MEMORIA);
        asmContexto.instruccion("mv " + registro + ", a0");
    }

}
