package semantico.analizadores;

import ast.clases.Clase;
import ast.clases.Metodo;
import ast.tipos.Tipo;
import enums.ModificadorAcceso;
import semantico.AnalisisContexto;
import semantico.Herencia;
import semantico.Tipos;
import tablas.InformeTipo;
import tablas.MetodoRecord;

import java.util.List;

public class AnalizadorOverride {

    private record Heredado(MetodoRecord metodo, InformeTipo dueno) {
    }

    private AnalizadorOverride() {
    }

    public static void validar(Clase nodoClase, InformeTipo infoClase, AnalisisContexto ctx) {

        List<InformeTipo> ancestros = Herencia.ancestros(infoClase, ctx.getTablaTipos());

        for (Metodo metodo : nodoClase.getMetodos()) {

            String firma = Herencia.firma(metodo.getNombreMetodo(), metodo.getParametros());
            Heredado heredado = buscar(ancestros, metodo.getNombreMetodo(), firma);

            if (metodo.isOverride() && heredado == null) {
                ctx.reportarError(metodo.getLinea(), metodo.getColumna(),
                        "@Override en '" + firma + "': ninguna clase padre declara un método con esa firma");
                continue;
            }

            if (!metodo.isOverride() && heredado != null) {
                ctx.reportarError(metodo.getLinea(), metodo.getColumna(),
                        "El método '" + firma + "' sobrescribe a '" + heredado.dueno().getNombre() + "."
                                + firma + "' pero le falta @Override");
                continue;
            }

            if (heredado != null
                    && !texto(metodo.getTipoRetorno()).equals(texto(heredado.metodo().tipoRetorno()))) {
                ctx.reportarError(metodo.getLinea(), metodo.getColumna(),
                        "El método '" + firma + "' debe devolver " + texto(heredado.metodo().tipoRetorno())
                                + ", igual que el método que sobrescribe en '" + heredado.dueno().getNombre() + "'");
            }
        }
    }

    private static Heredado buscar(List<InformeTipo> ancestros, String nombre, String firma) {
        for (InformeTipo ancestro : ancestros) {
            for (MetodoRecord candidato : ancestro.firmasDe(nombre)) {
                if (candidato.modificadorAcceso() == ModificadorAcceso.PRIVATE) continue;
                if (Herencia.firma(candidato).equals(firma)) {
                    return new Heredado(candidato, ancestro);
                }
            }
        }
        return null;
    }

    private static String texto(Tipo tipo) {
        return tipo == null ? "void" : Tipos.describir(tipo);
    }
}
