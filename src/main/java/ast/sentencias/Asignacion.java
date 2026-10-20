package ast.sentencias;

import ast.expresiones.*;
import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Asignacion extends Expresion {

    private Expresion destino;
    private Expresion valor;

    public Asignacion(int linea, int columna, Expresion destino, Expresion valor) {
        super(linea, columna);
        this.destino = destino;
        this.valor = valor;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        if (getValor() instanceof LlamadaFuncion lectura && ContextoC3D.esLectura(lectura.getNombre())) {
            TipoDato t = tipoDelDestino(contexto);
            System.err.println("DEBUG lectura destino=" + getDestino().getClass().getSimpleName()
                    + " tipo=" + t
                    + " tiposLocales=" + contexto.getTiposLocales()
                    + " tipoVariables=" + contexto.getTipoVariables());
            lectura.setTipoLectura(t);
        }

        String valor = getValor().generarC3D(contexto);
        Almacenamiento.guardar(getDestino(), valor, contexto);
        return valor;
    }

    /** Tipo de la variable o atributo que recibe la lectura. */
    private TipoDato tipoDelDestino(ContextoC3D contexto) {
        if (getDestino() instanceof Identificador id) {
            return contexto.tipoDeVariable(id.getNombreIdentificador());
        }
        if (getDestino() instanceof AccesoAtributo acc
                && acc.getBase() instanceof Identificador base
                && "this".equals(base.getNombreIdentificador())) {
            return contexto.tipoDeAtributo(contexto.getClaseActual(), acc.getAtributo());
        }
        return TipoDato.DESCONOCIDO;
    }
}
