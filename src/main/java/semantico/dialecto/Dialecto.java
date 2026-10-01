package semantico.dialecto;

import enums.TipoDato;


public interface Dialecto {


    String nombre();

    TipoDato tipoPrimitivo(String nombreEnElLenguaje);

    String nombrarTipo(TipoDato tipoDato);

    boolean permiteVariablesGlobales();

    boolean permiteDefinirEstructuras();

    boolean permiteDefinirClases();

    boolean archivoDebeCoincidirConClase();

    boolean permiteModulo();

    TipoDato resultadoDe(String operador, TipoDato izquierda, TipoDato derecha);

    boolean asignable(TipoDato esperado, TipoDato real);
}