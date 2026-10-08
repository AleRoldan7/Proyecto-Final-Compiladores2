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

    public LlamadaMetodo(int linea, int columna, Expresion objeto, String metodo, List<Expresion> argumentos) {
        super(linea, columna);
        this.objeto = objeto;
        this.metodo = metodo;
        this.argumentos = argumentos;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        List<String> args = new ArrayList<>();
        args.add(objeto.generarC3D(contexto));     // self

        if (argumentos != null) {
            for (Expresion arg : argumentos) {
                args.add(arg.generarC3D(contexto));
            }
        }

        // Si el semántico no asignó claseReceptor, se busca la clase que define el método
        String nombreFuncion = contexto.resolverMetodo(claseReceptor, metodo, args.size());

        return contexto.llamar(nombreFuncion, args, tipoResultado);
    }

    /*
    @Override
    public String generarC3D(ContextoC3D contexto) {

        String obj = objeto.generarC3D(contexto);
        contexto.agregar("param", obj, null, null);

        if (argumentos != null) {
            for (Expresion arg : argumentos) {
                String valor = arg.generarC3D(contexto);
                contexto.agregar("param", valor, null, null);
            }
        }

        String temporal = contexto.nuevoTemporal(tipoResultado);
        int cantidadArgs = ((argumentos == null) ? 0 : argumentos.size()) + 1;

        String nombreFuncion = (claseReceptor != null)
                ? ContextoC3D.nombreFuncion(claseReceptor, metodo)
                : metodo;
        contexto.agregar("call", nombreFuncion, String.valueOf(cantidadArgs), temporal);

        return temporal;
    }
     */
}