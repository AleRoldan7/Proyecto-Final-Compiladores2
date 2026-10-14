package semantico;

import ast.declaraciones.Parametro;
import enums.ModificadorAcceso;
import lombok.AllArgsConstructor;
import tablas.InformeTipo;
import tablas.MetodoRecord;
import tablas.TablaTipos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class Herencia {

    public static List<InformeTipo> ancestros(InformeTipo clase, TablaTipos tablaTipos) {
        List<InformeTipo> cadena = new ArrayList<>();
        Set<String> vistos = new HashSet<>();
        vistos.add(clase.getNombre());

        String nombrePadre = clase.getNombreSuperClase();
        while (nombrePadre != null && vistos.add(nombrePadre)) {
            InformeTipo padre = tablaTipos.obtener(nombrePadre);
            if (padre == null) break;
            cadena.add(padre);
            nombrePadre = padre.getNombreSuperClase();
        }
        return cadena;
    }

    public static boolean tieneCiclo(InformeTipo clase, TablaTipos tipos) {
        Set<String> vistos = new HashSet<>();
        vistos.add(clase.getNombre());

        String actual = clase.getNombreSuperClase();
        while (actual != null) {
            if (!vistos.add(actual)) return true;
            InformeTipo padre = tipos.obtener(actual);
            if (padre == null) return false;
            actual = padre.getNombreSuperClase();
        }
        return false;
    }

    public static boolean esSubclaseDe(String hija, String padre, TablaTipos tipos) {
        if (hija == null || padre == null) return false;
        if (hija.equals(padre)) return true;

        InformeTipo clase = tipos.obtener(hija);
        if (clase == null) return false;

        return ancestros(clase, tipos).stream().anyMatch(a -> a.getNombre().equals(padre));
    }

    public static InformeTipo ownerDeAtributo(InformeTipo clase, String atributo, TablaTipos tipos) {
        if (clase.tieneAtributo(atributo)) return clase;
        for (InformeTipo ancestro : ancestros(clase, tipos)) {
            if (ancestro.tieneAtributo(atributo)) return ancestro;
        }
        return null;
    }


    public static List<MetodoRecord> firmasVisibles(InformeTipo clase, String nombre, TablaTipos tipos) {
        List<MetodoRecord> resultado = new ArrayList<>(clase.firmasDe(nombre));
        Set<String> firmasVistas = new HashSet<>();
        resultado.forEach(m -> firmasVistas.add(firma(m)));

        for (InformeTipo ancestro : ancestros(clase, tipos)) {
            for (MetodoRecord m : ancestro.firmasDe(nombre)) {
                if (m.modificadorAcceso() == ModificadorAcceso.PRIVATE) continue;
                if (firmasVistas.add(firma(m))) {
                    resultado.add(m);
                }
            }
        }
        return resultado;
    }

    public static InformeTipo ownerDeMetodo(InformeTipo clase, MetodoRecord metodo, TablaTipos tipos) {
        if (clase.firmasDe(metodo.nombre()).contains(metodo)) return clase;
        for (InformeTipo ancestro : ancestros(clase, tipos)) {
            if (ancestro.firmasDe(metodo.nombre()).contains(metodo)) return ancestro;
        }
        return clase;
    }

    public static String firma(MetodoRecord metodo) {
        return firma(metodo.nombre(), metodo.parametros());
    }

    public static String firma(String nombre, List<Parametro> parametros) {
        StringBuilder sb = new StringBuilder(nombre).append("(");
        if (parametros != null) {
            for (int i = 0; i < parametros.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(Tipos.describir(parametros.get(i).getTipoParametro()));
            }
        }
        return sb.append(")").toString();
    }
}
