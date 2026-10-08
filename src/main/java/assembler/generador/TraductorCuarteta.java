package assembler.generador;

import c3d.Cuarteta;

public interface TraductorCuarteta {

    boolean soporta(String operador);

    void traducir(Cuarteta cuarteta, ContextoRiscV contextoRiscV);
}
