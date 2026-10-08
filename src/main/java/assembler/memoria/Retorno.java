package assembler.memoria;

import assembler.generador.ASMContexto;
import enums.Registro;

public class Retorno extends MemoriaCelda {

    public Retorno(ASMContexto asmContexto) {
        super(asmContexto, null);
    }

    public void apuntar(String registroDestino) {
        asmContexto.instruccion("la " + registroDestino + ", retval");
    }

    /**
     * Emite el ret_dispatch con caso por defecto.
     * El AST llama a este método UNA vez, después de todas las funciones.
     */
    public void emitirDispatch(int cantidadRetornos) {

        asmContexto.etiqueta("ret_dispatch");
        asmContexto.comentario("dispatcher de retornos");

        String regRetaddr = Registro.RETADDR.getNombreRegistro();

        for (int i = 0; i < cantidadRetornos; i++) {
            String etiquetaOmitir = asmContexto.nuevaEtiqueta("omitir_dispatch");
            asmContexto.instruccion("li t0, " + i);
            asmContexto.instruccion("bne " + regRetaddr + ", t0, " + etiquetaOmitir);
            asmContexto.instruccion("j ret_" + i);
            asmContexto.etiqueta(etiquetaOmitir);
        }

        // caso por defecto: retaddr inválido
        asmContexto.comentario("retaddr invalido: terminar programa");
        asmContexto.instruccion("li a7, 10");
        asmContexto.instruccion("ecall");
    }
}
