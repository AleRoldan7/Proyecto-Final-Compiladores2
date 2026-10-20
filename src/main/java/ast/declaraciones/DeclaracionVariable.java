package ast.declaraciones;

import ast.expresiones.Expresion;
import ast.tipos.Tipo;
import c3d.ContextoC3D;
import c3d.ConversorTipos;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeclaracionVariable extends Declaracion {

    private Tipo tipo;
    private String nombre;
    private Expresion inicializacion;


    public DeclaracionVariable(int linea, int columna, Tipo tipo, String nombre, Expresion inicializacion) {
        super(linea, columna);

        this.tipo = tipo;
        this.nombre = nombre;
        this.inicializacion = inicializacion;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {
        TipoDato tipoDato = ConversorTipos.aTipoDato(tipo);

        if (contexto.enFuncion()) {
            contexto.declararLocal(nombre, tipoDato);
        } else {
            contexto.declararGlobal(nombre, tipoDato);
        }

        String valor = (inicializacion != null) ? inicializacion.generarC3D(contexto) : null;

        if (valor != null) {
            contexto.asignar(nombre, valor);
        }
        return nombre;
    }

    /*
    @Override
    public String generarC3D(ContextoC3D contexto) {

        if (inicializacion != null) {
            String valor = inicializacion.generarC3D(contexto);
            contexto.asignar(nombre, valor);
        }

        return nombre;   // por si alguien usa la variable como expresión
    }
     */
}
