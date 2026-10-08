package assembler.traductor;

import assembler.generador.ContextoRiscV;
import assembler.generador.TraductorCuarteta;
import assembler.runtime.Ecall;
import c3d.Cuarteta;

import java.util.Set;

public class TraductorES implements TraductorCuarteta {

    @Override
    public boolean soporta(String operador) {
        return Set.of("print", "read").contains(operador);
    }

    @Override
    public void traducir(Cuarteta cuarteta, ContextoRiscV contextoRiscV) {

        if ("print".equals(cuarteta.getOperador())) {
            imprimir(cuarteta, contextoRiscV);
        } else {
            leer(cuarteta, contextoRiscV);
        }
    }

    private void imprimir(Cuarteta cuarteta, ContextoRiscV contextoRiscV) {

        String valor = cuarteta.getArg1();

        switch (contextoRiscV.tipoDe(valor)) {
            case TEXTO -> {
                contextoRiscV.operadoresRiscV.cargarEntero("a0", valor);
                Ecall.emitir(contextoRiscV.asmContexto, Ecall.IMPRIMIR_TEXTO);
            }
            case DECIMAL -> {
                contextoRiscV.operadoresRiscV.cargarDecimal("fa0", valor);
                Ecall.emitir(contextoRiscV.asmContexto, Ecall.IMPRIMIR_DECIMAL);
            }
            case CARACTER -> {
                contextoRiscV.operadoresRiscV.cargarEntero("a0", valor);
                Ecall.emitir(contextoRiscV.asmContexto, Ecall.IMPRIMIR_CARACTER);
            }
            default -> {
                contextoRiscV.operadoresRiscV.cargarEntero("a0", valor);
                Ecall.emitir(contextoRiscV.asmContexto, Ecall.IMPRIMIR_ENTERO);
            }
        }

        if ("nl".equals(cuarteta.getArg2())) {
            contextoRiscV.asmContexto.instruccion("li a0, 10");
            Ecall.emitir(contextoRiscV.asmContexto, Ecall.IMPRIMIR_CARACTER);
        }
    }

    private void leer(Cuarteta cuarteta, ContextoRiscV contextoRiscV) {

        String destino = cuarteta.getResultado();

        switch (contextoRiscV.tipoDe(destino)) {
            case TEXTO -> {
                contextoRiscV.asmContexto.instruccion("jal ra, rt_leer_texto");
                contextoRiscV.operadoresRiscV.guardarEntero("a0", destino);
            }
            case DECIMAL -> {
                Ecall.emitir(contextoRiscV.asmContexto, Ecall.LEER_DECIMAL);
                contextoRiscV.operadoresRiscV.guardarDecimal("fa0", destino);
            }

            case CARACTER -> {
                contextoRiscV.asmContexto.instruccion("jal ra, rt_leer_caracter");
                contextoRiscV.operadoresRiscV.guardarEntero("a0", destino);
            }

            default -> {
                Ecall.emitir(contextoRiscV.asmContexto, Ecall.LEER_ENTERO);
                contextoRiscV.operadoresRiscV.guardarEntero("a0", destino);
            }
        }
    }
}
