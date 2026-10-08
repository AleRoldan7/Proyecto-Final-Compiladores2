package assembler.traductor;

import assembler.generador.ContextoRiscV;
import assembler.generador.TraductorCuarteta;
import c3d.Cuarteta;

public class TraductorAsignacion implements TraductorCuarteta {


    @Override
    public boolean soporta(String operador) {
        return "=".equals(operador);
    }

    @Override
    public void traducir(Cuarteta cuarteta, ContextoRiscV contextoRiscV) {

        String origen = cuarteta.getArg1();
        String destino = cuarteta.getResultado();

        if ("retval".equals(destino)) {
            // escribir el resultado de la función
            if (contextoRiscV.esDecimal(origen)) {
                contextoRiscV.operadoresRiscV.cargarDecimal("ft0", origen);
                contextoRiscV.retorno.apuntar("t3");
                contextoRiscV.retorno.escribirDecimal("ft0", "t3");
            } else {
                contextoRiscV.operadoresRiscV.cargarEntero("t0", origen);
                contextoRiscV.retorno.apuntar("t3");
                contextoRiscV.retorno.escribirEntero("t0", "t3");
            }

        } else if ("retval".equals(origen)) {
            // recoger el resultado de la función
            contextoRiscV.retorno.apuntar("t3");
            if (contextoRiscV.esDecimal(destino)) {
                contextoRiscV.retorno.leerDecimal("ft0", "t3");
                contextoRiscV.operadoresRiscV.guardarDecimal("ft0", destino);
            } else {
                contextoRiscV.retorno.leerEntero("t0", "t3");
                contextoRiscV.operadoresRiscV.guardarEntero("t0", destino);
            }

        } else if (contextoRiscV.esDecimal(destino)) {
            contextoRiscV.operadoresRiscV.cargarDecimal("ft0", origen);
            contextoRiscV.operadoresRiscV.guardarDecimal("ft0", destino);

        } else {
            contextoRiscV.operadoresRiscV.cargarEntero("t0", origen);
            contextoRiscV.operadoresRiscV.guardarEntero("t0", destino);
        }
    }
}
