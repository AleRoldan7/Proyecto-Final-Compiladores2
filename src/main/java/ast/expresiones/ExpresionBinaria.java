package ast.expresiones;

import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpresionBinaria extends Expresion {

    private Expresion izquierda;
    private String operacion;
    private Expresion derecha;

    private TipoDato tipoResuelto;

    public ExpresionBinaria(int linea, int columna, Expresion izquierda, String operacion, Expresion derecha) {
        super(linea, columna);
        this.izquierda = izquierda;
        this.operacion = operacion;
        this.derecha = derecha;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        // && y || NO evaluan siempre los dos lados
        if (esLogico(operacion)) {
            return generarCortocircuito(contexto);
        }

        String izq = izquierda.generarC3D(contexto);
        String der = derecha.generarC3D(contexto);

        TipoDato tipo = (tipoResuelto != null) ? tipoResuelto : inferirTipoRespaldo(izq, der);

        return contexto.binaria(operacion, izq, der, tipo);
    }

    private String generarCortocircuito(ContextoC3D contexto) {

        boolean esAnd = "&&".equals(operacion);

        String resultado = contexto.nuevoTemporal(TipoDato.BOOLEANO);
        String etiquetaCorto = contexto.nuevaEtiqueta();
        String etiquetaFin = contexto.nuevaEtiqueta();

        // 1. Evaluar solo el lado izquierdo
        String izq = izquierda.generarC3D(contexto);

        // 2. Si ya se decide el resultado, saltar sin tocar la derecha
        if (esAnd) {
            contexto.saltoSiFalso(izq, etiquetaCorto);
        } else {
            contexto.saltoSiVerdadero(izq, etiquetaCorto);
        }

        // 3. Este codigo solo se ejecuta si hizo falta evaluar la derecha
        String der = derecha.generarC3D(contexto);
        contexto.asignar(resultado, der);
        contexto.salto(etiquetaFin);

        // 4. Camino corto
        contexto.agregarEtiqueta(etiquetaCorto);
        contexto.asignar(resultado, esAnd ? "0" : "1");

        contexto.agregarEtiqueta(etiquetaFin);
        return resultado;
    }

    private TipoDato inferirTipoRespaldo(String izq, String der) {

        if (esRelacional(operacion)) {
            return TipoDato.BOOLEANO;
        }

        if ("+".equals(operacion) && (esTexto(izq) || esTexto(der))) {
            return TipoDato.TEXTO;
        }

        if (esDecimal(izq) || esDecimal(der)) {
            return TipoDato.DECIMAL;
        }

        return TipoDato.ENTERO;
    }

    private boolean esTexto(String s) {
        return s != null && s.startsWith("\"");
    }

    private boolean esDecimal(String s) {
        return s != null && s.matches("-?\\d+\\.\\d+");
    }

    private boolean esRelacional(String op) {
        return op.equals("<") || op.equals(">") || op.equals("<=") || op.equals(">=")
                || op.equals("==") || op.equals("!=");
    }

    private boolean esLogico(String op) {
        return op.equals("&&") || op.equals("||");
    }
}