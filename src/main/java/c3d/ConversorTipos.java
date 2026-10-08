package c3d;

import ast.tipos.Tipo;
import enums.TipoDato;

public class ConversorTipos {

    public ConversorTipos() {
    }

    public static TipoDato aTipoDato(Tipo tipo) {
        if (tipo == null) {
            return TipoDato.DESCONOCIDO;
        }
        return aTipoDato(tipo.getNombre(), tipo.isArreglo());
    }

    public static TipoDato aTipoDato(String nombre, boolean esArreglo) {

        // un arreglo es una dirección del heap
        if (esArreglo || (nombre != null && nombre.contains("["))) {
            return TipoDato.ESTRUCTURA;
        }

        if (nombre == null) {
            return TipoDato.DESCONOCIDO;
        }

        return switch (nombre.toLowerCase()) {
            case "entero", "int"                        -> TipoDato.ENTERO;
            case "decimal", "double", "float", "flotante" -> TipoDato.DECIMAL;
            case "texto", "string", "cadena"            -> TipoDato.TEXTO;
            case "caracter", "char"                     -> TipoDato.CARACTER;
            case "booleano", "bool", "boolean"          -> TipoDato.BOOLEANO;
            case "void"                                 -> TipoDato.VOID;
            default                                     -> TipoDato.OBJETO;
        };
    }
}
