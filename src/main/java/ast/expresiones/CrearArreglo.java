package ast.expresiones;

import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;
import semantico.Tipos;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CrearArreglo extends Expresion {

    private String tipoBase;
    private List<Expresion> dimensiones;
    private List<Expresion> valoresIniciales;

    public CrearArreglo(int linea, int columna, String tipoBase,
                        List<Expresion> dimensiones, List<Expresion> valoresIniciales) {
        super(linea, columna);
        this.tipoBase = tipoBase;
        this.dimensiones = dimensiones;
        this.valoresIniciales = valoresIniciales;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        List<String> tamanios = new ArrayList<>();
        if (dimensiones != null) {
            for (Expresion dim : dimensiones) {
                tamanios.add(dim.generarC3D(contexto));
            }
        }
        if (tamanios.isEmpty()) {
            tamanios.add("0");
        }

        String temporal = reservarNivel(contexto, tamanios, 0);

        // Valores iniciales: solo aplican a arreglos de una dimensión
        if (valoresIniciales != null && tamanios.size() == 1) {
            for (int i = 0; i < valoresIniciales.size(); i++) {
                String valor = valoresIniciales.get(i).generarC3D(contexto);
                contexto.agregar("index_set", String.valueOf(i), valor, temporal);
            }
        }

        return temporal;
    }


    private String reservarNivel(ContextoC3D contexto, List<String> tamanios, int nivel) {

        String n = tamanios.get(nivel);
        String arreglo = contexto.nuevoTemporal(TipoDato.ESTRUCTURA);

        // Último nivel: celdas del tipo base
        if (nivel == tamanios.size() - 1) {
            int anchoElemento = contexto.esEstructura(tipoBase) ? contexto.tamanioEstructura(tipoBase) : 1;

            String totalCeldas = n;
            if (anchoElemento != 1) {
                totalCeldas = contexto.binaria("*", n, String.valueOf(anchoElemento), TipoDato.ENTERO);
            }

            contexto.agregar("new_array", tipoBase, totalCeldas, arreglo);
            return arreglo;
        }

        //  arreglo de punteros a filas
        contexto.agregar("new_array", tipoBase, n, arreglo);

        String i = contexto.nuevoTemporal(TipoDato.ENTERO);
        contexto.agregar("=", "0", null, i, TipoDato.ENTERO);

        String inicio = contexto.nuevaEtiqueta();
        String fin = contexto.nuevaEtiqueta();

        contexto.agregarEtiqueta(inicio);
        String cond = contexto.binaria("<", i, n, TipoDato.BOOLEANO);
        contexto.saltoSiFalso(cond, fin);

        String fila = reservarNivel(contexto, tamanios, nivel + 1);
        contexto.agregar("index_set", i, fila, arreglo);

        String siguiente = contexto.binaria("+", i, "1", TipoDato.ENTERO);
        contexto.agregar("=", siguiente, null, i, TipoDato.ENTERO);
        contexto.salto(inicio);

        contexto.agregarEtiqueta(fin);
        return arreglo;
    }
}
