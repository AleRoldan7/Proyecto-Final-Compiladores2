package assembler.traductor;

import assembler.generador.ContextoRiscV;
import assembler.generador.TraductorCuarteta;
import c3d.Cuarteta;
import enums.Registro;

import java.util.Set;

public class TraductorHeap implements TraductorCuarteta {

    private static final Set<String> OPERADORES = Set.of("new", "new_array", "attr_get", "index_get", "field_set", "index_set");

    @Override
    public boolean soporta(String operador) {
        return OPERADORES.contains(operador);
    }

    @Override
    public void traducir(Cuarteta cuarteta, ContextoRiscV contextoRiscV) {

        switch (cuarteta.getOperador()) {
            case "new", "new_array" -> reservar(cuarteta, contextoRiscV);
            case "attr_get", "index_get" -> leer(cuarteta, contextoRiscV);
            default -> escribir(cuarteta, contextoRiscV);
        }
    }

    private void reservar(Cuarteta cuarteta, ContextoRiscV contextoRiscV) {

        contextoRiscV.asmContexto.instruccion("mv t0, " + Registro.HP);
        contextoRiscV.operadoresRiscV.guardarEntero("t0", cuarteta.getResultado());

        contextoRiscV.operadoresRiscV.cargarEntero("t1", cuarteta.getArg2());
        contextoRiscV.asmContexto.instruccion("add " + Registro.HP + ", " + Registro.HP + ", t1");
    }

    private void leer(Cuarteta cuarteta, ContextoRiscV contextoRiscV) {

        contextoRiscV.operadoresRiscV.cargarEntero("t0", cuarteta.getArg1());
        contextoRiscV.operadoresRiscV.cargarEntero("t1", cuarteta.getArg2());
        contextoRiscV.asmContexto.instruccion("add t0, t0, t1");
        contextoRiscV.heap.direccion("t3", "t0");

        String res = cuarteta.getResultado();

        if (contextoRiscV.esDecimal(res)) {
            contextoRiscV.heap.leerDecimal("ft0", "t3");
            contextoRiscV.operadoresRiscV.guardarDecimal("ft0", res);
        } else {
            contextoRiscV.heap.leerEntero("t0", "t3");
            contextoRiscV.operadoresRiscV.guardarEntero("t0", res);
        }
    }

    private void escribir(Cuarteta cuarteta, ContextoRiscV contextoRiscV) {

        contextoRiscV.operadoresRiscV.cargarEntero("t0", cuarteta.getResultado());   // objeto o arreglo
        contextoRiscV.operadoresRiscV.cargarEntero("t1", cuarteta.getArg1());        // desplazamiento o índice
        contextoRiscV.asmContexto.instruccion("add t0, t0, t1");
        contextoRiscV.heap.direccion("t3", "t0");

        String valor = cuarteta.getArg2();

        if (contextoRiscV.esDecimal(valor)) {
            contextoRiscV.operadoresRiscV.cargarDecimal("ft0", valor);
            contextoRiscV.heap.escribirDecimal("ft0", "t3");
        } else {
            contextoRiscV.operadoresRiscV.cargarEntero("t1", valor);
            contextoRiscV.heap.escribirEntero("t1", "t3");
        }
    }
}
