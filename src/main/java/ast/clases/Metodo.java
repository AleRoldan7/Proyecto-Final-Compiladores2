package ast.clases;

import ast.NodoAST;
import ast.declaraciones.Parametro;
import ast.sentencias.Bloque;
import ast.sentencias.Sentencia;
import ast.sentencias.SentenciaReturn;
import ast.tipos.Tipo;
import c3d.ContextoC3D;
import c3d.ConversorTipos;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Metodo extends NodoAST {

    private String nombreMetodo;
    private Tipo tipoRetorno;
    private List<Parametro> parametros;
    private Bloque cuerpoMetodo;

    public Metodo(int linea, int columna, String nombreMetodo, Tipo tipoRetorno, List<Parametro> parametros, Bloque cuerpoMetodo) {
        super(linea, columna);
        this.nombreMetodo = nombreMetodo;
        this.tipoRetorno = tipoRetorno;
        this.parametros = parametros;
        this.cuerpoMetodo = cuerpoMetodo;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        String clase = contexto.getClaseActual();
        String nombreFuncion = ContextoC3D.nombreFuncion(clase, nombreMetodo);   // Pila_apilar

        List<String> nombres = new ArrayList<>();
        List<TipoDato> tiposParametros = new ArrayList<>();

        nombres.add("self");
        tiposParametros.add(TipoDato.OBJETO);

        if (parametros != null) {
            for (Parametro p : parametros) {
                nombres.add(p.getNombreParametro());
                tiposParametros.add(ConversorTipos.aTipoDato(p.getTipoParametro().getNombre(), p.isArreglo()));
            }
        }

        contexto.abrirFuncion(nombreFuncion, nombres, tiposParametros);

        if (cuerpoMetodo != null) {
            cuerpoMetodo.generarC3D(contexto);
        }

        // Si es void y no hay return, el flujo cae en end_ (retorno implícito)
        contexto.cerrarFuncion(nombreFuncion);

        return null;
    }

    /*
    @Override
    public String generarC3D(ContextoC3D contexto) {

        String clase = contexto.getClaseActual();
        String nombreFuncion = ContextoC3D.nombreFuncion(clase, nombreMetodo);   // Pila_apilar

        contexto.registrarFuncion(nombreFuncion);
        contexto.agregarEtiqueta("func_" + nombreFuncion);

        // El objeto va como primer parámetro
        contexto.agregar("param_decl", clase, "self", null);

        if (parametros != null) {
            for (Parametro p : parametros) {
                contexto.agregar("param_decl", p.getTipoParametro().getNombre(),
                        p.getNombreParametro(), null);
            }
        }

        cuerpoMetodo.generarC3D(contexto);

        if (esVoid() && !tieneReturnExplicito(cuerpoMetodo)) {
            contexto.agregar("return", null, null, null);
        }

        contexto.agregarEtiqueta("end_" + nombreFuncion);

        return null;
    }


    private boolean esVoid() {
        return tipoRetorno == null
                || "void".equals(tipoRetorno.getNombre())
                || tipoRetorno.getNombre() == null;
    }


    private boolean tieneReturnExplicito(Bloque bloque) {

        if (bloque == null || bloque.getSentencias() == null) {
            return false;
        }

        for (Sentencia s : bloque.getSentencias()) {
            if (contieneReturn(s)) return true;
        }

        return false;
    }

    private boolean contieneReturn(Object nodo) {

        if (nodo instanceof SentenciaReturn) {
            return true;
        }
        return false;
    }
     */
}
