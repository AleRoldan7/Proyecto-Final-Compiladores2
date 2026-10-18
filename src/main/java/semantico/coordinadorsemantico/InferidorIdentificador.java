package semantico.coordinadorsemantico;

import ast.expresiones.Identificador;
import ast.tipos.Tipo;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;
import tablas.FilaTabla;
import tablas.InformeTipo;

public class InferidorIdentificador implements InferirTipo<Identificador> {

    @Override
    public Tipo inferir(Identificador nodoIdentificador, AnalisisContexto analisisContexto) {

        if ("this".equals(nodoIdentificador.getNombreIdentificador())) {

            if (analisisContexto.getClaseActual() == null) {

                analisisContexto.reportarError(nodoIdentificador.getLinea(), nodoIdentificador.getColumna(),
                        "'this' solo puede usarse dentro de una clase");
                return null;
            }

            return Tipos.simple(nodoIdentificador.getLinea(), nodoIdentificador.getColumna(),
                    analisisContexto.getClaseActual().getNombre());
        }

        if ("super".equals(nodoIdentificador.getNombreIdentificador())) {
            InformeTipo actual = analisisContexto.getClaseActual();
            String padre = actual == null ? null : actual.getNombreSuperClase();

            if (padre == null) {
                analisisContexto.reportarError(nodoIdentificador.getLinea(), nodoIdentificador.getColumna(),
                        "'super' solo puede usarse en una clase que tiene extends");
                return null;
            }
            return new Tipo(nodoIdentificador.getLinea(), nodoIdentificador.getColumna(), padre, false, 0);
        }
        FilaTabla filaTabla = analisisContexto.getTablaSimbolos().buscar(nodoIdentificador.getNombreIdentificador());

        if  (filaTabla == null) {

            analisisContexto.reportarError(nodoIdentificador.getLinea(), nodoIdentificador.getColumna(), "La variabre '" +
                    nodoIdentificador.getNombreIdentificador() + "' no ha sido declarada");

            return null;
        }
        System.out.println("IDENTIFICADOR: " + nodoIdentificador.getNombreIdentificador()
                + " -> tipoEnTabla=[" + filaTabla.getTipo() + "]");
        return Tipos.desdeTexto(nodoIdentificador.getLinea(), nodoIdentificador.getColumna(), filaTabla.getTipo());
    }
}