package ast.expresiones;

import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LlamadaFuncion extends Expresion {

    private String nombre;
    private List<Expresion> argumentos;
    private boolean metodoDeClase;
    private TipoDato tipoResultado;
    private TipoDato tipoLectura;
    private String claseReceptor;

    public LlamadaFuncion(int linea, int columna, String nombre, List<Expresion> argumentos) {
        super(linea, columna);
        this.nombre = nombre;
        this.argumentos = argumentos;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        List<String> lugares = new ArrayList<>();
        if (getArgumentos() != null) {
            for (Expresion argumento : getArgumentos()) {
                lugares.add(argumento.generarC3D(contexto));
            }
        }

        if (ContextoC3D.esImpresion(getNombre())) {
            boolean conSalto = ContextoC3D.imprimeConSalto(getNombre());
            for (int i = 0; i < lugares.size(); i++) {
                boolean ultimo = (i == lugares.size() - 1);
                contexto.agregar("print", lugares.get(i), (conSalto && ultimo) ? "nl" : null, null);
            }
            return null;
        }

        if (ContextoC3D.esLectura(getNombre())) {
            TipoDato tipo = tipoLectura != null ? tipoLectura : tipoResultado;
            if (tipo == null || tipo == TipoDato.DESCONOCIDO) {
                throw new IllegalStateException("readln() sin tipo destino: asígnalo a una variable con tipo.");
            }
            String leido = contexto.nuevoTemporal(tipo);
            contexto.agregar("read", null, null, leido, tipo);
            return leido;
        }

        List<String> args = new ArrayList<>();
        if (metodoDeClase) {
            args.add(contexto.leerVariable("self", TipoDato.OBJETO));
        }
        args.addAll(lugares);

        if (metodoDeClase) {
            String clase = getClaseReceptor() != null
                    ? getClaseReceptor()
                    : contexto.getClaseActual();
            return contexto.llamarVirtual(clase, getNombre(), args, tipoResultado);
        }

        return contexto.llamar(getNombre(), args, tipoResultado);
    }
}