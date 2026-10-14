package semantico.analizadores;

import enums.ModificadorAcceso;
import semantico.Herencia;
import tablas.InformeTipo;
import tablas.TablaTipos;

import java.util.Objects;

public class AnalizadorModificadorAcceso {


    public static boolean puedeAcceder(ModificadorAcceso modificador, InformeTipo duena,
                                       InformeTipo llamadora, TablaTipos tipos) {

        ModificadorAcceso efectivo = modificador == null ? ModificadorAcceso.DEFAULT : modificador;

        if (llamadora == null) {
            return efectivo == ModificadorAcceso.PUBLIC;
        }

        return switch (efectivo) {
            case PUBLIC -> true;
            case PRIVATE -> llamadora.getNombre().equals(duena.getNombre());
            case DEFAULT -> mismoPaquete(duena, llamadora);
            case PROTECTED -> mismoPaquete(duena, llamadora)
                    || Herencia.esSubclaseDe(llamadora.getNombre(), duena.getNombre(), tipos);
        };
    }

    public static String mensajeDenegado(String tipoMiembro, String nombreMiembro, InformeTipo duena,
                                         ModificadorAcceso modificador, InformeTipo llamadora) {

        String desde = llamadora == null ? "este código" : "la clase '" + llamadora.getNombre() + "'";

        return "El " + tipoMiembro + " '" + nombreMiembro + "' de '" + duena.getNombre() + "' es "
                + texto(modificador) + " y no es accesible desde " + desde;
    }

    private static boolean mismoPaquete(InformeTipo a, InformeTipo b) {
        return Objects.equals(a.getPaquete(), b.getPaquete());
    }

    private static String texto(ModificadorAcceso modificador) {
        return switch (modificador == null ? ModificadorAcceso.DEFAULT : modificador) {
            case PUBLIC -> "public";
            case PRIVATE -> "private";
            case PROTECTED -> "protected";
            case DEFAULT -> "default (package-private)";
        };
    }
}
