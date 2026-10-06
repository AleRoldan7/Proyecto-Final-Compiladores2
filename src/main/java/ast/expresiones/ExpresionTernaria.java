package ast.expresiones;

import ast.sentencias.CondicionIf;
import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpresionTernaria extends Expresion {

    private Expresion condicionTernaria;
    private Expresion verdaderoTernaria;
    private Expresion falsoTernaria;
    private TipoDato tipoResultado;

    public ExpresionTernaria(int linea, int columna, Expresion condicionTernaria, Expresion verdaderoTernaria, Expresion falsoTernaria) {
        super(linea, columna);
        this.condicionTernaria = condicionTernaria;
        this.verdaderoTernaria = verdaderoTernaria;
        this.falsoTernaria = falsoTernaria;
    }
    @Override
    public String generarC3D(ContextoC3D contexto) {

        String etqV = contexto.nuevaEtiqueta();
        String etqF = contexto.nuevaEtiqueta();
        String etqFin = contexto.nuevaEtiqueta();

        String temp = contexto.nuevoTemporal(tipoResultado);

        // Evaluar la condición
        CondicionIf.generarCondicion(condicionTernaria, contexto, etqV, etqF);

        // Rama verdadera
        contexto.agregarEtiqueta(etqV);
        String v = verdaderoTernaria.generarC3D(contexto);
        contexto.asignar(temp, v);
        contexto.salto(etqFin);

        // Rama falsa
        contexto.agregarEtiqueta(etqF);
        String f = falsoTernaria.generarC3D(contexto);
        contexto.asignar(temp, f);
        contexto.salto(etqFin);

        contexto.agregarEtiqueta(etqFin);

        return temp;
    }
}
