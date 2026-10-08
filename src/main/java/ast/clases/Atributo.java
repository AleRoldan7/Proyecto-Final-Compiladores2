package ast.clases;

import ast.NodoAST;
import ast.expresiones.Expresion;
import ast.tipos.Tipo;
import c3d.ContextoC3D;
import enums.ModificadorAcceso;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Atributo extends NodoAST {

    private Tipo tipo;
    private String nombreAtributo;

    private Expresion inicializacion;

    private List<Expresion> dimensiones;

    private List<Expresion> valoresIniciales;

    private ModificadorAcceso modificadorAcceso;

    public Atributo(int linea, int columna, Tipo tipo, String nombreAtributo) {
        super(linea, columna);
        this.tipo = tipo;
        this.nombreAtributo = nombreAtributo;
        this.modificadorAcceso = ModificadorAcceso.DEFAULT;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        return null;
    }
}
