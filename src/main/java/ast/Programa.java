package ast;

import ast.clases.Clase;
import ast.declaraciones.Declaracion;
import ast.declaraciones.DeclaracionFuncion;
import ast.estructuras.Estructura;
import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Programa extends NodoAST {

    private List<String> importaciones;
    private List<Estructura> estructuras;
    private List<Clase> clases;
    private List<DeclaracionFuncion> funciones;
    private List<Declaracion> declaraciones;
    public Programa(int linea, int columna, List<String> importaciones, List<Estructura> estructuras,
                    List<Clase> clases, List<DeclaracionFuncion> funciones, List<Declaracion> declaraciones) {

        super(linea, columna);

        this.importaciones = importaciones;
        this.estructuras = estructuras;
        this.clases = clases;
        this.funciones = funciones;
        this.declaraciones = declaraciones;
    }

    @Override
    public String toString() {
        return "Programa{" +
                "estructuras=" + estructuras +
                ", funciones=" + funciones +
                '}';


    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        // 1. Registrar todas las estructuras primero
        if (estructuras != null) {

            for (Estructura estructura : estructuras) {

                List<ContextoC3D.CampoDef> campos = estructura.getCampos()
                        .stream()
                        .map(campo -> {

                            String tipo = campo.getTipo().getNombre();

                            boolean esEmbebido = estructuras.stream()
                                    .anyMatch(e -> e.getNombre().equals(tipo));

                            TipoDato tipoDato = esEmbebido
                                    ? TipoDato.ESTRUCTURA
                                    : convertirTipoDatoDesdeNombre(tipo);

                            int anchoDeclarado = 1;

                            if (campo.getTipo().isArreglo()
                                    && campo.getTipo().getSize() != null
                                    && !campo.getTipo().getSize().isEmpty()) {

                                anchoDeclarado = campo.getTipo().getSize().get(0);
                            }

                            return new ContextoC3D.CampoDef(
                                    campo.getNombre(),
                                    esEmbebido,
                                    esEmbebido ? tipo : null,
                                    tipoDato,
                                    anchoDeclarado
                            );
                        })
                        .toList();

                contexto.registrarEstructura(
                        estructura.getNombre(),
                        campos
                );
            }

            contexto.resolverEstructuras();
        }

        if (clases != null) {
            for (Clase c : clases) {
                c.generarC3D(contexto);
            }
        }

        if (funciones != null) {
            for (DeclaracionFuncion f : funciones) {
                f.generarC3D(contexto);
            }
        }

        // Variables globales del programa (.pig): se ejecutan ANTES de main
        if (declaraciones != null && !declaraciones.isEmpty()) {
            contexto.abrirGlobales();
            for (Declaracion d : declaraciones) {
                d.generarC3D(contexto);
            }
            contexto.cerrarGlobales();
        }


        return null;
    }

    private static TipoDato convertirTipoDatoDesdeNombre(String nombre) {

        if (nombre == null) {
            return TipoDato.DESCONOCIDO;
        }

        return switch (nombre.toLowerCase()) {
            case "entero", "int"              -> TipoDato.ENTERO;
            case "decimal", "double", "float" -> TipoDato.DECIMAL;
            case "texto", "string", "cadena"  -> TipoDato.TEXTO;
            case "caracter", "char"           -> TipoDato.CARACTER;
            case "booleano", "bool", "boolean"-> TipoDato.BOOLEANO;
            case "void"                       -> TipoDato.VOID;
            default                           -> TipoDato.OBJETO;
        };
    }
}
