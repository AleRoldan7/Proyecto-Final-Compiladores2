package tablas;

import ast.declaraciones.Parametro;
import ast.tipos.Tipo;
import enums.ModificadorAcceso;

import java.util.List;

public record MetodoRecord(String nombre, Tipo tipoRetorno, List<Parametro> parametros, ModificadorAcceso modificadorAcceso) {
}
