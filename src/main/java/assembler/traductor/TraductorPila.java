package assembler.traductor;

import assembler.generador.ContextoRiscV;
import assembler.generador.TraductorCuarteta;
import c3d.Cuarteta;

import java.util.Set;

public class TraductorPila implements TraductorCuarteta {

    @Override
    public boolean soporta(String operador) {
        return Set.of("stack_get", "stack_set").contains(operador);
    }

    @Override
    public void traducir(Cuarteta cuarteta, ContextoRiscV contextoRiscV) {

        contextoRiscV.operadoresRiscV.cargarEntero("t0", cuarteta.getArg1());
        contextoRiscV.pila.direccion("t3", "t0");          // t3 = dirección de la celda

        if ("stack_get".equals(cuarteta.getOperador())) {

            String res = cuarteta.getResultado();

            if (contextoRiscV.esDecimal(res)) {
                contextoRiscV.pila.leerDecimal("ft0", "t3");
                contextoRiscV.operadoresRiscV.guardarDecimal("ft0", res);
            } else {
                contextoRiscV.pila.leerEntero("t0", "t3");
                contextoRiscV.operadoresRiscV.guardarEntero("t0", res);
            }
            return;
        }

        String valor = cuarteta.getArg2();

        if (contextoRiscV.esDecimal(valor)) {
            contextoRiscV.operadoresRiscV.cargarDecimal("ft0", valor);
            contextoRiscV.pila.escribirDecimal("ft0", "t3");
        } else {
            contextoRiscV.operadoresRiscV.cargarEntero("t1", valor);
            contextoRiscV.pila.escribirEntero("t1", "t3");
        }
    }

}
