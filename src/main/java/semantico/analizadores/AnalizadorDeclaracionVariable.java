package semantico.analizadores;

import ast.declaraciones.DeclaracionVariable;
import ast.expresiones.Expresion;
import ast.expresiones.LlamadaFuncion;
import ast.tipos.Tipo;
import enums.Categoria;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.coordinadorsemantico.InferidorLlamadaFuncion;
import semantico.coordinadorsemantico.InferirTipoCoordinador;
import semantico.interfazsemantica.AnalizadorSemantico;

@Getter
@Setter
@AllArgsConstructor
public class AnalizadorDeclaracionVariable implements AnalizadorSemantico<DeclaracionVariable> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public void analizar(DeclaracionVariable nodoVariable, AnalisisContexto analisisContexto) {

        String nombre =  nodoVariable.getNombre();

        if (analisisContexto.getTablaSimbolos().existeEnAmbitoActual(nombre)) {

            analisisContexto.reportarError(nodoVariable.getLinea(), nodoVariable.getColumna(), "'" + nombre + "' ya fue declarado");

        } else {

            analisisContexto.getTablaSimbolos().declarar(nombre, Categoria.VARIABLE, Tipos.describir(nodoVariable.getTipo(), analisisContexto), "", nodoVariable.getLinea());
        }

        Expresion inicializacion = nodoVariable.getInicializacion();

        /*
        if (inicializacion != null) {

            Tipo inicio = inferirTipoCoordinador.inferir(inicializacion, analisisContexto);

            if (!Tipos.asignable(nodoVariable.getTipo(), inicio, analisisContexto)) {

                analisisContexto.reportarError(nodoVariable.getLinea(), nodoVariable.getColumna(),  "No se puede inicializar '" + nombre + "' de tipo "
                        + Tipos.describir(nodoVariable.getTipo(), analisisContexto) + " con un valor de tipo " + Tipos.describir(inicio, analisisContexto));
            }
        }
         */

        if (inicializacion == null) {
            return;
        }

        Tipo tipoVariable = nodoVariable.getTipo();
        Tipo tipoInicializacion;

        if (esLectura(inicializacion)) {
            tipoInicializacion = tipoVariable;

        } else {
            tipoInicializacion = inferirTipoCoordinador.inferir(inicializacion, analisisContexto);
        }

        if (tipoInicializacion == null) {
            return;
        }

        if (!Tipos.asignable(tipoVariable, tipoInicializacion, analisisContexto)) {

            analisisContexto.reportarError(nodoVariable.getLinea(), nodoVariable.getColumna(),
                    "No se puede inicializar '" + nombre + "' de tipo" + Tipos.describir(tipoVariable, analisisContexto) +
                    " con un valor de tipo " + Tipos.describir(tipoInicializacion, analisisContexto));
        }
    }

    private boolean esLectura(Expresion expresion) {
        return expresion instanceof LlamadaFuncion llamada && InferidorLlamadaFuncion.esLectura(llamada.getNombre());
    }
}
