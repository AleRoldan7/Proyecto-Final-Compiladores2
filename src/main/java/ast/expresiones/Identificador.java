package ast.expresiones;

import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;
import org.fxmisc.richtext.model.TextOps;

@Getter
@Setter
public class Identificador extends Expresion {

    private String nombreIdentificador;

    public Identificador(int linea, int columna, String nombreIdentificador) {
        super(linea, columna);
        this.nombreIdentificador = nombreIdentificador;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        String nombre = getNombreIdentificador();

        // this -> el parámetro "self" vive en la pila
        if ("this".equals(nombre)) {
            return contexto.leerVariable("self", TipoDato.OBJETO);
        }

        // Variable local o parámetro: se lee de la pila
        if (contexto.esLocal(nombre)) {
            return contexto.leerVariable(nombre, contexto.tipoDeVariable(nombre));
        }

        // Atributo del objeto actual
        if (contexto.esAtributo(nombre)) {
            TipoDato tipo = contexto.tipoDeAtributo(contexto.getClaseActual(), nombre);
            String self = contexto.leerVariable("self", TipoDato.OBJETO);
            String temporal = contexto.nuevoTemporal(tipo);
            contexto.agregar("attr_get", self,
                    String.valueOf(contexto.desplazamiento(contexto.getClaseActual(), nombre)), temporal, tipo);
            return temporal;
        }

        return nombre;
    }

    /*
    @Override
    public String generarC3D(ContextoC3D contexto) {

        String nombre = getNombreIdentificador();

        if ("this".equals(nombre)) {
            return "self";
        }

        if (!contexto.esLocal(nombre) && contexto.esAtributo(nombre)) {

            TipoDato tipoDato = contexto.tipoDeAtributo(contexto.getClaseActual(), nombre);

            String temporal = contexto.nuevoTemporal(tipoDato);
            TipoDato tipo = contexto.tipoDeAtributo(contexto.getClaseActual(), nombre);
            contexto.agregar("attr_get", "self",
                    String.valueOf(contexto.desplazamiento(contexto.getClaseActual(), nombre)), temporal, tipo);
            return temporal;
        }

        return nombre;
    }
     */
}