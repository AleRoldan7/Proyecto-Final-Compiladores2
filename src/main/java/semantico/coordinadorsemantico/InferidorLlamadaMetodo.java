package semantico.coordinadorsemantico;

import ast.expresiones.Expresion;
import ast.expresiones.LlamadaMetodo;
import ast.tipos.Tipo;
import lombok.AllArgsConstructor;
import semantico.AnalisisContexto;
import semantico.Herencia;
import semantico.Tipos;
import semantico.analizadores.AnalizadorModificadorAcceso;
import semantico.interfazsemantica.InferirTipo;
import tablas.InformeTipo;
import tablas.MetodoRecord;
import tablas.TablaTipos;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class InferidorLlamadaMetodo implements InferirTipo<LlamadaMetodo> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(LlamadaMetodo nodoMetodo, AnalisisContexto analisisContexto) {

        Tipo tipoObjeto;
        if (nodoMetodo.isEsSuper()) {
            tipoObjeto = tipoDeSuper(nodoMetodo, analisisContexto);
        } else {
            tipoObjeto = inferirTipoCoordinador.inferir(nodoMetodo.getObjeto(), analisisContexto);
        }

        List<Expresion> argumentos = nodoMetodo.getArgumentos() == null
                ? List.of() : nodoMetodo.getArgumentos();

        List<Tipo> tiposArgumentos = new ArrayList<>();
        boolean argumentosValidos = true;

        for (Expresion argumento : argumentos) {
            Tipo tipo = inferirTipoCoordinador.inferir(argumento, analisisContexto);
            if (tipo == null) {
                argumentosValidos = false;
            }
            tiposArgumentos.add(tipo);
        }

        if (tipoObjeto == null) {
            return null;
        }

        if (tipoObjeto.isArreglo()) {
            analisisContexto.reportarError(nodoMetodo.getLinea(), nodoMetodo.getColumna(),
                    "No se puede invocar '" + nodoMetodo.getMetodo() + "' sobre un arreglo de "
                            + Tipos.describir(tipoObjeto, analisisContexto));
            return null;
        }

        InformeTipo tipoClase = analisisContexto.getTablaTipos().obtener(tipoObjeto.getNombre());

        if (tipoClase == null) {
            analisisContexto.reportarError(nodoMetodo.getLinea(), nodoMetodo.getColumna(),
                    "'" + tipoObjeto.getNombre() + "' no es una clase declarada (¿falta el import?)");
            return null;
        }

        TablaTipos tipos = analisisContexto.getTablaTipos();
        List<MetodoRecord> candidatos = Herencia.firmasVisibles(tipoClase, nodoMetodo.getMetodo(), tipos);

        if (candidatos.isEmpty()) {
            analisisContexto.reportarError(nodoMetodo.getLinea(), nodoMetodo.getColumna(),
                    "'" + tipoObjeto.getNombre() + "' no tiene un método '" + nodoMetodo.getMetodo() + "'");
            return null;
        }

        if (!argumentosValidos) {
            return null;
        }

        MetodoRecord firma = Tipos.resolverSobrecarga(candidatos, tiposArgumentos, analisisContexto);

        if (firma == null) {
            analisisContexto.reportarError(nodoMetodo.getLinea(), nodoMetodo.getColumna(),
                    "No existe una versión de '" + nodoMetodo.getMetodo() + "' que reciba esos argumentos");
            return null;
        }

        InformeTipo dueno = Herencia.ownerDeMetodo(tipoClase, firma, tipos);
        InformeTipo llamadora = analisisContexto.getClaseActual();

        if (!AnalizadorModificadorAcceso.puedeAcceder(firma.modificadorAcceso(), dueno, llamadora, tipos)) {
            analisisContexto.reportarError(nodoMetodo.getLinea(), nodoMetodo.getColumna(),
                    AnalizadorModificadorAcceso.mensajeDenegado("método", nodoMetodo.getMetodo(), dueno,
                            firma.modificadorAcceso(), llamadora));
            return null;
        }

        nodoMetodo.setClaseReceptor(dueno.getNombre());

        Tipo retorno = firma.tipoRetorno();
        return retorno != null ? retorno : Tipos.simple(nodoMetodo.getLinea(), nodoMetodo.getColumna(), Tipos.VOID);

        /*
        InformeTipo tipoClase = analisisContexto.getTablaTipos().obtener(tipoObjeto.getNombre());

        if (tipoClase == null) {
            analisisContexto.reportarError(nodoMetodo.getLinea(), nodoMetodo.getColumna(),
                    "'" + tipoObjeto.getNombre() + "' no es una clase declarada (¿falta el import?)");
            return null;
        }

        if (!tipoClase.tieneMetodo(nodoMetodo.getMetodo())) {
            analisisContexto.reportarError(nodoMetodo.getLinea(), nodoMetodo.getColumna(),
                    "'" + tipoObjeto.getNombre() + "' no tiene un método '" + nodoMetodo.getMetodo() + "'");
            return null;
        }

        if (!argumentosValidos) {
            return null;
        }

        MetodoRecord firma = Tipos.resolverSobrecarga(tipoClase.firmasDe(nodoMetodo.getMetodo()), tiposArgumentos, analisisContexto);

        if (firma == null) {
            analisisContexto.reportarError(nodoMetodo.getLinea(), nodoMetodo.getColumna(),
                    "No existe una versión de '" + nodoMetodo.getMetodo() + "' que reciba esos argumentos");
            return null;
        }

        Tipo retorno = firma.tipoRetorno();
        return retorno != null
                ? retorno
                : Tipos.simple(nodoMetodo.getLinea(), nodoMetodo.getColumna(), Tipos.VOID);
    }
    */
    }

    private Tipo tipoDeSuper(LlamadaMetodo nodo, AnalisisContexto ctx) {
        InformeTipo actual = ctx.getClaseActual();
        String padre = actual == null ? null : actual.getNombreSuperClase();

        if (padre == null) {
            ctx.reportarError(nodo.getLinea(), nodo.getColumna(),
                    "'super' solo puede usarse en una clase que tiene extends");
            return null;
        }
        return new Tipo(nodo.getLinea(), nodo.getColumna(), padre, false, 0);
    }
}