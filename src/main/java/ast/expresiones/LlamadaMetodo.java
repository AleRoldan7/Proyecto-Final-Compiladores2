package ast.expresiones;

import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LlamadaMetodo extends Expresion {

    private Expresion objeto;
    private String metodo;
    private List<Expresion> argumentos;
    private TipoDato tipoResultado;

    private String claseReceptor;
    private boolean esSuper;

    public LlamadaMetodo(int linea, int columna, Expresion objeto, String metodo, List<Expresion> argumentos) {
        super(linea, columna);
        this.objeto = objeto;
        this.metodo = metodo;
        this.argumentos = argumentos;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {
        System.out.println("LlamadaMetodo " + metodo + " esSuper=" + esSuper
                + " claseReceptor=" + claseReceptor);
        List<String> args = new ArrayList<>();
        args.add(objeto.generarC3D(contexto));

        if (argumentos != null) {
            for (Expresion arg : argumentos) {
                args.add(arg.generarC3D(contexto));
            }
        }

        if (esSuper) {
            String nombre = contexto.resolverMetodoEstatico(claseReceptor, metodo, args.size());
            return contexto.llamar(nombre, args, tipoResultado);
        }


        String clase = claseReceptor != null ? claseReceptor : contexto.getClaseActual();
        if (clase == null) {
            throw new IllegalStateException(
                    "No se pudo determinar la clase del receptor de '" + metodo + "'");
        }
        return contexto.llamarVirtual(clase, metodo, args, tipoResultado);



    }

}