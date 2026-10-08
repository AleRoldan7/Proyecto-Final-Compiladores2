package ast.clases;

import ast.NodoAST;
import ast.declaraciones.Parametro;
import ast.sentencias.Bloque;
import c3d.ContextoC3D;
import c3d.ConversorTipos;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Constructor extends NodoAST {

    private String nombreClase;
    private List<Parametro> parametros;
    private Bloque cuerpoConstructor;

    public Constructor(int linea, int columna, String nombreClase, List<Parametro> parametros, Bloque cuerpoConstructor) {
        super(linea, columna);
        this.nombreClase = nombreClase;
        this.parametros = parametros;
        this.cuerpoConstructor = cuerpoConstructor;
    }


    @Override
    public String generarC3D(ContextoC3D contexto) {

        String nombreFuncion = ContextoC3D.nombreConstructor(nombreClase);

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

        if (cuerpoConstructor != null) {
            cuerpoConstructor.generarC3D(contexto);
        }

        // El retorno implícito es simplemente caer en la etiqueta end_
        contexto.cerrarFuncion(nombreFuncion);

        return null;
    }

    /*
    @Override
    public String generarC3D(ContextoC3D contexto) {

        String nombreFuncion = "init_" + nombreClase;

        contexto.registrarFuncion(nombreFuncion);
        contexto.agregarEtiqueta("func_" + nombreFuncion);

        contexto.agregar("param_decl", nombreClase, "self", null);

        if (parametros != null) {
            for (Parametro p : parametros) {
                contexto.agregar("param_decl",
                        p.getTipoParametro().getNombre(),
                        p.getNombreParametro(),
                        null);
            }
        }

        // Cuerpo
        cuerpoConstructor.generarC3D(contexto);

        // Retorno implícito
        contexto.agregar("return", null, null, null);

        contexto.agregarEtiqueta("end_" + nombreFuncion);

        return null;
    }
     */
}
