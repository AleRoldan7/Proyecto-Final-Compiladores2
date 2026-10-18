package ast.expresiones;

import c3d.ContextoC3D;
import enums.TipoDato;
import exceptiones.C3DException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LlamadaConstructorPadre extends Expresion{
    private String superClase;
    private List<Expresion> argumentos;

    public LlamadaConstructorPadre(int linea, int columna, String superClase, List<Expresion> argumentos) {
        super(linea, columna);
        this.superClase = superClase;
        this.argumentos = argumentos;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {
        if (superClase == null) {
            throw new C3DException("'super(...)' solo puede usarse en una clase que tiene extends");
        }

        List<String> args = new ArrayList<>();
        args.add(new Identificador(getLinea(), getColumna(), "this").generarC3D(contexto));
        for (Expresion arg : argumentos) {
            args.add(arg.generarC3D(contexto));
        }

        return contexto.llamar(ContextoC3D.nombreConstructor(superClase), args, TipoDato.VOID);
    }
}
