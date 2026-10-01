package semantico.dialecto;

import enums.TipoDato;

import java.util.Map;

public class DialectoY extends DialectoBase {

    private static final Map<String, TipoDato> TIPOS = Map.of(
            "entero",   TipoDato.ENTERO,
            "flotante", TipoDato.DECIMAL,
            "cadena",   TipoDato.TEXTO,
            "caracter", TipoDato.CARACTER,
            "bool",     TipoDato.BOOLEANO,
            "vacio",    TipoDato.VOID
    );

    private static final Map<TipoDato, String> NOMBRES = Map.of(
            TipoDato.ENTERO,   "entero",
            TipoDato.DECIMAL,  "flotante",
            TipoDato.TEXTO,    "cadena",
            TipoDato.CARACTER, "caracter",
            TipoDato.BOOLEANO, "bool",
            TipoDato.VOID,     "vacio"
    );

    @Override protected Map<String, TipoDato> diccionarioDeTipos() { return TIPOS; }
    @Override protected Map<TipoDato, String> nombresDeTipos()     { return NOMBRES; }

    @Override public String  nombre()                        { return "Y?"; }
    @Override public boolean permiteVariablesGlobales()       { return false; }
    @Override public boolean permiteDefinirEstructuras()      { return true; }
    @Override public boolean permiteDefinirClases()           { return false; }
    @Override public boolean archivoDebeCoincidirConClase()   { return false; }
}