package assembler.memoria;

import assembler.generador.ASMContexto;

public class Retorno extends MemoriaCelda {

    public Retorno(ASMContexto asmContexto) {
        super(asmContexto, null);
    }

    public void apuntar(String registroDestino) {
        asmContexto.instruccion("la " + registroDestino + ", retval");
    }
}
