package semantico.analizadores;

import ast.clases.Atributo;
import ast.clases.Clase;
import ast.clases.Constructor;
import ast.expresiones.LlamadaConstructorPadre;
import ast.sentencias.Sentencia;
import ast.sentencias.SentenciaExpresion;
import lombok.AllArgsConstructor;
import semantico.AnalisisContexto;
import semantico.Herencia;
import tablas.InformeTipo;
import tablas.MetodoRecord;

import java.util.List;

@AllArgsConstructor
public class AnalizadorSuper {

    public static void validarConstructores(Clase nodoClase, AnalisisContexto ctx) {

        String padre = nodoClase.getSuperClase();
        String nombre = nodoClase.getNombreClase();
        List<Constructor> constructores = nodoClase.getConstructores();

        if (constructores == null || constructores.isEmpty()) {
            if (padre != null && !tieneConstructorConAridad(padre, 0, ctx)) {
                ctx.reportarError(nodoClase.getLinea(), nodoClase.getColumna(),
                        "'" + nombre + "' no declara constructor y '" + padre
                                + "' no tiene constructor sin parámetros");
            }
            return;
        }

        for (Constructor constructor : constructores) {

            List<Sentencia> sentencias = (constructor.getCuerpoConstructor() == null) ? List.of() : constructor.getCuerpoConstructor().getSentencias();
            LlamadaConstructorPadre primera = llamadaSuper(sentencias.isEmpty() ? null : sentencias.get(0));

            if (padre == null) {
                continue;
            }

            if (primera == null) {
                if (!tieneConstructorConAridad(padre, 0, ctx)) {
                    ctx.reportarError(constructor.getLinea(), constructor.getColumna(),
                            "El constructor de '" + nombre + "' debe llamar a super(...) porque '"
                                    + padre + "' no tiene constructor sin parámetros");
                }
                continue;
            }

            ctx.permitirSuper(primera);

            int cantidad = primera.getArgumentos() == null ? 0 : primera.getArgumentos().size();
            if (!tieneConstructorConAridad(padre, cantidad, ctx)) {
                ctx.reportarError(constructor.getLinea(), constructor.getColumna(),
                        "'" + padre + "' no tiene constructor con " + cantidad + " parámetro(s)");
            }
        }
    }

    public static void validarAtributosNoRedeclarados(Clase nodoClase, InformeTipo infoClase, AnalisisContexto ctx) {

        List<InformeTipo> ancestros = Herencia.ancestros(infoClase, ctx.getTablaTipos());

        for (Atributo atributo : nodoClase.getAtributos()) {
            String nombre = atributo.getNombreAtributo();

            for (InformeTipo ancestro : ancestros) {
                if (ancestro.getOrdenAtributos().contains(nombre)) {
                    ctx.reportarError(atributo.getLinea(), atributo.getColumna(),
                            "El atributo '" + nombre + "' ya existe en '" + ancestro.getNombre()
                                    + "'; no se permite redeclararlo");
                    break;
                }
            }
        }
    }

    private static LlamadaConstructorPadre llamadaSuper(Sentencia sentencia) {
        if (sentencia instanceof SentenciaExpresion se && se.getExpresion() instanceof LlamadaConstructorPadre llamada) {
            return llamada;
        }
        return null;
    }

    private static boolean tieneConstructorConAridad(String clase, int cantidad, AnalisisContexto ctx) {

        InformeTipo info = ctx.getTablaTipos().obtener(clase);
        if (info == null) {
            return false;
        }

        List<MetodoRecord> constructores = info.getConstructores();   // ver supuestos
        if (constructores == null || constructores.isEmpty()) {
            return cantidad == 0;   // constructor implícito sin parámetros
        }

        return constructores.stream().anyMatch(c -> (c.parametros() == null ? 0 : c.parametros().size()) == cantidad);
    }
}
