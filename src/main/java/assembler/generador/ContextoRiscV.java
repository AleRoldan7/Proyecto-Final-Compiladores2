package assembler.generador;

import assembler.memoria.*;
import c3d.InferidorTipos;
import enums.TipoDato;

import java.util.Map;

public class ContextoRiscV {

    public final ASMContexto asmContexto = new ASMContexto();
    public final TablaVariables variables;
    public final PoolCadenas cadenas = new PoolCadenas();
    public final OperadoresRiscV operadoresRiscV;
    public final Pila pila;
    public final Heap heap;
    public final Retorno retorno;

    private final Map<String, TipoDato> tipos;

    public ContextoRiscV(Map<String, TipoDato> tipos) {
        this.tipos = tipos;
        this.variables = new TablaVariables(tipos);
        this.operadoresRiscV = new OperadoresRiscV(asmContexto, variables, cadenas, tipos);
        this.pila = new Pila(asmContexto);
        this.heap = new Heap(asmContexto);
        this.retorno = new Retorno(asmContexto);
    }

    public TipoDato tipoDe(String operando) {
        return InferidorTipos.tipoDe(operando, tipos);
    }

    public boolean esDecimal(String operando) {
        return tipoDe(operando) == TipoDato.DECIMAL;
    }

    public boolean esTexto(String operando) {
        return tipoDe(operando) == TipoDato.TEXTO;
    }
}
